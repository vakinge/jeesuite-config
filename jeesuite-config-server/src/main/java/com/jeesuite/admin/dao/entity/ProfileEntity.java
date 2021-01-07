package com.jeesuite.admin.dao.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.jeesuite.common.model.KeyValuePair;
import com.jeesuite.mybatis.core.BaseEntity;

@Table(name = "profiles")
public class ProfileEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String alias;

    @Column(name = "is_default")
    private Boolean isDefault;

    private Boolean enabled;
    
    @Transient
    private List<KeyValuePair>  extrAttrs;

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
     * @return alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return is_default
     */
    public Boolean getIsDefault() {
        return isDefault;
    }

    /**
     * @param isDefault
     */
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * @return enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

	/**
	 * @return the extrAttrs
	 */
	public List<KeyValuePair> getExtrAttrs() {
		return extrAttrs;
	}

	/**
	 * @param extrAttrs the extrAttrs to set
	 */
	public void setExtrAttrs(List<KeyValuePair> extrAttrs) {
		this.extrAttrs = extrAttrs;
	}
    
    
}