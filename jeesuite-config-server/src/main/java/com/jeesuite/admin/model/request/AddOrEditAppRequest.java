package com.jeesuite.admin.model.request;

public class AddOrEditAppRequest {

	private Integer id;
	private String name;
	private String alias;
	private Integer appType;
    private Integer masterUid;
    private String remarks;
    
    
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public Integer getAppType() {
		return appType;
	}
	public void setAppType(Integer appType) {
		this.appType = appType;
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
    
    
}
