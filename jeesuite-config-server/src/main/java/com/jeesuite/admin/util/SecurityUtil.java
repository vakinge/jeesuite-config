package com.jeesuite.admin.util;

import java.util.List;

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
	
	public static void requireAnyPermission(String env,List<String> appIds,GrantOperate operate){
		if(StringUtils.isBlank(env))throw new JeesuiteBaseException(1001, "profile字段缺失");
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
        List<String> permCodes = userInfo.getPermissonData().get(env);
        if(permCodes == null)throw new JeesuiteBaseException(403, "你没有该项目权限");
		if (appIds != null) {
			for (String appId : appIds) {
				if(permCodes.contains(buildPermissionCode(env, appId, operate))){
					return;
				}
			}
			throw new JeesuiteBaseException(403, "你没有该项目权限");
		}
	}
	
	public static void requireAllPermission(String env,List<String> appIds,GrantOperate operate){
		if(StringUtils.isBlank(env))throw new JeesuiteBaseException(1001, "字段[env]必填");
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
		 List<String> permCodes = userInfo.getPermissonData().get(env);
	        if(permCodes == null)throw new JeesuiteBaseException(403, "你没有该项目权限");
		for (String appId : appIds) {
			if(!permCodes.contains(buildPermissionCode(env, appId, operate))){
				throw new JeesuiteBaseException(403, "你没有appId["+appId+"]在环境["+env+"]权限");
			}
		}
	}

	 private static String buildPermissionCode(String env,String appId,GrantOperate operate){
	     return String.format("%s-%s:%s", env,appId,operate.name());
	 }
}
