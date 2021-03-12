package com.jeesuite.admin.controller.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.constants.GrantOperate;
import com.jeesuite.admin.constants.UserType;
import com.jeesuite.admin.dao.entity.BusinessGroupEntity;
import com.jeesuite.admin.dao.entity.ProfileEntity;
import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.entity.UserPermissionEntity;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
import com.jeesuite.admin.dao.mapper.BusinessGroupEntityMapper;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;
import com.jeesuite.admin.dao.mapper.UserEntityMapper;
import com.jeesuite.admin.dao.mapper.UserPermissionEntityMapper;
import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.admin.model.SelectOption;
import com.jeesuite.admin.model.UserGrantPermItem;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.model.request.GantPermRequest;
import com.jeesuite.admin.model.request.UpdateUserRequest;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.util.AssertUtil;
import com.jeesuite.security.SecurityDelegating;

@Controller
@RequestMapping("/admin/user")
public class UserAdminController {

	private @Autowired UserEntityMapper userMapper;
	private @Autowired AppEntityMapper appMapper;
	private @Autowired ProfileEntityMapper profileMapper;
	private @Autowired  UserPermissionEntityMapper userPermissionMapper;
    private @Autowired BusinessGroupEntityMapper businessGroupMapper;
	
	@RequestMapping(value = "list", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getUsers(@RequestParam(value="groupId",required=false) Integer groupId){
		List<UserEntity> list = queryUserList(groupId);
		List<BusinessGroupEntity> groups = businessGroupMapper.listAllEnabledGroup();
		Map<Integer, BusinessGroupEntity> idEntityMap = groups.stream().collect(Collectors.toMap(BusinessGroupEntity::getId, o -> o));
		Map<Integer, BusinessGroupEntity> masterEntityMap = groups.stream().filter(e -> e.getMasterUid() != null).collect(Collectors.toMap(BusinessGroupEntity::getMasterUid, o -> o));
		for (UserEntity entity : list) {
			if(masterEntityMap.containsKey(entity.getId())){
				entity.setGroupId(masterEntityMap.get(entity.getId()).getId());
				entity.setType(UserType.groupAdmin.name());
				entity.setGroupMaster(true);
			}
			if(StringUtils.isNotBlank(entity.getMobile())){
				entity.setMobile(entity.getMobile().substring(0,7).concat("****"));
			}
			if(entity.getGroupId() != null && entity.getGroupId() > 0){
				entity.setGroupName(idEntityMap.get(entity.getGroupId()).getName());
			}
		}
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
		UserEntity user = userMapper.selectByPrimaryKey(SecurityUtil.getLoginUserId());
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(user),HttpStatus.OK);
	}
	
