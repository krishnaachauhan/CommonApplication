package com.easynet.util;

import java.util.Iterator;
import java.util.Objects;
import javax.xml.bind.PropertyException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import com.easynet.bean.GetApplicationData;
import com.easynet.configuration.ReloadablePropertySourceConfig;
import com.easynet.impl.LoggerImpl;
import com.easynet.listener.RefreshConfigData;

@RefreshScope
@Component
public class MaskedColumnConfiguration{
	
	@Autowired
	CommonBase commonBase;
	
	private Logger LOGGER=LoggerFactory.getLogger(MaskedColumnConfiguration.class);
	
	private String LS_CONFIG_PATH=ReloadablePropertySourceConfig.configurableEnvironment.getProperty("COMM_CONFIG_PATH");
	
	private PropertiesConfiguration propertiesConfiguration;

	private JSONObject replaceKeyConfigJson=null;

	public MaskedColumnConfiguration() throws PropertyException {
		
		LoggerImpl loggerImpl=new LoggerImpl();
		try {
			
			PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();	            
			propertiesConfiguration.setDelimiterParsingDisabled(true);
			propertiesConfiguration.setDefaultListDelimiter('!');
			propertiesConfiguration.setListDelimiter('!');
			propertiesConfiguration.load(LS_CONFIG_PATH+"maskedcolumn.properties");
			
			this.propertiesConfiguration=propertiesConfiguration;
		}catch (Exception exception) {
			commonBase.getExceptionMSg(exception, LOGGER, loggerImpl, "IN:MaskedColumnConfiguration", "(ENP837)" ,"Error at the time of refresh MaskedColumnConfiguration data.",null);
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
	
	public Iterator<String> getPropertiesKeys() {
		return this.propertiesConfiguration.getKeys();
	}
	
	/**
	 *This method return the JSON object of masked column configuration and 
	 *	if not found the 1st set the object and then return it. 
	 * */
	public JSONObject getMaskedColumnProperties(){

		//check the null object and also check last modified time of file.
		if(this.replaceKeyConfigJson==null){					
			this.replaceKeyConfigJson=setMaskedColumnProperties();
			return this.replaceKeyConfigJson;
		}else{
			return this.replaceKeyConfigJson;
		}			
	}

	/**
	 *This method load the properties file into JSON object and return it. 
	 * */
	public JSONObject setMaskedColumnProperties(){

		JSONObject replaceKeysConfigJson=new JSONObject();
		LoggerImpl loggerImpl= new LoggerImpl();		
		
		try {								
			this.getPropertiesKeys().forEachRemaining(keyName->{
				replaceKeysConfigJson.put(keyName,this.getPropertyValue(keyName));
			});
			
			this.replaceKeyConfigJson=replaceKeysConfigJson;
		}catch(Exception exception) {
			loggerImpl.error(LOGGER,common.ofGetTotalErrString(exception,""),"IN:setMaskedColumnProperties");
		}
		return this.replaceKeyConfigJson;
	}
}
