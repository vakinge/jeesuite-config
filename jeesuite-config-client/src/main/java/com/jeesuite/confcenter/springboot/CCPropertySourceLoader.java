package com.jeesuite.confcenter.springboot;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.jeesuite.confcenter.ConfigcenterContext;

public class CCPropertySourceLoader implements PropertySourceLoader,PriorityOrdered,DisposableBean {

	private ConfigcenterContext ccContext = ConfigcenterContext.getInstance();
	@Override
	public String[] getFileExtensions() {
		return new String[] { "properties"};
	}

	@Override
	public PropertySource<?> load(String name, Resource resource, String profile)
			throws IOException {
		if (profile == null) {
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			
			ccContext.init(properties,true);
			
			ccContext.mergeRemoteProperties(properties);
			
			ccContext.syncConfigToServer(properties,true);
			
			if (!properties.isEmpty()) {
				return new PropertiesPropertySource(name, properties);
			}
		}
		return null;
	}
	
	@Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

	@Override
	public void destroy() throws Exception {
		ccContext.close();
	}

}
