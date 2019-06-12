package com.jeesuite.admin.controller.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.admin.dao.entity.ProfileEntity;
import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.entity.UserPermissionEntity;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;
import com.jeesuite.admin.dao.mapper.UserEntityMapper;
import com.jeesuite.admin.dao.mapper.UserPermissionEntityMapper;
import com.jeesuite.admin.model.SelectOption;
import com.jeesuite.admin.model.UserGrantPermGroup;
import com.jeesuite.admin.model.UserGrantPermItem;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.model.request.GantPermRequest;
import com.jeesuite.admin.model.request.UpdateUserRequest;
import com.jeesuite.admin.service.CacheQueryService;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.JeesuiteBaseException;

@Controller
@RequestMapping("/admin/user")
public class UserAdminController {

	private @Autowired UserEntityMapper userMapper;
	private @Autowired AppEntityMapper appMapper;
	private @Autowired ProfileEntityMapper profileMapper;
	private @Autowired  UserPermissionEntityMapper userPermissionMapper;
	private @Autowired CacheQueryService cacheQueryService;
	
	@RequestMapping(value = "list", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getUsers(){
		List<UserEntity> list = userMapper.findAllUser();
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(list),HttpStatus.OK);
	}
	
	@RequestMapping(value = "auth_options", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<WrapperResponseEntity> authOptions(){
		List<SelectOption> result = new ArrayList<>();
		List<ProfileEntity> list = profileMapper.findAllEnabledProfiles();
		for (ProfileEntity entity : list) {
			result.add(new SelectOption(entity.getName(), entity.getAlias()));
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(result),HttpStatus.OK);
	}
	
	@RequestMapping(value = "get/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getUser(@PathVariable("id") int id){
		UserEntity user = userMapper.selectByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(user),HttpStatus.OK);
	}
	
