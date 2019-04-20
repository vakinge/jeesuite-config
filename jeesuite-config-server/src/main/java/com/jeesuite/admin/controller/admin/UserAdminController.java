package com.jeesuite.admin.controller.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.constants.GrantOperate;
import com.jeesuite.admin.dao.entity.ProfileEntity;
import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.entity.UserPermissionEntity;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;
import com.jeesuite.admin.dao.mapper.UserEntityMapper;
import com.jeesuite.admin.dao.mapper.UserPermissionEntityMapper;
import com.jeesuite.admin.exception.JeesuiteBaseException;
import com.jeesuite.admin.model.SelectOption;
import com.jeesuite.admin.model.UserPermission;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.model.request.GantPermRequest;
import com.jeesuite.admin.model.request.UpdateUserRequest;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.util.DigestUtils;

@Controller
@RequestMapping("/admin/user")
public class UserAdminController {

	private @Autowired UserEntityMapper userMapper;
	private @Autowired ProfileEntityMapper profileMapper;
	private @Autowired  UserPermissionEntityMapper userPermissionMapper;
	
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
		if(StringUtils.isBlank(param.getName())){
			throw new JeesuiteBaseException(1001, "用户名不能为空");
		}
		if(userMapper.findByName(param.getName()) != null){
			throw new JeesuiteBaseException(1001, "用户名已存在");
		}
		param.setPassword(DigestUtils.md5WithSalt("12345678", param.getName()));
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
			
			String oldPassword = DigestUtils.md5WithSalt(param.getOldPassword(), entity.getName());
			if(!StringUtils.equals(entity.getPassword(), oldPassword)){
				throw new JeesuiteBaseException(1001, "原密码不正确");
			}
			entity.setPassword(DigestUtils.md5WithSalt(param.getPassword(), entity.getName()));
		}
		entity.setUpdatedAt(new Date());
		userMapper.updateByPrimaryKeySelective(entity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> deleteProfile(@PathVariable("id") int id){
		SecurityUtil.requireSuperAdmin();
		int delete = userMapper.deleteByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(delete > 0),HttpStatus.OK);
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
		
		List<UserPermissionEntity> oldPermissons = userPermissionMapper.findByUserId(param.getUserId());
		List<UserPermissionEntity> newPermissons = new ArrayList<>(param.getPermissions().size()); 
		String grantTarget;String grantOper;
		for (UserPermission perm : param.getPermissions()) {
			String[] tmpArrays = StringUtils.splitByWholeSeparator(perm.getPermissionCode(), ":");
			grantTarget = tmpArrays[0];
			grantOper = tmpArrays.length == 1 ? GrantOperate.RW.name() : tmpArrays[1];
			newPermissons.add(new UserPermissionEntity(param.getUserId(),perm.getType(), grantTarget,grantOper));
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
