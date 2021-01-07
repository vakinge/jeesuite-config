package com.jeesuite.admin.dao.entity;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.jeesuite.mybatis.core.BaseEntity;

@Table(name = "user_permissions")
public class UserPermissionEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    private String env = "all";

    @Column(name = "app_id")
    private Integer appId;

    private String operate;

    public UserPermissionEntity() {}

	public UserPermissionEntity(Integer userId, Integer appId, String operate) {
		super();
		this.userId = userId;
		this.appId = appId;
		this.operate = operate;
	}

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
     * @return user_id
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param userId
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
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
     * @return app_id
     */
    public Integer getAppId() {
        return appId;
    }

    /**
     * @param appId
     */
    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    /**
     * @return operate
     */
    public String getOperate() {
        return operate;
    }

    /**
     * @param operate
     */
    public void setOperate(String operate) {
        this.operate = operate;
    }
    
    public boolean equals2(UserPermissionEntity o){
    	if(!Objects.equals(userId, o.userId))return false;
    	if(!Objects.equals(appId, o.appId))return false;
    	if(!Objects.equals(operate, o.operate))return false;
    	return true;
    }
}