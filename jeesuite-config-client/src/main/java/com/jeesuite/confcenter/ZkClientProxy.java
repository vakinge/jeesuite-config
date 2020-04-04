/**
 * Confidential and Proprietary Copyright 2019 By 卓越里程教育科技有限公司 All Rights Reserved
 */
package com.jeesuite.confcenter;

import java.util.Map;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeesuite.common.json.JsonUtils;

/**
 * 
 * <br>
 * Class Name   : ZkClientProxy
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年11月18日
 */
public class ZkClientProxy {

	private final static Logger logger = LoggerFactory.getLogger("com.jeesuite.confcenter");
	
	private static final String NOTIFY_UPLOAD_CMD = "upload";
	
	private ZkClient zkClient;
	
	public ZkClientProxy(String zkServers, int connectionTimeout) {
		ZkConnection zkConnection = new ZkConnection(zkServers);
		zkClient = new ZkClient(zkConnection, connectionTimeout);	
	}
	
	public boolean exists(final String path) {
		return zkClient.exists(path);
	} 
	
	public void createPersistent(String path, boolean createParents) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
		zkClient.createPersistent(path, createParents);
    }
	
	public void createEphemeral(final String path) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
		zkClient.createEphemeral(path);
    }
	
	public boolean isAvailable(){
		return zkClient != null;
	}

	public void close(){
		if(zkClient != null)zkClient.close();
	}
	
	public void subscribeDataChanges(ConfigcenterContext context,String dataPath) {
		zkClient.subscribeDataChanges(dataPath, new IZkDataListener() {
			@Override
			public void handleDataDeleted(String arg0) throws Exception {}
			
			@Override
			public void handleDataChange(String path, Object data) throws Exception {
				if(data == null || StringUtils.isBlank(data.toString()))return;
				try {						
					Map<String, Object> changeDatas = JsonUtils.toObject(data.toString(),Map.class);
					context.updateConfig(changeDatas);
				} catch (Exception e) {
					logger.error("updateConfig error",e);
				}
			}
		});
	}
	
	public static void main(String[] args) {
		ZkClientProxy clientProxy = new ZkClientProxy("192.168.12.1:2181", 1000);
		System.out.println(clientProxy);
	}
}
