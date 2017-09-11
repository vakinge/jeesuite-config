package com.jeesuite.admin.model.request;

import org.apache.commons.lang3.StringUtils;

public class AddOrEditConfigRequest {

	private Integer id;
	private String appName;
	private String env;
	private String version = "0.0.0";
	private String name;
	private Short type;
	private String contents;
	private int global = 0;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getEnv() {
		return env;
	}
	public void setEnv(String env) {
		this.env = env;
	}
	public String getVersion() {
		return StringUtils.isBlank(version) ? "0.0.0" : version;
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
	public Short getType() {
		return type;
	}
	public void setType(Short type) {
		this.type = type;
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	public int getGlobal() {
		return global;
	}
	public void setGlobal(int global) {
		this.global = global;
	}
	
	
	
}
