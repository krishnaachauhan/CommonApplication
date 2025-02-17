package com.easynet.util;

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easynet.bean.GetApplicationData;
import com.easynet.bean.GetRequestUniqueData;
import com.easynet.configuration.PropConfiguration;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.LoggerImpl;

@Component
public class CommonJsonObject extends CommonBase{

	@Autowired
	PropConfiguration propConfiguration;
	
	@Autowired
	GetApplicationData getApplicationData;
	
	Logger LOGGER=LoggerFactory.getLogger(CommonJsonObject.class);
	
	@Autowired
	GetRequestUniqueData getRequestUniqueData;
	
//	@Autowired
//	GetDataDB getDataDB;
	
	@Autowired
	CallService callService;
	/**
	 * This method return then response data which is update into database.	 
	 * @param as_ResData -response data from which this method prepare data.
	 * @param as_apiName -Name of API.
	 * @param respDataKeysNames -Key name list which is put in json data.
	 * @param as_fromAccount	-From account number.
	 * @param as_toAccount -To account number.
	 * @param appendObject - it contains jsonObject for append the keys values in jsonObject.
	 * 
	 * */	 
	public JSONObject getDbUpdateReqData(String as_ResData,String as_fromAccount,String as_toAccount,String as_apiName,JSONObject respDataKeysNames,Object... appendObject) {

		String ls_statusCd="";
		String ls_responseData="";
		String ls_responsCd="";
		String ls_responseMsg="";
		boolean lb_trnReverse=true;
		//String  ls_addData="false";
		boolean lb_addErrorData=false;
		String 	ls_actualErrMsg="";
		
		JSONObject	responseJsonObject;
		JSONObject	mainResponseJsonObject=new JSONObjectImpl();
		JSONArray	responseJlist;
		LoggerImpl loggerImpl=null;
		String 	ls_errorData="";
		
		String [] ls_keyName;
		String ls_keyValue="";
		
		Object	objectJson;
		Object	addErrorData;
		
		try {
			
			loggerImpl=new LoggerImpl();
			//get the json object from response.
			
			loggerImpl.debug(LOGGER, "Preparing data from API response.", "IN:getDbUpdateReqData");
			
			//below code for append the values in json object.
			objectJson=common.getDataAtIndex(0,appendObject);
			addErrorData=common.getDataAtIndex(1,appendObject);
			
			lb_addErrorData=addErrorData==null?false:(Boolean)addErrorData;
			
			if(objectJson != null) {
				responseJsonObject = new JSONObjectImpl(objectJson.toString());
			}else {			
				responseJsonObject = new JSONObjectImpl();
			}
			//End 
			
			JSONObject responseDataJson=common.ofGetJsonObject(as_ResData);
			
			ls_statusCd=responseDataJson.getString("STATUS");
			ls_responsCd=responseDataJson.getString("RESPONSECODE");
			ls_responseMsg=responseDataJson.getString("RESPONSEMESSAGE");		
			
			responseJsonObject.put("API_NAME", as_apiName);
			responseJsonObject.put("RESPONSE_CODE", ls_responsCd);
			responseJsonObject.put("RESPONSE_MSG", ls_responseMsg);
			responseJsonObject.put("FROM_ACCT_NO", as_fromAccount);
			responseJsonObject.put("TO_ACCT_NO", as_toAccount);
			
			if(respDataKeysNames==null) {
				ls_keyName=new String[]{};
			}else {				
				ls_keyName=JSONObject.getNames(respDataKeysNames);
			}
			
			if(isSuccessStCode(ls_statusCd)){
				
				responseJlist=responseDataJson.getJSONArray("RESPONSE");
				
				JSONObject responseJson=new JSONObjectImpl(responseJlist.length() > 0 ? responseJlist.getJSONObject(0).toString() : "{}" );
				
				for (String keyName : ls_keyName) {
					ls_keyValue=respDataKeysNames.getString(keyName);	
					if(StringUtils.isBlank(ls_keyValue)) {
						responseJsonObject.put(keyName, "");	
					}
					else {
						responseJsonObject.put(keyName, responseJson.getString(ls_keyValue));
					}
				}
				
				lb_trnReverse=false;
				
			} else {
				/*New added for store some response data parameter in error response also.*/				
				if(lb_addErrorData){					
					responseJlist=responseDataJson.getJSONArray("RESPONSE");
					
					JSONObject responseJson=new JSONObjectImpl(responseJlist.length() > 0 ? responseJlist.getJSONObject(0).toString() : "{}" );
					
					for (String keyName : ls_keyName) {
						ls_keyValue=respDataKeysNames.getString(keyName);	
						if(StringUtils.isBlank(ls_keyValue)) {
							responseJsonObject.put(keyName, "");	
						}else{
							responseJsonObject.put(keyName, responseJson.optString(ls_keyValue));
						}
					}					
				}else {					
					for (String keyName : ls_keyName) {										
						responseJsonObject.put(keyName, "");					
					}
				}
				
				if(ST9999.equals(ls_responsCd)) {	
					//do not reverse transaction if connection time out error generated.
					lb_trnReverse=false;				
				}else {
					//reverse the transaction if error generated.
					lb_trnReverse=true;
				}			
			}
			
			mainResponseJsonObject.put("REVERSE_TRN",lb_trnReverse);
			mainResponseJsonObject.put("STATUS", ls_statusCd);
			mainResponseJsonObject.put("RESPONSE",new JSONArray().put(responseJsonObject));
			mainResponseJsonObject.put("ACTUAL_RESPONSE",responseDataJson.getJSONArray("RESPONSE"));
			
			loggerImpl.debug(LOGGER, "Data prepared and sending response.", "IN:getDbUpdateReqData");
			
			return mainResponseJsonObject;
		}catch(Exception exception) {
			
			ls_errorData= getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getDbUpdateReqData", "(ENP303)" );						
			return ofGetErrorMsg(ls_errorData,as_apiName,ST999,"Currently Service under maintenance so please try later (ENP303).",respDataKeysNames);
			
		}
	}
	
	
	/***
	 * This method return the error data which is used to update the details in database.
	 * @param msg - Actual response json data
	 * @param apiName -Name of API.
	 * @param as_responseCode -response message code.
	 * @param as_responseMsg -response message.
	 * @param respJsondataKeys -Key name list which is put in json data. 
	 * 
	 * */
	public JSONObject ofGetErrorMsg(String msg,String apiName,String as_responseCode,String as_responseMsg,JSONObject respJsondataKeys) {
				
		JSONObject responseJsonObject=new JSONObject();
		JSONObject mainResponseJsonObject=new JSONObject();	
		
		String[] ls_keyNames;
		
		ls_keyNames=JSONObject.getNames(respJsondataKeys);
		
		//generate the data which send to db API.
		responseJsonObject.put("API_NAME", apiName);
		responseJsonObject.put("RESPONSE_CODE", as_responseCode);
		responseJsonObject.put("RESPONSE_MSG", as_responseMsg);
		responseJsonObject.put("FROM_ACCT_NO", "");
		responseJsonObject.put("TO_ACCT_NO", "");
		
		for (String keyName : ls_keyNames) {										
			responseJsonObject.put(keyName, "");					
		}
		
		//set REVERSE_TRN true for revert previous transaction.
		mainResponseJsonObject.put("REVERSE_TRN", ST9999.equals(as_responseCode)?false:true);
		mainResponseJsonObject.put("STATUS",as_responseCode);
		mainResponseJsonObject.put("RESPONSE",new JSONArray().put(responseJsonObject));
		mainResponseJsonObject.put("ACTUAL_RESPONSE",new JSONArray(msg));
		
		return mainResponseJsonObject;		
	}
	
