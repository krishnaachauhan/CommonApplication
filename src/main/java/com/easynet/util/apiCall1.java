/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.easynet.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.easynet.bean.GetApplicationData;
import com.easynet.bean.GetRequestUniqueData;
import com.easynet.configuration.PropConfiguration;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.LoggerImpl;

@Component
public class apiCall1 extends CommonBase{

	@Autowired
	GetRequestUniqueData getRequestUniqueData;
	
	static Logger logger=LoggerFactory.getLogger(apiCall1.class);

	@Value("${client.api.connectionTimeout:30}")
	private int connectionTimeout;

	@Value("${client.api.readTimeout:120}")
	private int readTimeout;
	
	@Autowired
	PropConfiguration propConfiguration;
	
	@Autowired
	GetApplicationData applicationData;
	
	/**
	 * @param apiUrl       -pass the url link.
	 * @param httpHeaderss - String format json object to set into request header.
	 * @return -Return response data in string format.
	 */
	public String GetApi(String apiUrl, String httpHeaderss) {

		String responseString = "";
		String outputString = "";
		String AccessToken = "";
		JSONObject headerJson = null;
		LoggerImpl loggerImpl=null;
		JSONArray responseJlist=null;

		try {
			loggerImpl=new LoggerImpl();
			if (httpHeaderss != null && !"".equals(httpHeaderss)) {
				headerJson = new JSONObject(httpHeaderss);
				AccessToken = headerJson.optString("ACCESS_TOKEN", "");
			}

			URL url = new URL(apiUrl);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;

			// add reuqest header
			httpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty("Authorization", "Bearer " + AccessToken);
			httpConn.setConnectTimeout(connectionTimeout*1000);
			httpConn.setReadTimeout(readTimeout*1000);
			httpConn.setDoOutput(true);

			loggerImpl.debug(logger,"Calling API request.", "IN:GetApi");
			
			if (httpConn.getResponseCode() != 200) {
				
				//get error message of API and return error in response.
				InputStream errorObjectStream = httpConn.getErrorStream();
				BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorObjectStream));

				StringBuilder errorRespoceData = new StringBuilder();
				try {
					String line = null;
					while ((line = errorReader.readLine()) != null) {
						errorRespoceData.append(line);
					}
				}finally {

					errorReader.close();
					httpConn.disconnect();
				}	
				
				outputString=ofGetFailedMSg("common.exception",ST999,errorRespoceData.toString(),"(ENP713)");								
			} else {

				InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
				BufferedReader in = new BufferedReader(isr);

				// Write the message response to a String.
				while ((responseString = in.readLine()) != null) {
					outputString = outputString + responseString;
				}

				if(outputString.trim().startsWith("{")){
					responseJlist=new JSONArray().put(new JSONObject(outputString));
				}else if(outputString.trim().startsWith("[")){
					responseJlist=new JSONArray(outputString);
				}else{
					responseJlist=new JSONArray().put(outputString);
				}
				
				/* by sagar for set data into json format for identify response data. */
				//outputString = common.ofGetResponseJson(responseJlist,"","",ST0,"G","").toString();
				outputString = ofGetResponseJson(responseJlist,"","",ST0,"G","").toString();
				
			}
			
