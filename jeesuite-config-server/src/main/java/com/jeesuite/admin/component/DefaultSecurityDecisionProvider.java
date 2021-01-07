package com.jeesuite.admin.component;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jeesuite.admin.constants.UserType;
import com.jeesuite.admin.dao.entity.BusinessGroupEntity;
import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.mapper.BusinessGroupEntityMapper;
import com.jeesuite.admin.dao.mapper.UserEntityMapper;
import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.util.FormatValidateUtils;
import com.jeesuite.common.util.ResourceUtils;
import com.jeesuite.security.SecurityDecisionProvider;
import com.jeesuite.security.exception.UserNotFoundException;
import com.jeesuite.security.exception.UserPasswordWrongException;
import com.jeesuite.security.model.UserSession;
import com.jeesuite.spring.InstanceFactory;

@Component
public class DefaultSecurityDecisionProvider extends SecurityDecisionProvider<LoginUserInfo> {
	
	private static List<String> anonymousUrlPatterns = Arrays.asList("/user/login");
	
	private @Autowired UserEntityMapper userMapper;
	
	@Override
	public String contextPath() {
		return ResourceUtils.getProperty("server.servlet.context-path", "");
	}

	/**
	 * 可匿名访问接口，可使用“*”通配符
	 */
	@Override
	public List<String> anonymousUrlPatterns() {
		return anonymousUrlPatterns;
	}

	@Override
	public LoginUserInfo validateUser(String name, String password)
			throws UserNotFoundException, UserPasswordWrongException {
		
		UserEntity userEntity; 
		if(FormatValidateUtils.isMobile(name)){
			userEntity = userMapper.findByMobile(name);
		}else if(FormatValidateUtils.isEmail(name)){
			userEntity = userMapper.findByEmail(name);
		}else{
			userEntity = userMapper.findByName(name);
		}
		if(userEntity == null || !userEntity.getPassword().equals(UserEntity.encryptPassword(password))){
			throw new UserPasswordWrongException();
		}

		LoginUserInfo userInfo = new LoginUserInfo();
		userInfo.setId(userEntity.getId().toString());
		userInfo.setName(userEntity.getName());
		userInfo.setUserType(userEntity.getType());
		userInfo.setGroupId(userEntity.getGroupId());
		if(!userInfo.isSuperAdmin()){
			if(!userEntity.getEnabled()){
				throw new JeesuiteBaseException(1001, "该账号已停用");
			}
			//加载权限
			SecurityUtil.initPermssionDatas(userInfo);
		}
		
		if(userEntity.getGroupId() != null){			
			BusinessGroupEntity groupEntity = InstanceFactory.getInstance(BusinessGroupEntityMapper.class).selectByPrimaryKey(userEntity.getGroupId());
			if(groupEntity != null){
				userInfo.setGroupMaster(userEntity.getId().equals(groupEntity.getMasterUid()));
				if(userInfo.isGroupMaster()){
					userInfo.setGroupId(groupEntity.getId());
					userInfo.setUserType(UserType.groupAdmin.name());
				}
			}
		}

		return userInfo;
	}


	/**
	 * 所有需要授权的接口列表
	 */
	@Override
	public List<String> findAllUriPermissionCodes() {
		return null;
	}


	/**
	 * 某个用户拥有的接口权限列表
	 */
	@Override
	public List<String> getUserPermissionCodes(String userId) {
		return null;
	}

	@Override
	public void authorizedPostHandle(UserSession session) {}

	/**
	 * 超管账号
	 */
	@Override
	public String superAdminName() {
		return "sa";
	}

	@Override
	public String _401_Error_Page() {
		return null;
	}

	@Override
	public String _403_Error_Page() {
		return null;
	}
}