	/**
	 *Get the request json object of ababil API.
	 *@param ls_refValue -this value put in reference number and value lenght not greater than 10 char. 
	 * */
	public JSONObject ofGetAbabilCommJson(String ls_refValue) {
		
		JSONObject APIrequestDataJson=new JSONObject();
		Instant instant=Instant.now();
		
		APIrequestDataJson.put("requestId",getRequestUniqueData.getUniqueNumber());
		APIrequestDataJson.put("requestDateTime",instant.toString());
		APIrequestDataJson.put("referenceNumber",ls_refValue);
		
		return APIrequestDataJson;
	}

	/***
	 *this method used to get data in format which is used to update into databse. 
	 * */
	public JSONObject getDBupdateData(JSONObject requestData,JSONArray dbRequestDataList,String as_action) {

		String ls_dbProcessAction="";
		JSONObject dbRequestData=new JSONObject();
		String ls_action;
		ls_action =getRequestUniqueData.getLs_action();
		
		if("FUNDTRFVERIFY".equals(ls_action)||"FUNDTRF".equals(ls_action)||"EMAILFUNDPROCESS".equals(ls_action)) {
			ls_dbProcessAction="FUNDTRFUPD";
		}else if("CASHBYCODEVERIFY".equals(ls_action)) {
			ls_dbProcessAction="CASHBYCODEUPD";
		}else if("SCHFUNDTRF".equals(ls_action)) {
			ls_dbProcessAction="UPDSCHDULETRNDTL";
		}else if("INSURANCEREQVERIFY".equals(ls_action)) {
			ls_dbProcessAction="INSURANCEREQUPDATE";
		}else if("QRPROCESS".equals(ls_action)) {
			ls_dbProcessAction="QRUPDATE";
		}else if("QRVERIFY".equals(ls_action)) {
			ls_dbProcessAction="QRUPDATE";			
		}else if("CLUBFEEREQVERIFY".equals(ls_action)) {
			ls_dbProcessAction="CLUBFEEREQUPDATE";
		}else if("CARDPAYOTPVERIFY".equals(ls_action)) {
				ls_dbProcessAction="CARDPAYPROCESS";
		}else if("INTRBILLREQVERIFY".equals(ls_action)) {
			ls_dbProcessAction="INTRBILLREQUPDATE";
		}else if("POSITIVEPAY".equals(ls_action)) {
			ls_dbProcessAction="POSITIVEPAYUPD";
		}else if("VISAPAYREQVERIFY".equals(ls_action)) {
			ls_dbProcessAction="VISAPAYREQUPDATE";
		}else if("FDOTPVERIFY".equals(ls_action)) {
			ls_dbProcessAction="FDPROCESS";
		}else if("DPSOTPVERIFY".equals(ls_action)) {
			ls_dbProcessAction="DPSPROCESS";
		}else if("UTLBILLPAYREQVERIFY".equals(ls_action)) {
			ls_dbProcessAction="UTLBILLPAYREQUPDATE";
		}else if("TUITIONFEEREQVERIFY".equals(ls_action)) {
			ls_dbProcessAction="TUITIONFEEREQUPDATE";
		}else if("MOBRECHRGREQVERIFY".equals(ls_action)) {
			ls_dbProcessAction="MOBRECHRGREQUPDATE";
		}else if("CHQSTOPOTPVERIFY".equals(ls_action)) {
			ls_dbProcessAction="CHQSTOPPROCESS";
		}else if("CHQBKREQ".equals(ls_action)) {
			ls_dbProcessAction="CHQBKREQPROCESS";
		}else if("USERPAYVERIFY".equals(ls_action)) {
			ls_dbProcessAction="USERPAYUPDATE";
		}else {
			ls_dbProcessAction="";
		}
		
		if("UPDSCHDULETRNDTL".equals(ls_dbProcessAction)) {			  
			dbRequestData.put("ACTION",ls_dbProcessAction);			
			dbRequestData.put("BATCH_ID",requestData.getString("BATCH_ID"));
			dbRequestData.put("REQUEST_SR_CD",requestData.getString("REQUEST_SR_CD"));
			dbRequestData.put("USER_NAME",requestData.getString("USER_ID"));			
			dbRequestData.put("REQUEST_CD", requestData.getString("REQUEST_CD"));
			dbRequestData.put("AMOUNT", requestData.getString("AMOUNT"));
			dbRequestData.put("TRN_TYPE", requestData.getString("TRN_TYPE"));
			dbRequestData.put("DISPLAY_LANGUAGE",getRequestUniqueData.getLangCode());
			dbRequestData.put("RESPONSE",dbRequestDataList);
			
		}else if("CHQBKREQPROCESS".equals(ls_dbProcessAction)) {			  
			dbRequestData.put("ACTION",ls_dbProcessAction);						
			dbRequestData.put("ACTIVITY_CD", requestData.getString("ACTIVITY_CD"));
			dbRequestData.put("USER_ID",requestData.getString("USER_ID"));			
			dbRequestData.put("REQUEST_CD", requestData.getString("REQUEST_CD"));			
			dbRequestData.put("DISPLAY_LANGUAGE",getRequestUniqueData.getLangCode());
			dbRequestData.put("RESPONSE",dbRequestDataList);
			
		}else {
			dbRequestData.put("ACTION",ls_dbProcessAction);
			dbRequestData.put("USER_ID", requestData.getString("USER_ID"));
			dbRequestData.put("ACTIVITY_CD", requestData.getString("ACTIVITY_CD"));
			dbRequestData.put("REQUEST_CD", requestData.getString("REQUEST_CD"));
			dbRequestData.put("AMOUNT", requestData.optString("AMOUNT", ""));
			dbRequestData.put("TRN_TYPE", requestData.getString("TRN_TYPE"));
			dbRequestData.put("REQUEST_SR_CD",requestData.optString("REQUEST_SR_CD", "0"));
			dbRequestData.put("DISPLAY_LANGUAGE",getRequestUniqueData.getLangCode());
			dbRequestData.put("RESPONSE",dbRequestDataList);
		}
		return dbRequestData;
	}

	
	
//	public JSONObject doBlockUserDetail(String ls_requestData,String as_blk_desc,String as_process,String as_blk_status) throws Exception{		
//		
//		JSONObjectImpl requestDataJson=new JSONObjectImpl(ls_requestData);
//		String ls_DBresponseData="";
//				
//		requestDataJson.put("AUTH_FLAG",requestDataJson.optString("AUTH_FLAG","USERDTL"));
//		requestDataJson.put("ACTUAL_ACTION", requestDataJson.getString("ACTION"));
//		requestDataJson.put("BLOCK_ACTIVITY_DECRIPTION",as_blk_desc);
//		requestDataJson.put("ACTION", "BLOCK_CP_PROCESS");
//
//		requestDataJson.put("BLOCK_PROCESS_TYPE","V".equals(as_process)?"VERIFY_BLOCK_PROCESS":"UPDATE_BLOCK_STATUS");
//		requestDataJson.put("BLOCK_AUTH_STATUS",as_blk_status);
//		
//		ls_DBresponseData= getDataDB.ofGetResponseData(ConstantValue.DB_API_COMMON_SERVICE,"BLOCK_CP_PROCESS",requestDataJson.toString());
//
//		return common.ofGetJsonObject(ls_DBresponseData);			
//	}
		