	@Transactional
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> addUser(@RequestBody UserEntity param){
		if(StringUtils.isAnyBlank(param.getName(),param.getMobile())){
			throw new JeesuiteBaseException(1001, "用户名/手机号不能为空");
		}
		if(userMapper.findByName(param.getName()) != null){
			throw new JeesuiteBaseException(1001, "用户名已存在");
		}
		if(userMapper.findByMobile(param.getMobile()) != null){
			throw new JeesuiteBaseException(1001, "手机号已存在");
		}
		if(param.getType() == null) {
			throw new JeesuiteBaseException(1001, "用户类型不能为空");
		}
		
		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();
		
		BusinessGroupEntity groupEntity = null; 
		if(param.getGroupId() == null) {
			if(StringUtils.isBlank( param.getGroupName())) {
				throw new JeesuiteBaseException("请输入组名称");
			}
			groupEntity = new BusinessGroupEntity();
			groupEntity.setName( param.getGroupName());
			groupEntity.setCreatedAt(new Date());
			groupEntity.setCreatedBy(loginUserInfo.getName());
			businessGroupMapper.insertSelective(groupEntity);
			//
			param.setGroupId(groupEntity.getId());
		}
		
		SecurityUtil.hasPermssionFor(param);
		//默认手机后8位是密码
		BCrypt.hashpw(param.getMobile().substring(3), BCrypt.gensalt(4));
		param.setEnabled(true);
		if(param.getType() == null) {
			param.setType(UserType.user.name());
		}
		param.setCreatedAt(new Date());
		userMapper.insertSelective(param);
		//
		if(param.isGroupAdmin() && groupEntity != null) {
			groupEntity.setMaster(param.getName());
			groupEntity.setMasterUid(param.getId());
			businessGroupMapper.updateByPrimaryKeySelective(groupEntity);
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(param),HttpStatus.OK);
	}
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> updateUser(HttpServletRequest request,@RequestBody UpdateUserRequest param){
		UserEntity entity = userMapper.selectByPrimaryKey(SecurityUtil.getLoginUserId());
	
		if(StringUtils.isNotBlank(param.getEmail()))entity.setEmail(param.getEmail());
		if(StringUtils.isNotBlank(param.getMobile()))entity.setMobile(param.getMobile());
		if(StringUtils.isNotBlank(param.getPassword())){
			
			if(!BCrypt.checkpw(param.getOldPassword(), entity.getPassword())){
				throw new JeesuiteBaseException(1001, "原密码不正确");
			}
			entity.setPassword(BCrypt.hashpw(param.getPassword(), BCrypt.gensalt(4)));
		}
		if(param.getGroupId() != null && param.getGroupId() > 0){
			entity.setGroupId(param.getGroupId());
			LoginUserInfo userInfo = SecurityUtil.getLoginUserInfo();
			userInfo.setGroupId(param.getGroupId());
			SecurityDelegating.updateSession(userInfo);
		}
		entity.setUpdatedAt(new Date());
		userMapper.updateByPrimaryKeySelective(entity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "changeGroupAdmin/{id}", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> changeGroupAdminStatus(@PathVariable("id") Integer id){
		UserEntity userEntity = userMapper.selectByPrimaryKey(id);
		
		SecurityUtil.hasPermssionFor(userEntity);
		if(userEntity.isSuperAdmin()){
			throw new JeesuiteBaseException(1001, "该账号不允许该操作");
		}
		if(userEntity.getGroupId() == null || userEntity.getGroupId() == 0){
			throw new JeesuiteBaseException(1001, "该账号还没分配业务组");
		}
		if(userEntity.isGroupAdmin()){
			userMapper.updateUserType(userEntity.getId(), UserType.user.name());
		}else{
			userMapper.updateUserType(userEntity.getId(), UserType.groupAdmin.name());
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> deleteProfile(@PathVariable("id") Integer id){
		SecurityUtil.hasPermssionFor(userMapper.selectByPrimaryKey(id));
		userMapper.deleteByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(),HttpStatus.OK);
	}
	
	@RequestMapping(value = "resetpassword/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> resetpassword(@PathVariable("id") Integer id){
		UserEntity entity = userMapper.selectByPrimaryKey(id);
		//
		SecurityUtil.hasPermssionFor(entity);
		String password = RandomStringUtils.random(8, true, true);
		entity.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(4)));
		userMapper.updateByPrimaryKey(entity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(password),HttpStatus.OK);
	}
	
	@RequestMapping(value = "switch/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> switchProfile(@PathVariable("id") int id){
		UserEntity entity = userMapper.selectByPrimaryKey(id);
		//
		SecurityUtil.hasPermssionFor(entity);
		entity.setEnabled(!entity.getEnabled());
		userMapper.updateByPrimaryKey(entity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "grant_permissions", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> gantPermission(@RequestBody GantPermRequest param){
		if(param.getPermissions() == null || param.getPermissions().isEmpty()){
			throw new JeesuiteBaseException(1001, "至少选择一个应用权限");
		}
		if(param.getUserId() == 0){
			throw new JeesuiteBaseException(1001, "参数[userId]必填");
		}
		//
		SecurityUtil.hasPermssionFor(userMapper.selectByPrimaryKey(param.getUserId()));
		
		Map<Integer,UserPermissionEntity> oldPermissons = userPermissionMapper.findByUserId(param.getUserId())
				                                   .stream()
				                                   .collect(Collectors.toMap(UserPermissionEntity::getAppId, o -> o));
		
		Map<Integer,UserPermissionEntity> newPermissons = new HashMap<>(param.getPermissions().size()); 
		Integer appId;String grantOper;
		for (String  perm : param.getPermissions()) {
			String[] tmpArrays = StringUtils.splitByWholeSeparator(perm, ":");
			appId = Integer.parseInt(tmpArrays[0]);
			grantOper = tmpArrays[1];
			newPermissons.put(appId,new UserPermissionEntity(param.getUserId(), appId,grantOper));
		}
		List<UserPermissionEntity> addList = new ArrayList<>();
		List<UserPermissionEntity> updateList = new ArrayList<>();
		List<UserPermissionEntity> removeList = new ArrayList<>();
		
		if(!oldPermissons.isEmpty()){
			UserPermissionEntity tmpEntity;
			for (UserPermissionEntity entity : newPermissons.values()) {
				tmpEntity = oldPermissons.get(entity.getAppId());
				if(tmpEntity != null){
					if(!entity.equals2(tmpEntity)){
						if("NONE".equals(entity.getOperate())){
							removeList.add(entity);
						}else{
							updateList.add(entity);
						}
					}
				}else{
					addList.add(entity);
				}
			}
		}else{
			addList.addAll(newPermissons.values());
		}
		
		if(!addList.isEmpty()){
			userPermissionMapper.insertList(addList);
		}
		
		if(!updateList.isEmpty()){
			for (UserPermissionEntity entity : updateList) {
				entity.setOperate(oldPermissons.get(entity.getAppId()).getOperate());
				userPermissionMapper.updateByPrimaryKeySelective(entity);
			}
		}
		
        if(removeList != null && !removeList.isEmpty()){
            for (UserPermissionEntity entity : removeList) {
            	userPermissionMapper.deleteByPrimaryKey(entity.getId());
			}
		}
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}

	@RequestMapping(value = "user_permission_options", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getUserPermissionOptions(@RequestParam Integer userId){
		UserEntity userEntity = userMapper.selectByPrimaryKey(userId);
		AssertUtil.notNull(userEntity);
		if(userEntity.getGroupId() == null){
			throw new JeesuiteBaseException(1002, "该用户还没分配业务组");
		}
		List<UserGrantPermItem> datas = appMapper.findByGroupId(userEntity.getGroupId()).stream().map(e -> {
			String operate = userId.equals(e.getMasterUid()) ? GrantOperate.RW.name() : null;
			return new UserGrantPermItem(e.getId(), e.getAppName(), operate);
		}).collect(Collectors.toList());
		
		Map<Integer, UserPermissionEntity> userPermissons = userPermissionMapper.findByUserId(userId).stream().collect(Collectors.toMap(UserPermissionEntity::getAppId, e -> e));
		for (UserGrantPermItem app : datas) {
			if(userPermissons.containsKey(app.getAppId())){
				app.setOperate(userPermissons.get(app.getAppId()).getOperate());
			}
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(datas),HttpStatus.OK);
	}
	
	@RequestMapping(value = "options", method = RequestMethod.GET)
	public @ResponseBody List<SelectOption> getUserListOptions(@RequestParam(value="groupId",required=false) Integer groupId){
		List<SelectOption> result = new ArrayList<>();
		List<UserEntity> list = queryUserList(groupId);
		for (UserEntity entity : list) {
			result.add(new SelectOption(entity.getId().toString(), entity.getName()));
		}
		return result;
	}

	/**
	 * @param groupId
	 * @return
	 */
	private List<UserEntity> queryUserList(Integer groupId) {
		List<UserEntity> list;
		if(SecurityUtil.isSuperAdmin()){
			if(groupId != null){
				list = userMapper.findByGroupId(groupId);
			}else{				
				list = userMapper.findAllUser();
			}
		}else{
			list = userMapper.findByGroupId(SecurityUtil.getLoginUserInfo().getGroupId());
		}
		return list;
	}

}
