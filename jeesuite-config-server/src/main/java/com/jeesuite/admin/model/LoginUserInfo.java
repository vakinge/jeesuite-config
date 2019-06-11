package com.jeesuite.admin.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LoginUserInfo {

	private int id;
	private String name;
	private boolean superAdmin;
	@JsonIgnore
	private Map<String,List<String>> permissonData  = new HashMap<>();

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
	
	public Map<String, List<String>> getPermissonData() {
		return permissonData;
	}
	public void setPermissonData(Map<String, List<String>> permissonData) {
		this.permissonData = permissonData;
	}
	public List<String> getGrantedProfiles() {
		return new ArrayList<>(permissonData.keySet());
	}
	
	
}
