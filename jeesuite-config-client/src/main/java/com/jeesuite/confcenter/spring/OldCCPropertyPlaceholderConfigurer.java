/**
 * 
 */
package com.jeesuite.confcenter.spring;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.jeesuite.confcenter.ConfigcenterContext;
import com.jeesuite.spring.InstanceFactory;
import com.jeesuite.spring.SpringInstanceProvider;

/**
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2016年11月2日
 */
public class OldCCPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements DisposableBean,ApplicationContextAware{
	
	private boolean setRemoteEnabled = false;
	private ConfigcenterContext ccContext = ConfigcenterContext.getInstance();
	
	public void setRemoteEnabled(boolean remoteEnabled) {
		ccContext.setRemoteEnabled(remoteEnabled);
		setRemoteEnabled = true;
	}

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
	}



	@Override
	protected Properties mergeProperties() throws IOException {
		Properties properties = super.mergeProperties();
	    if(!setRemoteEnabled){
	    	ccContext.setRemoteEnabled(Boolean.parseBoolean(properties.getProperty("jeesuite.configcenter.enabled", "true")));
	    }
		ccContext.init(properties,false);
		
		ccContext.mergeRemoteProperties(properties);
		
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