			loggerImpl.debug(logger,"Response generated.", "IN:GetApi");
			httpConn.disconnect();
			
		} catch (SocketTimeoutException ex) {
			outputString= getTimeOutExceptionMSg(ex,logger,loggerImpl, "IN:GetApi", "(ENP003)" );		
		} catch (IOException e) {
			outputString= getExceptionMSg(e,logger,loggerImpl, "IN:GetApi", "(ENP004)" );
		
		} catch (Exception exception) {			
			outputString= getExceptionMSg(exception,logger,loggerImpl, "IN:GetApi", "(ENP005)");
			
		}
		return outputString;
	}

	/**
	 * This method call the API with GET type using urlconnection class and write the
	 *  API process log and performance time.
	 *  Also check response code as per APIType type.
	 *  
	 * @param apiUrl           -API URL
	 * @param httpHeaders 	   -pass the string format JSON header value.
	 * @param APIType		   -A-Ababil,F-Finacle,C-common.
	 * @return -Return the string response value.
	 */
	public String GetApi(String apiUrl, String httpHeaders,String APIType,String asrequestData) {
		String responseString = "";
		String outputString = "";
		String AccessToken = "";		
		LoggerImpl loggerImpl=null;
		
		String ls_ApiName="";
		String ls_responseCode="";
		String ls_errResponseCode="";
		JSONArray responseJlist=null;
		String ls_paraType = "";
		String ls_paraValue="";
		Set<String> keyNames;
		String ls_urlquery="";
		
		try {
			loggerImpl=new LoggerImpl();
			
			if(StringUtils.isNotBlank(asrequestData)){
				
				JSONObject apiParameter=new JSONObject(asrequestData);
				
				ls_paraType=apiParameter.getString("PARATYPE");
				
				if("U".equals(ls_paraType)){					
					ls_paraValue=apiParameter.getString("PARAVALUE");
					apiUrl=apiUrl+"/"+ls_paraValue;
				}else if("P".equals(ls_paraType)) {
					JSONObject apiParameterValue=apiParameter.getJSONObject("PARAVALUE");
					keyNames=apiParameterValue.keySet();
					
					for (String keyname : keyNames){
						ls_paraValue=apiParameterValue.getString(keyname);
						
						if(StringUtils.isNotBlank(ls_urlquery)){
							ls_urlquery=ls_urlquery+"&"+keyname+"="+ls_paraValue;							
						}else {
							ls_urlquery=keyname+"="+ls_paraValue;
						}						
					}	
					apiUrl=apiUrl+"?"+ls_urlquery;
				}				
			}
			
			URL url = new URL(apiUrl);
			
			if("U".equals(ls_paraType)){
				ls_ApiName=url.getPath().toString();
				ls_ApiName=ls_ApiName.substring(1,ls_ApiName.lastIndexOf("/"));
			}else {
				///get the API name from URL.	
				ls_ApiName=url.getPath().toString().substring(1);				
			}
					    		   
		    loggerImpl.generateProfiler(ls_ApiName);
			loggerImpl.startProfiler("Preparing request data and opening connection.");
						
			/* added by sagar for set access token value */
			if (StringUtils.isNotBlank(httpHeaders)) {
				JSONObject headerJson = new JSONObject(httpHeaders);
				AccessToken = headerJson.optString("ACCESS_TOKEN", "");
			}

			loggerImpl.debug(logger, "opening connection to url.", "IN:GetApi");
			
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			
			loggerImpl.debug(logger, "Connection established and preparing data.", "IN:GetApi");

			// add request header
			httpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			httpConn.setConnectTimeout(connectionTimeout*1000);
			httpConn.setReadTimeout(readTimeout*1000);
			
			/* Set only when value is not null */
			if (AccessToken != null && !"".equals(AccessToken)) {
				httpConn.setRequestProperty("Authorization", "Bearer " + AccessToken);
			}

			httpConn.setRequestMethod("GET");
			// get type also.
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
				
			/*Below code for set F-token in header in case of ABABIL request.*/
			if("A".equals(APIType)){
				httpConn.setRequestProperty("F-TOKEN", applicationData.manageFtoken(""));				
			}			
			
			loggerImpl.debug(logger, ls_ApiName+" API calling...", "IN:GetApi");						
			loggerImpl.startProfiler(ls_ApiName+" Calling..."); 
			
			ls_responseCode=String.valueOf(httpConn.getResponseCode());
			
			/*Below code for set F-token in session object in case of ababil request.*/
			if("A".equals(APIType)){
				applicationData.manageFtoken(httpConn.getHeaderField("F-TOKEN"));				
			}
			
			loggerImpl.startProfiler(ls_ApiName+" Called and preparing response data.");
			loggerImpl.debug(logger, ls_ApiName+" API called.", "IN:GetApi");
			
			if (!isSuccess200ResCode(ls_responseCode)) {
								
				//get error message of API and return error in response.
				InputStream errorObjectStream = httpConn.getErrorStream();
				
				StringBuilder errorRespoceData = new StringBuilder();

				if (errorObjectStream!=null &&errorObjectStream.available() > 0) {
					
					BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorObjectStream));
					
					try {
						String line = null;
						while ((line = errorReader.readLine()) != null){
							errorRespoceData.append(line);
						}
					}finally {
						errorReader.close();
						//httpConn.disconnect();
					}										
				}				
					
				if("A".equals(APIType)) {
					//this is when the account is ababil.							
					ls_errResponseCode=common.getAbabilErrorResCode(errorRespoceData.toString());
					
					if(StringUtils.isBlank(ls_errResponseCode)){ls_errResponseCode=ls_responseCode;}
				}else {
					ls_errResponseCode=ls_responseCode;
				}
				
				outputString=ofGetFailedMSg(ls_ApiName,ls_errResponseCode, errorRespoceData.toString(), "(ENP655)",APIType);
												
			} else {

				InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				StringBuilder successResponseData = new StringBuilder();
				
				// Write the message response to a String.
				while ((responseString = in.readLine()) != null) {
					successResponseData.append(responseString);					
				}
				
				outputString=successResponseData.toString();
				
				if(outputString.trim().startsWith("{")){
					responseJlist=new JSONArray().put(new JSONObject(outputString));
				}else if(outputString.trim().startsWith("[")){
					responseJlist=new JSONArray(outputString);
				}else{
					responseJlist=new JSONArray().put(outputString);
				}
				
				/*by sagar for set data into JSON format for identify response data.*/
				//outputString = common.ofGetResponseJson(responseJlist, ls_responseCode, "API Called Successfully", ST0,"G","").toString(); 
				outputString =ofGetResponseJson(responseJlist, ls_responseCode, "API Called Successfully", ST0,"G","common.API_suc").toString(); 
			}
			
			loggerImpl.debug(logger, "Response generated.", "IN:GetApi");
			
			httpConn.disconnect();		

		} catch (SocketTimeoutException ex) {		
			outputString= getTimeOutExceptionMSg(ex,logger,loggerImpl, "IN:GetApi", "(ENP654)");
				
		} catch (IOException ex) {
			outputString= getExceptionMSg(ex,logger,loggerImpl, "IN:GetApi", "(ENP653)");

		} catch (Exception ex) {
			outputString= getExceptionMSg(ex,logger,loggerImpl, "IN:GetApi", "(ENP652)");
			
	   }finally{
		   /*stop the profiler and print logs.*/
		   doStopProfileAndPrintLogs(logger, loggerImpl, "IN:GetApi");		   		
		}
		
		return outputString;
	}
	
	/**
	 * @param methodType       -Set the Method name,if this parameter is null then
	 *                         default value is "POST"
	 * @param apiUrl           -API URL
	 * @param apiParameter     -String format json request data.
	 * @param httpHeaders 	   -pass the string format json header value.
	 * @param APIType		   -A-Ababil,F-Finacle,C-common.
	 * @return -Return the string response value.
	 */
	public String PostApi(String methodType, String apiUrl, String apiParameter, String httpHeaders,String APIType) {

		String jsonInput = "";
		String responseString = "";
		String outputString = "";
		String AccessToken = "";
		String requestMethodType = "";
		LoggerImpl loggerImpl=null;		
		String ls_ApiName="";
		String ls_responseCode="";
		String ls_errResponseCode="";
		JSONArray responseJlist=null;

		try {
			loggerImpl=new LoggerImpl();
			URL url = new URL(apiUrl);
			
			///get the api name from url.			
			ls_ApiName=url.getPath().toString().substring(1);
		    		   
		    loggerImpl.generateProfiler(ls_ApiName);
			loggerImpl.startProfiler("Preparing request data and opening connection.");
			
			if (methodType != null && !"".equals(methodType)) {
				requestMethodType = methodType;
			} else { // default method type
				requestMethodType = "POST";
			}

			/* added by sagar for set access token value */
			if (httpHeaders != null && !"".equals(httpHeaders)) {
				JSONObject headerJson = new JSONObject(httpHeaders);
				AccessToken = headerJson.optString("ACCESS_TOKEN", "");
			}

			loggerImpl.debug(logger, "opening connection to url.", "IN:PostApi");
			
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			
			loggerImpl.debug(logger, "Connection established and preparing data.", "IN:PostApi");
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();

			jsonInput = apiParameter;

			byte[] buffer = new byte[jsonInput.length()];
			buffer = jsonInput.getBytes();
			bout.write(buffer);
			byte[] b = bout.toByteArray();

			// add request header
			httpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			httpConn.setConnectTimeout(connectionTimeout*1000); // set timeout 1 min
			httpConn.setReadTimeout(readTimeout*1000);
			
			/* Set only when value is not null */
			if (AccessToken != null && !"".equals(AccessToken)) {
				httpConn.setRequestProperty("Authorization", "Bearer " + AccessToken);
				
				JSONObjectImpl overRideHeaderjson=new JSONObjectImpl(httpHeaders);
				
				Set<String> jsonKeyData=overRideHeaderjson.keySet();
				
				for (String keyName : jsonKeyData) {						
					httpConn.setRequestProperty(keyName, overRideHeaderjson.getString(keyName));					
				}
			}

			httpConn.setRequestMethod(requestMethodType);// set from variable because of we need to send request body in
			// get type also.
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
				
			/*Below code for set F-token in header in case of ababil request.*/
			if("A".equals(APIType)){
				httpConn.setRequestProperty("F-TOKEN", applicationData.manageFtoken(""));				
			}
			
			try (OutputStream out_strm = httpConn.getOutputStream()) {
				out_strm.write(b);
			}
			
			loggerImpl.debug(logger, ls_ApiName+" API calling...", "IN:PostApi");						
			loggerImpl.startProfiler(ls_ApiName+" Calling..."); 
			
			ls_responseCode=String.valueOf(httpConn.getResponseCode());
			
			/*Below code for set F-token in session object in case of ababil request.*/
			if("A".equals(APIType)){
				applicationData.manageFtoken(httpConn.getHeaderField("F-TOKEN"));				
			}
			
			loggerImpl.startProfiler(ls_ApiName+" Called and preparing response data.");
			loggerImpl.debug(logger, ls_ApiName+" API called.", "IN:PostApi");
			
			if (!isSuccess200ResCode(ls_responseCode)) {
								
				//get error message of API and return error in response.
				InputStream errorObjectStream = httpConn.getErrorStream();
				StringBuilder errorRespoceData = new StringBuilder();

				if (errorObjectStream!=null && errorObjectStream.available() > 0) {
					BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorObjectStream));
					
					try {
						String line = null;
						while ((line = errorReader.readLine()) != null) {
							errorRespoceData.append(line);
						}
					}finally{
						errorReader.close();
						httpConn.disconnect();
					}	
				}
				
				
				if("A".equals(APIType)) {
					//this is when the account is ababil.							
					ls_errResponseCode=common.getAbabilErrorResCode(errorRespoceData.toString());
					
					if(StringUtils.isBlank(ls_errResponseCode)){ls_errResponseCode=ls_responseCode;}
				}else {
					ls_errResponseCode=ls_responseCode;
				}
				
				outputString=ofGetFailedMSg(ls_ApiName,ls_errResponseCode, errorRespoceData.toString(), "(ENP391)",APIType);
									
			} else {

				InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				StringBuilder successResponseData = new StringBuilder();
				
				// Write the message response to a String.
				while ((responseString = in.readLine()) != null) {
					successResponseData.append(responseString);					
				}
				
				outputString=successResponseData.toString();
				
				if(outputString.trim().startsWith("{")){
					responseJlist=new JSONArray().put(new JSONObject(outputString));
				}else if(outputString.trim().startsWith("[")){
					responseJlist=new JSONArray(outputString);
				}else{
					responseJlist=new JSONArray().put(outputString);
				}
				
				/*by sagar for set data into JSON format for identify response data.*/
				//outputString = common.ofGetResponseJson(responseJlist, ls_responseCode, "API Called Successfully",ST0,"G","").toString(); 
				outputString =ofGetResponseJson(responseJlist, ls_responseCode, "API Called Successfully",ST0,"G","common.API_suc").toString(); 
			}
			
			loggerImpl.debug(logger, "Response generated.", "IN:PostApi");
			
			httpConn.disconnect();		

		} catch (SocketTimeoutException ex) {	
			outputString= getTimeOutExceptionMSg(ex,logger,loggerImpl, "IN:PostApi", "(ENP007)");
	
		} catch (IOException ex) {
			outputString= getExceptionMSg(ex,logger, loggerImpl, "IN:PostApi", "(ENP008)" );
			
		} catch (Exception ex) {
			outputString= getExceptionMSg(ex,logger, loggerImpl, "IN:PostApi", "(ENP009)" );
												
		}finally{			
			/*stop the profiler and print logs.*/
			doStopProfileAndPrintLogs(logger, loggerImpl, "IN:PostApi");			
		}
		
		return outputString;
	}

	/**
	 * @param basePath base path of configuration.
	 * @param httpHeraders   extra header to get access token ,if not pass "";
	 * @param APIType		 -A-Ababil,F-Finacle,C-common.
	 * @return this method return the string format json object of token values.
	 */
	public String GenarateAccessToken(String basePath, String httpHeraders,String APIType) {
		
		String outputString = "";
		String apiUrl="";
		LoggerImpl loggerImpl=null;
		String ls_contentType="";
		String ls_ApiName="";
		String ls_errResponseCode="";
		
		try {
			loggerImpl=new LoggerImpl();
			
			String jsonInput = "";
			String ls_AuthUsername = "";
			String ls_AuthPassword = "";
			String ls_AccessToken = "";
			String ls_userName = "";
			String ls_password = "";
			String ls_grantType = "";
			String ls_responseCode="";
			
			loggerImpl.debug(logger, "Getting configuration..", "IN:GenarateAccessToken");
			
			ls_AuthUsername = readXML.getXmlData(basePath+">CLIENT_KEY");
			ls_AuthPassword = readXML.getXmlData(basePath+">CLIENT_PASSWORD");
			ls_userName 	= readXML.getXmlData(basePath+">USER_NAME");
			ls_password 	=readXML.getXmlData(basePath+">PASSWORD");
			ls_grantType 	=readXML.getXmlData(basePath+">GRANT_TYPE");
			apiUrl			=readXML.getXmlData(basePath+">OAUTH_URL");

			ls_AccessToken = ls_AuthUsername + ":" + ls_AuthPassword;
			ls_AccessToken = Base64.getEncoder().encodeToString(ls_AccessToken.getBytes());
			
			URL url = new URL(apiUrl);
			
			 ///get the api name from url.
			ls_ApiName=url.getPath().toString().substring(1);			 
		     
		    loggerImpl.generateProfiler(ls_ApiName);
			loggerImpl.startProfiler("Preparing request data and opening connection.");
			
			loggerImpl.debug(logger, "Opening connection to url.", "IN:GenarateAccessToken");
			
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			
			loggerImpl.debug(logger, "Setting header data.", "IN:GenarateAccessToken");
			
			/* If header value found then set into header. */
			if (httpHeraders != null && !"".equals(httpHeraders)) {
				JSONObject headerJson = new JSONObject(httpHeraders);
				ls_contentType=headerJson.getString("Content-Type");
				
				for (String keyName : headerJson.keySet()) {
					urlc.setRequestProperty(keyName, headerJson.getString(keyName));
				}
			}
			
			if(ls_contentType.equals("application/x-www-form-urlencoded")) {
				// request data for authorization
				jsonInput = "username=" + ls_userName + "&" + "password=" + ls_password + "&" + "grant_type="
						+ ls_grantType;				
			}else {
				JSONObject requestData=new JSONObject();
				requestData.put("username", ls_userName);
				requestData.put("password", ls_password);
				requestData.put("grant_type", ls_grantType);
				
				jsonInput=requestData.toString();
			}
			
			urlc.setRequestMethod("POST");
			//urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			urlc.setRequestProperty("Content-Language", "en-US");
			urlc.setRequestProperty("Authorization", "Basic " + ls_AccessToken);
			urlc.setRequestProperty("Accept", "*/*");
			urlc.setConnectTimeout(connectionTimeout*1000); // set timeout 1 min
			urlc.setReadTimeout(readTimeout*1000);			
			urlc.setUseCaches(false);
			urlc.setDoOutput(true);

			/*Below code for set F-token in header in case of ababil request.*/
			if("A".equals(APIType)){
				urlc.setRequestProperty("F-TOKEN", applicationData.manageFtoken(""));								
			}
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();

			loggerImpl.debug(logger, "Setting body data.", "IN:GenarateAccessToken");
			
			byte[] buffer = new byte[jsonInput.length()];
			buffer = jsonInput.getBytes();
			bout.write(buffer);
			byte[] b = bout.toByteArray();

			// Send request
			DataOutputStream wr = new DataOutputStream(urlc.getOutputStream());
			wr.write(b);
			wr.close();

			loggerImpl.debug(logger, ls_ApiName+" API calling..", "IN:GenarateAccessToken");
			
			ls_responseCode=String.valueOf(urlc.getResponseCode());
			
			/*Below code for set F-token in session object in case of ababil request.*/
			if("A".equals(APIType)){
				applicationData.manageFtoken(urlc.getHeaderField("F-TOKEN"));				
			}
			
			loggerImpl.startProfiler(ls_ApiName+" Called and preparing response data.");
			
			loggerImpl.debug(logger, ls_ApiName+" API called.", "IN:GenarateAccessToken");
			
			if (!isSuccess200ResCode(ls_responseCode)) {
								
				//get error message of API and return error in response.
				InputStream errorObjectStream = urlc.getErrorStream();
				StringBuilder errorRespoceData = new StringBuilder();


				if (errorObjectStream!=null && errorObjectStream.available() > 0) {
					BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorObjectStream));
					
					try {
						String line = null;
						while ((line = errorReader.readLine()) != null) {
							errorRespoceData.append(line);
						}
					}finally{
						errorReader.close();
					}	
				}
				
				
				if("A".equals(APIType)) {
					//this is when the account is ababil.							
					ls_errResponseCode=common.getAbabilErrorResCode(errorRespoceData.toString());
					
					if(StringUtils.isBlank(ls_errResponseCode)){ls_errResponseCode=ls_responseCode;}
				}else {
					ls_errResponseCode=ls_responseCode;
				}
				
				outputString=ofGetFailedMSg(ls_ApiName, ls_errResponseCode, errorRespoceData.toString(), "(ENP392)",APIType);
											
			} else {

				InputStreamReader isr = new InputStreamReader(urlc.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String responseString="";
				StringBuilder successResponseData = new StringBuilder();
				
				// Write the message response to a String.
				while ((responseString = in.readLine()) != null) {
					successResponseData.append(responseString);					
				}

				responseString=successResponseData.toString();
				
				if (responseString.trim().substring(0, 1).equals("[")) {
					responseString =responseString.substring(1, responseString.length() - 1);
				}
				
				loggerImpl.debug(logger, "setting data into json object", "IN:GenarateAccessToken");
				
				JSONObject jobj = new JSONObject(responseString);
				String access_token = jobj.getString("access_token");
				String refresh_token = jobj.getString("refresh_token");
				String expire = String.valueOf(jobj.getInt("expires_in"));
				//String scope = jobj.getString("scope");

				JSONObject responsetokenData = new JSONObject();

				responsetokenData.put("ACCESS_TOKEN", access_token);
				responsetokenData.put("REFRESH_TOKEN", refresh_token);
				responsetokenData.put("EXPIRE_TOKEN", expire);

				//outputString = common.ofGetResponseJson(null,"","",ST0,"G","",responsetokenData).toString();	
				outputString = ofGetResponseJson(null,"","",ST0,"G","",responsetokenData).toString();	
				
			}
			urlc.disconnect();
		
		} catch (SocketTimeoutException ex) {
			outputString=getTimeOutExceptionMSg(ex, logger, loggerImpl, "IN:GenarateAccessToken", "(ENP010)");
			
		} catch (IOException ex) {
			outputString= getExceptionMSg(ex, logger, loggerImpl, "IN:GenarateAccessToken", "(ENP011)");
			
		} catch (Exception exception) {
			outputString= getExceptionMSg(exception, logger, loggerImpl, "IN:GenarateAccessToken", "(ENP012)");
			
		}finally{
			/*stop the profiler and print logs.*/
			doStopProfileAndPrintLogs(logger, loggerImpl, "IN:GenarateAccessToken");					
		}
		
		return outputString;
	}
	
	/*
	 * Write Text Log
	 */
	public void WriteLog(String as_text) {
		readXML xmlread = new readXML();
		String as_path = xmlread.getPath() + File.separatorChar + "log" + File.separatorChar + "";
		System.out.println(as_path);
		try {

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
			SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
			simpleDateFormat2.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

			String date = simpleDateFormat.format(new Date());
			String date2 = simpleDateFormat2.format(new Date());
			FileWriter writer = new FileWriter(as_path + "LOG_" + date + ".txt", true);
			writer.write(date2 + " " + as_text);
			writer.write("\r\n");
			writer.close();

			File file = new File(as_path + "LOG_" + date + ".txt");
			if (file.exists()) {
				file.setExecutable(true);
				file.setReadable(true);
				file.setWritable(true);
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * Google Captcha Verify API
	 */
	public static boolean PostApiRecaptcha(HttpSession session, String apiParameter) throws JSONException {

		try {
			if (readXML.getXmlData("captcha_cd").equalsIgnoreCase("CC")) {
				if (AESEncryption.decryptText(session.getAttribute("captchakey").toString()).equals(apiParameter)) {
					return true;
				}
			} else if (readXML.getXmlData("captcha_cd").equalsIgnoreCase("GC")) {
				String responseString = "";
				String outputString = "";
				try {
					String apiURL = "https://www.google.com/recaptcha/api/siteverify?";
					String secret = readXML.getXmlData("site_secret_key");
					URL url = new URL(apiURL);
					URLConnection connection = url.openConnection();
					HttpURLConnection httpConn = (HttpURLConnection) connection;
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					String jsonInput = "secret=" + secret + "&response=" + apiParameter;
					byte[] buffer = new byte[jsonInput.length()];
					buffer = jsonInput.getBytes();
					bout.write(buffer);
					byte[] b = bout.toByteArray();

					// add reuqest header
					httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
					httpConn.setConnectTimeout(60000); // set timeout 1 min
					httpConn.setReadTimeout(60000);
					httpConn.setRequestMethod("GET");
					httpConn.setDoOutput(true);
					httpConn.setDoInput(true);

					OutputStream out_strm = httpConn.getOutputStream();
					out_strm.write(b);
					out_strm.close();

					InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
					BufferedReader in = new BufferedReader(isr);
					// Write the SOAP message response to a String.
					while ((responseString = in.readLine()) != null) {
						outputString = outputString + responseString;
					}

					httpConn.disconnect();

					if (outputString.trim().substring(0, 1).equals("[")) {
						outputString = (String) outputString.substring(1, outputString.length() - 1);
					} else {
						outputString = (String) outputString;
					}
					JSONObject jobj = new JSONObject(outputString);
					boolean success = jobj.getBoolean("success");
					if (success) {
						return success;
					}

				} catch (SocketTimeoutException ex) {
					return false;
				} catch (IOException e) {
					return false;
				}
			}
		} catch (Exception ex) {
			return false;
		}
		/* Temparary By Pass Captcha */
		// return true;
		return false;
	}

	/**NOT IN USED.**/
	public String getAPIWithBody(String apiUrl, String apiParameter, String httpHeaders){

		String responseString = "";		
		String outputString = "";
		String AccessToken = "";
		String actualErrMsg = "";		
		LoggerImpl loggerImpl=null;
		JSONArray responseJlist=null;
		
		try {
			
			loggerImpl=new LoggerImpl();
			loggerImpl.debug(logger,"apiUrl : " + apiUrl,"IN:getAPIWithBody");
			
			/* added by sagar for set access token value */
			if (httpHeaders != null && !"".equals(httpHeaders)) {
				JSONObject headerJson = new JSONObject(httpHeaders);
				AccessToken = headerJson.optString("ACCESS_TOKEN","");
			}

			//set timeout parameter
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectionTimeout*1000)
					.setConnectionRequestTimeout(readTimeout*1000)				
					.setSocketTimeout(connectionTimeout*1000).build();

			//create http connection with parameter
			CloseableHttpClient  httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();


			// client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

			HttpGetWithBody httpgetWithBody = new HttpGetWithBody();

			//Set url to http client
			httpgetWithBody.setURI(new URI(apiUrl));

			httpgetWithBody.setHeader("Content-Type", "application/json; charset=utf-8");

			/* Set only when value is not null */
			if (AccessToken != null && !"".equals(AccessToken)) {
				httpgetWithBody.setHeader("Authorization", "Bearer " + AccessToken);
			}

			//set the request body
			httpgetWithBody.setEntity(new ByteArrayEntity(apiParameter.getBytes("UTF8")));

			//call api to fectch data
			HttpResponse httpResponse = httpClient.execute(httpgetWithBody);
			//get the response body.
			responseString = EntityUtils.toString(httpResponse.getEntity());

			//Get the status of request.
			StatusLine statusLine=httpResponse.getStatusLine();

			if (statusLine.getStatusCode() != 200) {
				outputString = common.ofGetErrDataJsonObject("9", 
						propConfiguration.getMessageOfResCode("common.title.999", "Alert."), 
						propConfiguration.getMessageOfResCode("common.URL_not_respond","",""),
						responseString, "URL not respond.",String.valueOf(statusLine.getStatusCode()), "R");				
			} else {

				outputString=responseString;

				if(outputString.trim().startsWith("{")){
					responseJlist=new JSONArray().put(new JSONObject(outputString));
				}else if(outputString.trim().startsWith("[")){
					responseJlist=new JSONArray(outputString);
				}else{
					responseJlist=new JSONArray().put(outputString);
				}
				
				/* by sagar for set data into json format for identify response data. */
				//outputString = common.ofGetResponseJson(responseJlist, "","", ST0,"G","").toString(); 
				outputString = ofGetResponseJson(responseJlist, "","", ST0,"G","").toString();

			}
			//close connection
			httpClient.close();

		} catch (SocketTimeoutException ex) {
			actualErrMsg = common.ofGetTotalErrString(ex, "");
			outputString = common.ofGetErrDataJsonObject("2", 
					propConfiguration.getMessageOfResCode("common.title.999", "Alert."),
					propConfiguration.getMessageOfResCode("common.exception.999","","(ENP028)"),
					ex.getMessage(),"Currently Service under maintenance so please try later (ENP028).", ST999, "R");
			
			loggerImpl.error(logger,"SocketTimeoutException: " + actualErrMsg,"IN:getAPIWithBody");
		} catch (IOException ex) {
			actualErrMsg = common.ofGetTotalErrString(ex, "");
			loggerImpl.error(logger,"IOException: " + actualErrMsg,"IN:getAPIWithBody");
			outputString = common.ofGetErrDataJsonObject("2", 
					propConfiguration.getMessageOfResCode("common.title.999", "Alert."),
					propConfiguration.getMessageOfResCode("common.exception.999","","(ENP029)"), 
					ex.getMessage(),"Currently Service under maintenance so please try later (ENP029).", ST999, "R");
			
		} catch (Exception ex) {
			actualErrMsg = common.ofGetTotalErrString(ex, "");
			loggerImpl.error(logger,"Exception: " + actualErrMsg,"IN:getAPIWithBody");
			outputString = common.ofGetErrDataJsonObject("2",
					propConfiguration.getMessageOfResCode("common.title.999", "Alert."),
					propConfiguration.getMessageOfResCode("common.exception.999","","(ENP030)"),
					ex.getMessage(),"Currently Service under maintenance so please try later (ENP030).", ST999, "R");
		}
		
		System.gc();
		return outputString;
	}

	public String PostFileUploadApi(String apiUrl, JSONObject apiParameter, String httpHeaders) {		
		String twoHyphens = "--";
		String boundary = "**" + Long.toString(System.currentTimeMillis()) + "**";
		String lineEnd = "\r\n";
		String outputString = "";
		String ls_Authorization = "";
		int maxBufferSize = 1 * 1024 * 1024;
		int bytesRead;
		int bytesAvailable;
		int bufferSize;
		LoggerImpl loggerImpl=null;
		String ls_FileData="";
		InputStream apiFile=null;
		String ls_ApiName="";
		String ls_apiFileName="";
		
		try {
			loggerImpl=new LoggerImpl();

			URL url = new URL(apiUrl);
			
			///get the API name from URL.			
			ls_ApiName=url.getPath().toString().substring(1);
		    		   
		    loggerImpl.generateProfiler(ls_ApiName);
			loggerImpl.startProfiler("Preparing request data and opening connection.");
			loggerImpl.debug(logger,"Preparing request data.","IN:PostFileUploadApi");			
						
			ls_FileData=apiParameter.getString("FILE_DATA");
			ls_apiFileName=apiParameter.getString("FILE_NAME");
			
			apiFile = new ByteArrayInputStream(Base64.getDecoder().decode(ls_FileData));
			apiParameter.remove("FILE_DATA"); //no more used of this key
			
			/* for set access token value */
			if (httpHeaders != null && !"".equals(httpHeaders)) {
				JSONObject headerJson = new JSONObject(httpHeaders);
				ls_Authorization = headerJson.optString("ACCESS_TOKEN", "");
			}		
			
			loggerImpl.debug(logger, "opening connection to url.","IN:PostFileUploadApi");			
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;

			loggerImpl.debug(logger, "Connection established and preparing data.","IN:PostFileUploadApi");
			
			//add request header
			httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			//httpConn.setRequestProperty("Accept-Type", "application/json");
			//httpConn.setRequestProperty("Content-Type", "text/plain");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("Authorization", "Bearer " + ls_Authorization);
			httpConn.setRequestMethod("POST");
			httpConn.setUseCaches(false);
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			httpConn.setConnectTimeout(connectionTimeout*1000);
			httpConn.setReadTimeout(readTimeout*1000);

			//set header token
			DataOutputStream outputStream = new DataOutputStream(httpConn.getOutputStream());

			//Document File
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + ls_apiFileName + "\"" + lineEnd);
			outputStream.writeBytes("Content-Type: application/octet-stream" + lineEnd);
			outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

			outputStream.writeBytes(lineEnd);
			bytesAvailable = apiFile.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];

			bytesRead = apiFile.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = apiFile.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = apiFile.read(buffer, 0, bufferSize);
			}
			outputStream.writeBytes(lineEnd);

			for (String keyStr : apiParameter.keySet()) {
				String keyvalue = apiParameter.getString(keyStr);

				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				outputStream.writeBytes("Content-Disposition: form-data; name=\""+keyStr+"\"" + lineEnd);
				outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(keyvalue);
				outputStream.writeBytes(lineEnd);

			}

			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			
			loggerImpl.debug(logger, ls_ApiName+" API calling...","IN:PostFileUploadApi");						
			loggerImpl.startProfiler(ls_ApiName+" Calling..."); 
			
			int code = httpConn.getResponseCode();
			
			loggerImpl.startProfiler(ls_ApiName+" Called and preparing response data.");
			loggerImpl.debug(logger, ls_ApiName+" API called.","IN:PostFileUploadApi");
			
			outputStream.flush();
			outputStream.close();

			if (code == HttpURLConnection.HTTP_OK) {
				InputStream objInputstream = httpConn.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(objInputstream));

				StringBuilder respoceData = new StringBuilder();
				
				try {
					String line = null;
					while ((line = reader.readLine()) != null) {
						respoceData.append(line + "\n");
					}
					
					JSONObject JsonResponsedata = new JSONObject();

					JsonResponsedata.put("FILE_NM", respoceData.toString());
					
					//outputString = common.ofGetResponseJson(null,String.valueOf(code),"API Called Successfully",ST0 ,"","",JsonResponsedata).toString(); 
					outputString =ofGetResponseJson(null,String.valueOf(code),"API Called Successfully",ST0 ,"","common.API_suc",JsonResponsedata).toString(); 
				}finally{
					objInputstream.close();
					httpConn.disconnect();
				}
			} else{
				//get error message
				InputStream errorObjectStream = httpConn.getErrorStream();
				BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorObjectStream));

				StringBuilder errorRespoceData = new StringBuilder();
				try {
					String line = null;
					while ((line = errorReader.readLine()) != null) {
						errorRespoceData.append(line + "\n");
					}
				}finally {
					errorObjectStream.close();
					httpConn.disconnect();
				}					
				outputString=ofGetFailedMSg(ls_ApiName, String.valueOf(code), errorRespoceData.toString(), "(ENP058)");					
			}	

			loggerImpl.debug(logger,"Response generated.", "IN:PostApi");
			
			System.gc();
		}catch (SocketTimeoutException ex) {
			outputString=getTimeOutExceptionMSg(ex, logger, loggerImpl, "IN:PostFileUploadApi", "(ENP055)");
								
		} catch (IOException ex) {
			outputString=getExceptionMSg(ex, logger, loggerImpl, "IN:PostFileUploadApi", "(ENP056)");	
			
		} catch (Exception ex) {
			outputString=getExceptionMSg(ex, logger, loggerImpl, "IN:PostFileUploadApi", "(ENP057)");					
		}
		return outputString;
	}
}
