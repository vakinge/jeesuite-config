package com.jeesuite.admin.model;

import java.util.ArrayList;
import java.util.List;

public class LoginUserInfo {

	private int id;
	private String name;
	private boolean superAdmin;
	private List<String> grantedProfiles;
	private List<String> grantedAppIds = new ArrayList<>();
	
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
		return grantedProfiles == null ? (grantedProfiles = new ArrayList<>()) : grantedProfiles;
	}
	public void setGrantedProfiles(List<String> grantedProfiles) {
		this.grantedProfiles = grantedProfiles;
	}
	public List<String> getGrantedAppIds() {
		return grantedAppIds;
	}
	public void setGrantedAppIds(List<String> grantedAppIds) {
		this.grantedAppIds = grantedAppIds;
	}
	
	
}
