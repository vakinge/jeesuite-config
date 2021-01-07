package com.jeesuite.admin.dao.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesuite.mybatis.core.BaseEntity;

@Table(name = "app_configs_history")
public class AppConfigsHistoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "origin_id")
    private Integer originId;
    
    @Column(name = "app_id")
    private Integer appId;
    
    @Column(name = "app_name")
    private String appName;

    private String env;

    private String version;

    private String name;

    /**
     * 类型(1:文件，2:配置项，3：JSON)
     */
    private Short type;

    @Column(name = "created_at")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @Column(name = "created_by")
    private String createdBy;

    private String contents;
    
    @Transient
    private String activeContents;

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
    
    public Integer getOriginId() {
		return originId;
	}

	public void setOriginId(Integer originId) {
		this.originId = originId;
	}
	

	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppNames(String appName) {
		this.appName = appName;
	}

	/**
     * @return env
     */
    public String getEnv() {
        return env;
    }

    /**
     * @param env
     */
    public void setEnv(String env) {
        this.env = env;
    }

    /**
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取类型(1:文件，2:配置项，3：JSON)
     *
     * @return type - 类型(1:文件，2:配置项，3：JSON)
     */
    public Short getType() {
        return type;
    }

    /**
     * 设置类型(1:文件，2:配置项，3：JSON)
     *
     * @param type 类型(1:文件，2:配置项，3：JSON)
     */
    public void setType(Short type) {
        this.type = type;
    }

    /**
     * @return created_at
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return created_by
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return contents
     */
    public String getContents() {
        return contents;
    }

    /**
     * @param contents
     */
    public void setContents(String contents) {
        this.contents = contents;
    }
    
    
    
    public String getActiveContents() {
		return activeContents;
	}

	public void setActiveContents(String activeContents) {
		this.activeContents = activeContents;
	}

	//类型(1:文件，2:配置项,3:JSON)
    public String getTypeAlias(){
    	if(type.intValue() == 1)return "配置文件";
    	if(type.intValue() == 2)return "配置项";
    	if(type.intValue() == 3)return "JSON";
    	return null;
    }
}