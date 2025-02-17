package com.easynet.bean;

import java.util.Base64;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import com.easynet.util.MaskedColumnConfiguration;

@Component
@Service
@ApplicationScope
public class GetApplicationData implements ApplicationContextAware {

	private static 	ApplicationContext context;
	private String 	access_token=""; 
	private String 	refresh_token="";
	private int 	expire_in=0;
	private String 	scope="";
	private long	expireTokenTime=0;
	private String 	fToken="";
	private long	commonExpireTokenTime=0;
	private String 	commonAccess_token=""; 
	private int 	commonExpire_in=0;

	private JSONObject authTokenJsonObj;
	private JSONObject commonAuthTokenJson;
	private JSONObject replaceKeyConfigJson=null;
	private long lastFileModTime=System.currentTimeMillis();

	Logger LOGGER=LoggerFactory.getLogger(GetApplicationData.class);
	
	@Autowired
	MaskedColumnConfiguration maskedColumnConfiguration;

	/**This method set all data of detail of token*/
	public void setData(JSONObject data){

		access_token	=data.getString("ACCESS_TOKEN");	
		refresh_token	=data.getString("REFRESH_TOKEN");
		expire_in		=data.getInt("EXPIRE_TOKEN");

		//set the json data for future used.
		authTokenJsonObj=data;

		// set the expiry time 
		Calendar calender=Calendar.getInstance();
		calender.add(calender.SECOND, expire_in - 10);

		expireTokenTime=calender.getTimeInMillis();
	}

	/**This method set detail of common token.*/
	public void setCommonTokenData(String data) throws Exception{

		String[] tokenarr = data.split("[.]");
		if (tokenarr.length >= 2){
			JSONObject joJsonObject = new JSONObject(new String(Base64.getDecoder().decode(tokenarr[1])));
			Long expired = joJsonObject.getLong("exp") - 10;  

			//set the expire time.			
			this.commonAccess_token=data;
			this.commonExpireTokenTime=expired;  
			
			/*Set the access token data */
			JSONObject httpHeadersJson = new JSONObject();
			httpHeadersJson.put("ACCESS_TOKEN", data);
			this.commonAuthTokenJson=httpHeadersJson;					
		}		
	}

	/**This method are used to know the token is expired or not for ABABIl API.*/
	public boolean isTokenExpire() {		
		return System.currentTimeMillis() > expireTokenTime? true: false;		
	}

	/*For verify the token in expired or not.*/
	public boolean isCommonTokenExpired(){
		Long expired=this.getCommonExpireTokenTime();    			
		Long current = System.currentTimeMillis()/1000;
		return (current < expired)? false :true;		    		    		
	}

	public String getfToken() {
		return fToken;
	}
	public void setfToken(String fToken) {
		this.fToken = fToken;
	}
	public String getAccess_token() {
		return access_token;
	}
	public String getRefresh_token() {
		return refresh_token;
	}
	public int getExpire_in() {
		return expire_in;
	}
	public String getScope() {
		return scope;
	}
	public long getExpireTokenTime() {
		return expireTokenTime;
	}

	public JSONObject getAuthTokenJsonObj() {
		return authTokenJsonObj;
	}

	public long getCommonExpireTokenTime() {
		return commonExpireTokenTime;
	}

	public String getCommonAccess_token() {
		return commonAccess_token;
	}

	public JSONObject getCommonAuthTokenJson() {
		return commonAuthTokenJson;
	}

	public void setCommonAuthTokenJson(JSONObject commonAuthTokenJson) {
		this.commonAuthTokenJson = commonAuthTokenJson;
	}

	public synchronized String manageFtoken(String as_fToken) {

		if(StringUtils.isBlank(as_fToken)){
			return getfToken();
		}else {
			//set the F-token
			setfToken(as_fToken);
			return getfToken();
		}		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException{

		// store ApplicationContext reference to access required beans later on		
		GetApplicationData.context = applicationContext;		
	}

	public static <T extends Object> T getBean(Class<T> beanClass){
		return context.getBean(beanClass);
	}

	public JSONObject getReplaceKeyConfigJson() {
		return replaceKeyConfigJson;
	}

	/**
	 *This method return the JSON object of masked column configuration and 
	 *	if not found the 1st set the object and then return it. 
	 * */
	public JSONObject getMaskedColumnProperties(){
		return maskedColumnConfiguration.getMaskedColumnProperties();		
		}

//	/**
//	 *This method load the properties file into JSON object and return it. 
//	 * */
//	public JSONObject setMaskedColumnProperties(){
//
//		Properties prop = new Properties();
//		InputStream in=null;
//		JSONObject replaceKeysConfigJson=new JSONObject();
//		LoggerImpl loggerImpl= new LoggerImpl();
//		String projectFolderpath="";
//		
//		try {		
//			try { 
//				projectFolderpath=ConfigurationValues.COMMON_PROJECT_FOLDER_PATH;//ReloadablePropertySourceConfig.configurableEnvironment.getProperty("PROJECT_FOLDER_PATH");
//				File maskedPropertyFile=new File(projectFolderpath+"maskedcolumn.properties");
//				//in=new ClassPathResource(File.separatorChar +"maskedcolumn.properties").getInputStream();		
//				in=new FileInputStream(maskedPropertyFile);
//				prop.load(in);
//			}catch(Exception err) {	
//				loggerImpl.error(LOGGER,common.ofGetTotalErrString(err,"Error generated at the time of getting maskedcolumn property file."),"IN:setMaskedColumnProperties");
//			}
//
//			//set values of maskedcolumn.properties file into JSON object. 			
//			prop.forEach((k, v) -> { 
//				replaceKeysConfigJson.put((String)k, (String)v);				
//			});
//		}catch(Exception exception) {
//			loggerImpl.error(LOGGER,common.ofGetTotalErrString(exception,""),"IN:setMaskedColumnProperties");
//		}finally {
//			try {
//				in.close();
//			} catch (IOException exception) {
//				loggerImpl.error(LOGGER,common.ofGetTotalErrString(exception,""),"IN:setMaskedColumnProperties");
//			}
//		}		
//		return replaceKeysConfigJson;
//	}
}