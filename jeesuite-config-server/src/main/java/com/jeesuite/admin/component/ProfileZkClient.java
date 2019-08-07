/**
 * Confidential and Proprietary Copyright 2019 By 卓越里程教育科技有限公司 All Rights Reserved
 */
package com.jeesuite.admin.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.jeesuite.admin.constants.ProfileExtrAttrName;
import com.jeesuite.admin.dao.entity.ProfileEntity;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;

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
	
	private Map<String, ZkClient> profileZkClientMapping = new HashMap<>();
	private Map<String, String> profileZkServersMapping = new HashMap<>();
	
	private @Autowired ProfileEntityMapper profileMapper;
	
	public ZkClient getClient(String profile){
		return profileZkClientMapping.get(profile);
	}
	
	public String getZkServer(String profile){
		return profileZkServersMapping.get(profile);
	}
	
	public Map<String, ZkClient> getClients(){
		return new HashMap<>(profileZkClientMapping);
	}

	@Override
	public void run(String... args) throws Exception {
		List<ProfileEntity> profiles = profileMapper.findAllEnabledProfiles();
		for (ProfileEntity profile : profiles) {
			String zkServers = profileMapper.findExtrAttr(profile.getName(), ProfileExtrAttrName.ZK_SERVERS.name());
			if(zkServers != null){
				try {	
					if(profileZkServersMapping.values().contains(zkServers)){
						inner:for (String sameProfileName : profileZkServersMapping.keySet()) {
							if(profileZkServersMapping.get(sameProfileName).equals(zkServers)){								
								profileZkClientMapping.put(profile.getName(), profileZkClientMapping.get(sameProfileName));
								logger.info("create zkClient ok -> profile:{},sameProfileName:{}",profile,sameProfileName);
								break inner;
							}
						}
					}else{
						ZkConnection zkConnection = new ZkConnection(zkServers);
						ZkClient zkClient = new ZkClient(zkConnection, 3000);
						profileZkClientMapping.put(profile.getName(), zkClient);
						profileZkServersMapping.put(profile.getName(), zkServers);
						logger.info("create zkClient ok -> profile:{},zkServers:{}",profile.getName(),zkServers);
					}
					
				} catch (Exception e) {
					logger.error("create zkClient:" + zkServers,e);
				}
			}
		}
		//
		ConfigStateHolder.setProfileZkClient(this);
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
