/**
 * 
 */
package com.jeesuite.confcenter.spring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.jeesuite.common.util.ResourceUtils;
import com.jeesuite.confcenter.ConfigcenterContext;
import com.jeesuite.spring.InstanceFactory;
import com.jeesuite.spring.SpringInstanceProvider;

/**
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2016年11月2日
 */
public class CCPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements DisposableBean,ApplicationContextAware{
	
	private final static Logger logger = LoggerFactory.getLogger(CCPropertyPlaceholderConfigurer.class);
	
	private ConfigcenterContext ccContext = ConfigcenterContext.getInstance();
	
	public void setRemoteEnabled(boolean remoteEnabled) {
		ccContext.setRemoteEnabled(remoteEnabled);
	}

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
	}



	@Override
	protected Properties mergeProperties() throws IOException {
		Properties properties = super.mergeProperties();
		
		URL resource = Thread.currentThread().getContextClassLoader().getResource("confcenter.properties");
		Properties config = null;
		if(resource != null){			
			config = new Properties();
			config.load(new FileReader(new File(resource.getPath())));
			ResourceUtils.merge(config);
		}else{
			config = ResourceUtils.getAllProperties("jeesuite.configcenter");
		}
		ccContext.init(false);
		
		ccContext.mergeRemoteProperties(properties);
		
		ccContext.syncConfigToServer(properties,true);
		
		return properties;
		//
	}

	@Override
	public void destroy() throws Exception {
		ccContext.close();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if(InstanceFactory.getInstanceProvider() == null){
			InstanceFactory.setInstanceProvider(new SpringInstanceProvider(applicationContext));
		}
	}


}
