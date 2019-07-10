package com.jeesuite.confcenter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import com.jeesuite.common.crypt.AES;
import com.jeesuite.common.crypt.Base64;
import com.jeesuite.common.http.HttpRequestEntity;
import com.jeesuite.common.http.HttpResponseEntity;
import com.jeesuite.common.http.HttpUtils;
import com.jeesuite.common.json.JsonUtils;
import com.jeesuite.common.util.DigestUtils;
import com.jeesuite.common.util.NodeNameHolder;
import com.jeesuite.common.util.ResourceUtils;
import com.jeesuite.confcenter.listener.HttpConfigChangeListener;
import com.jeesuite.confcenter.listener.ZkConfigChangeListener;
import com.jeesuite.spring.InstanceFactory;
import com.jeesuite.spring.helper.EnvironmentHelper;


public class ConfigcenterContext {

	private final static Logger logger = LoggerFactory.getLogger("com.jeesuite");
	
	private static ConfigcenterContext instance = new ConfigcenterContext();
	
	public static final String MANAGER_PROPERTY_SOURCE = "configcenter";
	
	private static final String IGNORE_PLACEHOLER = "[Ignore]";
	
	private static final String CRYPT_PREFIX = "{Cipher}";

	private static final String PLACEHOLDER_PREFIX = "${";
	private static final String PLACEHOLDER_SUFFIX = "}";
	
	private Boolean remoteEnabled;

	private final String nodeId = NodeNameHolder.getNodeId();
	private String[] apiBaseUrls;
	private String app;
	private String env;
	private String version;
	private String secret;
	private String globalSecret;
	private boolean remoteFirst = false;
	private boolean isSpringboot;
	private int syncIntervalSeconds = 90;
	private ConfigChangeListener configChangeListener;
	
	private List<ConfigChangeHanlder> configChangeHanlders;
	
	private ConfigStatus status;
	
	public enum ConfigStatus{
		INITED,FETCHED,UPLOAED
	}
	
	private ConfigcenterContext() {}

	public synchronized void init(Properties properties,boolean isSpringboot) {
		
		logger.info("client.nodeId:",nodeId);
		
		ResourceUtils.merge(properties);
		
		System.setProperty("client.nodeId", nodeId);
		System.setProperty("springboot", String.valueOf(isSpringboot));
		this.isSpringboot = isSpringboot;
		String defaultAppName = getValue("spring.application.name");
		app = getValue("jeesuite.configcenter.appName",defaultAppName);
		if(remoteEnabled == null)remoteEnabled = Boolean.parseBoolean(getValue("jeesuite.configcenter.enabled","true"));
		
		if(!remoteEnabled)return;
		
		env = getValue("jeesuite.configcenter.profile","dev");
		
		Validate.notBlank(env,"[jeesuite.configcenter.profile] is required");
		
		setApiBaseUrl(getValue("jeesuite.configcenter.base.url"));
		
		version = getValue("jeesuite.configcenter.version","0.0.0");
		
		syncIntervalSeconds = ResourceUtils.getInt("jeesuite.configcenter.sync-interval-seconds", 90);
		
		System.out.println(String.format("\n=====Configcenter config=====\nappName:%s\nenv:%s\nversion:%s\nremoteEnabled:%s\napiBaseUrls:%s\n=====Configcenter config=====", app,env,version,remoteEnabled,JsonUtils.toJson(apiBaseUrls)));
		status = ConfigStatus.INITED; 
	}

	public static ConfigcenterContext getInstance() {
		return instance;
	}
	
	public boolean isRemoteEnabled() {
		return remoteEnabled;
	}
	public void setRemoteEnabled(boolean remoteEnabled) {
		this.remoteEnabled = remoteEnabled;
	}
	public String[] getApiBaseUrls() {
		return apiBaseUrls;
	}
	
	public void setApiBaseUrl(String apiBaseUrl) {
		
		Validate.notBlank(apiBaseUrl,"[jeesuite.configcenter.base.url] is required");
		
		String[] urls = apiBaseUrl.split(",|;");
		this.apiBaseUrls = new String[urls.length];
		
		for (int i = 0; i < urls.length; i++) {
			if(urls[i].endsWith("/")){
				this.apiBaseUrls[i] = urls[i].substring(0, urls[i].length() - 1);
			}else{
				this.apiBaseUrls[i] = urls[i];
			}
			
		}
	}
	public String getApp() {
		return app;
	}

	public String getEnv() {
		return env;
	}

	public String getVersion() {
		return version;
	}
    
	public String getSecret() {
		return secret;
	}
	
	public boolean isRemoteFirst() {
		return remoteFirst;
	}
	
	public int getSyncIntervalSeconds() {
		return syncIntervalSeconds;
	}
	
	public String getNodeId() {
		return nodeId;
	}
	
	public boolean isSpringboot() {
		return isSpringboot;
	}
	
	
	
	public ConfigStatus getStatus() {
		return status;
	}

