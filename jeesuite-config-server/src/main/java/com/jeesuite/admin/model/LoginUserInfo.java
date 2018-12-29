package com.jeesuite.admin.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginUserInfo {

	private int id;
	private String name;
	private boolean superAdmin;
	private Map<String, List<String>> permissons;
	
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
	
	
	public Map<String, List<String>> getPermissons() {
		return permissons == null ? (permissons = new HashMap<>()) : permissons;
	}
	public void setPermissons(Map<String, List<String>> permissons) {
		this.permissons = permissons;
	}
	public List<String> getGrantedProfiles() {
		return new ArrayList<>(getPermissons().keySet());
	}
	
}
