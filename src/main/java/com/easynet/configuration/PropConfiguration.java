package com.easynet.configuration;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Objects;
import javax.xml.bind.PropertyException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import com.easynet.bean.GetRequestUniqueData;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.CommonBase;
import com.easynet.util.MaskedColumnConfiguration;
import com.easynet.util.common;

@RefreshScope
@Component
//@PropertySource(value = "file:${COMMON_PROJECT_FOLDER_PATH_KEY}responsecode_mapping.properties", factory = ReloadablePropertySourceFactory.class)
//@PropertySource(value ="${CONFIG_PATH}responsecode_mapping.properties")
public class PropConfiguration extends CommonBase{

	private String ls_bnMessage="\u09AC\u09B0\u09CD\u09A4\u09AE\u09BE\u09A8\u09C7 \u09AA\u09B0\u09BF\u09B7\u09C7\u09AC\u09BE \u09B0\u0995\u09CD\u09B7\u09A3\u09BE\u09AC\u09C7\u0995\u09CD\u09B7\u09A3\u09C7\u09B0 \u0985\u09A7\u09C0\u09A8\u09C7 \u09B0\u09AF\u09BC\u09C7\u099B\u09C7 \u09A4\u09BE\u0987 \u09A6\u09AF\u09BC\u09BE \u0995\u09B0\u09C7 \u09AA\u09B0\u09C7 \u099A\u09C7\u09B7\u09CD\u099F\u09BE \u0995\u09B0\u09C1\u09A8.";
	private String ls_enMessage="Currently service is under maintenance so please try later.";
	private String ls_gujMessage="\u0AB9\u0ABE\u0AB2\u0AAE\u0ABE\u0A82 \u0AB8\u0AC7\u0AB5\u0ABE \u0A9C\u0ABE\u0AB3\u0AB5\u0AA3\u0AC0 \u0AB9\u0AC7\u0AA0\u0AB3 \u0A9B\u0AC7 \u0AA4\u0AC7\u0AA5\u0AC0 \u0A95\u0AC3\u0AAA\u0ABE \u0A95\u0AB0\u0AC0\u0AA8\u0AC7 \u0AAA\u0A9B\u0AC0\u0AA5\u0AC0 \u0AAA\u0ACD\u0AB0\u0AAF\u0ABE\u0AB8 \u0A95\u0AB0\u0ACB.";		
	
	private String LS_CONFIG_PATH=ReloadablePropertySourceConfig.configurableEnvironment.getProperty("COMM_CONFIG_PATH");	
	private PropertiesConfiguration propertiesConfiguration;

	private Logger LOGGER=LoggerFactory.getLogger(PropConfiguration.class);

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private GetRequestUniqueData getRequestUniqueData;
	
	
	public PropConfiguration() throws PropertyException {
		LoggerImpl loggerImpl=new LoggerImpl();
		
		try {
			PropertiesConfiguration propertiesConfiguration=new PropertiesConfiguration();			
			propertiesConfiguration.setDefaultListDelimiter('!');
			propertiesConfiguration.setListDelimiter('!');			
			propertiesConfiguration.load(LS_CONFIG_PATH+"responsecode_mapping.properties");
			this.propertiesConfiguration = propertiesConfiguration;	            
		}catch (Exception exception) {
			getExceptionMSg(exception, LOGGER, loggerImpl, "IN:PropConfiguration", "(ENP838)" ,"Error at the time of refresh PropConfiguration data.",null);
			throw new PropertyException(exception);
		} 
	}	

	public String getPropertyValue(String keyName, String defaultValue) {
		Object keyvalue=this.propertiesConfiguration.getProperty(keyName);
		
		if(!Objects.isNull(keyvalue)){
			return String.valueOf(keyvalue);
		}else {
			return defaultValue;
		}								
	}	
	