	public void mergeRemoteProperties(Properties properties){
		Properties remoteProperties = getAllRemoteProperties();
		if(remoteProperties != null){
			//合并属性
			Set<Entry<Object, Object>> entrySet = remoteProperties.entrySet();
			for (Entry<Object, Object> entry : entrySet) {
				//本地配置优先
				if(isRemoteFirst() == false && properties.containsKey(entry.getKey())){
					
					logger.info("config[{}] exists in location,skip~",entry.getKey());
					continue;
				}
				String value = entry.getValue().toString();
				if(IGNORE_PLACEHOLER.contentEquals(value))continue;
				properties.setProperty(entry.getKey().toString(), value);
			}
		}
		
		//替换本地变量占位符
		Set<Entry<Object, Object>> entrySet = properties.entrySet();
		for (Entry<Object, Object> entry : entrySet) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			
			ResourceUtils.add(key, value);
			if(value.contains(PLACEHOLDER_PREFIX)){
				properties.setProperty(key, ResourceUtils.getProperty(key));
			}
		}
	}
	

	private Properties getAllRemoteProperties(){
		if(!remoteEnabled)return null;
		
		Properties properties = new Properties();

		Map<String,Object> map = fetchConfigFromServer();
		if(map == null){
			throw new RuntimeException("fetch remote config error!");
		}
		
		//解密密匙
		secret =  Objects.toString(map.remove("jeesuite.configcenter.encrypt-secret"),null);
		globalSecret = Objects.toString(map.remove("jeesuite.configcenter.global-encrypt-secret"),null);
		remoteFirst = Boolean.parseBoolean(Objects.toString(map.remove("jeesuite.configcenter.remote-config-first"),"false"));
		properties.putAll(map);
		properties.clear();
		
		Set<String> keys = map.keySet();
		for (String key : keys) {
			Object value = decodeEncryptIfRequire(map.get(key));
			properties.put(key, value);
		}
	
		status = ConfigStatus.FETCHED; 
		
		return properties;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String,Object> fetchConfigFromServer(){
		Map<String,Object> result = null;
		String errorMsg = null;
        for (String apiBaseUrl : apiBaseUrls) {
        	String url = String.format("%s/api/fetch_all_configs?appName=%s&env=%s&version=%s", apiBaseUrl,app,env,version);
    		System.out.println("fetch configs url:" + url);
    		String jsonString = null;
    		try {
    			HttpResponseEntity response = HttpUtils.get(url);
        		if(response.isSuccessed()){
        			jsonString = response.getBody();
        			result = JsonUtils.toObject(jsonString, Map.class);
        			if(result.containsKey("code")){
        				errorMsg = result.get("msg").toString();
        				System.out.println("fetch error:"+errorMsg);
        				result = null;
        			}else{
        				break;
        			}
        		}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        //
        if(result == null){
        	result = LocalCacheUtils.read();
        }else{
        	LocalCacheUtils.write(result);
        }
        return result;
	}
	
	public void syncConfigToServer(Properties properties,boolean first){
		
		if(!remoteEnabled)return;
		if(status.equals(ConfigStatus.INITED))return;
		
		String syncType = properties.getProperty("jeesuite.configcenter.sync-type");
		List<String> sortKeys = new ArrayList<>();
		Map<String, String> params = new  HashMap<>();
		
		params.put("nodeId", nodeId);
		params.put("appName", app);
		params.put("env", env);
		params.put("version", version);
		params.put("springboot", String.valueOf(isSpringboot));
		params.put("syncIntervalSeconds", String.valueOf(syncIntervalSeconds));
		params.put("syncType", syncType);
		String serverPort = ServerEnvUtils.getServerPort();
	    if(StringUtils.isNumeric(serverPort)){	    	
	    	params.put("serverport", serverPort);
	    }
		params.put("serverip", ServerEnvUtils.getServerIpAddr());
		
		Set<Entry<Object, Object>> entrySet = properties.entrySet();
		for (Entry<Object, Object> entry : entrySet) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			params.put(key, hideSensitive(key, value));
			sortKeys.add(key);
		}
		
		if(first){	
			Collections.sort(sortKeys);
			System.out.println("==================final config list start==================");
			for (String key : sortKeys) {
				System.out.println(String.format("%s = %s", key,params.get(key) ));
			}
			System.out.println("==================final config list end====================");
			//register listener
			registerListener(syncType);
		}else{
			String serverip = EnvironmentHelper.getProperty("spring.cloud.client.ipAddress");
			if(StringUtils.isNotBlank(serverip)){
				params.put("serverip", serverip);
			}
		}
		
		for (String apiBaseUrl : apiBaseUrls) {			
			String url = apiBaseUrl + "/api/notify_final_config";
			logger.info("syncConfigToServer,url:" + url);
			HttpResponseEntity responseEntity = HttpUtils.postJson(url, JsonUtils.toJson(params),HttpUtils.DEFAULT_CHARSET);
			if(responseEntity.isSuccessed()){
				logger.info("syncConfigToServer[{}] Ok",url);
			}else{
				logger.warn("syncConfigToServer[{}] error",url);
			}
		}
	
		status = ConfigStatus.UPLOAED; 
	}

	private void registerListener(String syncType) {
		if("zookeeper".equals(syncType)){
			String zkServers = getValue("jeesuite.configcenter.sync-zk-servers");
			Validate.notBlank(zkServers,"config[jeesuite.configcenter.sync-zk-servers] is required for syncType [zookeepr]");
			configChangeListener = new ZkConfigChangeListener(zkServers);
		}else{
			configChangeListener = new HttpConfigChangeListener();
		}
		configChangeListener.register(this);
	}
	
	public synchronized void updateConfig(Map<String, Object> updateConfig){
		if(!updateConfig.isEmpty()){
			Set<String> keySet = updateConfig.keySet();
			for (String key : keySet) {
				String oldValue = ResourceUtils.getProperty(key);
				ResourceUtils.add(key, decodeEncryptIfRequire(updateConfig.get(key)).toString());
				
				StandardEnvironment environment = InstanceFactory.getInstance(StandardEnvironment.class);
				MutablePropertySources propertySources = environment.getPropertySources();
				
				MapPropertySource source = null;
				synchronized (propertySources) {					
					if(!propertySources.contains(MANAGER_PROPERTY_SOURCE)){
						source = new MapPropertySource(MANAGER_PROPERTY_SOURCE, new LinkedHashMap<String, Object>());
						environment.getPropertySources().addFirst(source);
					}else{
						source = (MapPropertySource) propertySources.get(MANAGER_PROPERTY_SOURCE);
					}
				}
				
				Map<String, Object> map = (Map<String, Object>) source.getSource();
				Properties properties = new Properties();
				properties.putAll(map);
				properties.putAll(updateConfig);
				propertySources.replace(source.getName(), new PropertiesPropertySource(source.getName(), properties));
		        logger.info("Config [{}] Change,oldValue:{},newValue:{}",key,oldValue,updateConfig.get(key));
			}
			
			if(configChangeHanlders == null){
				configChangeHanlders = new ArrayList<>();					
				Map<String, ConfigChangeHanlder> interfaces = InstanceFactory.getInstanceProvider().getInterfaces(ConfigChangeHanlder.class);
				if(interfaces != null){
					configChangeHanlders.addAll(interfaces.values());
				}
			}
			
			for (ConfigChangeHanlder hander : configChangeHanlders) {
				try {
					hander.onConfigChanged(updateConfig);
					logger.info("invoke {}.onConfigChanged successed!",hander.getClass().getName());
				} catch (Exception e) {
					e.printStackTrace();
					logger.warn("invoke {}.onConfigChanged error,msg:{}",hander.getClass().getName(),e.getMessage());
				}
			}
		}
	}
	
	public boolean pingCcServer(String pingUrl,int retry){
		boolean result = false;
		try {
			System.out.println("pingCcServer ,retry:"+retry);
			result = HttpUtils.get(pingUrl,HttpRequestEntity.create().connectTimeout(2000).readTimeout(2000)).isSuccessed();
		} catch (Exception e) {}
		if(retry == 0)return false;
		if(!result){
			try {Thread.sleep(1500);} catch (Exception e) {}
			return pingCcServer(pingUrl,--retry);
		} 
		
		return result;
	}
	
	public void close(){
		configChangeListener.unRegister();
	}
	
	private Object decodeEncryptIfRequire(Object data) {
		if (data.toString().startsWith(CRYPT_PREFIX)) {
			Validate.notBlank(secret,"config[jeesuite.configcenter.encrypt-secret] is required");
			data = data.toString().replace(CRYPT_PREFIX, "");
			String decryptString;
			try {
				decryptString = decryptWithAES(secret, data.toString());
			} catch (Exception e) {
				decryptString = decryptWithAES(globalSecret, data.toString());
			}
			return decryptString;
		}
		return data;
	}
	
	private static String decryptWithAES(String key, String data){
		try {
			String secretKey = DigestUtils.md5(key).substring(16);
			byte[] bytes = AES.decrypt(Base64.decode(data.getBytes(StandardCharsets.UTF_8)),  secretKey.getBytes(StandardCharsets.UTF_8));
			return  new String(bytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	List<String> sensitiveKeys = new ArrayList<>(Arrays.asList("pass","key","secret","token","credentials"));
	private String hideSensitive(String key,String orign){
		if(StringUtils.isAnyBlank(key,orign))return "";
		boolean is = false;
		for (String k : sensitiveKeys) {
			if(is = key.toLowerCase().contains(k))break;
		}
		int length = orign.length();
		if(is && length > 1)return orign.substring(0, length/2).concat("****");
		return orign;
	}
	
	private String getValue(String key){
		return getValue(key,null);
	}
	private String getValue(String key,String defVal){
		//jvm 启动参数优先
		String value = System.getProperty(key);
		if(StringUtils.isNotBlank(value))return value;
		value = StringUtils.trimToNull(ResourceUtils.getProperty(key,defVal));
		if(StringUtils.isNotBlank(value)){	
			if(value.startsWith(PLACEHOLDER_PREFIX)){
				String refKey = value.substring(2, value.length() - 1).trim();
				value = ResourceUtils.getProperty(refKey);
			}
		}
		return value;
	}
}
