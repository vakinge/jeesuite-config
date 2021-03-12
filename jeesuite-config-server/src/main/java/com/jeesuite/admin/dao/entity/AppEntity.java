package com.jeesuite.admin.dao.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesuite.admin.constants.AppType;
import com.jeesuite.common.model.KeyValuePair;
import com.jeesuite.mybatis.core.BaseEntity;

@Table(name = "apps")
public class AppEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "app_key",updatable = false)
    private String appKey;

    @Column(name = "group_id",updatable=false)
	private Integer groupId;

    @Column(name = "app_name")
    private String appName;
    @Column(name = "service_id")
    private String serviceId;

    private String master;

    @Column(name = "master_uid")
    private Integer masterUid;
    
    @Column(name = "app_type")
    private String appType;

    private String remarks;
    
    @Column(name = "health_uri")
    private String healthUri;
    
    private Boolean enabled = Boolean.TRUE;

    @Column(name = "created_at",updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @Column(name = "created_by",updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;
    
    @Transient
    private List<KeyValuePair>  extrAttrs;
    
    @Transient
    private String apiDocUrl;
    
    @Transient
    private List<String> instanceIds;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
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

	/**
     * @return master
     */
    public String getMaster() {
        return master;
    }

    /**
     * @param master
     */
    public void setMaster(String master) {
        this.master = master;
    }

    /**
     * @return master_uid
     */
    public Integer getMasterUid() {
        return masterUid;
    }

    /**
     * @param masterUid
     */
    public void setMasterUid(Integer masterUid) {
        this.masterUid = masterUid;
    }
    

    public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}


	/**
     * @return remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


	public String getFullName(){
    	if(StringUtils.equals(appName, appKey))return appName;
    	return String.format("%s(%s)", appName,appKey);
    }


	/**
	 * @return the apiDocUrl
	 */
	public String getApiDocUrl() {
		return apiDocUrl;
	}

	/**
	 * @param apiDocUrl the apiDocUrl to set
	 */
	public void setApiDocUrl(String apiDocUrl) {
		this.apiDocUrl = apiDocUrl;
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
	
	/**
	 * @return the enabled
	 */
	public Boolean getEnabled() {
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the updatedAt
	 */
	public Date getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * @param updatedAt the updatedAt to set
	 */
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	

	/**
	 * @return the instanceIds
	 */
	public List<String> getInstanceIds() {
		return instanceIds;
	}

	/**
	 * @param instanceIds the instanceIds to set
	 */
	public void setInstanceIds(List<String> instanceIds) {
		this.instanceIds = instanceIds;
	}
	
	public boolean isActive(){
		return this.instanceIds != null && this.instanceIds.size() > 0;
	}

	public String getAppTypeAlias(){
		if(StringUtils.isBlank(appType))return null;
		return AppType.valueOf(appType).getCnName();
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public List<KeyValuePair> getExtrAttrs() {
		return extrAttrs;
	}

	public void setExtrAttrs(List<KeyValuePair> extrAttrs) {
		this.extrAttrs = extrAttrs;
	}

	
}