	public String getDefaultMsg(String as_langCode ) {	
		
		if("BN".equalsIgnoreCase(as_langCode)){					
			try {
				return new String(ls_bnMessage.getBytes("UTF-8"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return ls_enMessage;
			}			
		}else {
			return ls_enMessage;
		}
	}		

	/**
	 * This method return the value of given response code.
	 * 
	 * @param keyName -response code name with api short name.
	 * @param object  -vararag of object type.<br>
	 *                1.Source Type. A).A-Ababil. B).F-Finacle. C).C-common
	 *
	 * @return This method return the response code value.<br>
	 *         value of key found other than 99 or 999 then return default 999.
	 * 
	 **/
	public String getResponseCode(String keyName, Object... object) {
		String ls_value = "";
		String ls_returnValue = "";
		String ls_sourceType = "F";
		String ls_ReponseCode = "";
		String ls_commonKeyName = "";

		// get the data for specific api keys
		if (!keyName.startsWith("common")) {

			ls_sourceType = String.valueOf(common.getDataAtIndex(0, object));

			if ("A".equals(ls_sourceType)) {
				ls_sourceType = "Ababil.";
				ls_commonKeyName = "common." + ls_sourceType;
			} else if ("C".equals(ls_sourceType)) {
				ls_sourceType = "common.";
				ls_commonKeyName = "common.";
			} else {
				ls_sourceType = "Finacle.";
				ls_commonKeyName = "common." + ls_sourceType;
			}

			ls_value = getPropertyValue(ls_sourceType + keyName, "");

			// if specific key not found then get data for common keys.
			if ("".equals(ls_value)) {

				ls_ReponseCode = keyName.substring(keyName.lastIndexOf(".") + 1);
				ls_commonKeyName = ls_commonKeyName + ls_ReponseCode;
				ls_value = getPropertyValue(ls_commonKeyName, ST999);
			}
		} else {
			ls_value = getPropertyValue(keyName, ST999);
		}
				
		 if(ST99.equals(ls_value) || ST999.equals(ls_value)){
			 ls_returnValue=ls_value; 
		 }else{ 
			 ls_returnValue=ST999; 
		 }		 

		return ls_returnValue;
	}

	/**
	 * This method return the respective language message of given response code.
	 * 
	 * @param keyName      -response code name with api short name.if key not found
	 *                     then return the common message.
	 * @param defaultValue -default value if key not found.
	 * @param object       -this is vararg type object. <br>
	 *                     1.1st Argument is appendMessage Value.<br>
	 *                     2.2nd Source Type<br>
	 *                     		A)A-Ababil. B)F-Finacle. C)C-common.
	 *                     3.3rd argument is language code.<br>
	 *                     	If language code is empty then it will take from GetRequestUniqueData object.<br>
	 *                     4.for append MSG code value by default in parameter pass as append value.
	 *                     	
	 * @return This method return the message if key not found then return default
	 *         message.
	 * 
	 **/
	public String getMessageOfResCode(String keyName, String defaultValue, @Nullable Object... object) {
		String ls_returnValue = "";
		String ls_appendMsgValue = null;
		String ls_sourceType = "F";
		String ls_ReponseCode = "";
		String ls_commonKeyName = "";
		String ls_langCode="";
		String ls_defAppendCodeValue="";
		String mesIdentifier=ReloadablePropertySourceConfig.configurableEnvironment.getProperty("MsgIdentifier");
		
		ls_langCode=common.getStringDataAtIndex(2, object);
		ls_defAppendCodeValue=common.getStringDataAtIndex(3, object);
		
		if(StringUtils.isBlank(ls_langCode)) {
			ls_langCode = getRequestUniqueData.getLangCode();			
		}
		
		if (StringUtils.isBlank(ls_langCode)) {
			ls_langCode = "EN";
		}

		if (!keyName.startsWith("common")) {

			ls_sourceType =common.getStringDataAtIndex(1, object);
			if ("A".equals(ls_sourceType)) {
				ls_sourceType = "Ababil.";
				ls_commonKeyName = "common." + ls_sourceType;
			} else if ("C".equals(ls_sourceType)) {
				ls_sourceType = "common.";
				ls_commonKeyName = "common.";
			} else {
				ls_sourceType = "Finacle.";
				ls_commonKeyName = "common." + ls_sourceType;
			}

			ls_returnValue = messageSource.getMessage(ls_sourceType + keyName, null, "",getLocalObj(ls_langCode));

			// if specific key not found then get data for common keys.
			if ("".equals(ls_returnValue)) {

				ls_ReponseCode = keyName.substring(keyName.lastIndexOf(".") + 1);
				ls_commonKeyName = ls_commonKeyName + ls_ReponseCode;
				ls_returnValue = messageSource.getMessage(ls_commonKeyName, null, defaultValue,getLocalObj(ls_langCode));
			}
		} else {
			ls_returnValue = messageSource.getMessage(keyName, null, defaultValue,getLocalObj(ls_langCode));
		}

		//if no message found the return default configured msg..
		if ("".equals(ls_returnValue)) {
			ls_returnValue = messageSource.getMessage("common.defaultMsg",null,getDefaultMsg(ls_langCode),getLocalObj(ls_langCode));
		}
					
		//check and get the values
		Object appendObject=common.getDataAtIndex(0, object);
		if(appendObject!=null){ls_appendMsgValue = String.valueOf(appendObject); }				

		if (StringUtils.isNotBlank(ls_appendMsgValue)&& keyName.trim().endsWith(ST999) && ("Y".equals(mesIdentifier)||"Y".equals(ls_defAppendCodeValue))){

			if (ls_returnValue.trim().endsWith(".")) {
				ls_returnValue = ls_returnValue.substring(0, ls_returnValue.length() - 1);
				ls_returnValue = ls_returnValue.trim() + " " + ls_appendMsgValue + ".";
			}else {
				ls_returnValue = ls_returnValue.trim() + " " + ls_appendMsgValue + ".";
			}
			
		}

		return ls_returnValue;
	}
	
	/**
	 *This method is used to get the language code.
	 * @param ls_langCode
	 * @return ls_langCode
	 */		
  public Locale getLocalObj(String ls_langCode) {
	  
	return new Locale(ls_langCode);
	
}
	

//       private Locale getLocalObj(String ls_langCode) {
//		
//		if("EN".equalsIgnoreCase(ls_langCode)){
//			if(this.enLocalObj ==null) {this.enLocalObj=new Locale("en");}
//			return this.enLocalObj;
//		}else if("GUJ".equalsIgnoreCase(ls_langCode)) {
//			if(this.gujLocalObj ==null) {this.gujLocalObj=new Locale("guj");}
//			return this.gujLocalObj;
//		}else if("SP".equalsIgnoreCase(ls_langCode)) {
//			if(this.spLocalObj ==null) {this.spLocalObj=new Locale("sp");}
//			return this.spLocalObj;
//		}else if("FRENCH".equalsIgnoreCase(ls_langCode)) {
//			if(this.frenchLocalObj ==null) {this.frenchLocalObj=new Locale("french");}
//			return this.frenchLocalObj;
//		}else {
//			if(this.enLocalObj ==null) {this.enLocalObj=new Locale("en");}
//			return this.enLocalObj;
//		}
//    }
}