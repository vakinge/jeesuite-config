package com.jeesuite.admin.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.admin.util.IpUtils;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.admin.util.WebUtils;
import com.jeesuite.common.util.WhoUseMeReporter;
import com.jeesuite.spring.helper.EnvironmentHelper;

public class SecurityInterceptor implements HandlerInterceptor {

	private static final String ADMIN_URI_PREFIX = "/admin";
	private static final String API_URI_PREFIX = "/api";
	private static String notloginRspJson = "{\"code\": 401,\"msg\":\"401 Unauthorized\"}";
	private static String ipForbiddenRspJson = "{\"code\": 403,\"msg\":\"ipForbidden\"}";
	private static List<String> ipWhiteList = new ArrayList<>();
	
	private boolean extranetEnabled = Boolean.parseBoolean(EnvironmentHelper.getProperty("api.extranet.enabled"));
	private boolean  ipfilterEnabled = Boolean.parseBoolean(EnvironmentHelper.getProperty("safe.ipfilter.enabled"));
	
	
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

		//客户端APi鉴权
		if(uri.startsWith(API_URI_PREFIX)){
			//只允许内网
			if(extranetEnabled == false && isInnerIpaddr == false ){
				WebUtils.responseOutWithJson(response,"{\"code\": 403,\"msg\":\"禁止外网访问\"}");
				return false;
			}
			//TODO 验证签名
//			String clientId = request.getParameter("client_id");
//			String signature = request.getParameter("signature");
			
			return true;
		}
		
		if(uri.startsWith(ADMIN_URI_PREFIX) && ipfilterEnabled && !isInnerIpaddr && !ipWhiteList.contains(ipAddr)){
			WebUtils.responseOutWithJson(response,ipForbiddenRspJson);
			return false;
		}
		
		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();
		WhoUseMeReporter.post(request.getServerName(), "configcenter");
		if(loginUserInfo == null){
			if(WebUtils.isAjax(request)){
				WebUtils.responseOutWithJson(response,notloginRspJson);
			}else{				
				response.sendRedirect(request.getContextPath()+"/login.html");  
			}
            return false; 
		}

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {}
	
	
	
	public static synchronized void setIpWhiteList(String ips){
		ipWhiteList.clear();
		if(StringUtils.isBlank(ips))return;
		ipWhiteList.addAll(Arrays.asList(ips.split(",|;|，")));
	}

}
