package com.easynet.util;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.easynet.bean.GetApplicationData;
import com.easynet.configuration.PropConfiguration;
import com.easynet.impl.LoggerImpl;

@Controller
public class RestAPICaller extends CommonBase{

	@Autowired
	apiCall1 apiCall1;

	@Autowired
	GetApplicationData getApplicationData;

	Logger LOGGER=LoggerFactory.getLogger(RestAPICaller.class);
	
	@Autowired
	PropConfiguration propConfiguration;
	
	/***
	 *This method used to call only ABABIl API.
	 *This method 1st check the token ,if token is expired the get token 1st and then call actual API.
	 *@param apiUrl 	- API Endpoint URL.
	 *@param requestData- API request Data.
	 *@param methodType	- Type of API call.<br>default value will be post.
	 * 
	 * */
	public String ofCallAbabilAPI(String apiUrl, String requestData,String ... asMethodType ){

		String ls_tokenData;
		JSONObject tokenDataJson;
		String ls_statusCD="";
		String headerData="";
		String ls_reponseData="";
		LoggerImpl loggerImpl = null;
		
		JSONObject responseDataJson;
		String ls_actualResponse="";
		JSONArray actualResponseJlist=null; 
		String ls_methodType="POST";
		
		try {
			loggerImpl =new LoggerImpl();
			
			//first verify the token 
			if(getApplicationData.isTokenExpire()){
				
				String req_header="{\"Content-Type\":\"application/json\"}";
				ls_tokenData=apiCall1.GenarateAccessToken("ABABIL_API>AUTHORIZATION",req_header,"A");

				tokenDataJson=common.ofGetJsonObject(ls_tokenData);
				ls_statusCD=tokenDataJson.getString("STATUS");
				if(isSuccessStCode(ls_statusCD)){

					//set the json data.
					getApplicationData.setData(tokenDataJson);

					headerData=tokenDataJson.toString();

				}else {
					return ls_tokenData; 
				}		
			}else {
				headerData=getApplicationData.getAuthTokenJsonObj().toString();			
			}
			
			ls_methodType=(String)common.getDataAtIndex(0,asMethodType);
			if(StringUtils.isBlank(ls_methodType)) {
				ls_methodType="POST";
			}			
			
			//call api for get the details.
			if("GET".equals(ls_methodType)){
				ls_reponseData=apiCall1.GetApi(apiUrl,headerData,"A",requestData);
			}else {
				ls_reponseData=apiCall1.PostApi(ls_methodType, apiUrl, requestData, headerData,"A");				
			}
						
			responseDataJson=common.ofGetJsonObject(ls_reponseData);
			String ls_responseCode=responseDataJson.getString("RESPONSECODE");
			
			//SET the status 100 for fund transfer.
			if(isSuccess200ResCode(ls_responseCode)){
				responseDataJson.put("RESPONSECODE",ST100);
			}
			
			Object responseObject=responseDataJson.get("RESPONSE");
			
			//if string then only set change data.
			if(responseObject instanceof String){
				
				ls_actualResponse=(String)responseObject;
				
				if(ls_actualResponse.trim().startsWith("{")){
					actualResponseJlist=new JSONArray().put(new JSONObject(ls_actualResponse));
				}else {
					actualResponseJlist=new JSONArray(ls_actualResponse);
				}
				
				responseDataJson.put("RESPONSE", actualResponseJlist);				
			}					
			return responseDataJson.toString();	
			
		}catch(Exception exception) {
			return getExceptionMSg(exception,LOGGER, loggerImpl, "IN:ofCallAbabilAPI", "(ENP374)", null,"A");					
		}
	}
	