	/**
	 *This method used for get the card key detail by calling API. 
	 * @param ls_cardNo -card number.
	 * @param ls_keyName -key name for which you want the data.
	 * @return return the data of key from card detail.
	 * @exception if any exception is generated then ,it throws it.
	 * */
	public String ofGetCardKeyDetail(String ls_cardNo,String ls_keyName)throws Exception {		
		 String ls_responseData="";
		 String ls_status;
		 JSONObject responseDataJson;
		 JSONObjectImpl cardDataErrorMsg;
		 
		 JSONObjectImpl requestAPIDataJson=new JSONObjectImpl();
		 requestAPIDataJson.put("CARD_NO",ls_cardNo);
		 requestAPIDataJson.put("KEYNAME",ls_keyName);
		 requestAPIDataJson.put("ACTION","GETCARDKEYDATA");
		 requestAPIDataJson.put("DISPLAY_LANGUAGE",getRequestUniqueData.getLangCode());			 
		 ls_responseData=callService.doServiceRequest(requestAPIDataJson.toString(),"GETCARDKEYDATA");
		 
		 responseDataJson=common.ofGetJsonObject(ls_responseData);
		 ls_status=responseDataJson.getString("STATUS");
		 
		 if(!ST0.equals(ls_status)){
			 if(responseDataJson.has("ERR_MSG")){
				 return ls_responseData;
			 }else{
				 	cardDataErrorMsg=new JSONObjectImpl();
				 	cardDataErrorMsg.put("KEY_DATA", "");
				 	cardDataErrorMsg.put("ERR_MSG", new JSONArray().put(responseDataJson));										
					
					//return common.ofGetResponseJson(null,"", "",ls_status,"","",cardDataErrorMsg).toString();
				 	return ofGetResponseJson(null,"", "",ls_status,"","",cardDataErrorMsg).toString();
			 }			 			 
		 }else {
			 return ls_responseData;
		 }				 
	} 
	
