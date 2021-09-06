package com.jeesuite.admin.util;

import java.util.ArrayList;
import java.util.List;

import com.jeesuite.admin.constants.GrantOperate;
import com.jeesuite.admin.constants.UserType;
import com.jeesuite.admin.dao.entity.ApplicationEntity;
import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.entity.UserPermissionEntity;
import com.jeesuite.admin.dao.mapper.ApplicationEntityMapper;
import com.jeesuite.admin.dao.mapper.UserPermissionEntityMapper;
import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.spring.InstanceFactory;
import com.jeesuite.springweb.CurrentRuntimeContext;

public class SecurityUtil {
	
	public static LoginUserInfo getLoginUserInfo(){
		return (LoginUserInfo) CurrentRuntimeContext.getCurrentUser();
	}
	
	public static Integer getLoginUserId(){
		String id = CurrentRuntimeContext.getCurrentUser().getId();
		return Integer.parseInt(id);
	}
	
	public static boolean isSuperAdmin(){
		LoginUserInfo userInfo = getLoginUserInfo();
		return userInfo != null && userInfo.isSuperAdmin();
	}
	
	public static void requireSuperAdmin(){
		if(!isSuperAdmin())throw new JeesuiteBaseException(403, "超级管理员才有权限操作");
	}

	public static void hasPermssionFor(UserEntity targetUser){
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
		if(!userInfo.isGroupAdmin())throw new JeesuiteBaseException(403, "你不是业务组管理员");
		if(!userInfo.getGroupId().equals(targetUser.getGroupId())){
			throw new JeesuiteBaseException(403, "不能操作其他组成员");
		}
		if(targetUser.getId() != null && !userInfo.getId().equals(targetUser.getId().toString())){
			throw new JeesuiteBaseException(403, "不能操作本人");
		}
		if(!userInfo.isGroupMaster() && UserType.superAdmin.name().equals(targetUser.getType())){
			throw new JeesuiteBaseException(403, "组负责人才能操作组管理员");
		}
	}
	
	public static void requireAnyPermission(List<String> appIds,GrantOperate operate){
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
        List<String> permCodes = userInfo.getGrantPermissons();
        if(permCodes == null)throw new JeesuiteBaseException(403, "你没有该项目权限");
		if (appIds != null) {
			for (String appId : appIds) {
				if(permCodes.contains(buildPermissionCode(appId, operate))){
					return;
				}
			}
			throw new JeesuiteBaseException(403, "你没有该项目权限");
		}
	}
	
	public static void requireAllPermission(Integer groupId, Integer appId, GrantOperate operate) {
		LoginUserInfo userInfo = getLoginUserInfo();
		if (userInfo.isSuperAdmin())
			return;
		if (userInfo.getGroupId() != null && userInfo.getGroupId().equals(groupId))
			return;
		if(appId == null || appId == 0){
			throw new JeesuiteBaseException(403, "你没有全局配置权限");
		}
		List<String> permCodes = userInfo.getGrantPermissons();
		if (permCodes == null)
			throw new JeesuiteBaseException(403, "你没有该项目权限");
		if (!permCodes.contains(buildPermissionCode(appId.toString(), operate))) {
			throw new JeesuiteBaseException(403, "你没有appId[" + appId + "]权限");
		}
	}
	
	public static void  initPermssionDatas(LoginUserInfo loginUserInfo){

		List<ApplicationEntity> apps = new ArrayList<>(0);
		if(loginUserInfo.getUserType().equals(UserType.user.name())) {
			apps = InstanceFactory.getInstance(ApplicationEntityMapper.class).findByMaster(Integer.parseInt(loginUserInfo.getId()));
		} else if (loginUserInfo.getUserType().equals(UserType.groupAdmin.name())) {
 			apps = InstanceFactory.getInstance(ApplicationEntityMapper.class).findByGroupId(loginUserInfo.getGroupId());
		}
		String permissionCode;
		for (ApplicationEntity entity : apps) {
			permissionCode = buildPermissionCode(entity.getId().toString(), GrantOperate.RW);
			loginUserInfo.getGrantPermissons().add(permissionCode);
			loginUserInfo.getGrantAppIds().add(entity.getId());
		}
		Integer userId = Integer.parseInt(loginUserInfo.getId());
		List<UserPermissionEntity> userPermissions = InstanceFactory.getInstance(UserPermissionEntityMapper.class).findByUserId(userId);
		for (UserPermissionEntity entity : userPermissions) {
			if(loginUserInfo.getGrantAppIds().contains(entity.getAppId())){
				continue;
			}
			permissionCode = buildPermissionCode(entity.getAppId().toString(), GrantOperate.valueOf(entity.getOperate()));
			loginUserInfo.getGrantPermissons().add(permissionCode);
			if(!loginUserInfo.getGrantAppIds().contains(entity.getAppId())){
				loginUserInfo.getGrantAppIds().add(entity.getAppId());
			}
		}
	}
	
	public static void reloadPermssionDatas(){
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
		userInfo.getGrantAppIds().clear();
		userInfo.getGrantPermissons().clear();
		initPermssionDatas(userInfo);
	}
	
	 private static String buildPermissionCode(String appId,GrantOperate operate){
	     return String.format("%s:%s", appId,operate.name());
	 }

}
