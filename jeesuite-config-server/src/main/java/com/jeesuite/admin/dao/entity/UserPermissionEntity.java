package com.jeesuite.admin.dao.entity;

import com.jeesuite.admin.dao.BaseEntity;
import javax.persistence.*;

@Table(name = "user_permissions")
public class UserPermissionEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "grant_type")
    private String grantType;

    @Column(name = "grant_target")
    private String grantTarget;

    @Column(name = "grant_operate")
    private String grantOperate;
    

    public UserPermissionEntity() {}

	public UserPermissionEntity(Integer userId, String grantType, String grantTarget, String grantOperate) {
		this.userId = userId;
		this.grantType = grantType;
		this.grantTarget = grantTarget;
		this.grantOperate = grantOperate;
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
     * @return grant_type
     */
    public String getGrantType() {
        return grantType;
    }

    /**
     * @param grantType
     */
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }


    public String getGrantTarget() {
		return grantTarget;
	}

	public void setGrantTarget(String grantTarget) {
		this.grantTarget = grantTarget;
	}

	/**
     * @return grant_operate
     */
    public String getGrantOperate() {
        return grantOperate;
    }

    /**
     * @param grantOperate
     */
    public void setGrantOperate(String grantOperate) {
        this.grantOperate = grantOperate;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((grantTarget == null) ? 0 : grantTarget.hashCode());
		result = prime * result + ((grantType == null) ? 0 : grantType.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserPermissionEntity other = (UserPermissionEntity) obj;
		if (grantTarget == null) {
			if (other.grantTarget != null)
				return false;
		} else if (!grantTarget.equals(other.grantTarget))
			return false;
		if (grantType == null) {
			if (other.grantType != null)
				return false;
		} else if (!grantType.equals(other.grantType))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
    
    
}