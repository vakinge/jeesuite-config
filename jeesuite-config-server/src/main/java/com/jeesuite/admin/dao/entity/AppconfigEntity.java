package com.jeesuite.admin.dao.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesuite.mybatis.core.BaseEntity;

@Table(name = "app_configs")
public class AppconfigEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "app_ids")
    private String appIds;
    @Column(name = "env",updatable = false)
    private String env;

    private String version;

    @Column(name = "name",updatable = false)
    private String name;

    /**
     * 类型(1:文件，2:配置项,3:JSON)
     */
    @Column(name = "type",updatable = false)
    private Short type;
    
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

    private String contents;
    
    @Transient
    private String appNames;

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

    
    public String getAppIds() {
		return StringUtils.trimToNull(appIds);
	}

	public void setAppIds(String appIds) {
		this.appIds = appIds;
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
     * 获取类型(1:文件，2:配置项)
     *
     * @return type - 类型(1:文件，2:配置项)
     */
    public Short getType() {
        return type;
    }

    /**
     * 设置类型(1:文件，2:配置项)
     *
     * @param type 类型(1:文件，2:配置项)
     */
    public void setType(Short type) {
        this.type = type;
    }
    
    public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
     * @return created_at
     */
    public Date getCreatedAt() {
        return createdAt == null ? updatedAt : createdAt;
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
        return createdBy == null ? updatedBy : createdBy;
    }

    /**
     * @param createdBy
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return updated_at
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return updated_by
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
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
    
    //全局配置
    public boolean getGlobal() {
		return StringUtils.isBlank(appIds);
	}
    
    //类型(1:文件，2:配置项,3:JSON)
    public String getTypeAlias(){
    	if(type.intValue() == 1)return "配置文件";
    	if(type.intValue() == 2)return "配置项";
    	if(type.intValue() == 3)return "JSON";
    	return null;
    }

	public String getAppNames() {
		return appNames;
	}

	public void setAppNames(String appNames) {
		this.appNames = appNames;
	}

}