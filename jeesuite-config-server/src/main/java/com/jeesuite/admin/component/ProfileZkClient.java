package com.jeesuite.admin.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.jeesuite.admin.constants.ProfileExtrAttrName;
import com.jeesuite.admin.dao.entity.ProfileEntity;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;
import com.jeesuite.common.json.JsonUtils;
import com.jeesuite.common.util.NetworkUtils;
import com.jeesuite.common.util.NodeNameHolder;

/**
 * 
 * <br>
 * Class Name   : ProfileZkClient
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年7月18日
 */
@Component
public class ProfileZkClient implements CommandLineRunner,DisposableBean{

	private static Logger logger = LoggerFactory.getLogger("configcenter");
	
	private final static String ZK_ROOT_PATH = "/confcenter";
	
	public final static String PROFILE_ZK_CHANGED_EVENT = "confcenter_profile_zk_changed";
	
	private Map<String, ZkClient> profileZkClientMapping = new HashMap<>();
	private Map<String, String> profileZkServersMapping = new HashMap<>();
	
	private @Autowired ProfileEntityMapper profileMapper;
	
	@Value("${spring.profiles.active}")
	private String envProfile;
	
	public ZkClient getClient(String profile){
		if(!profileZkServersMapping.containsKey(profile)){
			initProfileClient(profile,null);
		}
		return profileZkClientMapping.get(profile);
	}
	
	public String getZkServer(String profile){
		return profileZkServersMapping.get(profile);
	}
	
	public Map<String, ZkClient> getClients(){
		return new HashMap<>(profileZkClientMapping);
	}
	
	public void remove(String profile,boolean publishNotify){
		logger.info("remove profileZkClient:{}",profile);
		String servers = profileZkServersMapping.remove(profile);
		if(!profileZkClientMapping.containsKey(profile)){
			return;
		}
		try {profileZkClientMapping.get(profile).close();} catch (Exception e) {}
		profileZkClientMapping.remove(profile);
		//
		EventPublishClient.unregisterEventGroup(profile);
		logger.info("remove profileZkClient:{},servers:{} OK",profile,servers);
		//
		if(publishNotify){			
			publishZkChangedNotify(profile, null);
		}
	}
	
	public void add(String profile,String zkServers,boolean publishNotify){
		logger.info("add profileZkClient:{}",profile);
		if(zkServers.equals(profileZkServersMapping.get(profile)))return;
		if(!profileZkClientMapping.containsKey(profile)){
			initProfileClient(profile,zkServers);
		}
		//
		if(publishNotify && profileZkClientMapping.containsKey(profile)){			
			publishZkChangedNotify(profile, zkServers);
		}
	}
	
	public void publishChangeConfig(String profile,List<String> appKeys, Map<String, String> changeConfigs){
		ZkClient zkClient = getClient(profile);
		if(zkClient == null)return;
		String zkPath;
		for (String appKey : appKeys) {
			zkPath = ZK_ROOT_PATH + "/" + profile + "/" + appKey + "/nodes";
			if(!zkClient.exists(zkPath))return;
			if(zkClient.countChildren(zkPath) == 0){
				return;
			}
			zkClient.writeData(zkPath, JsonUtils.toJson(changeConfigs));
			logger.info("publishChangeConfig OK -> zkPath:{},changeConfigs:{}",zkPath,changeConfigs);
		}
	}

	@Override
	public void run(String... args) throws Exception {
		List<ProfileEntity> profiles = profileMapper.findAllEnabledProfiles();
		for (ProfileEntity profile : profiles) {
			initProfileClient(profile.getName(),null);
		}
		
		//订阅zk配置变更事件
		ZkClient currentProfileClient = profileZkClientMapping.get(envProfile);
		if(currentProfileClient != null){
			String zkPath = EventPublishClient.ROOT_PATH + envProfile + "/" + PROFILE_ZK_CHANGED_EVENT;
			if(!currentProfileClient.exists(zkPath)){
				currentProfileClient.createPersistent(zkPath, true);
			}
			currentProfileClient.subscribeDataChanges(zkPath, new IZkDataListener() {
				@Override
				public void handleDataDeleted(String dataPath) throws Exception {}
				
				@Override
				public void handleDataChange(String dataPath, Object data) throws Exception {
					Map map = JsonUtils.toObject(data.toString(), Map.class);
					if(NodeNameHolder.getNodeId().equals(map.get("nodeId").toString())){
						return;
					}
					logger.info(">>receive zkProfileConfig change event -> {}",map);
					String profile = map.get("profile").toString();
					if("remove".equals(map.get(EventPublishClient.EVENT_TAG).toString())){
						remove(profile,false);
					}else{
						String zkServers = map.get("zkServers").toString();
						add(profile, zkServers,false);
					}
				}
			});
		}
		
	}
	
	private void publishZkChangedNotify(String profile,String zkServers){
		String tag;
		Map<String, Object> datas = new HashMap<>(3);
		datas.put("profile", profile);
		datas.put("nodeId", NodeNameHolder.getNodeId());
		if(zkServers == null){
			tag = "remove";
		}else{
			tag = "add";
			datas.put("zkServers", zkServers);
		}
		EventPublishClient.publish(envProfile, PROFILE_ZK_CHANGED_EVENT, tag, datas);
	}

	/**
	 * @param profile
	 */
	private synchronized void initProfileClient(String profileName,String zkServers) {
		if(profileZkClientMapping.containsKey(profileName))return;
		if(StringUtils.isBlank(zkServers)){
			zkServers = profileMapper.findExtrAttr(profileName, ProfileExtrAttrName.zkServers.name());
		}
		if(StringUtils.isNotBlank(zkServers) && NetworkUtils.telnet(zkServers, 2000)){
			try {
				ZkConnection zkConnection = new ZkConnection(zkServers);
				ZkClient zkClient = new ZkClient(zkConnection, 3000);
				//
				String parentPath = ZK_ROOT_PATH + "/" + profileName;
				if(!zkClient.exists(parentPath)){
					zkClient.createPersistent(parentPath, true);
				}
				
				profileZkClientMapping.put(profileName, zkClient);
				profileZkServersMapping.put(profileName, zkServers);
				//
				EventPublishClient.registerEventGroup(profileName, zkClient);
				logger.info("create zkClient ok -> profile:{},zkServers:{}",profileName,zkServers);
			} catch (Exception e) {
				profileZkServersMapping.put(profileName, null);
				logger.error("create zkClient:" + zkServers,e);
			}
		}else if(StringUtils.isNotBlank(zkServers)) {
			logger.warn("!!!can't connect -> profileName:{},zkServers:{}",profileName,zkServers);
		}
	}
	
	@Override
	public void destroy() throws Exception {
		profileZkClientMapping.values().forEach(client -> {
			try {
				client.close();
			} catch (Exception e) {}
		});
	}

}
