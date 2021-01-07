package com.jeesuite.admin.model.request;

import org.apache.commons.lang3.StringUtils;

import com.jeesuite.admin.model.Constants;

public class AddOrEditConfigRequest {

	private Integer id;
	private Integer groupId;
	private Integer appId;
	private String env;
	private String version = Constants.DEFAULT_CONFIG_VERSION;
	private String name;
	private Short type;
	private String contents;
	private Boolean global = false;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the appId
	 */
	public Integer getAppId() {
		return appId;
	}
	/**
	 * @param appId the appId to set
	 */
	public void setAppId(Integer appId) {
		this.appId = appId;
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
	public Boolean getGlobal() {
		return global == null ? false : global;
	}
	public void setGlobal(Boolean global) {
		this.global = global;
	}


	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
}