	/***
	 *This method used to call common API with token.
	 *This method 1st check the token ,if token is expired the get token 1st and then call actual API.
	 *@param apiUrl - API EndPoint URL.
	 *@param requestData  - API request Data.
	 *@param methodType - Method Type of request
	 *@param UploadFile - If want to upload file then "Y" else "N".
	 * */
	public String doCallCommonAPI(String apiUrl, String requestData,String methodType,String UploadFile,String headers){

		JSONObject tokenDataJson;
		String ls_statusCD="";
		String headerData="";
		String ls_reponseData="";
		String ls_apiURL="";
		String ls_apiMethodType="";
		String ls_authUname="";
		String ls_authPass="";
		String ls_authorizationJson="";
		String ls_accessTokenData="";
		
		LoggerImpl loggerImpl = null;
		
		JSONObject responseDataJson;
		String ls_actualResponse="";
		JSONArray actualResponseJlist=null; 

		try {
			loggerImpl =new LoggerImpl();
			
			JSONObject headerJson =new JSONObject(headers); 
			
			
			//first verify the token 
			if(getApplicationData.isCommonTokenExpired()){
				
				ls_apiURL = readXML.getXmlData("root>CUST_360>AUTHORIZATION>OAUTH_URL");
				ls_apiMethodType = readXML.getXmlData("root>CUST_360>AUTHORIZATION>METHOD_TYPE");
				ls_authUname=readXML.getXmlData("root>CUST_360>AUTHORIZATION>USER_NAME");
				ls_authPass=readXML.getXmlData("root>CUST_360>AUTHORIZATION>PASSWORD");

				//Make request data of authorization.
				JSONObject ls_authorizationReqJson=new JSONObject();
				ls_authorizationReqJson.put("username",ls_authUname);
				ls_authorizationReqJson.put("password",ls_authPass);

				/*Call API for get Access token*/
				ls_authorizationJson = apiCall1.PostApi(ls_apiMethodType, ls_apiURL, ls_authorizationReqJson.toString(), null,"C");			
								
				tokenDataJson=common.ofGetJsonObject(ls_authorizationJson);
				ls_statusCD=tokenDataJson.getString("STATUS");
				
				if(isSuccessStCode(ls_statusCD)){

					/*get access token data from response key*/
					ls_accessTokenData = tokenDataJson.getJSONArray("RESPONSE").getJSONObject(0).getString("token");
					
					//set the json data.
					getApplicationData.setCommonTokenData(ls_accessTokenData);
					
					headerData=getApplicationData.getCommonAuthTokenJson().toString();
					headerJson.put("ACCESS_TOKEN", getApplicationData.getCommonAuthTokenJson().getString("ACCESS_TOKEN"));
					headerData = headerJson.toString();
				}else {
					return ls_authorizationJson; 
				}		
			}else {
				headerData=getApplicationData.getCommonAuthTokenJson().toString();
				headerJson.put("ACCESS_TOKEN", getApplicationData.getCommonAuthTokenJson().getString("ACCESS_TOKEN"));
				headerData = headerJson.toString();
			}

			if(StringUtils.isBlank(methodType)){methodType="POST";}
			
			if("Y".equals(UploadFile)){
				ls_reponseData=apiCall1.PostFileUploadApi(apiUrl,new JSONObject(requestData),headerData);
			}else{
				//call API for get the details.
				ls_reponseData=apiCall1.PostApi(methodType, apiUrl, requestData, headerData,"C");
			}
			
			responseDataJson=common.ofGetJsonObject(ls_reponseData);
			String ls_responseCode=responseDataJson.getString("RESPONSECODE");
			
			//SET the status 100 for fund transfer.
			if(isSuccess200ResCode(ls_responseCode)){
				responseDataJson.put("RESPONSECODE",ST100);
			}
			
			Object responseObject=responseDataJson.get("RESPONSE");
			
			//if string then only set change data.
			if(responseObject instanceof String){
				
				ls_actualResponse=(String)responseObject;
				
				if(ls_actualResponse.trim().startsWith("{")){
					actualResponseJlist=new JSONArray().put(new JSONObject(ls_actualResponse));
				}else {
					actualResponseJlist=new JSONArray(ls_actualResponse);
				}
				
				responseDataJson.put("RESPONSE", actualResponseJlist);				
			}					
			return responseDataJson.toString();	
			
		}catch(Exception exception) {
			return getExceptionMSg(exception, LOGGER, loggerImpl, "IN:doCallCommonAPI", "(ENP575)" );		
		}
	}
}
