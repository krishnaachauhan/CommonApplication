package com.easynet.configuration;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import com.acute.dao.crypto.DataEncrDecrImpl;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.CommonBase;
import com.easynet.util.GetFileConfig;

@RefreshScope
@Component
public class ConfigCredentials {

	private Properties properties=null;

	@Autowired
	private CommonBase commonBase;	


	private Logger LOGGER=LoggerFactory.getLogger(ConfigCredentials.class);

	public ConfigCredentials() throws Exception{
		LoggerImpl loggerImpl=new LoggerImpl();

		try {
			String LS_CONFIG_PATH=ReloadablePropertySourceConfig.configurableEnvironment.getProperty("COMM_CONFIG_PATH");
			String ls_PropertyFilePath=GetFileConfig.GetFileFromConfigServer(LS_CONFIG_PATH, "Credentials.properties");
			properties = DataEncrDecrImpl.ofGetInstance().ofGetDecrData(ls_PropertyFilePath);

			//delete the file after load in properties object.
			GetFileConfig.doDeleleConfigFile(ls_PropertyFilePath);		

		}catch(Exception exception) {
			commonBase.getExceptionMSg(exception, LOGGER, loggerImpl, "IN:ConfigCredentials", "(ENP839)" ,"Error at the time of refresh ConfigCredentials data.",null);
			throw new Exception(exception);
		}  
	}

	public Properties getProperties() {
		return properties;
	}

	public String getProperty(String key) {
		return getProperties().getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return getProperties().getProperty(key,defaultValue);
	}
}
