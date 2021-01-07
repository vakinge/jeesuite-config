package com.jeesuite.admin.controller.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.component.EventPublishClient;
import com.jeesuite.admin.constants.AppType;
import com.jeesuite.admin.constants.GrantOperate;
import com.jeesuite.admin.constants.UserType;
import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.admin.dao.entity.ProfileEntity;
import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
import com.jeesuite.admin.dao.mapper.AppconfigEntityMapper;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;
import com.jeesuite.admin.dao.mapper.UserEntityMapper;
import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.admin.model.PageResult;
import com.jeesuite.admin.model.SelectOption;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.model.request.AddOrEditAppRequest;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.model.IdParam;
import com.jeesuite.common.model.KeyValuePair;
import com.jeesuite.common.model.Page;
import com.jeesuite.common.model.PageParams;
import com.jeesuite.common.util.AssertUtil;
import com.jeesuite.common.util.BeanUtils;
import com.jeesuite.mybatis.plugin.pagination.PageExecutor;

@Controller
@RequestMapping("/admin/app")
public class AppAdminController {

	final static Logger logger = LoggerFactory.getLogger("controller");
	
	private @Autowired AppEntityMapper appMapper;
	private @Autowired ProfileEntityMapper profileMapper;
	private @Autowired UserEntityMapper userMapper;
	private @Autowired AppconfigEntityMapper appconfigMapper;
	
	@RequestMapping(value = "listAll", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<WrapperResponseEntity> findApps(
			@RequestParam(value="appType",required = false) String appType
			){
		
		List<AppEntity> list = findAppByCurrentUserAndParams(appType,null);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(list),HttpStatus.OK);
	}

