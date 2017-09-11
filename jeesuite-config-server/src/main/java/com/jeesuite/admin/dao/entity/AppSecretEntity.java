package com.jeesuite.admin.dao.entity;

import com.jeesuite.admin.dao.BaseEntity;
import javax.persistence.*;

@Table(name = "app_secret")
public class AppSecretEntity extends BaseEntity {
	
	public static enum SecretType{
		DES,RSA
	}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "app_id")
    private Integer appId;

    private String env;

    @Column(name = "secret_type")
    private String secretType;

    @Column(name = "secret_key")
    private String secretKey;

    @Column(name = "secret_pass")
    private String secretPass;

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
     * @return secret_type
     */
    public String getSecretType() {
        return secretType;
    }

    /**
     * @param secretType
     */
    public void setSecretType(String secretType) {
        this.secretType = secretType;
    }

    /**
     * @return secret_key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * @return secret_pass
     */
    public String getSecretPass() {
        return secretPass;
    }

    /**
     * @param secretPass
     */
    public void setSecretPass(String secretPass) {
        this.secretPass = secretPass;
    }
}