package com.jeesuite.admin.model;

import java.util.ArrayList;
import java.util.List;

public class UserGrantPermGroup {

	private String env;
	private String envName;
	private List<UserGrantPermItem> perms = new ArrayList<>();
	
	public UserGrantPermGroup() {}

	public UserGrantPermGroup(String env, String envName) {
		super();
		this.env = env;
		this.envName = envName;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}
	
	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public List<UserGrantPermItem> getPerms() {
		return perms;
	}

	public void setPerms(List<UserGrantPermItem> perms) {
		this.perms = perms;
	}

}
