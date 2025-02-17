package com.easynet.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.client.SoapFaultClientException;

import com.easynet.bean.GetRequestUniqueData;
import com.easynet.configuration.PropConfiguration;
import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.LoggerImpl;

/**
 * This is a base class used for specify common functionality to all classes.
 * 
 * @author Sagar Umate @date-05/02/2022
 * 
 **/
@Component
public class CommonBase {

	public final static String ST0 = "0";
	public final static String ST99 = "99";
	public final static String ST999 = "999";
	public final static String ST9999 = "9999";
	public final static String ST9990 = "9990";
	public final static String ST99999 = "99999";
	public final static String ST9991 = "9991";
	public final static String ST9992 = "9992";
	public final static String ST9993 = "9993";
	public final static String ST100 = "100";
	public final static String ST200 = "200";

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private GetRequestUniqueData getRequestUniqueData;

	@Autowired
	PropConfiguration propConfiguration;

	@Autowired
	JdbcTemplate jdbcTemplate;

	/***
	 * Thsi method return the database connection with auto commit false parameter.
	 * 
	 * @return Connection Object.
	 * @exception throws any exception generated in getting connection.
	 */
	public Connection getDbConnection() throws SQLException {
		Connection connection = jdbcTemplate.getDataSource().getConnection();
		connection.setAutoCommit(false);
		return connection;
	}

