package com.jeesuite.admin.model.request;

import com.jeesuite.admin.model.Constants;

public class QueryConfigRequest {

	private String appId;
	private String env;
	private String version = Constants.DEFAULT_CONFIG_VERSION;
	private String name;

	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getEnv() {
		return env;
	}
	public void setEnv(String env) {
		this.env = env;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
