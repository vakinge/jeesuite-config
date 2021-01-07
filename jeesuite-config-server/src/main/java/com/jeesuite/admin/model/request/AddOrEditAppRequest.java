package com.jeesuite.admin.model.request;

public class AddOrEditAppRequest {

	private Integer id;
	private String appKey;
    private String appName;
    private String serviceId;
	private String appType;
    private Integer masterUid;
    private String remarks;
    private Integer groupId;
    private int requestTimeout;
    private String anonymousUris;
    private String healthUri;
    
    
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * @return the appKey
	 */
	public String getAppKey() {
		return appKey;
	}
	/**
	 * @param appKey the appKey to set
	 */
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	/**
	 * @return the appName
	 */
	public String getAppName() {
		return appName;
	}
	/**
	 * @param appName the appName to set
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}
	/**
	 * @param serviceId the serviceId to set
	 */
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
	public Integer getMasterUid() {
		return masterUid;
	}
	public void setMasterUid(Integer masterUid) {
		this.masterUid = masterUid;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	/**
	 * @return the requestTimeout
	 */
	public int getRequestTimeout() {
		return requestTimeout;
	}
	/**
	 * @param requestTimeout the requestTimeout to set
	 */
	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
	/**
	 * @return the anonymousUris
	 */
	public String getAnonymousUris() {
		return anonymousUris;
	}
	/**
	 * @param anonymousUris the anonymousUris to set
	 */
	public void setAnonymousUris(String anonymousUris) {
		this.anonymousUris = anonymousUris;
	}
	/**
	 * @return the healthUri
	 */
	public String getHealthUri() {
		return healthUri;
	}
	/**
	 * @param healthUri the healthUri to set
	 */
	public void setHealthUri(String healthUri) {
		this.healthUri = healthUri;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public String getAppType() {
		return appType;
	}
	public void setAppType(String appType) {
		this.appType = appType;
	}
	
	
}
