/**
 * Confidential and Proprietary Copyright 2019 By 卓越里程教育科技有限公司 All Rights Reserved
 */
package com.jeesuite.confcenter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.jeesuite.common.http.HttpResponseEntity;
import com.jeesuite.common.http.HttpUtils;
import com.jeesuite.common.json.JsonUtils;
import com.jeesuite.common.util.ResourceUtils;

/**
 * 
 * <br>
 * Class Name   : InternalConfigChangeListener
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年7月18日
 */
public class InternalConfigChangeListener {

	private final static Logger logger = LoggerFactory.getLogger("com.jeesuite.confcenter");

	private final static String ROOT_PATH = "/confcenter";
	private static final String NOTIFY_UPLOAD_CMD = "upload";
	private ScheduledExecutorService hbScheduledExecutor;
	private ZkClient zkClient;
	
	private String syncType;

	public InternalConfigChangeListener(String zkServers) {
		if(StringUtils.isNotBlank(zkServers)){
			ZkConnection zkConnection = new ZkConnection(zkServers);
			try {			
				zkClient = new ZkClient(zkConnection, 5000);
			} catch (Exception e) {
				logger.warn("register_ZkConfigChangeListener_error",e);
			}
		}
		
		ConfigcenterContext context = ConfigcenterContext.getInstance();
		if(zkClient != null){
			resisterZkListener(context);
			syncType = "zookeepr";
		}else{
			resisterHttpListener(context);
			syncType = "http";
		}
	}
	
	/**
	 * @return the syncType
	 */
	public String getSyncType() {
		return syncType;
	}



	public void close(){
		if(zkClient !=  null)zkClient.close();
		if(hbScheduledExecutor != null)hbScheduledExecutor.shutdown();
	}

	/**
	 * @param context
	 */
	private void resisterHttpListener(ConfigcenterContext context) {
		hbScheduledExecutor = Executors.newScheduledThreadPool(1);

		final String[] syncStatusUrls = new String[context.getApiBaseUrls().length];
		for (int i = 0; i < context.getApiBaseUrls().length; i++) {
			syncStatusUrls[i] = context.getApiBaseUrls()[i] + "/api/sync_status";
		}
		
		final Map<String, String> params = new HashMap<>();
		params.put("nodeId", context.getNodeId());
		params.put("appName", context.getApp());
		params.put("env", context.getEnv());
		hbScheduledExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				// 由于初始化的时候还拿不到spring.cloud.client.ipAddress，故在同步过程上送
				if (context.isSpringboot()) {
					String serverip = ResourceUtils.getProperty("spring.cloud.client.ipAddress");
					if (StringUtils.isNotBlank(serverip)) {
						params.put("serverip", serverip);
					}
				}

				boolean updated = false;
				for (String url : syncStatusUrls) {
					url = ConfigcenterContext.getInstance().buildTokenParameter(url);
					HttpResponseEntity response = HttpUtils.postJson(url, JsonUtils.toJson(params),
							HttpUtils.DEFAULT_CHARSET);
					if(updated)return;
					// 刷新服务端更新的配置
					if (updated = response.isSuccessed()) {
						JsonNode jsonNode = JsonUtils.getNode(response.getBody(), "data");
						Map map = JsonUtils.toObject(jsonNode.toString(), Map.class);
						context.updateConfig(map);
					}
				}

			}
		}, 5, context.getSyncIntervalSeconds(), TimeUnit.SECONDS);
	}

	/**
	 * 
	 */
	private void resisterZkListener(ConfigcenterContext context) {
		String appParentPath = ROOT_PATH + "/" + context.getEnv() + "/" + context.getApp() + "/nodes";
		if(!zkClient.exists(appParentPath)){
			zkClient.createPersistent(appParentPath, true);
		}
		
		String appNodePath = appParentPath + "/" + context.getNodeId();
		
		if(zkClient.exists(appNodePath)){
			logger.info("node path[{}] exists",appNodePath);
			return;
		}
		//创建node节点
		zkClient.createEphemeral(appNodePath);
		
		zkClient.subscribeDataChanges(appParentPath, new IZkDataListener() {
			@Override
			public void handleDataDeleted(String arg0) throws Exception {}
			
			@Override
			public void handleDataChange(String path, Object data) throws Exception {
				if(data == null || StringUtils.isBlank(data.toString()))return;
				if(NOTIFY_UPLOAD_CMD.equals(data)){
					logger.info("receive cmd[{}] from path[{}]",data,path);
					Properties properties = ResourceUtils.getAllProperties();
					context.syncConfigToServer(properties,false);
					logger.info("process cmd[{}] ok~",data);
				}else{		
					try {						
						Map<String, Object> changeDatas = JsonUtils.toObject(data.toString(),Map.class);
						context.updateConfig(changeDatas);
					} catch (Exception e) {
						logger.error("updateConfig error",e);
					}
				}
				
			}
		});
	}

	
}
