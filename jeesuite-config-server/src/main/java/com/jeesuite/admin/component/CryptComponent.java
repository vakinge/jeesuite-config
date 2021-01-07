package com.jeesuite.admin.component;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.crypt.AES;
import com.jeesuite.common.crypt.Base64;
import com.jeesuite.common.util.DigestUtils;

@Component
public class CryptComponent {

	public static String cryptPrefix = "{Cipher}";
	
	@Value("${config.crypto.secretKey:jeesuite}")
	private String secretKey = "jeesuite";

	public String getCryptKey(int appId,String env){
		String base = secretKey + Integer.toHexString(appId).concat(env);
		return DigestUtils.md5(base);
	}
	
	public String encrypt(int appId,String env,String data) {
		String key = getCryptKey(appId, env);
		return cryptPrefix + encryptWithAES(key, data.replace(cryptPrefix, ""));
	}
	
	public boolean isEncrpted(int appId,String env,String data){
		if(!data.startsWith(cryptPrefix))return false;
		String key = getCryptKey(appId, env);
		try {
			decryptWithAES(key, data.replace(cryptPrefix, ""));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private static String encryptWithAES(String key, String data){
		try {
			String secretKey = DigestUtils.md5(key).substring(16);
			byte[] bytes = AES.encrypt(data.getBytes(StandardCharsets.UTF_8), secretKey.getBytes(StandardCharsets.UTF_8));
			return  Base64.encodeToString(bytes, false);
		} catch (Exception e) {
			throw new JeesuiteBaseException(9999, "加密失败");
		}
	}
	
	private static String decryptWithAES(String key, String data){
		try {
			String secretKey = DigestUtils.md5(key).substring(16);
			byte[] bytes = AES.decrypt(Base64.decode(data.getBytes(StandardCharsets.UTF_8)),  secretKey.getBytes(StandardCharsets.UTF_8));
			return  new String(bytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new JeesuiteBaseException(9999, "解密失败");
		}
	}
	
	public static void main(String[] args) {
		CryptComponent component = new CryptComponent();
		component.secretKey = "zy2019@123";
		String cryptKey = component.getCryptKey(37, "prd");
		System.out.println(decryptWithAES(cryptKey, "SJEfop6UDw8xollCT72sT+pI48bf4vXV7cgi5CsEpr4="));
	}


}
