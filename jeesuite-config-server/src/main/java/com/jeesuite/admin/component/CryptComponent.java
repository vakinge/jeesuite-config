package com.jeesuite.admin.component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.jeesuite.admin.dao.entity.AppSecretEntity;
import com.jeesuite.admin.dao.entity.AppSecretEntity.SecretType;
import com.jeesuite.admin.dao.mapper.AppSecretEntityMapper;
import com.jeesuite.admin.exception.JeesuiteBaseException;
import com.jeesuite.common.crypt.RSA;
import com.jeesuite.common.util.SimpleCryptUtils;

@Component
public class CryptComponent implements EnvironmentAware {

	private @Autowired AppSecretEntityMapper appSecretMapper;

	private static Map<String, PublicKey> rsaKeyPairs = new HashMap<>();

	private KeyStore keyStore;

	private static final String DES_PREFIX = "{Cipher}";

	private static final String RSA_PREFIX = "{Cipher:RSA}";

	private static String keyStoreInitErrorMsg = "";

	public AppSecretEntity getAppSecret(int appId, String env, String encryptType) {
		AppSecretEntity entity = appSecretMapper.get(appId, env, encryptType);
		//
		if (entity == null && SecretType.DES.name().equals(encryptType)) {
			entity = new AppSecretEntity();
			entity.setAppId(appId);
			entity.setEnv(env);
			entity.setSecretType(encryptType);
			entity.setSecretKey(UUID.randomUUID().toString().replaceAll("-", ""));
			appSecretMapper.insertSelective(entity);
		}
		return entity;
	}

	private PublicKey getRsaPublicKey(AppSecretEntity appSecret) {
		if (keyStore == null)
			throw new JeesuiteBaseException(10, "无RSA私钥配置-" + keyStoreInitErrorMsg);
		String key = appSecret.getEnv() + appSecret.getAppId();
		PublicKey rsaKey = rsaKeyPairs.get(key);
		if (rsaKey == null) {
			synchronized (rsaKeyPairs) {
				String keyPassword = SimpleCryptUtils.decrypt(appSecret.getSecretKey(), appSecret.getSecretPass());
				rsaKey = RSA.loadPublicKeyFromKeyStore(keyStore, appSecret.getSecretKey(), keyPassword);
				rsaKeyPairs.put(key, rsaKey);
			}
		}
		return rsaKey;
	}

	public String encode(AppSecretEntity appSecret, String data) {
		if (SecretType.RSA.name().equalsIgnoreCase(appSecret.getSecretType())) {
			return RSA_PREFIX + RSA.encrypt(getRsaPublicKey(appSecret), data);
		} else {
			return DES_PREFIX + SimpleCryptUtils.encrypt(appSecret.getSecretKey(), data);
		}

	}

	@Override
	public void setEnvironment(Environment environment) {

		String location = environment.getProperty("cc.encrypt.keyStore.location");
		String storeType = environment.getProperty("cc.encrypt.keyStore.type", "JCEKS");
		String storePass = environment.getProperty("cc.encrypt.keyStore.password");

		System.out.println("cc.encrypt.keyStore.location:" + location);
		if (StringUtils.isNotBlank(location)) {
			try {
				keyStore = KeyStore.getInstance(storeType);
				InputStream is = new FileInputStream(location);
				keyStore.load(is, storePass.toCharArray());
			} catch (Exception e) {
				keyStore = null;
				keyStoreInitErrorMsg = e.getMessage();
				System.err.println("load RSA KeyStore error,"+keyStoreInitErrorMsg);
			}
		}

	}

}
