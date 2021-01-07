package com.jeesuite.admin.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jeesuite.admin.constants.UserType;
import com.jeesuite.common.model.AuthUser;

public class LoginUserInfo extends AuthUser{

	private String mobile;
	private String email;
	private Integer groupId;
	private boolean groupMaster;
	@JsonIgnore
	private List<String> grantPermissons  = new ArrayList<>();
	@JsonIgnore
	private List<Integer> grantAppIds = new ArrayList<>();
	
	/**
	 * @return the mobile
	 */
	public String getMobile() {
		return mobile;
	}
	/**
	 * @param mobile the mobile to set
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	public boolean isSuperAdmin() {
		return UserType.superAdmin.name().equals(getUserType());
	}
	
	public boolean isGroupAdmin() {
		return UserType.groupAdmin.name().equals(getUserType());
	}

	public List<String> getGrantPermissons() {
		return grantPermissons;
	}

	public void setGrantPermissons(List<String> grantPermissons) {
		this.grantPermissons = grantPermissons;
	}

	public List<Integer> getGrantAppIds() {
		return grantAppIds;
	}
	public void setGrantAppIds(List<Integer> grantAppIds) {
		this.grantAppIds = grantAppIds;
	}

	/**
	 * @return the groupId
	 */
	public Integer getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public boolean isGroupMaster() {
		return groupMaster;
	}

	public void setGroupMaster(boolean groupMaster) {
		this.groupMaster = groupMaster;
	}
	
	
}
