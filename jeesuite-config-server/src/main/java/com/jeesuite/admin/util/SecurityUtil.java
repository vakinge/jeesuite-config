package com.jeesuite.admin.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.jeesuite.admin.constants.GrantOperate;
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
	
	public static void requireAnyPermission(String env,String appIds,GrantOperate operate){
		if(StringUtils.isBlank(env))throw new JeesuiteBaseException(1001, "profile字段缺失");
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
		if(!userInfo.getGrantedProfiles().contains(env + ":" + operate.name())){
			throw new JeesuiteBaseException(403, "你没有profile["+env+"]权限");
		}
		
		if (appIds != null) {
			String[] appIdArrays = StringUtils.split(appIds, ",");
			for (String appId : appIdArrays) {
				if(userInfo.getGrantedAppIds().contains(appId)){
					return;
				}
			}
			throw new JeesuiteBaseException(403, "你没有该项目权限");
		}
	}
	
	public static void requireAllPermission(String env,GrantOperate operate){
		if(StringUtils.isBlank(env))throw new JeesuiteBaseException(1001, "profile字段缺失");
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
		if(!userInfo.getGrantedProfiles().contains(env + ":" + operate.name())){
			throw new JeesuiteBaseException(403, "你没有profile["+env+"]权限");
		}
	}
	
	public static void requireAllPermission(String env,String appIds,GrantOperate operate){
		if(StringUtils.isBlank(env))throw new JeesuiteBaseException(1001, "profile字段缺失");
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
		if(!userInfo.getGrantedProfiles().contains(env + ":" + operate.name())){
			throw new JeesuiteBaseException(403, "你没有profile["+env+"]权限");
		}
		
		if (appIds != null) {
			String[] appIdArrays = StringUtils.split(appIds, ",");
			for (String appId : appIdArrays) {
				if(!userInfo.getGrantedAppIds().contains(appId)){
					throw new JeesuiteBaseException(403, "你没有appId["+appId+"]权限");
				}
			}
		}
	}

}
