package com.easynet.listener;

import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import com.easynet.configuration.ReloadablePropertySourceConfig;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.CommonBase;
import com.easynet.util.readXML;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

@Configuration
public class RefreshConfigData extends CommonBase{

	private Logger LOGGER=LoggerFactory.getLogger(RefreshConfigData.class);

	@EventListener
	public void updateConfigData(EnvironmentChangeEvent environmentChangeEvent) throws Exception {	  
		LoggerImpl loggerImpl =new LoggerImpl();

		try {
			//update the urldetails config file data.
			readXML.ofsetdata();
			//update the logback configuration.
			reloadLoggerConfiguration();			
		}catch(Exception exception) {
			getExceptionMSg(exception, LOGGER, loggerImpl, "IN:updateConfigData", "(ENP836)" ,"Error at the time of refresh data.",null);
			throw exception;
		}
	}

	private void reloadLoggerConfiguration() throws Exception {
		String LS_CONFIG_PATH=ReloadablePropertySourceConfig.configurableEnvironment.getProperty("RES_CONFIG_PATH");

		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		URL logbackConfigUrl=new URL(LS_CONFIG_PATH+"logback.xml");

		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(loggerContext);
		loggerContext.reset();
		configurator.doConfigure(logbackConfigUrl);		    	   		
	}
}
