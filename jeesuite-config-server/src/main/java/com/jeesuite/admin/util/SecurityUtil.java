package com.jeesuite.admin.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.jeesuite.admin.dao.entity.UserPermissionEntity;
import com.jeesuite.admin.exception.JeesuiteBaseException;
import com.jeesuite.admin.model.Constants;
import com.jeesuite.admin.model.LoginUserInfo;

public class SecurityUtil {
	
	public static LoginUserInfo getLoginUserInfo(){
		 HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		return (LoginUserInfo) request.getSession().getAttribute(Constants.LOGIN_SESSION_KEY);
	}
	
	public static boolean isSuperAdmin(){
		LoginUserInfo userInfo = getLoginUserInfo();
		return userInfo != null && userInfo.isSuperAdmin();
	}
	
	public static void requireSuperAdmin(){
		if(!isSuperAdmin())throw new JeesuiteBaseException(403, "超级管理员才有权限操作");
	}
	
	public static void requireAnyPermission(String env,String appIds){
		if(StringUtils.isBlank(env))throw new JeesuiteBaseException(1001, "profile字段缺失");
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
		if(!userInfo.getGrantedProfiles().contains(env)){
			throw new JeesuiteBaseException(403, "你没有profile["+env+"]权限");
		}
		
		if (appIds != null) {
			String permissionCode;
			String[] appIdArrays = StringUtils.split(appIds, ",");
			for (String appId : appIdArrays) {
				permissionCode = UserPermissionEntity.buildPermissionCode(userInfo.getId(), env,Integer.parseInt(appId));
				if (userInfo.getPermissons().get(env).contains(permissionCode)) {
					return;
				}
			}
			throw new JeesuiteBaseException(403, "你没有该项目权限");
		}
	}
	
	public static void requireAllPermission(String env){
		if(StringUtils.isBlank(env))throw new JeesuiteBaseException(1001, "profile字段缺失");
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
		if(!userInfo.getGrantedProfiles().contains(env)){
			throw new JeesuiteBaseException(403, "你没有profile["+env+"]权限");
		}
	}
	
	public static void requireAllPermission(String env,String appIds){
		if(StringUtils.isBlank(env))throw new JeesuiteBaseException(1001, "profile字段缺失");
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
		if(!userInfo.getGrantedProfiles().contains(env)){
			throw new JeesuiteBaseException(403, "你没有profile["+env+"]权限");
		}
		
		if (appIds != null) {
			String permissionCode;
			String[] appIdArrays = StringUtils.split(appIds, ",");
			for (String appId : appIdArrays) {
				permissionCode = UserPermissionEntity.buildPermissionCode(userInfo.getId(), env,Integer.parseInt(appId));
				if (!userInfo.getPermissons().get(env).contains(permissionCode)) {
					throw new JeesuiteBaseException(403, "你没有该项目权限");
				}
			}
		}
	}

}
