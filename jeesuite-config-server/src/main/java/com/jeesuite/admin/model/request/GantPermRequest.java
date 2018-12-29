package com.jeesuite.admin.model.request;

import java.util.List;

import com.jeesuite.admin.model.UserPermission;

public class GantPermRequest {

	private int userId;
	private String env;
	private List<UserPermission> permissions;
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getEnv() {
		return env;
	}
	public void setEnv(String env) {
		this.env = env;
	}
	public List<UserPermission> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<UserPermission> permissions) {
		this.permissions = permissions;
	}
	
	

}
