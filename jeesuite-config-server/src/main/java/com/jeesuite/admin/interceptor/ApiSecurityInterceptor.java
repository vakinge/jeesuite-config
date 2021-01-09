package com.jeesuite.admin.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.jeesuite.admin.constants.ProfileExtrAttrName;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;
import com.jeesuite.spring.InstanceFactory;
import com.jeesuite.spring.helper.EnvironmentHelper;
import com.jeesuite.springweb.utils.IpUtils;
import com.jeesuite.springweb.utils.WebUtils;

public class ApiSecurityInterceptor implements HandlerInterceptor {

	
	private boolean extranetEnabled = Boolean.parseBoolean(EnvironmentHelper.getProperty("api.extranet.enabled"));

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
	
}
