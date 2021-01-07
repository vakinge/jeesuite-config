package com.jeesuite.admin.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.security.SecurityDelegating;
import com.jeesuite.security.model.UserSession;
import com.jeesuite.springweb.exception.UnauthorizedException;
import com.jeesuite.springweb.model.WrapperResponse;


@Controller
@RequestMapping("/user")
public class UserLoginController {

	@RequestMapping(value = "login", method = RequestMethod.POST)
	@ResponseBody
	public WrapperResponse<String> login(HttpServletRequest request,@RequestBody Map<String, String> params){
		String userName = StringUtils.trimToEmpty(params.get("userName"));
		String password = StringUtils.trimToEmpty(params.get("password"));
		
		UserSession session = SecurityDelegating.doAuthentication(userName, password);
		return new WrapperResponse<>(session.getSessionId());
	}
	
	@RequestMapping(value = "login_user_info", method = RequestMethod.GET)
	@ResponseBody
	public WrapperResponse<LoginUserInfo> loginUserInfo(HttpServletRequest request){
		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();
		if(loginUserInfo == null) {
			throw new UnauthorizedException();
		}
		return new WrapperResponse<>(loginUserInfo);
	}
	
	@RequestMapping(value = "logout", method = RequestMethod.POST)
	@ResponseBody
	public  WrapperResponse<String> logout(){
		SecurityDelegating.doLogout();
		return new WrapperResponse<>(); 
	}
	

}
