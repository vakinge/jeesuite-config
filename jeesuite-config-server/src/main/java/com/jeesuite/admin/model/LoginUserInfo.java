package com.jeesuite.admin.model;

import java.util.ArrayList;
import java.util.List;

public class LoginUserInfo {

	private int id;
	private String name;
	private boolean superAdmin;
	private List<String> grantedProfiles  = new ArrayList<>(4);
	private List<String> grantedPermissions  = new ArrayList<>();
	
	public LoginUserInfo(String name) {
		super();
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isSuperAdmin() {
		return superAdmin;
	}
	public void setSuperAdmin(boolean superAdmin) {
		this.superAdmin = superAdmin;
	}
	
	public List<String> getGrantedProfiles() {
		return grantedProfiles;
	}
	public void setGrantedProfiles(List<String> grantedProfiles) {
		this.grantedProfiles = grantedProfiles;
	}
	public List<String> getGrantedPermissions() {
		return grantedPermissions;
	}
	public void setGrantedPermissions(List<String> grantedPermissions) {
		this.grantedPermissions = grantedPermissions;
	}
	
	
	
}
