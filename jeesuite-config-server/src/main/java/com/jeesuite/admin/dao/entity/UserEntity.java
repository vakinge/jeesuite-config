package com.jeesuite.admin.dao.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.jeesuite.common.util.DigestUtils;
import com.jeesuite.mybatis.core.BaseEntity;

@Table(name = "users")
public class UserEntity extends BaseEntity {
	
	private static final String salts = DigestUtils.md5(UserEntity.class.getName());
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name",updatable=false)
    private String name;

    private String password;

    private String mobile;

    private String email;

    @Column(name = "type",updatable=false)
    private Short type;

    private Short status;

    @Column(name = "created_at",updatable=false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;
    

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

    /**
     * @return type
     */
    public Short getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Short type) {
        this.type = type;
    }

    /**
     * @return status
     */
    public Short getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(Short status) {
        this.status = status;
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
    
    public static String encryptPassword(String password) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < password.length(); i++) {
			sb.append(password.charAt(i)).append(salts.substring(i*2, (i+1)*2));
		}
		return DigestUtils.md5(sb.toString());

	}
    
    public static void main(String[] args) {
		System.out.println(encryptPassword("admin123"));
	}
    
}