	@RequestMapping(value = "list",method = RequestMethod.POST)
	public @ResponseBody PageResult<AppEntity> pageQuery(
			@RequestParam(value="appType",required = false) String appType,
		    @RequestParam(value = "pageNo")Integer pageNo,
		    @RequestParam(value = "pageSize")Integer pageSize){
		Map<String, Object> queryParams = new HashMap<>();

		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();

		if(loginUserInfo.getUserType().equals(UserType.user.name())) {
			queryParams.put("enabled", 1);
			queryParams.put("userId", loginUserInfo.getId());
		} else if(loginUserInfo.getUserType().equals(UserType.groupAdmin.name())) {
			queryParams.put("groupId",loginUserInfo.getGroupId());
		}
		if(appType != null){
			queryParams.put("appType", appType);
		}

		Page<AppEntity> page = PageExecutor.pagination(new PageParams(pageNo, pageSize), new PageExecutor.PageDataLoader<AppEntity>() {
			@Override
			public List<AppEntity> load() {
				return appMapper.findByQueryParams(queryParams);
			}
		});
		return new PageResult<>(pageNo, pageSize, page.getTotal(), page.getData());
	}
	
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getApp(@PathVariable("id") int id){
		AppEntity entity = appMapper.selectByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(entity),HttpStatus.OK);
	}
	
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> addApp(@RequestBody AddOrEditAppRequest addAppRequest){
		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();
		if(!loginUserInfo.isSuperAdmin() || addAppRequest.getMasterUid() == null || addAppRequest.getMasterUid() == 0){
			addAppRequest.setMasterUid(Integer.parseInt(loginUserInfo.getId()));
		}

		if(!loginUserInfo.isSuperAdmin() && addAppRequest.getGroupId() == null){
			addAppRequest.setGroupId(loginUserInfo.getGroupId());
		}
		
		if(addAppRequest.getGroupId() == null){
			throw new JeesuiteBaseException(1002, "业务组必填");
		}

		if(appMapper.findByAppKey(addAppRequest.getAppKey()) != null){
			throw new JeesuiteBaseException(1002, "应用["+addAppRequest.getAppKey()+"]已存在");
		}
		AppEntity appEntity = BeanUtils.copy(addAppRequest, AppEntity.class);
		//
		UserEntity master = userMapper.selectByPrimaryKey(addAppRequest.getMasterUid());
		appEntity.setMaster(master.getName());
		appEntity.setCreatedAt(new Date());
		appEntity.setCreatedBy(SecurityUtil.getLoginUserInfo().getName());
		appMapper.insertSelective(appEntity);
		
		//
		SecurityUtil.reloadPermssionDatas();
		//发送事件
		EventPublishClient.publishAllEnvs("configChanged", "app", null);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> updateApp(@RequestBody AddOrEditAppRequest addAppRequest){
		AppEntity app = appMapper.selectByPrimaryKey(addAppRequest.getId());
		if(app == null){
			throw new JeesuiteBaseException(1002, "应用不存在");
		}
		SecurityUtil.requireAllPermission(app.getGroupId(), app.getId(), GrantOperate.RW);

		AppEntity appEntity = BeanUtils.copy(addAppRequest, AppEntity.class);
		
		if(addAppRequest.getMasterUid() != null && addAppRequest.getMasterUid() > 0 
				&& !addAppRequest.getMasterUid().equals(app.getMasterUid())){
			UserEntity master = userMapper.selectByPrimaryKey(addAppRequest.getMasterUid());
			appEntity.setMaster(master.getName());
		}
		
		appEntity.setAnonymousUris(StringUtils.trimToEmpty(appEntity.getAnonymousUris()));
		appEntity.setUpdatedBy(SecurityUtil.getLoginUserInfo().getName());
		appEntity.setUpdatedAt(new Date());
		appMapper.updateByPrimaryKeySelective(appEntity);
		//发送事件
		EventPublishClient.publishAllEnvs("configChanged", "app", null);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> deleteApp(@PathVariable("id") Integer id){
		
		SecurityUtil.requireSuperAdmin();
		//
		AppEntity app = appMapper.selectByPrimaryKey(id);
		if(app == null){
			throw new JeesuiteBaseException(1002, "应用不存在");
		}
		//
		app.setEnabled(false);
		app.setUpdatedBy(SecurityUtil.getLoginUserInfo().getName());
		app.setUpdatedAt(new Date());
		appMapper.updateByPrimaryKey(app);
		//删除关联的配置
		appconfigMapper.deleteByAppId(id.toString());
		
		//发送事件
		EventPublishClient.publishAllEnvs("configChanged", "app", null);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(),HttpStatus.OK);
	}
	
	@RequestMapping(value = "toggle", method = RequestMethod.POST)
	public @ResponseBody WrapperResponseEntity enableSwitch(@RequestBody IdParam<Integer> param){
        AppEntity entity = appMapper.selectByPrimaryKey(param.getId());
		AssertUtil.notNull(entity);
		SecurityUtil.requireAllPermission(entity.getGroupId(), entity.getId(), GrantOperate.RW);
		entity.setEnabled(!entity.getEnabled());
		//发送事件
		EventPublishClient.publishAllEnvs("configChanged", "app", null);
		
		appMapper.updateByPrimaryKeySelective(entity);
		
		return new WrapperResponseEntity();
	}
	
	@RequestMapping(value = "options", method = RequestMethod.GET)
	public @ResponseBody List<SelectOption> getAppOptions(@RequestParam(value="env",required=false) String env
			,@RequestParam(value="grantType",required=false) String grantType
			,@RequestParam(value="appType",required = false) String appType
			,@RequestParam(value="formatValue",required = false) String formatValue){
		List<AppEntity> list = findAppByCurrentUserAndParams(appType, grantType);
		return list.stream().map(e -> {
			return new SelectOption("appKey".equals(formatValue) ? e.getAppKey() : String.valueOf(e.getId()), e.getFullName());
		}).collect(Collectors.toList());
	}
	
	@RequestMapping(value = "type/options", method = RequestMethod.GET)
	public @ResponseBody List<SelectOption> getAppTypeOptions(){
		AppType[] values = AppType.values();
		List<SelectOption> list = new ArrayList<>(values.length);
        for (AppType appType : values) {
        	list.add(new SelectOption(appType.name(), appType.getCnName()));
		}
		return list;
	}
	
	@RequestMapping(value = "extrAttrs", method = RequestMethod.GET)
	public @ResponseBody WrapperResponseEntity getExtrAttrs(@RequestParam int appId,@RequestParam String name){
		List<KeyValuePair> attrs = new ArrayList<>();
		List<ProfileEntity> profiles = profileMapper.findAllEnabledProfiles();
		for (ProfileEntity profile : profiles) {
			String extrAttr = appMapper.findExtrAttr(appId, profile.getName(), name);
			attrs.add(new KeyValuePair(profile.getName(), StringUtils.trimToEmpty(extrAttr)));
		}
		return new WrapperResponseEntity(attrs);
	}

	
	private List<AppEntity> findAppByCurrentUserAndParams(String appType,String operate){
		
		Map<String, Object> param = new HashMap<String, Object>(4);
		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();
		if(loginUserInfo.isGroupAdmin()){
			param.put("groupId", loginUserInfo.getGroupId());
		}else if(!loginUserInfo.isSuperAdmin()){
			param.put("userId", loginUserInfo.getId());
		}else{
			if(StringUtils.isNotBlank(operate)){
				param.put("operate", operate);
			}
		}

		if(appType != null){
			param.put("appType", appType);
		}

		return appMapper.findByQueryParams(param);
	}
}
