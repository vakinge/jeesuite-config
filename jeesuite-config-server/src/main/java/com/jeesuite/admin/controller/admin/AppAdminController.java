package com.jeesuite.admin.controller.admin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
import com.jeesuite.admin.dao.mapper.UserEntityMapper;
import com.jeesuite.admin.exception.JeesuiteBaseException;
import com.jeesuite.admin.model.SelectOption;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.model.request.AddOrEditAppRequest;
import com.jeesuite.admin.service.CacheQueryService;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.util.BeanUtils;

import tk.mybatis.mapper.entity.Example;

@Controller
@RequestMapping("/admin/app")
public class AppAdminController {

	final static Logger logger = LoggerFactory.getLogger("controller");
	
	private @Autowired CacheQueryService cacheQueryService;
	private @Autowired AppEntityMapper appMapper;
	private @Autowired UserEntityMapper userMapper;
	
	@RequestMapping(value = "list", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> findAllApps(){
		List<AppEntity> list = appMapper.selectAll();
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(list),HttpStatus.OK);
	}
	
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getApp(@PathVariable("id") int id){
		AppEntity entity = cacheQueryService.findAppEntity(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(entity),HttpStatus.OK);
	}
	
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> addApp(@RequestBody AddOrEditAppRequest addAppRequest){
		SecurityUtil.requireSuperAdmin();
		if(addAppRequest.getMasterUid() == null || addAppRequest.getMasterUid() == 0){
			throw new JeesuiteBaseException(1002, "请选择项目负责人");
		}
		Example example = new Example(AppEntity.class);
		example.createCriteria().andEqualTo("name", addAppRequest.getName());
		int count = appMapper.selectCountByExample(example);
		if(count > 0){
			throw new JeesuiteBaseException(1002, "应用["+addAppRequest.getName()+"]已存在");
		}
		AppEntity appEntity = BeanUtils.copy(addAppRequest, AppEntity.class);
		//
		UserEntity master = userMapper.selectByPrimaryKey(addAppRequest.getMasterUid());
		appEntity.setMaster(master.getName());
		appMapper.insertSelective(appEntity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> updateApp(@RequestBody AddOrEditAppRequest addAppRequest){
		SecurityUtil.requireSuperAdmin();
		AppEntity app = appMapper.selectByPrimaryKey(addAppRequest.getId());
		if(app == null){
			throw new JeesuiteBaseException(1002, "应用不存在");
		}
		AppEntity appEntity = BeanUtils.copy(addAppRequest, AppEntity.class);
		
		if(addAppRequest.getMasterUid() != null && addAppRequest.getMasterUid() > 0 
				&& !addAppRequest.getMasterUid().equals(app.getMasterUid())){
			UserEntity master = userMapper.selectByPrimaryKey(addAppRequest.getMasterUid());
			appEntity.setMaster(master.getName());
		}
		
		appMapper.updateByPrimaryKeySelective(appEntity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> deleteApp(@PathVariable("id") int id){
		SecurityUtil.requireSuperAdmin();
		int delete = appMapper.deleteByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(delete > 0),HttpStatus.OK);
	}
	
	@RequestMapping(value = "options", method = RequestMethod.GET)
	public @ResponseBody List<SelectOption> getAppOptions(){
		List<SelectOption> result = new ArrayList<>();
		List<AppEntity> list = null;
		if(SecurityUtil.isSuperAdmin()){
			list = appMapper.selectAll();
		}else{
			list = appMapper.findByMaster(SecurityUtil.getLoginUserInfo().getId());
		}
		
		list.stream().sorted(new Comparator<AppEntity>() {
			@Override
			public int compare(AppEntity o1, AppEntity o2) {
				return o1.getName().compareTo(o2.getName());
			}
		}).forEach( entity -> {
			String text = StringUtils.equals(entity.getName(), entity.getAlias()) ? entity.getName() : entity.getName() + "(" + entity.getAlias() + ")";
			result.add(new SelectOption(String.valueOf(entity.getId()), text));
		});
		return result;
	}
	
}
