/**
 * Confidential and Proprietary Copyright 2019 By 卓越里程教育科技有限公司 All Rights Reserved
 */
package com.jeesuite.confcenter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeesuite.common.http.HttpResponseEntity;
import com.jeesuite.common.http.HttpUtils;
import com.jeesuite.common.json.JsonUtils;
import com.jeesuite.common.util.NetworkUtils;
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
	
	private static final String LATEST_FETCH_TIMESTAMP = "latest.fetch.timestamp";

	private final static String ROOT_PATH = "/confcenter";
	private ScheduledExecutorService hbScheduledExecutor;
	private ZkClientProxy zkClient;
	
	public InternalConfigChangeListener(String zkServers) {
		try {
			Class.forName("org.I0Itec.zkclient.ZkClient");
			if(StringUtils.isNotBlank(zkServers) && NetworkUtils.telnet(zkServers, 2000)){				
				zkClient = new ZkClientProxy(zkServers, 5000);
			}
		} catch (Exception e) {}
		
		ConfigcenterContext context = ConfigcenterContext.getInstance();
		if(zkClient != null && zkClient.isAvailable()){
			resisterZkListener(context);
			logger.info("resisterZkUpdaterListener OK zkServers:{}",zkServers);
		}else{
			resisterHttpListener(context);
		}
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

		final String url = context.getApiBaseUrls()[0] + "/api/fetch_changed_configs";
		final Map<String, String> params = new HashMap<>();
		params.put("version", context.getVersion());
		params.put("appName", context.getApp());
		params.put("env", context.getEnv());
		if(context.isIgnoreGlobal()){
			params.put("ignoreGlobal", String.valueOf(context.isIgnoreGlobal()));
		}
		params.put("lastTime", ResourceUtils.getProperty(LATEST_FETCH_TIMESTAMP, String.valueOf(System.currentTimeMillis())));
		hbScheduledExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				String safeUrl = ConfigcenterContext.getInstance().buildTokenParameter(url);
				HttpResponseEntity response = HttpUtils.postJson(safeUrl, JsonUtils.toJson(params),
						HttpUtils.DEFAULT_CHARSET);
				// 刷新服务端更新的配置
				if (response.isSuccessed()) {
					Map map = JsonUtils.toObject(response.getBody(), Map.class);
					if(map.containsKey("data")){
						map = (Map) map.get("data");
						if(map.containsKey(LATEST_FETCH_TIMESTAMP)){
							params.put("lastTime", map.remove(LATEST_FETCH_TIMESTAMP).toString());
						}
						if(!map.isEmpty())context.updateConfig(map);
					}
				}
			}
		}, context.getSyncIntervalSeconds(), context.getSyncIntervalSeconds(), TimeUnit.SECONDS);
		
		logger.info("resisterHttpUpdaterListener OK intervalSeconds:{},lastFetchTime:{}",context.getSyncIntervalSeconds(),params.get("lastTime"));
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
		zkClient.subscribeDataChanges(context, appParentPath);
		System.out.println("configClient nodePath:" + appNodePath);
		System.out.println("configClient subscribeDataChangePath:" + appParentPath);
		
	}

	
}
