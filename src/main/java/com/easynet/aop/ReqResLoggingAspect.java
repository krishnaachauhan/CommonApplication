package com.easynet.aop;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.easynet.impl.LoggerImpl;
import com.easynet.util.CommonBase;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Aspect
public class ReqResLoggingAspect extends CommonBase{

	Logger LOGGER=LoggerFactory.getLogger("REQ_RES_LOGGER");
	
	ObjectMapper xmlMapper = new ObjectMapper();
	ObjectMapper jsonMapper = new ObjectMapper();

	@Value("${REQ_RES_LOGS:N}")
	String ls_reqResLogsEnabled;

	@Around("execution(* com.easynet.service.apiService..*(..)) || execution(* com.easynet.util.GetData..*(..))"
			+ "|| execution(* com.easynet.util.GetDataDB..*(..))"
			+ "||execution(* com.easynet.util.apiCall1..*(..))")
	public Object printRequestResLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		String ls_requestData="";
		String ls_apiName="";
		Object result = null;
		String ls_className ="";
		String ls_methodName ="";
		LoggerImpl loggerImpl=null;
		int li_arguLength=0;

		String [] ls_parameterList;
		String ls_parameterName="";
		String ls_restApiName="";
		
		if("Y".equals(ls_reqResLogsEnabled)){	

			loggerImpl=new LoggerImpl();

			JSONArray argumentJList=new JSONArray();

			MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();

			//Get intercepted method details
			ls_className = methodSignature.getDeclaringType().getSimpleName();
			ls_methodName = methodSignature.getName();          
			ls_parameterList=methodSignature.getParameterNames();

			Object[] arguments = proceedingJoinPoint.getArgs();

			li_arguLength=arguments.length;

			if(li_arguLength > 0 ) {

				for (int i=0;i < li_arguLength;i++) {

					JSONObject argJson=new JSONObject();
					Object requestData=arguments[i];
					ls_parameterName=ls_parameterList[i];

					/*For print the API Name in logs.*/
					if(("apiUrl".equals(ls_parameterName)||"basePath".equals(ls_parameterName))&& "apiCall1".equals(ls_className)){						
						try {
							URL url = new URL((String)requestData);														
							ls_restApiName="EX:"+url.getPath().toString().substring(1);							
						}catch(MalformedURLException exception){							
							ls_restApiName="EX:"+(String)requestData;
						}						
						ls_apiName=ls_restApiName+"-Request";						
					}
					
					if(requestData!= null){
						
						if(requestData instanceof String) {
							ls_requestData=(String)requestData;
							
							if(ls_requestData.trim().startsWith("{")){
								argJson.put("parameterValue", new JSONObject(ls_requestData));
							}else if(ls_requestData.trim().startsWith("[")){
								argJson.put("parameterValue", new JSONArray(ls_requestData));
							}else{
								argJson.put("parameterValue",ls_requestData);
							}
						
						}else if(requestData instanceof JSONObject || requestData instanceof JSONArray ){ 
							argJson.put("parameterValue", requestData);							 
						}else {
							//convert the respective object into JSON format.
							try {
								ls_requestData=jsonMapper.writeValueAsString(requestData);															
								argJson.put("parameterValue", ls_requestData);
							}catch(Exception e) {
								//If exception generated then set direct object values.
								argJson.put("parameterValue",requestData.toString());
							}
						}						
					}else {
						argJson.put("parameterValue", "null");
					}
					
					argJson.put("parameterName", ls_parameterName);					
					argumentJList.put(argJson);					
				}			
				
				if(StringUtils.isBlank(ls_apiName)){					
					ls_apiName=ls_className+"-"+ls_methodName+"-Request";
				}
				loggerImpl.trace(LOGGER,argumentJList.toString(),ls_apiName);        	
			}
		}

		result = proceedingJoinPoint.proceed();

		if("Y".equals(ls_reqResLogsEnabled)) {

			if(result!=null) {

				if(result instanceof String) {
					ls_requestData=(String)result;
				}else if(result instanceof JSONObject || result instanceof JSONArray ) {
					ls_requestData=result.toString();
				}else {
					try {
						//convert the respective object into JSON format.										
						ls_requestData=jsonMapper.writeValueAsString(result);																					
					}catch(Exception e) {
						//If exception generated then set direct object values.
						ls_requestData=result.toString();
					}
				}
								
				if(StringUtils.isAnyBlank(ls_apiName,ls_restApiName)){
					ls_apiName=ls_className+"-"+ls_methodName+"-Response";
				}else{
					ls_apiName=ls_restApiName+"-Response";
				}
								
				loggerImpl.trace(LOGGER,ls_requestData,ls_apiName);   
			}  
		}
		return result;
	}
	