	/**
	 *This method used for get formated error message when fund trf service API called. 
	 *@param as_responseData -Actual response data,store for future used.
	 *@param as_apiName -Name of name for which this method return the data.
	 * @return return the formated error response data.
	 * @exception if any exception is generated then ,it throws it.
	 * */
	public String getFundTrfFormatedResponse(String as_responseData,String as_apiName)throws Exception {		
		 String ls_statusCd="";
		 String ls_responseCode;
		 JSONObject responseDataJson;
		 String ls_message;		  
		 
		 responseDataJson=common.ofGetJsonObject(as_responseData);
		 ls_responseCode=responseDataJson.optString("RESPONSECODE");
		 ls_statusCd=responseDataJson.getString("STATUS");
		 
		 if(!ST9990.equals(ls_responseCode)){
			 ls_message=responseDataJson.optString("MESSAGE","Error generated at the time of processing request.");			 
		 }else{
			 ls_message="Error generated at the time of calling another service.";
		 }
		 		 		 
		 if(!ST0.equals(ls_statusCd)&&(ST9990.equals(ls_responseCode)|| !responseDataJson.has("REVERSE_TRN"))){			 
			 return ofGetErrorMsg(as_responseData,as_apiName,ls_responseCode,ls_message, getAPIWiseDBReqParameter(as_apiName)).toString();			 
		 }else{
			 return as_responseData;
		 }				 
	}	
	
	/**
	 *This method used for get the request parameter of DB API as per api name.
	 *@param as_apiName -API name for which this method return the parameter list. 
	 * @exception if any exception is generated then this method throws it.
	 * */
	public JSONObject getAPIWiseDBReqParameter(String as_apiName) throws Exception {
		
		JSONObjectImpl returnResKeyDataJson=new JSONObjectImpl();
			
		if("doFinacleTransactionFI".equals(as_apiName) ||
			"doFinacleTransactionReversalFI".equals(as_apiName)
				){
			returnResKeyDataJson.put("REF_TRAN_ID", "");			
		}					
		return returnResKeyDataJson;
	}
	
	/***
	 *This method call the respective API and format the response data. 
	 * @param as_requestData -request data for API.
	 * @param as_actioName -Action name of API.
	 * @param as_apiName -API name.
	 * @exception If exception generated then it throws the exception.
	 */
	public String ofCallAPIAndFormatData(String as_requestData,String as_actioName,String as_apiName) throws Exception {
		String ls_apiResponseData;				
		ls_apiResponseData=callService.doServiceRequest(as_requestData,as_actioName);
		return getFundTrfFormatedResponse(ls_apiResponseData,as_apiName);
	}
}

