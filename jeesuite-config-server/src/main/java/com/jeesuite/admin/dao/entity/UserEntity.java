package com.jeesuite.admin.dao.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.jeesuite.admin.constants.UserType;
import com.jeesuite.mybatis.core.BaseEntity;

@Table(name = "users")
public class UserEntity extends BaseEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name",updatable=false)
    private String name;

    private String password;

    private String mobile;

    @Column(name = "email",updatable=false)
    private String email;

    @Column(name = "type",updatable=false)
    private String type;

    private Boolean enabled;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "created_at",updatable=false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;
    
    @Transient
    private String groupName;
    @Transient
    private boolean groupMaster;

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

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return mobile
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * @param mobile
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    /**
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
		return type;
	}

	public void setType(String type) {
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
        return createdAt;
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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
	 * @return the groupId
	 */
	public Integer getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getTypeAlias(){
		return UserType.valueOf(type).getCnName();
	}
	
	public boolean isGroupMaster() {
		return groupMaster;
	}

	public void setGroupMaster(boolean groupMaster) {
		this.groupMaster = groupMaster;
	}
	
	public boolean isSuperAdmin() {
		return UserType.superAdmin.name().equals(getType());
	}
	
	public boolean isGroupAdmin() {
		return UserType.groupAdmin.name().equals(getType());
	}


}