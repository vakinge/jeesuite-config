package com.jeesuite.admin.dao.entity;

import com.jeesuite.admin.dao.BaseEntity;
import javax.persistence.*;

import org.apache.commons.lang3.StringUtils;

@Table(name = "apps")
public class AppEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    
    private String alias;
 

    @Column(name = "notify_emails")
    private String notifyEmails;

    private String master;

    @Column(name = "master_uid")
    private Integer masterUid;

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
     * @return notify_emails
     */
    public String getNotifyEmails() {
        return notifyEmails;
    }

    /**
     * @param notifyEmails
     */
    public void setNotifyEmails(String notifyEmails) {
        this.notifyEmails = notifyEmails;
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

}