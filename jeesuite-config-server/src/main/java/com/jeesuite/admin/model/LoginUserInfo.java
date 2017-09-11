package com.jeesuite.admin.model;

import java.util.ArrayList;
import java.util.List;

public class LoginUserInfo {

	private int id;
	private String name;
	private boolean superAdmin;
	private List<String> gantProfiles;
	
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
	public List<String> getGantProfiles() {
		if(gantProfiles == null)gantProfiles = new ArrayList<String>();
		return gantProfiles;
	}
	public void setGantProfiles(List<String> gantProfiles) {
		this.gantProfiles = gantProfiles;
	}
	
	
}
