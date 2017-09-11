package com.jeesuite.admin.dao.entity;

import com.jeesuite.admin.dao.BaseEntity;
import java.util.Date;
import javax.persistence.*;

@Table(name = "app_configs")
public class AppconfigEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "app_name")
    private String appName;
    @Column(name = "env",updatable = false)
    private String env;

    private String version;

    @Column(name = "name",updatable = false)
    private String name;

    /**
     * 类型(1:文件，2:配置项,3:JSON)
     */
    private Short type;

    @Column(name = "created_at",updatable = false)
    private Date createdAt;

    @Column(name = "created_by",updatable = false)
    private Integer createdBy;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

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

    /**
     * @return app_name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * @param appName
     */
    public void setAppName(String appName) {
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
    public Integer getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy
     */
    public void setCreatedBy(Integer createdBy) {
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
    public Integer getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy
     */
    public void setUpdatedBy(Integer updatedBy) {
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
    
    public int getGlobal() {
		return "global".equals(appName) ? 1 : 0;
	}
    
    //类型(1:文件，2:配置项,3:JSON)
    public String getTypeAlias(){
    	if(type.intValue() == 1)return "配置文件";
    	if(type.intValue() == 2)return "配置项";
    	if(type.intValue() == 3)return "JSON";
    	return null;
    }
}