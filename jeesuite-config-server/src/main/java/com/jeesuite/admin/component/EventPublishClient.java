/*
 * Copyright 2016-2018 www.jeesuite.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeesuite.admin.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeesuite.common.json.JsonUtils;

/**
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年12月10日
 */
public class EventPublishClient {

	private static Logger logger = LoggerFactory.getLogger("com.zyframework.core.event");
	
	public static final String 	EVENT_TAG = "event_tag";
	public final static String PARAM_TIMESTAMP = "timestamp";
	public final static String ROOT_PATH = "/gloabl_event/";
	private Map<String, ZkClient> groupClients = new HashMap<>();
	
	private static EventPublishClient instance = new EventPublishClient();;
	
	private EventPublishClient() {}
	
	private static EventPublishClient getInstance() {
		return instance;
	}
	
	public synchronized static void registerEventGroup(String evnetGroup,ZkClient zkClient){
		getInstance().groupClients.put(evnetGroup, zkClient);
	}
	
	public synchronized static void unregisterEventGroup(String evnetGroup){
		if(getInstance().groupClients.containsKey(evnetGroup))return;
		try {getInstance().groupClients.get(evnetGroup).close();} catch (Exception e) {}
		getInstance().groupClients.remove(evnetGroup);
	}

	
	public static void publishAllEnvs(String eventName,String tag,Map<String, Object> datas){
		Set<String> envs = getInstance().groupClients.keySet();
		for (String env : envs) {
			publish(env, eventName, tag, datas);
		}
	}
	
	public static void publish(String env,String eventName,String tag,Map<String, Object> datas){
		if(!getInstance().groupClients.containsKey(env)){
			logger.warn("zookeeper for env:[{}] not found -> eventName:{}",env,eventName);
			return;
		}
		ZkClient zkClient = getInstance().groupClients.get(env);
		String path = ROOT_PATH + env + "/" + eventName;
		try {	
			if(!zkClient.exists(path)){
				logger.info("No client subscribe this event -> env:{},eventName:{} ",env,eventName);
				return;
			}
			if(datas == null){
				datas = new HashMap<>(2);
			}
			if(tag != null){
				datas.put(EVENT_TAG, tag);
			}
			datas.put(PARAM_TIMESTAMP, System.currentTimeMillis());
			zkClient.writeData(path, JsonUtils.toJson(datas));
			logger.info("publish_event_successed -> event:{},path:{},tag:{}",eventName,path,tag);
		} catch (Exception e) {
			logger.error(String.format("publish_event_error -> event:%s,path:%s,tag:%s",eventName,path,tag),e);
		}
	}
	
	public static void close(){
		if(instance == null)return;
		Iterator<Entry<String, ZkClient>> iterator = instance.groupClients.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, ZkClient> entry = iterator.next();
			try {entry.getValue().close();} catch (Exception e) {}
			iterator.remove();
		}
	}
	
}