//	@Around("execution(* com.easynet.configuration.SOAPConnector.callWebService(String,String,Object))")
//	public Object printExtRequestResLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//
//		String ls_requestData="";
//		String ls_apiName="";
//		String ls_actualApiName="";
//		
//		Object result = null;
//		String ls_className ="";
//		String ls_methodName ="";
//		LoggerImpl loggerImpl=null;
//		int li_arguLength=0;
//
//		String [] ls_parameterList;
//		String ls_parameterName="";
//
//		if("Y".equals(ls_reqResLogsEnabled)) {	
//
//			loggerImpl=new LoggerImpl();
//						
//			xmlMapper.addMixIn(JAXBElement.class, JacksonJAXBElement.class);
//
//			MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
//
//			//Get intercepted method details
//			ls_className = methodSignature.getDeclaringType().getSimpleName();
//			ls_methodName = methodSignature.getName();          
//			ls_parameterList=methodSignature.getParameterNames();
//
//			Object[] arguments = proceedingJoinPoint.getArgs();
//
//			li_arguLength=arguments.length;
//
//			if(li_arguLength > 0 ) {
//
//				for (int i=0;i < li_arguLength;i++) {
//
//					Object requestData=arguments[i];
//					ls_parameterName=ls_parameterList[i];
//
//					if(requestData!= null){
//						
//						if("request".equals(ls_parameterName)) {
//							try {
//								//convert the respective object into JSON format.										
//								ls_requestData = xmlMapper.writeValueAsString(requestData);																						
//							}catch(Exception e) {
//								//If exception generated then set direct object values.
//								ls_requestData=requestData.toString();
//							}
//						}
//						
//						if("apiName".equals(ls_parameterName)) {							
//							ls_actualApiName="EX:"+(String)requestData;
//							ls_apiName=ls_actualApiName+"-Request";
//						}											
//					}else{
//						ls_requestData="null";
//					}												
//				}	
//				
//				if(StringUtils.isBlank(ls_apiName)){
//					ls_apiName=ls_className+"-"+ls_methodName+"-Request";
//				}
//				
//				loggerImpl.trace(LOGGER,ls_requestData,ls_apiName);        	
//			}
//		}
//
//		try {
//			result = proceedingJoinPoint.proceed();
//		}catch(WebServiceIOException exception) {
//			//write the error logs code.
//			printExtRequestResErrorLogCode(loggerImpl,ST9999,ls_apiName,ls_className,ls_methodName,ls_actualApiName);					
//			throw exception;
//			
//		} catch (SoapFaultClientException soapException) {
//			//write the error logs code.
//			printExtRequestResErrorLogCode(loggerImpl,ST999,ls_apiName,ls_className,ls_methodName,ls_actualApiName);							
//			throw soapException;
//		}
//		
//		if("Y".equals(ls_reqResLogsEnabled)){
//
//			if(result!=null){
//				
//				try {
//					//convert the respective object into JSON format.											
//					ls_requestData = xmlMapper.writeValueAsString(result);
//				}catch(Exception e) {
//					//If exception generated then set direct object values.
//					ls_requestData=result.toString();
//				}
//				
//				if(StringUtils.isBlank(ls_apiName)){
//					ls_apiName=ls_className+"-"+ls_methodName+"-Response";
//				}else{
//					ls_apiName=ls_actualApiName+"-Response";
//				}				
//				loggerImpl.trace(LOGGER,ls_requestData,ls_apiName); 
//				
//			}
//		}
//		return result;
//	}
	
	/***
	 *Used this method instead of afterthrowing aop advice.because of we need to get other detail like api name. 
	 *date -14/12/2021
	 *
	 * */
	private void printExtRequestResErrorLogCode(LoggerImpl loggerImpl,String ls_responseCode,String ls_apiName,String ls_className,String ls_methodName,String ls_actualApiName) {
		
		if("Y".equals(ls_reqResLogsEnabled)){
		
			String ls_errorData="";
			JSONObject errorJson=new JSONObject();	
			
			errorJson.put("responseCode",ls_responseCode);
			errorJson.put("responseMessage","Exception Generated");			
			ls_errorData=new JSONObject().put("return", errorJson).toString();
			
			if(StringUtils.isBlank(ls_apiName)){
				ls_apiName=ls_className+"-"+ls_methodName+"-Response";
			}else{
				ls_apiName=ls_actualApiName+"-Response";
			}				
			loggerImpl.trace(LOGGER,ls_errorData,ls_apiName);
			
		}
	}
}
