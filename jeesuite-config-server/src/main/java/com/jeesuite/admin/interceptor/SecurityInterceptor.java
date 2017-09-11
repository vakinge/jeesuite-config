package com.jeesuite.admin.interceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.spring.helper.EnvironmentHelper;

public class SecurityInterceptor implements HandlerInterceptor {

	private static String notloginRspJson = "{\"code\": 401,\"msg\":\"401 Unauthorized\"}";
	private static String ipForbiddenRspJson = "{\"code\": 403,\"msg\":\"ipForbidden\"}";
	private static List<String> ipWhiteList = new ArrayList<>();
	private boolean extranetEnabled = Boolean.parseBoolean(EnvironmentHelper.getProperty("api.extranet.enabled"));
	private boolean  ipfilterEnabled = Boolean.parseBoolean(EnvironmentHelper.getProperty("safe.ipfilter.enabled"));
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		String ipAddr = getIpAddr(request);
		
		boolean isInnerIpaddr = ipAddr.startsWith("127")  || ipAddr.startsWith("192.168") || ipAddr.startsWith("10.");
		
		SecurityUtil.getOperateLog().setIpAddr(ipAddr);
		SecurityUtil.getOperateLog().setActName(request.getRequestURI());
		
		//客户端APi鉴权
		if(request.getRequestURI().startsWith("/api")){
			//只允许内网
			if(extranetEnabled == false && isInnerIpaddr == false ){
				responseOutWithJson(response,"{\"code\": 403,\"msg\":\"禁止外网访问\"}");
				return false;
			}
			//TODO 验证签名
//			String clientId = request.getParameter("client_id");
//			String signature = request.getParameter("signature");
			
			return true;
		}
		
		if(ipfilterEnabled && !isInnerIpaddr && !ipWhiteList.contains(ipAddr)){
			responseOutWithJson(response,ipForbiddenRspJson);
			return false;
		}
		
		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();
		if(loginUserInfo == null){
			if(isAjax(request)){
				responseOutWithJson(response,notloginRspJson);
			}else{				
				response.sendRedirect(request.getContextPath()+"/login.html");  
			}
            return false; 
		}
		
		SecurityUtil.getOperateLog().setUid(loginUserInfo.getId());
		SecurityUtil.getOperateLog().setUname(loginUserInfo.getName());
		
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		SecurityUtil.clearOperateLogHolder();
	}
	
	private boolean isAjax(HttpServletRequest request){
	    return  (request.getHeader("X-Requested-With") != null  
	    && "XMLHttpRequest".equals(request.getHeader("X-Requested-With").toString())) ;
	}
	
	private void responseOutWithJson(HttpServletResponse response,String json) {  
	    //将实体对象转换为JSON Object转换  
	    response.setCharacterEncoding("UTF-8");  
	    response.setContentType("application/json; charset=utf-8");  
	    PrintWriter out = null;  
	    try {  
	        out = response.getWriter();  
	        out.append(json);  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } finally {  
	        if (out != null) {  
	            out.close();  
	        }  
	    }  
	}  
	
	
	private static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		return ip;
	} 
	
	public static synchronized void setIpWhiteList(String ips){
		ipWhiteList.clear();
		if(StringUtils.isBlank(ips))return;
		ipWhiteList.addAll(Arrays.asList(ips.split(",|;|，")));
	}

}