	public void closeDbObject(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				/* ignored */
			}
		}
	}

	// This method return proper sql message only.

	public String removeORACodes(String as_inputString) {

		String ls_msg = StringUtils.EMPTY;
		String ls_inputMsg = as_inputString;
		int li_start = ls_inputMsg.indexOf("ORA-");

		if (li_start == -1) {
			return as_inputString;
		}

		String ls_userMsg = ls_inputMsg.substring(0, li_start);
		ls_inputMsg = ls_inputMsg.substring(li_start + 10);
		int li_list = ls_inputMsg.indexOf("ORA-");

		ls_msg = ls_inputMsg.substring(0, (li_list == -1 ? ls_inputMsg.length() : li_list));

		return (ls_userMsg.isEmpty() ? "" : ls_userMsg + "\n") + ls_msg.trim();
	}

	public void closeDbObject(CallableStatement callableStatement) {
		if (callableStatement != null) {
			try {
				callableStatement.close();
			} catch (SQLException e) {
				/* ignored */
			}
		}
	}

	public void closeDbObject(CallableStatement callableStatement, ResultSet resultSet) {
		closeDbObject(callableStatement);

		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				/* ignored */
			}
		}
	}

	public void closeDbObject(PreparedStatement preparedStatement, ResultSet resultSet) {
		closeDbObject(preparedStatement);

		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				/* ignored */
			}
		}
	}

	public void closeDbObject(PreparedStatement preparedStatement) {
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				/* ignored */
			}
		}
	}

	public void closeDbObject(Connection connection, ResultSet resultSet, CallableStatement callableStatement) {
		closeDbObject(connection);
		closeDbObject(callableStatement, resultSet);
	}

	public void closeDbObject(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement) {
		closeDbObject(connection);
		closeDbObject(preparedStatement, resultSet);
	}

	public void closeDbObject(Connection connection, CallableStatement callableStatement) {
		closeDbObject(connection);
		closeDbObject(callableStatement);
	}

	public void closeDbObject(Connection connection, PreparedStatement preparedStatement) {
		closeDbObject(connection);
		closeDbObject(preparedStatement);
	}

	/**
	 * This method return the soap exception message in JSON format data.
	 * 
	 * @param soapException -Soap Exception object of type @SoapFaultClientException
	 * @param LOGGER        -Logger object created the child class.
	 * @param loggerImpl    -LoggerImpl object created in child class.
	 * @param as_apiName    -name of Internal API to write into log.
	 * @param asMsgCode     -Error message code for identification of MSG.
	 * @param as_userMsg    -custome error MSG for append before exception message.
	 * @param msgFlag       -flag to get the message from configuration as below
	 *                      <br>
	 *                      1.C-common API messages. <br>
	 *                      2.A-Ababil API messages. <br>
	 *                      3.F-Finacle API messages <br>
	 *                      if not passing any values the default value will be "F".
	 * 
	 * @return return the string JSON data.
	 */
	public final String getSoapExceptionMSg(SoapFaultClientException soapException, Logger LOGGER,
			LoggerImpl loggerImpl, String as_apiName, String asMsgCode, String as_userMsg, String msgFlag) {
		String ls_actualErrMsg;
		String ls_responseData;

		if (StringUtils.isBlank(as_apiName))
			as_apiName = "";
		if (StringUtils.isBlank(asMsgCode))
			asMsgCode = "";
		if (StringUtils.isBlank(as_userMsg))
			as_userMsg = "";
		if (StringUtils.isBlank(msgFlag))
			msgFlag = "F";

		ls_actualErrMsg = soapException.getFaultStringOrReason();
		ls_responseData = common.ofGetErrDataJsonArray(ST999,
				propConfiguration.getMessageOfResCode("common.title." + ST999, "Alert.", "", msgFlag),
				propConfiguration.getMessageOfResCode("common.exception." + ST999, "", asMsgCode, msgFlag),
				ls_actualErrMsg,
				propConfiguration.getMessageOfResCode("common.exception." + ST999, "", asMsgCode, msgFlag, "EN", "Y"),
				ST999, "R", getClassNameAndMethodName());

		ls_actualErrMsg = common.ofGetTotalErrString(soapException, as_userMsg);
		loggerImpl.error(LOGGER, "Exception : " + ls_actualErrMsg, as_apiName);
		return ls_responseData;
	}

	/**
	 * This method return the soap exception message in JSON format data.
	 * 
	 * @param soapException -Soap Exception object of type @SoapFaultClientException
	 * @param LOGGER        -Logger object created the child class.
	 * @param loggerImpl    -LoggerImpl object created in child class.
	 * @param as_apiName    -name of Internal API to write into log.
	 * @param asMsgCode     -Error message code for identification of MSG.
	 * @return return the string JSON data.
	 */
	public final String getSoapExceptionMSg(SoapFaultClientException soapException, Logger LOGGER,
			LoggerImpl loggerImpl, String as_apiName, String asMsgCode) {
		return getSoapExceptionMSg(soapException, LOGGER, loggerImpl, as_apiName, asMsgCode, null, null);
	}

	/**
	 * This method return the exception message in JSON format data.
	 * 
	 * @param exception  -Exception object of type @Exception
	 * @param LOGGER     -Logger object created the child class.
	 * @param loggerImpl -LoggerImpl object created in child class.
	 * @param as_apiName -name of Internal API to write into log.
	 * @param asMsgCode  -Error message code for identification of MSG.
	 * @param as_userMsg -custome error MSG for append before exception message.
	 * @param msgFlag    -flag to get the message from configuration as below <br>
	 *                   1.C-common API messages. <br>
	 *                   2.A-Ababil API messages. <br>
	 *                   3.F-Finacle API messages <br>
	 *                   if not passing any values the default value will be "F".
	 * @return return the string JSON data.
	 */
	public final String getExceptionMSg(Exception exception, Logger LOGGER, LoggerImpl loggerImpl, String as_apiName,
			String asMsgCode, String as_userMsg, String msgFlag) {
		String ls_actualErrMsg;
		String ls_defaultErrMsg = null;
		String ls_errorMsg = null;
		String ls_responseData;

		if (StringUtils.isBlank(as_apiName))
			as_apiName = "";
		if (StringUtils.isBlank(asMsgCode))
			asMsgCode = "";
		if (StringUtils.isBlank(as_userMsg))
			as_userMsg = "";
		if (StringUtils.isBlank(msgFlag))
			msgFlag = "F";

		if (exception instanceof JSONException) {
			JSONException jsonException = (JSONException) exception;
			ls_defaultErrMsg = jsonException.getMessage();
		} else if (exception instanceof SQLException) {
			SQLException sqlException = (SQLException) exception;
			ls_defaultErrMsg = removeORACodes(exception.getMessage());

		} else {
			ls_errorMsg = propConfiguration.getMessageOfResCode("common.exception." + ST999, "", asMsgCode, msgFlag);
			ls_defaultErrMsg = ls_errorMsg;
		}
		ls_actualErrMsg = common.ofGetTotalErrString(exception, as_userMsg);
		loggerImpl.error(LOGGER, "Exception : " + ls_actualErrMsg, as_apiName);
		ls_responseData = common.ofGetErrDataJsonArray(ST999,
				propConfiguration.getMessageOfResCode("common.title." + ST999, "Alert.", "", msgFlag),
				propConfiguration.getMessageOfResCode("common.exception." + ST999, "", asMsgCode, msgFlag),
				exception.getMessage(), ls_defaultErrMsg, ST999, "R", getClassNameAndMethodName());

		return ls_responseData;
	}

	/**
	 * This method return the exception message in JSON format data.
	 * 
	 * @param exception  -Exception object of type @Exception
	 * @param LOGGER     -Logger object created the child class.
	 * @param loggerImpl -LoggerImpl object created in child class.
	 * @param as_apiName -name of Internal API to write into log.
	 * @param asMsgCode  -Error message code for identification of MSG.
	 * @return return the string JSON data.
	 */
	public final String getExceptionMSg(Exception exception, Logger LOGGER, LoggerImpl loggerImpl, String as_apiName,
			String asMsgCode) {

		return getExceptionMSg(exception, LOGGER, loggerImpl, as_apiName, asMsgCode, null, null);
	}

	/**
	 * This method return the timeout exception message in JSON format data.
	 * 
	 * @param exception  -Exception object of type @Exception
	 * @param LOGGER     -Logger object created the child class.
	 * @param loggerImpl -LoggerImpl object created in child class.
	 * @param as_apiName -name of Internal API to write into log.
	 * @param asMsgCode  -Error message code for identification of MSG.
	 * @param as_userMsg -custome error MSG for append before exception message.
	 * @param msgFlag    -flag to get the message from configuration as below <br>
	 *                   1.C-common API messages. <br>
	 *                   2.A-Ababil API messages. <br>
	 *                   3.F-Finacle API messages <br>
	 *                   if not passing any values the default value will be "F".
	 * @return return the string JSON data.
	 */
	public final String getTimeOutExceptionMSg(Exception exception, Logger LOGGER, LoggerImpl loggerImpl,
			String as_apiName, String asMsgCode, String as_userMsg, String msgFlag) {
		String ls_actualErrMsg;
		String ls_responseData;

		if (StringUtils.isBlank(as_apiName))
			as_apiName = "";
		if (StringUtils.isBlank(asMsgCode))
			asMsgCode = "";
		if (StringUtils.isBlank(as_userMsg))
			as_userMsg = "";
		if (StringUtils.isBlank(msgFlag))
			msgFlag = "F";

		ls_actualErrMsg = common.ofGetTotalErrString(exception, as_userMsg);
		loggerImpl.error(LOGGER, "IO Exception : " + ls_actualErrMsg, as_apiName);
		ls_responseData = common.ofGetErrDataJsonArray(ST999,
				propConfiguration.getMessageOfResCode("common.title." + ST9999, "Alert.", "", msgFlag),
				propConfiguration.getMessageOfResCode("common.exception." + ST9999, "", asMsgCode, msgFlag),
				exception.getMessage(),
				propConfiguration.getMessageOfResCode("common.exception." + ST9999, "", asMsgCode, msgFlag, "EN", "Y"),
				ST9999, "R", getClassNameAndMethodName());

		return ls_responseData;
	}

	/**
	 * This method return the timeout exception message in JSON format data.
	 * 
	 * @param exception  -Exception object of type @Exception
	 * @param LOGGER     -Logger object created the child class.
	 * @param loggerImpl -LoggerImpl object created in child class.
	 * @param as_apiName -name of Internal API to write into log.
	 * @param asMsgCode  -Error message code for identification of MSG.
	 * 
	 * @return return the string JSON data.
	 */
	public final String getTimeOutExceptionMSg(Exception exception, Logger LOGGER, LoggerImpl loggerImpl,
			String as_apiName, String asMsgCode) {

		return getTimeOutExceptionMSg(exception, LOGGER, loggerImpl, as_apiName, asMsgCode, null, null);
	}

	/**
	 * This method stop the child profile and print the info logs.
	 * 
	 * @param LOGGER     -Logger object created the child class.
	 * @param loggerImpl -LoggerImpl object created in child class.
	 * @param as_apiName -name of Internal API to write into log.
	 */
	public final void doStopProfileAndPrintLogs(Logger LOGGER, LoggerImpl loggerImpl, String as_apiName) {

		if (StringUtils.isBlank(as_apiName))
			as_apiName = "";

		loggerImpl.stopAndPrintOptLogs(LOGGER, "All API called successfully.", as_apiName);
		loggerImpl.info(LOGGER, "Response generated and send to client.", as_apiName);
	}

	/**
	 * // * This method return the failed message for response code other than 100.
	 * // * @param asFailCode -Name of external API from which system will read the
	 * message from properties files. // * @param asResponseCode -Response code of
	 * external API. // * @param asResponseMessage -Response message of external
	 * API. // * @param asMsgCode -Message code for identification. // * @return
	 * return the string format JSON data. // *
	 **/
	public final String ofGetFailedMSg(String asFailCode, String asResponseCode, String asResponseMessage,
			String asMsgCode) {

		return ofGetFailedMSg(asFailCode, asResponseCode, asResponseMessage, asMsgCode, null);
	}

	/**
	 * This method return the failed message for response code other than 100.
	 * 
	 * @param asFailCode        -Name of external API from which system will read
	 *                          the message from properties files.
	 * @param asResponseCode    -Response code of external API.
	 * @param asResponseMessage -Response message of external API.
	 * @param asMsgCode         -Message code for identification.
	 * @param asDefaultMsg      -default message,if found null then put default ENG
	 *                          MSG.
	 * 
	 * @return return the string format JSON data.
	 **/
	public final String ofGetFailedMSg(String asFailCode, String asResponseCode, String asResponseMessage,
			String asMsgCode, String asDefaultMsg) {

		String callerClassName = "";
		String ls_enMessage = "";
		String ls_CodeMsg = "";

		String ls_langCode = "";

		ls_langCode = getRequestUniqueData.getLangCode();

		ls_enMessage = asDefaultMsg;

		callerClassName = getClassNameAndMethodName();

		JSONObjectImpl errorObject = new JSONObjectImpl();

		// Set the default message in message data not found.
		if (StringUtils.isBlank(asFailCode)) {
			ls_CodeMsg = messageSource.getMessage("common.default.FailedMsg", null, "",
					propConfiguration.getLocalObj(ls_langCode));

			if (StringUtils.isBlank(asDefaultMsg)) {
				ls_enMessage = messageSource.getMessage("common.default.FailedMsg", null, "",
						propConfiguration.getLocalObj("en"));
				errorObject.put("MESSAGE_EN", ls_enMessage);
			} else {
				errorObject.put("MESSAGE_EN", ls_enMessage);
			}
			errorObject.put("MESSAGE", ls_CodeMsg);

		} else {

			ls_CodeMsg = messageSource.getMessage(asFailCode, null, "", propConfiguration.getLocalObj(ls_langCode));

			if (StringUtils.isBlank(asDefaultMsg)) {
				ls_enMessage = messageSource.getMessage(asFailCode, null, "", propConfiguration.getLocalObj("en"));
				errorObject.put("MESSAGE_EN", ls_enMessage);
			} else {
				errorObject.put("MESSAGE_EN", ls_enMessage);
			}
			errorObject.put("MESSAGE", ls_CodeMsg);

		}

		errorObject.put("RESPONSECODE", asResponseCode);
		errorObject.put("ERR_PATH", callerClassName);
		errorObject.put("STATUS", asResponseCode);
		// errorObject.put("MESSAGE_EN", ls_enMessage);
		// errorObject.put("RESPONSE", errorListJsonObject);
		errorObject.put("RESPONSEMESSAGE", asResponseMessage);

		return errorObject.toString();
	}

	/**
	 * // * This method return the failed message for response code other than 100.
	 * // * @param as_apiName -Name of external API from which system will read the
	 * message from properties files. // * @param asResponseCode -Response code of
	 * external API. // * @param asResponseMessage -Response message of external
	 * API. // * @param asMsgCode -Message code for identification. // * @return
	 * return the string format JSON data.
	 * 
	 * 
	 * public final String ofGetFailedMsg(String as_apiName,String
	 * asResponseCode,String asResponseMessage,String asMsgCode){
	 * 
	 * return
	 * ofGetFailedMsg(as_apiName,asResponseCode,asResponseMessage,asMsgCode,null); }
	 * 
	 * /** This method return the failed message for response code other than 100.
	 * 
	 * @param as_apiName        -Name of external API from which system will read
	 *                          the message from properties files.
	 * @param asResponseCode    -Response code of external API.
	 * @param asResponseMessage -Response message of external API.
	 * @param asMsgCode         -Message code for identification.
	 * @param msgFlag           -flag to get the message from configuration as below
	 *                          <br>
	 *                          1.C-common API messages. <br>
	 *                          2.A-Ababil API messages. <br>
	 *                          3.F-Finacle API messages <br>
	 *                          if not passing any values the default value will be
	 *                          "F".
	 * 
	 * @return return the string format JSON data.
	 * 
	 * 
	 *         public final String ofGetFailedMsg(String as_apiName,String
	 *         asResponseCode,String asResponseMessage,String asMsgCode,String
	 *         msgFlag){
	 * 
	 *         return
	 *         ofGetFailedMsg(as_apiName,asResponseCode,asResponseMessage,asMsgCode,msgFlag,null,null);
	 *         }
	 * 
	 *         /** This method return the failed message for response code other
	 *         than 100.
	 * @param as_apiName           -Name of external API from which system will read
	 *                             the message from properties files.
	 * @param asResponseCode       -Response code of external API.
	 * @param asResponseMessage    -Response message of external API.
	 * @param asMsgCode            -Message code for identification.
	 * @param msgFlag              -flag to get the message from configuration as
	 *                             below <br>
	 *                             1.C-common API messages. <br>
	 *                             2.A-Ababil API messages. <br>
	 *                             3.F-Finacle API messages <br>
	 *                             if not passing any values the default value will
	 *                             be "F".
	 * @param asAppendResCodeValue -Pass the value,which want to append to the key
	 *                             name of get message.
	 * @param asDefaultMsg         -default message,if found null then put default
	 *                             ENG MSG.
	 * 
	 * @return return the string format JSON data.
	 * 
	 * 
	 *         public final String ofGetFailedMsg(String as_apiName,String
	 *         asResponseCode,String asResponseMessage,String asMsgCode,String
	 *         msgFlag,String asAppendResCodeValue,String asDefaultMsg){
	 * 
	 *         String ls_langResCodeMsg; String ls_resCodeAppendValue;
	 * 
	 *         if(StringUtils.isBlank(msgFlag)) msgFlag="F";
	 *         if(StringUtils.isBlank(asAppendResCodeValue))
	 *         asAppendResCodeValue="";
	 * 
	 *         if(StringUtils.isBlank(as_apiName)) as_apiName="";
	 *         if(StringUtils.isBlank(asResponseCode)) asResponseCode="";
	 *         if(StringUtils.isBlank(asResponseMessage)) asResponseMessage="";
	 *         if(StringUtils.isBlank(asMsgCode)) asMsgCode="";
	 * 
	 *         if(StringUtils.isBlank(asResponseCode)) { ls_resCodeAppendValue="";
	 *         }else { ls_resCodeAppendValue="."+asResponseCode; }
	 * 
	 *         ls_langResCodeMsg=propConfiguration.getResponseCode(as_apiName+ls_resCodeAppendValue,msgFlag);
	 * 
	 *         if(StringUtils.isBlank(asResponseCode)) {
	 *         asResponseCode=ls_langResCodeMsg; }
	 * 
	 *         if(StringUtils.isBlank(asDefaultMsg)) {
	 *         asDefaultMsg=propConfiguration.getMessageOfResCode(as_apiName+ls_resCodeAppendValue+asAppendResCodeValue,"",asMsgCode,msgFlag,"EN","Y");
	 *         }
	 * 
	 *         return common.ofGetErrDataJsonArray(ls_langResCodeMsg,
	 *         propConfiguration.getMessageOfResCode("common.title."+ls_langResCodeMsg,
	 *         "Alert.","",msgFlag),
	 *         propConfiguration.getMessageOfResCode(as_apiName+ls_resCodeAppendValue+asAppendResCodeValue,"",asMsgCode,msgFlag),
	 *         asResponseMessage, asDefaultMsg,asResponseCode,
	 *         "R",getClassNameAndMethodName()); }
	 **/

	public static Object getDataAtIndex(int index, Object... object) {

		int lenght = object == null ? 0 : object.length;
		if (index <= lenght - 1) {
			return object[index];
		} else {
			return null;
		}
	}

	private String ls_DefaultSucMsg = "Success.";

	/**
	 * This method is used to common response for all API.
	 **/
	public final JSONObject ofGetResponseJson(JSONArray responseJlist, String responseCode, String responseMessage,
			Object... values) throws Exception {

		Object objectJson;
		String ls_statusCodeObj;
		String ls_colorObj;
		String ls_messageObj;

		Object statusCodeObj;
		Object colorObj;
		Object as_apiName;

		String ls_langCode;
		String ls_returnValue = "";

		JSONObject responseJsonObject = null;

		ls_langCode = getRequestUniqueData.getLangCode();

		objectJson = getDataAtIndex(3, values);

		responseJsonObject = new JSONObject();

		statusCodeObj = getDataAtIndex(0, values);
		colorObj = getDataAtIndex(1, values);
		as_apiName = getDataAtIndex(2, values);

		ls_statusCodeObj = statusCodeObj == null || String.valueOf(statusCodeObj).equals("") ? CommonBase.ST0
				: String.valueOf(statusCodeObj);
		// ls_colorObj=colorObj==null||String.valueOf(colorObj).equals("")?"G":String.valueOf(colorObj);
		ls_messageObj = as_apiName == null ? "" : String.valueOf(as_apiName);

		// Set the default message in message data not found.
		if (StringUtils.isBlank(ls_messageObj)) {

			if (ls_statusCodeObj.equals("0")) {
				ls_returnValue = messageSource.getMessage("common.default.SucMsg", null, "",
						propConfiguration.getLocalObj(ls_langCode));
			} else
				ls_returnValue = ls_messageObj;

			if (StringUtils.isBlank(responseMessage)) {
				responseJsonObject.put("RESPONSEMESSAGE", ls_DefaultSucMsg);
			} else
				responseJsonObject.put("RESPONSEMESSAGE", StringUtils.isBlank(responseMessage) ? "" : responseMessage);

		} else {
			ls_returnValue = messageSource.getMessage(ls_messageObj, values, "",
					propConfiguration.getLocalObj(ls_langCode));
			if (StringUtils.isBlank(ls_returnValue)) {
				ls_returnValue = ls_messageObj;
			}

			responseJsonObject.put("RESPONSEMESSAGE", StringUtils.isBlank(responseMessage) ? "" : responseMessage);

		}

		responseJsonObject.put("STATUS", ls_statusCodeObj);
		// responseJsonObject.put("COLOR",ls_colorObj);
		responseJsonObject.put("RESPONSE", responseJlist == null ? new JSONArray() : responseJlist);
		responseJsonObject.put("MESSAGE", ls_returnValue);
		responseJsonObject.put("RESPONSECODE", StringUtils.isBlank(responseCode) ? "" : responseCode);
		// responseJsonObject.put("RESPONSEMESSAGE",StringUtils.isBlank(responseMessage)?"":responseMessage);

		return responseJsonObject;
	}

	public final JSONObject ofGetResponseJson(JSONArrayImpl responseJlist, String responseCode, String responseMessage,
			Object... values) throws Exception {

		Object objectJson;
		String ls_statusCodeObj;
		String ls_colorObj;
		String ls_messageObj;

		Object statusCodeObj;
		Object colorObj;
		Object as_apiName;

		String ls_langCode;
		String ls_returnValue = "";

		JSONObject responseJsonObject = null;

		ls_langCode = getRequestUniqueData.getLangCode();

		objectJson = getDataAtIndex(3, values);

		responseJsonObject = new JSONObject();

		statusCodeObj = getDataAtIndex(0, values);
		colorObj = getDataAtIndex(1, values);
		as_apiName = getDataAtIndex(2, values);

		ls_statusCodeObj = statusCodeObj == null || String.valueOf(statusCodeObj).equals("") ? CommonBase.ST0
				: String.valueOf(statusCodeObj);
		// ls_colorObj=colorObj==null||String.valueOf(colorObj).equals("")?"G":String.valueOf(colorObj);
		ls_messageObj = as_apiName == null ? "" : String.valueOf(as_apiName);

		// Set the default message in message data not found.
		if (StringUtils.isBlank(ls_messageObj)) {

			if (ls_statusCodeObj.equals("0")) {
				ls_returnValue = messageSource.getMessage("common.default.SucMsg", null, "",
						propConfiguration.getLocalObj(ls_langCode));
			} else
				ls_returnValue = ls_messageObj;

			if (StringUtils.isBlank(responseMessage)) {
				responseJsonObject.put("RESPONSEMESSAGE", ls_DefaultSucMsg);
			} else
				responseJsonObject.put("RESPONSEMESSAGE", StringUtils.isBlank(responseMessage) ? "" : responseMessage);

		} else {
			ls_returnValue = messageSource.getMessage(ls_messageObj, values, "",
					propConfiguration.getLocalObj(ls_langCode));
			if (StringUtils.isBlank(ls_returnValue)) {
				ls_returnValue = ls_messageObj;
			}

			responseJsonObject.put("RESPONSEMESSAGE", StringUtils.isBlank(responseMessage) ? "" : responseMessage);

		}

		responseJsonObject.put("STATUS", ls_statusCodeObj);
		// responseJsonObject.put("COLOR",ls_colorObj);
		responseJsonObject.put("RESPONSE", responseJlist == null ? new JSONArray() : responseJlist);
		responseJsonObject.put("MESSAGE", ls_returnValue);
		responseJsonObject.put("RESPONSECODE", StringUtils.isBlank(responseCode) ? "" : responseCode);
		// responseJsonObject.put("RESPONSEMESSAGE",StringUtils.isBlank(responseMessage)?"":responseMessage);

		return responseJsonObject;
	}

	/**
	 * @param aErrorTitle -Error message title.
	 * @param aErrorMsg   -Error message which is shown to user.
	 * @param aError      -Actual error message.
	 * @return -Return String value of json format.
	 */
	public final JSONArray ofGetErrDataJson(String aErrorTitle, String aErrorMsg, String aError) {

		JSONObject errorObject = new JSONObject();
		if (aError != null && !"".equals(aError)) {
			if (aError.trim().substring(0, 1).equals("[") && common.ofvalidateJSonArrayData(aError)) {
				errorObject.put("ERROR", new JSONArray(aError));
			} else if (aError.trim().substring(0, 1).equals("{")) {
				errorObject.put("ERROR", new JSONObject(aError));
			} else {
				errorObject.put("ERROR", aError);
			}
		} else {
			errorObject.put("ERROR", "");
		}

		errorObject.put("ERROR_TITLE", aErrorTitle);
		errorObject.put("ERROR_MSG", aErrorMsg);

		JSONArray errorListObject = new JSONArray();
		errorListObject.put(errorObject);
		return errorListObject;
	}

	/**
	 * @param Errorcode    -code message which taken from properties file which is
	 *                     shown to user.
	 * @param asDefaultMsg - default English Message.
	 * @param aErrorStatus - Error Status for identification of error.
	 * @param aError       -Actual error message.
	 * @param aErrorTitle  -Error message title.
	 * @param object       - Multiple object value.<br>
	 *                     1.Caller path. 2.message in ENG language.
	 * @return -Return String value of JSONLIst format.
	 */
	public final String ofGetErrDataJsonObject(String Errorcode, String asDefaultMsg, String aErrorStatus,
			String aError, String aErrorTitle, Object... object) {

		String callerClassName = "";
		String ls_enMessage = "";
		String ls_CodeMsg = "";

		String ls_langCode = "";

		ls_langCode = getRequestUniqueData.getLangCode();

		ls_enMessage = asDefaultMsg;

		callerClassName = getClassNameAndMethodName();

		JSONObjectImpl errorObject = new JSONObjectImpl();
		JSONArray errorListJsonObject = new JSONArray();

		// Set the default message in message data not found.
		if (StringUtils.isBlank(Errorcode)) {
			ls_CodeMsg = messageSource.getMessage("common.default.ErrMsg", null, "",
					propConfiguration.getLocalObj(ls_langCode));
			// ls_enMessage=messageSource.getMessage("common.default.ErrMsg",null,"",propConfiguration.getLocalObj("en"));

			if (StringUtils.isBlank(asDefaultMsg)) {
				ls_enMessage = messageSource.getMessage("common.default.ErrMsg", null, "",
						propConfiguration.getLocalObj("en"));
				errorObject.put("MESSAGE_EN", ls_enMessage);
			} else {
				errorObject.put("MESSAGE_EN", ls_enMessage);
			}
			errorObject.put("MESSAGE", ls_CodeMsg);

		} else {

			ls_CodeMsg = messageSource.getMessage(Errorcode, object, "", propConfiguration.getLocalObj(ls_langCode));

			if (StringUtils.isBlank(asDefaultMsg)) {
				ls_enMessage = messageSource.getMessage(Errorcode, object, "", propConfiguration.getLocalObj("en"));
				errorObject.put("MESSAGE_EN", ls_enMessage);
			} else {
				errorObject.put("MESSAGE_EN", ls_enMessage);
			}
			errorObject.put("MESSAGE", ls_CodeMsg);

		}

		errorListJsonObject = ofGetErrDataJson(aErrorTitle, ls_CodeMsg, aError);

		errorObject.put("ERR_PATH", callerClassName);
		errorObject.put("STATUS", aErrorStatus);
		errorObject.put("RESPONSE", errorListJsonObject);
		errorObject.put("RESPONSEMESSAGE", aError);
		// errorObject.put("MESSAGE_EN", ls_enMessage);

		return errorObject.toString();
	}

	/***
	 * This function used for check the response code is of success or failed.
	 * 
	 * @param asResponseCode -Response code of external API.
	 * @return boolean values.
	 */
	public final boolean isSuccessResCode(String asResponseCode) {

		if (StringUtils.isNotBlank(asResponseCode) && ST100.equals(asResponseCode)) {
			return true;
		} else {
			return false;
		}
	}

	/***
	 * This function used for check the 200 response code is of success or failed.
	 * 
	 * @param asResponseCode -Response code of external API.
	 * @return boolean values.
	 */
	public final boolean isSuccess200ResCode(String asResponseCode) {

		if (StringUtils.isNotBlank(asResponseCode) && ST200.equals(asResponseCode)) {
			return true;
		} else {
			return false;
		}
	}

	/***
	 * This function used for check the status code is success or not.
	 * 
	 * @param asStatus -status code of internal API.
	 * @return boolean values.
	 */
	public final boolean isSuccessStCode(String asStatus) {

		if (StringUtils.isNotBlank(asStatus) && ST0.equals(asStatus)) {
			return true;
		} else {
			return false;
		}
	}

	/****
	 * This method used for get the class name and method name of calling class and
	 * method.
	 * 
	 */
	private String getClassNameAndMethodName() {
		String callerClassName = "";
		String methodname = "";
		String[] callerClassNameList;
		int callerClassNameCnt;

		try {
			StackTraceElement stackTraceElement[] = new Exception().getStackTrace();
			callerClassName = stackTraceElement[2].getClassName();
			methodname = stackTraceElement[2].getMethodName();

			callerClassNameList = callerClassName.split("[.]");
			callerClassNameCnt = callerClassNameList.length;

			if (callerClassNameCnt == 1) {
				callerClassName = callerClassNameList[0] + "/" + methodname;
			} else if (callerClassNameCnt >= 2) {
				callerClassName = callerClassNameList[callerClassNameCnt - 2] + "/"
						+ callerClassNameList[callerClassNameCnt - 1] + "/" + methodname;
			}

		} catch (Exception err) {
			callerClassName = "Error in get class name.";
		}
		return callerClassName;
	}

	/**
	 * This method check the blank data and return the formated MSG.
	 * 
	 * @return return the MSG if any data is blank ,otherwise return empty string.
	 */
	public final String doCheckBlankData(CharSequence... css) {
		if (StringUtils.isAnyBlank(css)) {
			return common.ofGetErrDataJsonArray(ST99,
					propConfiguration.getMessageOfResCode("common.title." + ST99, "Validation Failed."),
					propConfiguration.getMessageOfResCode("common.invalid_req_data", "Invalid Request."),
					"Null values found in request data.",
					propConfiguration.getMessageOfResCode("common.invalid_req_data", "Invalid Request.", "", "", "EN"),
					ST99, "R", getClassNameAndMethodName());
		} else {
			return "";
		}
	}

	/**
	 * This method check the blank data and return the formated MSG.
	 * 
	 * @return return the MSG if any data is blank ,otherwise return empty string.
	 */
	public final String doCheckBlankDataWithDefMSg(String asDefaultMsg, CharSequence... css) {
		if (StringUtils.isBlank(asDefaultMsg))
			asDefaultMsg = "Null values found in request data.";

		if (StringUtils.isAnyBlank(css)) {
			return common.ofGetErrDataJsonArray(ST99,
					propConfiguration.getMessageOfResCode("common.title." + ST99, "Validation Failed."),
					propConfiguration.getMessageOfResCode("common.invalid_req_data", "Invalid Request."), asDefaultMsg,
					propConfiguration.getMessageOfResCode("common.invalid_req_data", "Invalid Request.", "", "", "EN"),
					ST99, "R", getClassNameAndMethodName());
		} else {
			return "";
		}
	}

	/**
	 * This method return the wrong source type MSG.
	 * 
	 */
	public String getWrongSourceTypeMsg(String asMsgCode) {
		if (StringUtils.isBlank(asMsgCode))
			asMsgCode = "";

		return common.ofGetErrDataJsonArray(ST999,
				propConfiguration.getMessageOfResCode("common.title." + ST999, "Alert."),
				propConfiguration.getMessageOfResCode("common.invalid_source", "", asMsgCode),
				"Wrong source type found.",
				propConfiguration.getMessageOfResCode("common.invalid_source", "", asMsgCode, "", "EN", "Y"), ST999,
				"R", getClassNameAndMethodName());
	}

	/**
	 * This method return the wrong account type MSG.
	 * 
	 */
	public String getWrongAcctTypeMsg(String asMsgCode) {
		if (StringUtils.isBlank(asMsgCode))
			asMsgCode = "";

		return common.ofGetErrDataJsonArray(ST999,
				propConfiguration.getMessageOfResCode("common.title." + ST999, "Alert."),
				propConfiguration.getMessageOfResCode("common.invalid_req_data", "", asMsgCode),
				"Wrong acct type found.",
				propConfiguration.getMessageOfResCode("common.invalid_req_data", "", asMsgCode, "", "EN", "Y"), ST99,
				"R", getClassNameAndMethodName());
	}

	/**
	 * This method return the wrong account type MSG.
	 * 
	 */
	public String getNoAPIFoundMsg() {
		return common.ofGetErrDataJsonArray(ST999,
				propConfiguration.getMessageOfResCode("common.title." + ST999, "Alert."),
				propConfiguration.getMessageOfResCode("common.exception." + ST999, ""),
				"No API found for this request parameter.",
				propConfiguration.getMessageOfResCode("common.exception." + ST999, "", "", "", "EN"), ST999, "R",
				getClassNameAndMethodName());
	}

	public String getConfigKeyValue(String as_APIName, String as_serviceName, String as_basePath, String as_keyName)
			throws Exception {

		return common.getPathKeyValue(as_APIName, as_serviceName, as_basePath, as_keyName);
	}

	public String getFinacleApiUsername(String as_APIName, String as_serviceName) throws Exception {

		return getConfigKeyValue(as_APIName, as_serviceName, "", "USER_NAME");
	}

	public String getFinacleApiPassword(String as_APIName, String as_serviceName) throws Exception {

		return getConfigKeyValue(as_APIName, as_serviceName, "", "PASSWORD");
	}

	public Object getSqlDateFromString(String ls_data) {
		Object obj;

		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
			java.util.Date date = sdf1.parse(ls_data);
			obj = new java.sql.Date(date.getTime());
		} catch (Exception exception) {
			try {
				SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
				java.util.Date date = sdf1.parse(ls_data);
				obj = new java.sql.Date(date.getTime());
			} catch (Exception ex) {
				try {
					SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
					java.util.Date date = sdf1.parse(ls_data);
					obj = new java.sql.Date(date.getTime());
				} catch (Exception e) {
					try {
						SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
						java.util.Date date = sdf1.parse(ls_data);
						obj = new java.sql.Date(date.getTime());
					} catch (Exception error) {
						try {
							SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
							java.util.Date date = sdf1.parse(ls_data);
							obj = new java.sql.Date(date.getTime());
						} catch (Exception er) {
							try {
								SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yy", Locale.ENGLISH);
								java.util.Date date = sdf1.parse(ls_data);
								obj = new java.sql.Date(date.getTime());
							} catch (Exception err) {
								try {
									SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",
											Locale.ENGLISH);
									java.util.Date date = sdf1.parse(ls_data);
									obj = new java.sql.Date(date.getTime());
								} catch (Exception exe) {
									try {
										SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
										java.util.Date date = sdf1.parse(ls_data);
										obj = new java.sql.Date(date.getTime());
									} catch (Exception ex1) {

										try {
											SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
											java.util.Date date = sdf1.parse(ls_data);
											obj = new java.sql.Date(date.getTime());
										} catch (Exception exeception1) {

											try {
												SimpleDateFormat sdf1 = new SimpleDateFormat(
														"EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);
												java.util.Date date = sdf1.parse(ls_data);
												obj = new java.sql.Date(date.getTime());
											} catch (Exception exeception2) {

												try {
													obj = new java.sql.Date(java.sql.Date.parse(ls_data));
												} catch (Exception e1) {
													obj = ls_data;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return obj;

	}
}
