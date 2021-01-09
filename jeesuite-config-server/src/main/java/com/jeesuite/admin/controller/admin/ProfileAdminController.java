package com.jeesuite.admin.controller.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

import com.jeesuite.admin.component.ProfileZkClient;
import com.jeesuite.admin.constants.ProfileExtrAttrName;
import com.jeesuite.admin.dao.entity.ProfileEntity;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;
import com.jeesuite.admin.model.SelectOption;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.model.KeyValuePair;

@Controller
@RequestMapping("/admin/profile")
public class ProfileAdminController {

	private @Autowired ProfileEntityMapper profileMapper;
	
	private @Autowired ProfileZkClient profileZkClient;
	
	@RequestMapping(value = "list", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getProfiles(){
		List<ProfileEntity> list = profileMapper.selectAll();
//		for (ProfileEntity entity : list) {
//			List<KeyValuePair> extrAttrs = profileMapper.findExtrAttrs(entity.getName());
//			entity.setExtrAttrs(extrAttrs);
//		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(list),HttpStatus.OK);
	}
	
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getProfile(@PathVariable("id") int id){
		ProfileEntity entity = profileMapper.selectByPrimaryKey(id);
		List<KeyValuePair> extrAttrs = profileMapper.findExtrAttrs(entity.getName());
		entity.setExtrAttrs(extrAttrs);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(entity),HttpStatus.OK);
	}
	
	@RequestMapping(value = "update/attrs", method = RequestMethod.POST)
	public @ResponseBody WrapperResponseEntity updateProfileAttrs(@RequestParam("id") int id,@RequestBody List<KeyValuePair> param){
		SecurityUtil.requireSuperAdmin();
		ProfileEntity entity = profileMapper.selectByPrimaryKey(id);
		Map<String, String> orginAttrs = profileMapper.findExtrAttrs(entity.getName()).stream().collect(Collectors.toMap(KeyValuePair::getKey, KeyValuePair::getValue));
		for (KeyValuePair kvPair : param) {
			if(!orginAttrs.containsKey(kvPair.getKey())){
				profileMapper.insertExtrAttr(entity.getName(), kvPair.getKey(), kvPair.getValue());
			}else if(!kvPair.getValue().equals(orginAttrs.get(kvPair.getKey()))){
				profileMapper.updateExtrAttr(entity.getName(), kvPair.getKey(), kvPair.getValue());
			}else{
				continue;
			}
			
			if(StringUtils.equals(kvPair.getKey(), ProfileExtrAttrName.zkServers.name())){
				if(StringUtils.isNotBlank(orginAttrs.get(kvPair.getKey()))){
					profileZkClient.remove(entity.getName(),true);
				}
				if(StringUtils.isNotBlank(kvPair.getValue())){
					profileZkClient.add(entity.getName(),kvPair.getValue(),true);
				}
			}
		}
		return new WrapperResponseEntity();
	}
	
	@RequestMapping(value = "options", method = RequestMethod.GET)
	public @ResponseBody List<SelectOption> getProfiles2(){
		List<SelectOption> result = new ArrayList<>();
		List<ProfileEntity> list = profileMapper.findAllEnabledProfiles();
		for (ProfileEntity entity : list) {
			result.add(new SelectOption(entity.getName(), entity.getAlias() + "("+entity.getName()+")"));
		}
		return result;
	}
	
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> addProfile(@RequestBody ProfileEntity param){
		SecurityUtil.requireSuperAdmin();
		ProfileEntity entity = profileMapper.findByName(param.getName());
		if(entity != null)throw new JeesuiteBaseException(1002, "Profile["+param.getName()+"]已存在");
		profileMapper.insertSelective(param);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> updateProfile(@RequestBody ProfileEntity param){
		SecurityUtil.requireSuperAdmin();
		if(param.getId() == null || param.getId() == 0){
			throw new JeesuiteBaseException(1003, "id参数缺失");
		}
		ProfileEntity entity = profileMapper.selectByPrimaryKey(param.getId());
		entity.setAlias(param.getAlias());
		entity.setName(param.getName());
		profileMapper.updateByPrimaryKey(entity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> deleteProfile(@PathVariable("id") Integer id){
		SecurityUtil.requireSuperAdmin();
		ProfileEntity entity = profileMapper.selectByPrimaryKey(id);
		if(entity.getIsDefault())throw new JeesuiteBaseException(1003, "默认profile不能删除");
		profileMapper.deleteByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(),HttpStatus.OK);
	}
	
	@RequestMapping(value = "switch/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> switchProfile(@PathVariable("id") int id){
		SecurityUtil.requireSuperAdmin();
		ProfileEntity entity = profileMapper.selectByPrimaryKey(id);
		entity.setEnabled(!entity.getEnabled());
		profileMapper.updateByPrimaryKey(entity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
}
