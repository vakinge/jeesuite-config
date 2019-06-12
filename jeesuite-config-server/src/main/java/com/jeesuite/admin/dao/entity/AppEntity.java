package com.jeesuite.admin.dao.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import com.jeesuite.mybatis.core.BaseEntity;

@Table(name = "apps")
public class AppEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    
    private String alias;

    private String master;

    @Column(name = "master_uid")
    private Integer masterUid;
    
    @Column(name = "app_type")
    private Integer appType;

    private String remarks;

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
     * @return name
     */
    public String getName() {
        return name;
    }
    
    

    public String getAlias() {
		return StringUtils.isNotBlank(alias) ? alias : name;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
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
    

    public Integer getAppType() {
		return appType;
	}

	public void setAppType(Integer appType) {
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
    	if(StringUtils.equals(alias, name))return name;
    	return String.format("%s(%s)", alias,name);
    }

}