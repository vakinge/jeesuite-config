package com.jeesuite.admin.interceptor;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.jeesuite.admin.annotation.ValidateSign;
import com.jeesuite.admin.constants.AppExtrAttrName;
import com.jeesuite.admin.constants.ProfileExtrAttrName;
import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;
import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.crypt.DES;
import com.jeesuite.common.util.DigestUtils;
import com.jeesuite.common.util.ResourceUtils;
import com.jeesuite.spring.InstanceFactory;
import com.jeesuite.springweb.WebConstants;
import com.jeesuite.springweb.utils.IpUtils;
import com.jeesuite.springweb.utils.WebUtils;

public class ApiSecurityInterceptor implements HandlerInterceptor {

	
	private boolean extranetEnabled = ResourceUtils.getBoolean("api.extranet.enabled", false);
	
	private static String commonErrorRspTemplate= "{\"code\": %s,\"msg\":\"%s\"}";
	
	private AppEntityMapper appMapper;
	

	public AppEntityMapper getAppMapper() {
		if(appMapper != null) {
			return appMapper;
		}
		appMapper = InstanceFactory.getInstance(AppEntityMapper.class);
		return appMapper;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if(StringUtils.isNotBlank(contextPath)){
			uri = uri.replace(contextPath, StringUtils.EMPTY);
		}

		String ipAddr = IpUtils.getIpAddr(request);
		
		boolean isInnerIpaddr = IpUtils.isInnerIp(ipAddr);
		
		//验证签名
		if(!validateSign(handler,request,response))return false;

		//只允许内网
		if(extranetEnabled == false && isInnerIpaddr == false ){
			WebUtils.responseOutJson(response,"{\"code\": 403,\"msg\":\"禁止外网["+ipAddr+"]访问\"}");
			return false;
		}
		
		if(extranetEnabled && !isInnerIpaddr) {
			String env = request.getParameter("env");
			if(!validateWhiteIpAddress(env, ipAddr)) {
				WebUtils.responseOutJson(response,"{\"code\": 403,\"msg\":\"当前IP不在白名单范围\"}");
				return false;
			}
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {}
	

	private boolean validateWhiteIpAddress(String env,String ipAddr) {
		ProfileEntityMapper mapper = InstanceFactory.getInstance(ProfileEntityMapper.class);
		String ipWhiteList = mapper.findExtrAttr(env, ProfileExtrAttrName.ipWhiteList.name());
		if(StringUtils.isBlank(ipWhiteList))return true;
		String[] ips = ipWhiteList.split(";|；");
		for (String ip : ips) {
			if(ipAddr.contains(ip.replace("*", ""))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean validateSign(Object handler,HttpServletRequest request,HttpServletResponse response) {
		if(handler instanceof HandlerMethod == false)return true;
		HandlerMethod method = (HandlerMethod)handler;
		if(!method.getMethod().isAnnotationPresent(ValidateSign.class))return true;
		
		String authtoken = request.getParameter("authtoken");
		String env = request.getParameter("env");
		String appName = request.getHeader(WebConstants.HEADER_INVOKER_APP_ID);
		if(StringUtils.isBlank(appName)) {
			appName = request.getParameter("appName");
		}
		
		if(StringUtils.isAnyBlank(env,appName,authtoken)) {
			WebUtils.responseOutJson(response,String.format(commonErrorRspTemplate,400, "[env,appName,authtoken]不能为空"));
			return false;
		}
		
		AppEntity entity = getAppMapper().findByAppKey(appName);
		
		if(entity == null) {
			WebUtils.responseOutJson(response,String.format(commonErrorRspTemplate,400, "[appName]不存在"));
			return false;
		}
		
		String token = getAppMapper().findExtrAttr(entity.getId(), env, AppExtrAttrName.API_TOKEN.name());
		String decryptStr = null;
		try {
			decryptStr = DES.decrypt(token.substring(0,8), authtoken);
		} catch (Exception e) {
			WebUtils.responseOutJson(response,String.format(commonErrorRspTemplate,400, "[authtoken]不匹配"));
			return false;
		}
		long timestamp = Long.parseLong(decryptStr.substring(6));
		if(!DigestUtils.md5Short(token).equals(decryptStr.substring(0,6))) {
			WebUtils.responseOutJson(response,String.format(commonErrorRspTemplate,400, "[authtoken]不匹配"));
			return false;
		}
		if(new Date().getTime() - timestamp > 180 * 1000){
			WebUtils.responseOutJson(response,String.format(commonErrorRspTemplate,400, "[authtoken]已过期"));
			return false;
		}
		
		return true;
	}
	
}
