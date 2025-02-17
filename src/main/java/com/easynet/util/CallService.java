package com.easynet.util;

import java.util.Enumeration;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import com.easynet.bean.GetRequestUniqueData;
import com.easynet.configuration.PropConfiguration;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.LoggerImpl;

@Component
public class CallService extends CommonBase{

	@Autowired
	ServiceIdConfiguration serviceIdConfiguration;
	
	@Autowired
	RestTemplate restTemplate;
		
	@Autowired
	private PropConfiguration propConfiguration;

    @Autowired
	private GetRequestUniqueData getRequestUniqueData;
    
    static Logger LOGGER=LoggerFactory.getLogger(CallService.class);
    
    /****
     *This method used for call other API. 
     *@param ls_requestData - Request Data for other API in JSON format.
     *@param ls_action - Action name of other API.
     *@author Sagar Umate
     *@date 15/12/2021
     *@exception If any exception generated then it handle by this method and return in JSON format. 
     * 
     ***/
	public String doServiceRequest(String ls_requestData,String ls_action,Object ... otherParameter){

		HttpStatus httpStatus;
		String ls_actualResData="";
		String ls_errorData="";
		LoggerImpl loggerImpl =new LoggerImpl();
		String ls_langResCodeMsg="";
		JSONObject requestCommonDataJson;
		
		try {				
			HttpServletRequest request=getRequestUniqueData.getRequest();
			requestCommonDataJson=getRequestUniqueData.getRequestCommonDataJson();
			
			//set the common parameter
			JSONObject requestData=new JSONObject(ls_requestData);								
			requestData.put("BROWSER", requestCommonDataJson.optString("BROWSER"));
			requestData.put("LONGITUDE", requestCommonDataJson.optString("LONGITUDE"));
			requestData.put("DEVICE", requestCommonDataJson.optString("DEVICE"));
			requestData.put("USER_ID", requestCommonDataJson.optString("USER_ID"));
			requestData.put("DEVICE_IP", requestCommonDataJson.optString("DEVICE_IP"));
			requestData.put("DISPLAY_LANGUAGE", requestCommonDataJson.optString("DISPLAY_LANGUAGE"));
			requestData.put("IMEI", requestCommonDataJson.optString("IMEI"));
			requestData.put("DEVICE_MAC", requestCommonDataJson.optString("DEVICE_MAC"));
			requestData.put("DEVICE_IPV6", requestCommonDataJson.optString("DEVICE_IPV6"));
			requestData.put("DEVICE_NM", requestCommonDataJson.optString("DEVICE_NM"));
			requestData.put("CHANNEL", requestCommonDataJson.optString("CHANNEL"));
			requestData.put("DEVICE_USER_NM", requestCommonDataJson.optString("DEVICE_USER_NM"));
			requestData.put("VERSION", requestCommonDataJson.optString("VERSION"));
			requestData.put("DEVICE_ID", requestCommonDataJson.optString("DEVICE_ID"));
			requestData.put("LATITUDE", requestCommonDataJson.optString("LATITUDE"));
			requestData.put("UNIQUE_REF_ID", requestCommonDataJson.optString("UNIQUE_REF_ID"));
			requestData.put("MAIN_REQ_FROM_ACTION", requestCommonDataJson.optString("ACTION"));
			requestData.put("ACTION",ls_action);
						
			String ls_url=serviceIdConfiguration.getPropertyValue("ACTION."+ls_action+".url");
					
			ls_url=ls_url+"/"+ls_action;
			
			/*get object to store header values comes in request*/
			HttpHeaders headers = new HttpHeaders(); 
			loggerImpl.debug(LOGGER,"Getting header details.", "IN:doServiceRequest");
			
			Enumeration<String> request_header = request.getHeaderNames();
	
			while (request_header.hasMoreElements()) {
				// add the names of the request headers into the list
				String header_name = request_header.nextElement();				
				headers.set(header_name,request.getHeader(header_name));				
			}
	
			Object overRideHeaders=common.getDataAtIndex(0,otherParameter);
			if(overRideHeaders!=null) {
				JSONObjectImpl overRideHeaderjson=(JSONObjectImpl)overRideHeaders;
								
				Set<String> jsonKeyData=overRideHeaderjson.keySet();
				
				for (String keyName : jsonKeyData) {						
					headers.set(keyName, overRideHeaderjson.getString(keyName));					
				}
			}
			
			//set action in header for used it in filters.
			headers.set("ACTION", ls_action);
			headers.set("REQUEST_FROM","INTERNAL");
			headers.set("X-Real-IP","");//set null then system will get the requesting IP.
			
			HttpEntity<String> httpRequestObj = new HttpEntity<>(requestData.toString(),headers);
	
			try{			
				loggerImpl.debug(LOGGER,"Calling API for action "+ls_action, "IN:doServiceRequest");
				/*Call the API and get response data*/
				ResponseEntity<String> result =restTemplate.exchange(ls_url,HttpMethod.POST,httpRequestObj,String.class);
				
				loggerImpl.debug(LOGGER,"API Called for action "+ls_action, "IN:doServiceRequest");
				httpStatus=result.getStatusCode();
				
				if(HttpStatus.OK==httpStatus){
					/*get the response body of request*/									
					ls_actualResData=result.getBody();								
				}else {
					ls_langResCodeMsg=propConfiguration.getResponseCode("common."+httpStatus.value());					
					ls_actualResData = common.ofGetErrDataJsonObject(ls_langResCodeMsg,
							propConfiguration.getMessageOfResCode("common.title."+ls_langResCodeMsg, "Alert.",""),
							propConfiguration.getMessageOfResCode("common."+httpStatus.value(),"","(ENP666)."),
							"Another API return the error.-"+httpStatus.toString(),						
							propConfiguration.getMessageOfResCode("common."+httpStatus.value(),"","(ENP666).","","EN","Y"),
							CommonBase.ST9990, "R");
				}				
			}catch (HttpStatusCodeException ex){	
				ls_errorData = common.ofGetTotalErrString(ex,"HttpStatusCodeException in Calling API.");
				loggerImpl.error(LOGGER,ls_errorData,"IN:doServiceRequest");
				
				httpStatus=ex.getStatusCode();
				ls_errorData=ex.getResponseBodyAsString(); 
				
				ls_actualResData= common.ofGetErrDataJsonArray(CommonBase.ST999,"Alert.",
						propConfiguration.getMessageOfResCode("common.exception."+CommonBase.ST9990,"","(ENP667)"),
						String.valueOf(httpStatus.value())+"-"+ls_errorData,
						propConfiguration.getMessageOfResCode("common.exception."+CommonBase.ST9990,"","(ENP667)","","EN","Y"), CommonBase.ST9990, "R");						
			}
		}catch (Exception exception){	
			ls_errorData = common.ofGetTotalErrString(exception,"Exception in Calling API.");
			loggerImpl.error(LOGGER,ls_errorData,"IN:doServiceRequest");
			
			ls_actualResData= common.ofGetErrDataJsonArray(CommonBase.ST999,"Alert.",
					propConfiguration.getMessageOfResCode("common.exception."+CommonBase.ST9990,"","(ENP668)"),
					exception.getMessage(),
					propConfiguration.getMessageOfResCode("common.exception."+CommonBase.ST9990,"","(ENP668)","","EN","Y"),
					CommonBase.ST9990, "R");						
		}					
		return ls_actualResData;
	}
}