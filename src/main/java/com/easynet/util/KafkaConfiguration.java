package com.easynet.util;

import java.util.Objects;
import javax.xml.bind.PropertyException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import com.easynet.configuration.ReloadablePropertySourceConfig;
import com.easynet.impl.LoggerImpl;

@RefreshScope
@Component
public class KafkaConfiguration {
	
	@Autowired
	CommonBase commonBase;
	
	private Logger LOGGER=LoggerFactory.getLogger(KafkaConfiguration.class);
	
	String LS_CONFIG_PATH=ReloadablePropertySourceConfig.configurableEnvironment.getProperty("COMM_CONFIG_PATH");
	
	PropertiesConfiguration propertiesConfiguration;

	public KafkaConfiguration() throws PropertyException {
		LoggerImpl loggerImpl=new LoggerImpl();
		
		try {
			PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();	            
			propertiesConfiguration.setDelimiterParsingDisabled(true);
			propertiesConfiguration.setDefaultListDelimiter('!');
			propertiesConfiguration.setListDelimiter('!');
			propertiesConfiguration.load(LS_CONFIG_PATH+"KafkaConfirguration.properties");
			
			this.propertiesConfiguration =propertiesConfiguration;	            
		}catch (Exception exception) {
			commonBase.getExceptionMSg(exception, LOGGER, loggerImpl, "IN:KafkaConfiguration", "(ENP859)" ,"Error at the time of refresh KafkaConfiguration data.",null);
			throw new PropertyException(exception);
		} 
	}	

	public String getPropertyValue(String keyName) {
		Object keyvalue=this.propertiesConfiguration.getProperty(keyName);
		
		if(!Objects.isNull(keyvalue)){
			return String.valueOf(keyvalue);
		}else {
			return "";
		}								
	}		
}
