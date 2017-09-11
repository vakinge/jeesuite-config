package com.jeesuite.admin.util;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.jeesuite.admin.dao.entity.OperateLogEntity;
import com.jeesuite.admin.exception.JeesuiteBaseException;
import com.jeesuite.admin.model.Constants;
import com.jeesuite.admin.model.LoginUserInfo;

public class SecurityUtil {
	
	private static ThreadLocal<OperateLogEntity> operateLogHolder = new ThreadLocal<>();
	
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
	
	public static void requireProfileGanted(String profile){
		if(StringUtils.isBlank(profile))throw new JeesuiteBaseException(1001, "profile字段缺失");
		LoginUserInfo userInfo = getLoginUserInfo();
		if(userInfo.isSuperAdmin())return;
		if(!userInfo.getGantProfiles().contains(profile)){
			throw new JeesuiteBaseException(403, "你没有profile["+profile+"]权限");
		}
	}
	
	public static OperateLogEntity getOperateLog(){
		OperateLogEntity log = operateLogHolder.get();
		if(log == null){
			log = new OperateLogEntity();
			log.setActTime(new Date());
			operateLogHolder.set(log);
		}
		return log;
	}
	
	public static void clearOperateLogHolder(){
		operateLogHolder.remove();
	}
}
