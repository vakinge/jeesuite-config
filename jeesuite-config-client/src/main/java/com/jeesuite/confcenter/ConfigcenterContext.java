package com.jeesuite.confcenter;

import java.security.PrivateKey;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.jeesuite.common.crypt.RSA;
import com.jeesuite.common.http.HttpRequestEntity;
import com.jeesuite.common.http.HttpResponseEntity;
import com.jeesuite.common.http.HttpUtils;
import com.jeesuite.common.json.JsonUtils;
import com.jeesuite.common.util.NodeNameHolder;
import com.jeesuite.common.util.ResourceUtils;
import com.jeesuite.common.util.SimpleCryptUtils;
import com.jeesuite.confcenter.listener.HttpConfigChangeListener;
import com.jeesuite.confcenter.listener.ZkConfigChangeListener;
import com.jeesuite.spring.InstanceFactory;
import com.jeesuite.spring.helper.EnvironmentHelper;


public class ConfigcenterContext {

	private final static Logger logger = LoggerFactory.getLogger(ConfigcenterContext.class);
	
	private static ConfigcenterContext instance = new ConfigcenterContext();
	
	private static final String MANAGER_PROPERTY_SOURCE = "manager";
	
	private PrivateKey rsaPrivateKey;
	
	private static final String DES_PREFIX = "{Cipher}";

	private static final String RSA_PREFIX = "{Cipher:RSA}";
	
	private Boolean remoteEnabled;

	private final String nodeId = NodeNameHolder.getNodeId();
	private String[] apiBaseUrls;
	private String app;
	private String env;
	private String version;
	private String secret;
	private boolean remoteFirst = false;
	private boolean isSpringboot;
	private int syncIntervalSeconds = 90;
	private String pingUri = "/api/ping";
	private ConfigChangeListener configChangeListener;
	
	private List<ConfigChangeHanlder> configChangeHanlders;
	
	private ConfigcenterContext() {}

