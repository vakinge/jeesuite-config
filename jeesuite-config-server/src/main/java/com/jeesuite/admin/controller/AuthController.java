package com.jeesuite.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.entity.UserPermissionEntity;
import com.jeesuite.admin.dao.mapper.UserEntityMapper;
import com.jeesuite.admin.dao.mapper.UserPermissionEntityMapper;
import com.jeesuite.admin.interceptor.SecurityInterceptor;
import com.jeesuite.admin.model.Constants;
import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.json.JsonUtils;
import com.jeesuite.common.util.FormatValidateUtils;
import com.jeesuite.spring.helper.EnvironmentHelper;
import com.jeesuite.springweb.utils.IpUtils;

@Controller
@RequestMapping("/auth")
public class AuthController {

	private static Logger logger = LoggerFactory.getLogger("configcenter");
	
	private @Autowired UserEntityMapper userMapper;
	private @Autowired  UserPermissionEntityMapper userPermissionMapper;

	
	@RequestMapping(value = "login", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> login(HttpServletRequest request,@RequestBody Map<String, String> params){
		String userName = StringUtils.trimToEmpty(params.get("userName"));
		String password = StringUtils.trimToEmpty(params.get("password"));
		
		UserEntity userEntity = FormatValidateUtils.isMobile(userName) ? userMapper.findByMobile(userName) : userMapper.findByName(userName);
		if(userEntity == null || !userEntity.getPassword().equals(UserEntity.encryptPassword(password))){
			return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(4001, "账号或密码错误"),HttpStatus.OK);
		}
		
		LoginUserInfo loginUserInfo = new LoginUserInfo(userEntity.getName());
		loginUserInfo.setSuperAdmin(userEntity.getType().intValue() == 1);
		loginUserInfo.setId(userEntity.getId());
		if(!loginUserInfo.isSuperAdmin()){	
			if(userEntity.getStatus() != 1){
				throw new JeesuiteBaseException(1001, "该账号已停用");
			}
			//加载权限
			List<UserPermissionEntity> userPermissions = userPermissionMapper.findByUserId(userEntity.getId());
			List<String> permCodes;
			for (UserPermissionEntity entity : userPermissions) {
				permCodes = loginUserInfo.getPermissonData().get(entity.getEnv());
				if(permCodes == null){
					loginUserInfo.getPermissonData().put(entity.getEnv(), permCodes = new ArrayList<>());
				}
				permCodes.add(entity.toPermissionCode());
				//
				if(!loginUserInfo.getGrantAppIds().contains(entity.getAppId())){
					loginUserInfo.getGrantAppIds().add(entity.getAppId());
				}
			}
		}
	
		request.getSession().setAttribute(Constants.LOGIN_SESSION_KEY, loginUserInfo);
		logger.info(">>PermissonData:{}", JsonUtils.toJson(loginUserInfo.getPermissonData()));
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(loginUserInfo),HttpStatus.OK);
	}
	
	@RequestMapping(value = "login_user_info", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> loginUserInfo(HttpServletRequest request){
		LoginUserInfo loginUserInfo = (LoginUserInfo) request.getSession().getAttribute(Constants.LOGIN_SESSION_KEY);
		if(loginUserInfo == null)new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(401,"未登录"),HttpStatus.OK);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(loginUserInfo),HttpStatus.OK);
	}
	
	@RequestMapping(value = "logout", method = RequestMethod.GET)
	public String logout(HttpServletRequest request){
		request.getSession().removeAttribute(Constants.LOGIN_SESSION_KEY);
		return "redirect:" + request.getContextPath() + "/login.html"; 
	}
	
	@RequestMapping(value = "update_safe_ipaddr", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<WrapperResponseEntity> updateSafeIpaddr(HttpServletRequest request,@RequestParam("authcode") String authcode){
		
		SecurityUtil.requireSuperAdmin();
		//
		if(StringUtils.isBlank(authcode))throw new JeesuiteBaseException(411, "安全码不能为空");
		if(!authcode.equals(EnvironmentHelper.getProperty("sensitive.operation.authcode")))throw new JeesuiteBaseException(411, "安全码错误");
		
		String ipAddr = IpUtils.getIpAddr(request);
		SecurityInterceptor.setIpWhiteList(ipAddr);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(ipAddr),HttpStatus.OK); 
	}
}