	@RequestMapping(value = "myinfo", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getMyInfo(){
		UserEntity user = userMapper.selectByPrimaryKey(SecurityUtil.getLoginUserInfo().getId());
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(user),HttpStatus.OK);
	}
	
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> addUser(@RequestBody UserEntity param){
		SecurityUtil.requireSuperAdmin();
		if(StringUtils.isAnyBlank(param.getName(),param.getMobile())){
			throw new JeesuiteBaseException(1001, "用户名/手机号不能为空");
		}
		if(userMapper.findByName(param.getName()) != null){
			throw new JeesuiteBaseException(1001, "用户名已存在");
		}
		if(userMapper.findByMobile(param.getMobile()) != null){
			throw new JeesuiteBaseException(1001, "手机号已存在");
		}
		param.setPassword(UserEntity.encryptPassword(param.getMobile().substring(3)));
		param.setStatus((short)1);
		param.setType((short)2);
		param.setCreatedAt(new Date());
		userMapper.insertSelective(param);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(param),HttpStatus.OK);
	}
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> modifyPassword(@RequestBody UpdateUserRequest param){
		UserEntity entity = userMapper.selectByPrimaryKey(SecurityUtil.getLoginUserInfo().getId());
		if(entity.getName().equals("test")){
			throw new JeesuiteBaseException(1001, "测试账号不允许编辑");
		}
		if(StringUtils.isNotBlank(param.getEmail()))entity.setEmail(param.getEmail());
		if(StringUtils.isNotBlank(param.getMobile()))entity.setMobile(param.getMobile());
		if(StringUtils.isNotBlank(param.getPassword())){
			
			String oldPassword = UserEntity.encryptPassword(param.getOldPassword());
			if(!StringUtils.equals(entity.getPassword(), oldPassword)){
				throw new JeesuiteBaseException(1001, "原密码不正确");
			}
			entity.setPassword(UserEntity.encryptPassword(param.getPassword()));
		}
		entity.setUpdatedAt(new Date());
		userMapper.updateByPrimaryKeySelective(entity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> deleteProfile(@PathVariable("id") Integer id){
		SecurityUtil.requireSuperAdmin();
		userMapper.deleteByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(),HttpStatus.OK);
	}
	
	@RequestMapping(value = "switch/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> switchProfile(@PathVariable("id") int id){
		SecurityUtil.requireSuperAdmin();
		UserEntity entity = userMapper.selectByPrimaryKey(id);
		entity.setStatus(entity.getStatus() == 1 ? (short)0 : (short)1);
		userMapper.updateByPrimaryKey(entity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "grant_permissions", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> gantPermission(@RequestBody GantPermRequest param){
		SecurityUtil.requireSuperAdmin();
		if(param.getPermissions() == null || param.getPermissions().isEmpty()){
			throw new JeesuiteBaseException(1001, "至少选择一个应用权限");
		}
		if(param.getUserId() == 0 || StringUtils.isBlank(param.getEnv())){
			throw new JeesuiteBaseException(1001, "参数[userId,env]必填");
		}
		List<UserPermissionEntity> oldPermissons = userPermissionMapper.findByUserIdAndEnv(param.getUserId(),param.getEnv());
		List<UserPermissionEntity> newPermissons = new ArrayList<>(param.getPermissions().size()); 
		Integer appId;String grantOper;
		for (String  perm : param.getPermissions()) {
			String[] tmpArrays = StringUtils.splitByWholeSeparator(perm, ":");
			appId = Integer.parseInt(tmpArrays[0]);
			grantOper = tmpArrays[1];
			newPermissons.add(new UserPermissionEntity(param.getUserId(),param.getEnv(), appId,grantOper));
		}
		List<UserPermissionEntity> addList;
		List<UserPermissionEntity> removeList = null;
		if(!oldPermissons.isEmpty()){
			addList = new ArrayList<>(newPermissons);
			addList.removeAll(oldPermissons);
			removeList = new ArrayList<>(oldPermissons);
			removeList.removeAll(newPermissons);
		}else{
			addList = newPermissons;
		}
		
		if(!addList.isEmpty()){
			userPermissionMapper.insertList(addList);
		}
		
        if(removeList != null && !removeList.isEmpty()){
            for (UserPermissionEntity entity : removeList) {
            	userPermissionMapper.deleteByPrimaryKey(entity.getId());
			}
		}
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "get_user_permissions", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getUserPermission(@RequestParam Integer userId){
		Map<String, UserGrantPermGroup> permGroups = profileMapper.findAllEnabledProfiles().stream().collect(Collectors.toMap(ProfileEntity::getName, e -> {
			return new UserGrantPermGroup(e.getName(),e.getAlias());
		}));
		
		List<UserPermissionEntity> userPermssions = userPermissionMapper.findByUserId(userId);
		UserGrantPermItem item;
		AppEntity appEntity;
		for (UserPermissionEntity entity : userPermssions) {
			if(!permGroups.containsKey(entity.getEnv()))continue;
			appEntity = cacheQueryService.findAppEntity(entity.getAppId());
			if(appEntity == null)continue;
			item = new UserGrantPermItem();
			item.setAppId(entity.getAppId());
			item.setAppName(appEntity.getFullName());
			item.setOperate(entity.getOperate());
			permGroups.get(entity.getEnv()).getPerms().add(item);
		}
		
		List<UserGrantPermGroup> groupList = new ArrayList<>(permGroups.values());
		Collections.sort(groupList,new Comparator<UserGrantPermGroup>() {
			@Override
			public int compare(UserGrantPermGroup o1, UserGrantPermGroup o2) {
				return o1.getEnv().compareTo(o2.getEnv());
			}
		});
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(groupList),HttpStatus.OK);
	}
	
	@RequestMapping(value = "user_permission_options", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getUserPermissionOptions(@RequestParam Integer userId,@RequestParam String env){
		List<UserGrantPermItem> datas = appMapper.selectAll().stream().map(e -> {
			return new UserGrantPermItem(e.getId(), e.getAlias(), null);
		}).collect(Collectors.toList());
		
		Map<Integer, UserPermissionEntity> userPermissons = userPermissionMapper.findByUserIdAndEnv(userId,env).stream().collect(Collectors.toMap(UserPermissionEntity::getAppId, e -> e));
		for (UserGrantPermItem app : datas) {
			if(userPermissons.containsKey(app.getAppId())){
				app.setOperate(userPermissons.get(app.getAppId()).getOperate());
			}
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(datas),HttpStatus.OK);
	}
	
	@RequestMapping(value = "options", method = RequestMethod.GET)
	public @ResponseBody List<SelectOption> getUserListOptions(){
		List<SelectOption> result = new ArrayList<>();
		List<UserEntity> list = userMapper.findAllUser();
		for (UserEntity entity : list) {
			result.add(new SelectOption(entity.getId().toString(), entity.getName()));
		}
		return result;
	}

}