	public void init(boolean isSpringboot) {
		this.isSpringboot = isSpringboot;
		String defaultAppName = getValue("spring.application.name");
		app = getValue("jeesuite.configcenter.appName",defaultAppName);
		if(remoteEnabled == null)remoteEnabled = Boolean.parseBoolean(getValue("jeesuite.configcenter.enabled","true"));
		
		if(!remoteEnabled)return;
		
		String defaultEnv = getValue("spring.profiles.active");
		env = getValue("jeesuite.configcenter.profile",defaultEnv);
		
		Validate.notBlank(env,"[jeesuite.configcenter.profile] is required");
		
		setApiBaseUrl(getValue("jeesuite.configcenter.base.url"));
		
		version = getValue("jeesuite.configcenter.version","0.0.0");
		
		syncIntervalSeconds = ResourceUtils.getInt("jeesuite.configcenter.sync-interval-seconds", 90);
		
		System.out.println(String.format("\n=====configcenter=====\nappName:%s\nenv:%s\nversion:%s\nremoteEnabled:%s\n=====configcenter=====", app,env,version,remoteEnabled));
		
		String location = StringUtils.trimToNull(ResourceUtils.getProperty("jeesuite.configcenter.encrypt-keyStore-location"));
		String storeType = ResourceUtils.getProperty("jeesuite.configcenter.encrypt-keyStore-type", "JCEKS");
		String storePass = ResourceUtils.getProperty("jeesuite.configcenter.encrypt-keyStore-password");
		String alias = ResourceUtils.getProperty("jeesuite.configcenter.encrypt-keyStore-alias");
		String keyPass = ResourceUtils.getProperty("jeesuite.configcenter.encrypt-keyStore-keyPassword", storePass);
		try {			
			if(StringUtils.isNotBlank(location)){
				if(location.toLowerCase().startsWith("classpath")){
					Resource resource = new ClassPathResource(location.substring(location.indexOf(":") + 1));
					location = resource.getFile().getAbsolutePath();
				}
				rsaPrivateKey = RSA.loadPrivateKeyFromKeyStore(location, alias, storeType, storePass, keyPass);
			}
		} catch (Exception e) {
			System.out.println("load private key:"+location);
			e.printStackTrace();
		}
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
	

	public Properties getAllRemoteProperties(){
		if(!remoteEnabled)return null;
		
		Properties properties = new Properties();

		Map<String,Object> map = fetchConfigFromServer(2);
		if(map == null){
			throw new RuntimeException("fetch remote config error!");
		}
		
		//DES解密密匙
		secret =  Objects.toString(map.remove("jeesuite.configcenter.encrypt-secret"),null);
		remoteFirst = Boolean.parseBoolean(Objects.toString(map.remove("jeesuite.configcenter.remote-config-first"),"false"));
		
		Set<String> keys = map.keySet();
		for (String key : keys) {
			Object value = decodeEncryptIfRequire(map.get(key));
			properties.put(key, value);
		}
	
		return properties;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String,Object> fetchConfigFromServer(int retry){
		if(retry == 0)return null;
		Map<String,Object> result = null;
		String errorMsg = null;
        for (String apiBaseUrl : apiBaseUrls) {
        	String url = String.format("%s/api/fetch_all_configs?appName=%s&env=%s&version=%s", apiBaseUrl,app,env,version);
    		System.out.println("fetch configs url:" + url);
    		String jsonString = null;
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
		}
        
        if(result == null){
        	result = fetchConfigFromServer(--retry);
        }
        return result;
	}
	
	public void syncConfigToServer(Properties properties,boolean first){
		
		if(!remoteEnabled)return;
		
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
		params.put("serverport", ServerEnvUtils.getServerPort());
		params.put("serverip", ServerEnvUtils.getServerIpAddr());
		
		Set<Entry<Object, Object>> entrySet = properties.entrySet();
		for (Entry<Object, Object> entry : entrySet) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			//如果远程配置了占位符，希望引用本地变量
			if(value.contains("${")){
				value = setReplaceHolderRefValue(properties,key,value);
			}
			
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
			HttpUtils.postJson(url, JsonUtils.toJson(params),HttpUtils.DEFAULT_CHARSET);
			logger.info("syncConfigToServer Ok");
		}
	
	}

	private void registerListener(String syncType) {
		if("zookeeper".equals(syncType)){
			String zkServers = ResourceUtils.getProperty("jeesuite.configcenter.sync-zk-servers");
			if(StringUtils.isBlank(zkServers)){
				throw new RuntimeException("config[jeesuite.configcenter.sync-zk-servers] is required for syncType [zookeepr] ");
			}
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
				//publish change event
				if(InstanceFactory.getInstance(EnvironmentChangeListener.class) != null){
					try {								
						InstanceFactory.getInstanceProvider().getApplicationContext().publishEvent(new EnvironmentChangeEvent(updateConfig.keySet()));
					} catch (Exception e) {
						e.printStackTrace();
					}
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
						logger.warn("invoke {}.onConfigChanged error,msg:{}",hander.getClass().getName(),e.getMessage());
					}
				}
				
		        logger.info("Config [{}] Change,oldValue:{},newValue:{}",key,oldValue,updateConfig.get(key));
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
	
    private String setReplaceHolderRefValue(Properties properties, String key, String value) {
		
    	String[] segments = value.split("\\$\\{");
		String seg;
		
		StringBuilder finalValue = new StringBuilder();
		for (int i = 0; i < segments.length; i++) {
			seg = StringUtils.trimToNull(segments[i]);
			if(StringUtils.isBlank(seg))continue;
			
			if(seg.contains("}")){	
				String refKey = seg.substring(0, seg.indexOf("}")).trim();
				//其他非${}的占位符如：{{host}}
				String withBraceString = null;
				if(seg.contains("{")){
					withBraceString = seg.substring(seg.indexOf("}")+1);
				}
				
				//如果包含默认值，如：${host:127.0.0.1}
				String orginKey = refKey;
				if(refKey.contains(":")){
					refKey = refKey.split(":")[0];
				}
				
				String refValue = properties.containsKey(refKey) ? properties.get(refKey).toString() : "${" + orginKey + "}";
				finalValue.append(refValue);
				
				if(withBraceString != null){
					finalValue.append(withBraceString);
				}else{
					String[] segments2 = seg.split("\\}");
					if(segments2.length == 2){
						finalValue.append(segments2[1]);
					}
				}
			}else{
				finalValue.append(seg);
			}
		}
		
		properties.put(key, finalValue.toString());
		
		return finalValue.toString();
	}

	private Object decodeEncryptIfRequire(Object data) {
		if (data.toString().startsWith(RSA_PREFIX)) {
			if(rsaPrivateKey == null){
				throw new RuntimeException("configcenter [rsaPrivateKey] is required");
			}
			data = data.toString().replace(RSA_PREFIX, "");
			return RSA.decrypt(rsaPrivateKey, data.toString());
		} else if (data.toString().startsWith(DES_PREFIX)) {
			if(StringUtils.isBlank(secret)){
				throw new RuntimeException("configcenter [jeesuite.configcenter.encrypt-secret] is required");
			}
			data = data.toString().replace(DES_PREFIX, "");
			return SimpleCryptUtils.decrypt(secret, data.toString());
		}
		return data;
	}

	List<String> sensitiveKeys = new ArrayList<>(Arrays.asList("pass","key","secret","token","credentials"));
	private String hideSensitive(String key,String orign){
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
			if(value.startsWith("${")){
				String refKey = value.substring(2, value.length() - 1).trim();
				value = ResourceUtils.getProperty(refKey);
			}
		}
		return value;
	}
}
