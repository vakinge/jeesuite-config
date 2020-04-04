package com.jeesuite.confcenter.springboot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.jeesuite.confcenter.ConfigcenterContext;

public class CCPropertySourceLoader implements PropertySourceLoader, Ordered{

	private final static Logger logger = LoggerFactory.getLogger("com.jeesuite");

	private ConfigcenterContext ccContext = ConfigcenterContext.getInstance();
	private String profiles = null;

	@Override
	public String[] getFileExtensions() {
		return new String[] { "properties","yml","yaml" };
	}
	
	public PropertySource<?> load(String name, Resource resource, String profile) throws IOException {

		logger.info("load PropertySource -> name:{},profile:{}", name, profile);
		if (profile == null) {
			Properties properties = loadProperties(name,resource);
			if (profiles == null) {
				profiles = properties.getProperty("spring.profiles.active");
			} else {
				logger.info("spring.profiles.active = " + profiles + ",ignore load remote config");
			}
			// 如果指定了profile，则也不加载远程配置
			if (profiles == null && ccContext.isRemoteEnabled() && !ccContext.isProcessed()) {
				ccContext.init(properties, true);
				ccContext.mergeRemoteProperties(properties);
			}

			if (!properties.isEmpty()) {
				return new PropertiesPropertySource(name, properties);
			}
		}
		return null;
	}
	
	public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
		Properties properties =  loadProperties(name,resource);
		if (profiles == null) {
			profiles = properties.getProperty("spring.profiles.active");
		} else {
			logger.info("spring.profiles.active = " + profiles + ",ignore load remote config");
		}
		// 如果指定了profile，则也不加载远程配置
		if (profiles == null && ccContext.isRemoteEnabled() && !ccContext.isProcessed()) {
			ccContext.init(properties, true);
			ccContext.mergeRemoteProperties(properties);
			PropertySource<?> props = new PropertiesPropertySource(ConfigcenterContext.MANAGER_PROPERTY_SOURCE, properties);
			return Arrays.asList(props);
		}
		
		return new ArrayList<>();
	}
	
	private Properties loadProperties(String name, Resource resource) throws IOException{
		Properties properties = null;
		if(name.contains("properties")){
			properties = PropertiesLoaderUtils.loadProperties(resource);
		}else{
			YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
			bean.setResources(resource);
			properties = bean.getObject();
		}
		return properties;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 11;
	}

}
