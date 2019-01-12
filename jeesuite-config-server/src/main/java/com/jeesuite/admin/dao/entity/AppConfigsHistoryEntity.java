package com.jeesuite.admin.dao.entity;

import com.jeesuite.admin.dao.BaseEntity;
import java.util.Date;
import javax.persistence.*;

@Table(name = "app_configs_history")
public class AppConfigsHistoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "origin_id")
    private Integer originId;
    
    @Column(name = "app_ids")
    private String appIds;

    private String env;

    private String version;

    private String name;

    /**
     * 类型(1:文件，2:配置项，3：JSON)
     */
    private Short type;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "created_by")
    private String createdBy;

    private String contents;

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

	/**
     * @return app_ids
     */
    public String getAppIds() {
        return appIds;
    }

    /**
     * @param appIds
     */
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
}