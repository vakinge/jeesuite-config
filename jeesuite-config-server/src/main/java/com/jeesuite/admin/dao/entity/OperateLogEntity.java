package com.jeesuite.admin.dao.entity;

import com.jeesuite.admin.dao.BaseEntity;
import com.jeesuite.common.json.JsonUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

import org.apache.commons.lang3.StringUtils;

@Table(name = "operate_logs")
public class OperateLogEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer uid;

    private String uname;

    @Column(name = "act_name")
    private String actName;

    @Column(name = "ip_addr")
    private String ipAddr;

    @Column(name = "act_time")
    private Date actTime;

    @Column(name = "biz_data")
    private String bizData;
    
    @Column(name = "before_data")
    private String beforeData;

    @Column(name = "after_data")
    private String afterData;
    
    @Transient
    private Map<String, Object> bizDataMap = new HashMap<>();

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
     * @return uid
     */
    public Integer getUid() {
        return uid;
    }

    /**
     * @param uid
     */
    public void setUid(Integer uid) {
        this.uid = uid;
    }

    /**
     * @return uname
     */
    public String getUname() {
        return uname;
    }

    /**
     * @param uname
     */
    public void setUname(String uname) {
        this.uname = uname;
    }

    /**
     * @return act_name
     */
    public String getActName() {
        return actName;
    }

    /**
     * @param actName
     */
    public void setActName(String actName) {
        this.actName = actName;
    }

    /**
     * @return ip_addr
     */
    public String getIpAddr() {
        return ipAddr;
    }

    /**
     * @param ipAddr
     */
    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    /**
     * @return act_time
     */
    public Date getActTime() {
        return actTime;
    }

    /**
     * @param actTime
     */
    public void setActTime(Date actTime) {
        this.actTime = actTime;
    }

    public String getBizData() {
    	if(StringUtils.isBlank(bizData) && bizDataMap.size() > 0){
    		bizData = JsonUtils.toJson(bizDataMap);
    	}
		return bizData;
	}

	public OperateLogEntity addBizData(String key,Object value) {
		this.bizDataMap.put(key, value);
		return this;
	}
	

	public Map<String, Object> getBizDataMap() {
		return bizDataMap;
	}

	public void setBizDataMap(Map<String, Object> bizDataMap) {
		this.bizDataMap = bizDataMap;
	}

	/**
     * @return before_data
     */
    public String getBeforeData() {
        return beforeData;
    }

    /**
     * @param beforeData
     */
    public void setBeforeData(String beforeData) {
        this.beforeData = beforeData;
    }

    /**
     * @return after_data
     */
    public String getAfterData() {
        return afterData;
    }

    /**
     * @param afterData
     */
    public void setAfterData(String afterData) {
        this.afterData = afterData;
    }
}