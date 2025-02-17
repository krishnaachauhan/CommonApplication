package com.easynet.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.LoggerImpl;

public class common {

	public static String msgIdentifier;

	public static Logger LOGGER = LoggerFactory.getLogger(common.class);

	public static String[] getStringArgObj(String[] args, int al_deflength) {
		String[] ls_runArg;
		if (args != null) {
			ls_runArg = new String[args.length + al_deflength];

			for (int i = 0; i < args.length; i++) {
				ls_runArg[i] = args[i];
			}
		} else {
			ls_runArg = new String[al_deflength];
		}

		return ls_runArg;
	}

	public static String getConfig(String argu) {
		String value = "";

		Properties prop = new Properties();
		InputStream input = null;

		try {

			String filename = "application.properties";
			input = common.class.getClassLoader().getResourceAsStream(filename);
			if (input == null) {
				System.out.println("Sorry, unable to find ");
				return "";
			}
			// load a properties file from class path, inside static method
			prop.load(input);
			value = prop.getProperty(argu);
			if (value == null) {
				value = "";
			}

		} catch (Exception e) {
			value = "";
			e.printStackTrace();
		}
		return value;
	}

	public static String GetFormetedDate(String ls_FromDateFormat, String ls_toDateFormat, String ls_date) {
		try {
			DateFormat df = new SimpleDateFormat(ls_FromDateFormat);
			DateFormat df2 = new SimpleDateFormat(ls_toDateFormat);
			String dateToString = df2.format(df.parse(ls_date));
			return dateToString;
		} catch (Exception e) {
			e.printStackTrace();
			return ls_date;
		}
	}

	public static void errorLogPrint(String argu) {
		System.out.println("===================Netbanking API ======================");
		System.out.println(argu);
		System.out.println("==================================================");
	}

	public static String xmlConvert(String argu) {

		if (argu.trim().substring(0, 1).equals("[")) {
			argu = (String) argu.substring(1, argu.length() - 1);
		} else {
			argu = (String) argu;
		}

		return XML.toString(new JSONObject(argu));
	}

	public static Document loadXML(String xml) throws Exception {
		DocumentBuilderFactory fctr = DocumentBuilderFactory.newInstance();
		DocumentBuilder bldr = fctr.newDocumentBuilder();
		InputSource insrc = new InputSource(new StringReader(xml));
		return bldr.parse(insrc);
	}

	public static JSONObject xmlToJson(String xml) throws JAXBException {
		// String xmlString = xml;
		// String jsonString = null;
		JSONObject ls_jsonobj = null;
		Document lobj_loadXML;
		try {
			if (xml == null) {
				return getErrorMsg("BBPS Response failed");
			}

			lobj_loadXML = loadXML(xml);
			ls_jsonobj = new JSONObject(lobj_loadXML.getElementsByTagName("string").item(0).getTextContent());
			return ls_jsonobj;

		} catch (Exception ex) {
			return getErrorMsg("BBPS Exception  Response failed " + ex.getMessage());
			// return "[{\"ResponseCode\":\"99\",\"ResponseMessage\":\"Something going
			// wrong, JAXBException : \"" + ex.getMessage() + "\"}]";
		}

	}

	public static JSONObject SoapXmlToJson(String xml, String as_tag_nm) throws JAXBException {
		// String xmlString = xml;
		// String jsonString = null;
		JSONObject ls_jsonobj = null;
		Document lobj_loadXML;
		try {
			if (xml == null) {
				return getErrorMsg("BBPS Response failed");
			}

			lobj_loadXML = loadXML(xml);
			ls_jsonobj = new JSONObject(
					lobj_loadXML.getElementsByTagName(as_tag_nm + "Result").item(0).getTextContent());
			return ls_jsonobj;

		} catch (Exception ex) {
			return getErrorMsg("BBPS Exception  Response failed " + ex.getMessage());
			// return "[{\"ResponseCode\":\"99\",\"ResponseMessage\":\"Something going
			// wrong, JAXBException : \"" + ex.getMessage() + "\"}]";
		}

	}

	public static JSONObject xmlToJsonObj(String xml) throws JAXBException, JSONException {
		String xmlString = xml;
		if (xml == null) {
			return getErrorMsg("BBPS Response failed");
		}
		try {
			JAXBContext jc = JAXBContext.newInstance(String.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			StreamSource xmlSource = new StreamSource(new StringReader(xmlString));
			JAXBElement<String> je = (JAXBElement<String>) unmarshaller.unmarshal(xmlSource, String.class);
			return new JSONObject(je.getValue());
		} catch (JAXBException ex) {
			return getErrorMsg("BBPS JAXBException Response failed " + ex.getMessage());
			// return "[{\"ResponseCode\":\"99\",\"ResponseMessage\":\"Something going
			// wrong, JAXBException : \"" + ex.getMessage() + "\"}]";
		} catch (Exception ex) {
			return getErrorMsg("BBPS Exception  Response failed " + ex.getMessage());
			// return "[{\"ResponseCode\":\"99\",\"ResponseMessage\":\"Something going
			// wrong, JAXBException : \"" + ex.getMessage() + "\"}]";
		}
	}

	public static JSONObject getErrorMsg(String as_message) {
		String ls_error;
		ls_error = "[{\"ResponseCode\":\"0\",\"ResponseMessage\":\"Something going wrong, JAXBException : \""
				+ as_message + "\"}]";
		try {
			return new JSONObject(ls_error);
		} catch (JSONException e) {
			return null;
		}
	}

	public String convertMapToSring(Map<String, ?> map) {
		String mapAsString = map.keySet().stream().map(key -> key + "=" + map.get(key))
				.collect(Collectors.joining(", ", "{", "}"));
		return mapAsString;
	}

	public Map<String, String> convertMapToString(String mapAsString) {
		Map<String, String> map = Arrays.stream(mapAsString.split(",")).map(entry -> entry.split("="))
				.collect(Collectors.toMap(entry -> entry[0].trim(), entry -> entry[1].trim()));
		return map;
	}

	public static void LogPrintError(String error) {
		System.out.println("We are sorry for the inconvenience caused to you,HRMS : " + error);
	}

	public static void PrintErrLog(long uniqueNumber, String Msg) {
		System.out.println(
				"=========================Netbanking Error Print=>" + uniqueNumber + "============================");
		System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())
				+ "  Error :  " + Msg);
	}

	/**
	 *
	 * @param ls_date
	 * @return
	 */
	public static boolean IsExpireDate(String ls_date) {

		Date d1 = null;
		Date d2 = null;

		SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
		try {
			Date date = new Date();
			String dateStart = format.format(date);
			String dateStop = ls_date;
			// System.out.println("dateStart : " + dateStart);
			// System.out.println("dateStop : " + dateStop);
			try {
				d1 = format.parse(dateStart);
				d2 = format.parse(dateStop);
			} catch (ParseException e) {
				// System.out.println("ParseException : " + e.getMessage());
				// e.printStackTrace();
				PrintErrLog(0, "IsExpireDate ParseException: " + e.getMessage());
			}
			// Get msec from each, and subtract.
			long diff = d2.getTime() - d1.getTime();
			long diffSeconds = diff / 1000 % 60;

			if (diffSeconds < 30) {
				return true;
			}
			return false;
		} catch (Exception e) {
			PrintErrLog(0, "IsExpireDate Exception : " + e.getMessage());
			return false;
		}
	}

	/**
	 * @param aErrorTitle -Error message title.
	 * @param aErrorMsg   -Error message which is shown to user.
	 * @param aError      -Actual error message.
	 * @return -Return String value of json format.
	 */
	public static JSONArray ofGetErrDataJson(String aErrorTitle, String aErrorMsg, String aError) {

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
	 * @param aSucessTitle -Sucess message title.
	 * @param aSucessMsg   -Sucess message which is shown to user.
	 * @return -Return String value of json format.
	 */
	public static JSONArray ofGetSucessDataJson(String aSucessTitle, String aSucessMsg) {

		JSONObject errorObject = new JSONObject();

		errorObject.put("TITLE", aSucessTitle);
		errorObject.put("MESSAGE", aSucessMsg);

		JSONArray errorListObject = new JSONArray();
		errorListObject.put(errorObject);
		return errorListObject;
	}

	/**
	 * @param aErrorStatus - Error Status for identification of error.
	 * @param aErrorTitle  -Error message title.
	 * @param aErrorMsg    -Error message which is shown to user.
	 * @param aError       -Actual error message.
	 * @param asDefaultMsg - default English message..
	 * @param aresponse_cd - Response code of API.
	 * @param acolor       -Color code of Message
	 * @param object       - Multiple object value.<br>
	 *                     1.Caller path. 2.message in ENG language.
	 * 
	 * @return -Return String value of JSOList format.
	 */
	public static String ofGetErrDataJsonArray(String aErrorStatus, String aErrorTitle, String aErrorMsg, String aError,
			String asDefaultMsg, String aresponse_cd, String acolor, Object... object) {

		JSONObject errorObjectJson;
		String ls_errorObject = "";
		JSONArray errorObjectJlist = new JSONArray();
		String callerClassName = "";
		String methodname = "";
		String[] callerClassNameList;
		int callerClassNameCnt;

		/*
		 * get the class name of caller class. Do not remove below code from here it is
		 * write to get the calling class detail.
		 */
		try {
			// get the class name from caller method.
			callerClassName = (String) common.getDataAtIndex(0, object);

			if (StringUtils.isBlank(callerClassName)) {
				callerClassName = new Exception().getStackTrace()[1].getClassName();
				methodname = new Exception().getStackTrace()[1].getMethodName();

				// simple "." is not work in split
				callerClassNameList = callerClassName.split("[.]");
				callerClassNameCnt = callerClassNameList.length;

				if (callerClassNameCnt == 1) {
					callerClassName = callerClassNameList[0] + "/" + methodname;
				} else if (callerClassNameCnt >= 2) {
					callerClassName = callerClassNameList[callerClassNameCnt - 2] + "/"
							+ callerClassNameList[callerClassNameCnt - 1] + "/" + methodname;
				}
			}
		} catch (Exception err) {
			callerClassName = "Error in get class name.";
		}

		// call method to get the error object
		ls_errorObject = ofGetErrDataJsonObject(aErrorStatus, aErrorTitle, aErrorMsg, aError, asDefaultMsg,
				aresponse_cd, acolor, callerClassName);

		errorObjectJson = new JSONObject(ls_errorObject);

		errorObjectJlist.put(errorObjectJson);
		return errorObjectJlist.toString();
	}

	/**
	 * @param aErrorStatus - Error Status for identification of error.
	 * @param aErrorTitle  -Error message title.
	 * @param aErrorMsg    -Error message which is shown to user.
	 * @param aError       -Actual error message.
	 * @param asDefaultMsg - default English Message.
	 * @param aresponse_cd - Response code of API.
	 * @param acolor       -Color code of Message
	 * @param object       - Multiple object value.<br>
	 *                     1.Caller path. 2.message in ENG language.
	 * @return -Return String value of JSONLIst format.
	 */
	public static String ofGetErrDataJsonObject(String aErrorStatus, String aErrorTitle, String aErrorMsg,
			String aError, String asDefaultMsg, String aresponse_cd, String acolor, Object... object) {

		String callerClassName = "";
		String methodname = "";
		String[] callerClassNameList;
		int callerClassNameCnt;
		String ls_defaultMsg = "";
		String ls_enMessage = "";

		// get the class name of caller class
		try {
			// get the class name from caller method.
			callerClassName = (String) common.getDataAtIndex(0, object);
			ls_enMessage = asDefaultMsg;

			if (StringUtils.isBlank(callerClassName)) {
				callerClassName = new Exception().getStackTrace()[1].getClassName();
				methodname = new Exception().getStackTrace()[1].getMethodName();

				// simple "." is not work in split
				callerClassNameList = callerClassName.split("[.]");
				callerClassNameCnt = callerClassNameList.length;

				if (callerClassNameCnt == 1) {
					callerClassName = callerClassNameList[0] + "/" + methodname;
				} else if (callerClassNameCnt >= 2) {
					callerClassName = callerClassNameList[callerClassNameCnt - 2] + "/"
							+ callerClassNameList[callerClassNameCnt - 1] + "/" + methodname;
				}
			}
		} catch (Exception err) {
			callerClassName = "Error in get class name.";
		}

		JSONObjectImpl errorObject = new JSONObjectImpl();
		JSONArray errorListJsonObject = new JSONArray();

		/// write code for remove ENP msg.
		if (StringUtils.isNotBlank(asDefaultMsg) && "N".equals(msgIdentifier)) {
			int position = asDefaultMsg.indexOf("(ENP");
			if (position > 0) {
				asDefaultMsg = asDefaultMsg.substring(0, position).trim() + ".";
			}
		}
		if (StringUtils.isNotBlank(aErrorMsg) && "N".equals(msgIdentifier)) {
			int position = aErrorMsg.indexOf("(ENP");
			if (position > 0) {
				aErrorMsg = aErrorMsg.substring(0, position).trim() + ".";
			}
		}
		//// end code

//		// Set the default message in message data not found.
//		if (StringUtils.isBlank(aErrorMsg)) {
//			ls_defaultMsg = asDefaultMsg;
//		}
//
//		else {
//			ls_defaultMsg = aErrorMsg;
//		}

		// set the response message in case of key mapped as to show response MSG
		// instead on MSG from property file.
		if ("<RESPONSEMESSAGE>".equals(asDefaultMsg)) {
			asDefaultMsg = aError;
		}

		errorListJsonObject = ofGetErrDataJson(aErrorTitle, asDefaultMsg, aError);

		errorObject.put("ERR_PATH", callerClassName);
		errorObject.put("STATUS", aErrorStatus);
		errorObject.put("MESSAGE", asDefaultMsg);
		errorObject.put("RESPONSE", errorListJsonObject);
		errorObject.put("RESPONSECODE", aresponse_cd);
		errorObject.put("RESPONSEMESSAGE", aError);
		errorObject.put("MESSAGE_EN", ls_enMessage);
		errorObject.put("ACTIVITY_CD", "");
		errorObject.put("COLOR", acolor);

		return errorObject.toString();
	}

	public static String ofGetTotalErrString(Exception err, String userMessage) {
		String ls_error_msg = "";
		ls_error_msg = userMessage + "\r\n" + err.getMessage();
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		err.printStackTrace(writer);
		ls_error_msg = ls_error_msg + "\r\n" + stringWriter.toString();
		return ls_error_msg;
	}

	public static String GetMaskingMBAndRmSpChar(String ls_mobile) {
		String ls_result = "";
		int as = 0;
		int li_noLength = ls_mobile.length();
		int li_skipedDigit = 0;

		li_skipedDigit = li_noLength == 13 ? 3 : 1;

		for (int i = 0; i < li_noLength; i++) {
			as = (int) ls_mobile.charAt(i);
			if (as < 48 || as > 57) {
				continue;
			} else {
				if (i <= li_skipedDigit || i >= ls_mobile.length() - 3) {
					ls_result = ls_result + ls_mobile.charAt(i);
				} else {
					ls_result = ls_result + "X";
				}
			}

		}
		return ls_result;
	}

	public static String GetMBAndRmSpChar(String ls_mobile) {
		String ls_result = "";
		int as = 0;
		int li_noLength = ls_mobile.length();

		for (int i = 0; i < li_noLength; i++) {
			as = (int) ls_mobile.charAt(i);
			if (as < 48 || as > 57) {
				continue;
			} else {
				ls_result = ls_result + ls_mobile.charAt(i);
			}
		}
		return ls_result;
	}

	public static String getMaskingCardNo(String ls_cardNo) {
		String ls_result = "";
		int ll_cardLenght = 0;
		ll_cardLenght = ls_cardNo.length();

		if (StringUtils.isNotBlank(ls_cardNo)) {
			if (ll_cardLenght > 10) {
				ls_result = ls_cardNo.substring(0, 6);
				ls_result = ls_result + StringUtils.rightPad("", ll_cardLenght - 10, "*");
				ls_result = ls_result + ls_cardNo.substring(ll_cardLenght - 4);
				return ls_result;
			} else if (ll_cardLenght > 4) {
				ls_result = "*" + ls_cardNo.substring(ll_cardLenght - 4);
				return ls_result;
			} else {
				return ls_cardNo;
			}
		} else {
			return ls_cardNo;
		}
	}

	public static boolean isAnyEquals(String st1, String... strall) {
		boolean lb_match = false;
		int li_strLenght = 0;
		if (st1 == null || strall == null) {
			return false;
		}
		li_strLenght = strall.length;

		for (int i = 0; i < li_strLenght; i++) {
			if (st1.equals(strall[i])) {
				lb_match = true;
				break;
			}
		}
		return lb_match;
	}

	public static int getLastIndex(String[] as_strList) {
		int li_index = 0;
		if (as_strList == null) {
			return li_index;
		} else {
			li_index = as_strList.length - 1;
			return li_index;
		}
	}

	public static String GetMaskingEmail(String ls_mail) {
		String maskedEmail = ls_mail.replaceAll("(?<=.{1}).(?=[^@]*?.@)", "X");
		return maskedEmail;
	}

	public static String ofGetAcctCardFalg(String as_Data) {
		String ls_flag = "";

		if ("FINACLE".equalsIgnoreCase(as_Data) || "ABABIL".equalsIgnoreCase(as_Data)) {
			ls_flag = "A";
		} else if ("TRANZWARE".equalsIgnoreCase(as_Data)) {
			ls_flag = "C";
		} else {
			ls_flag = as_Data;
		}
		return ls_flag;
	}

	private static String ofReplaceData(String a_replace_char, String a_key_data) {
		String ls_repString = "";
		StringBuffer repStringAppend = new StringBuffer();
		int strLenght = 0;
		char keyCharData, replaceCharData;
		String ls_replaceArray[];
		boolean lb_exit = false;
		boolean lb_isPattarnFound = false;

		if (StringUtils.isBlank(a_replace_char))
			a_replace_char = "";

		if (StringUtils.isAnyBlank(a_key_data))
			return a_key_data;

		strLenght = a_key_data.length();

		if (a_replace_char.trim().startsWith("$[")) {
			lb_isPattarnFound = true;
			a_replace_char = a_replace_char.substring(2, a_replace_char.length() - 1);
		}

		if (a_replace_char.contains(",")) {
			ls_replaceArray = a_replace_char.split(",");
		} else {
			ls_replaceArray = new String[] { a_replace_char };
		}

		for (String ls_replaceStr : ls_replaceArray) {

			if (strLenght == ls_replaceStr.length()) {

				for (int i = 0; i < strLenght; i++) {

					replaceCharData = ls_replaceStr.charAt(i);

					if ('*' == (replaceCharData)) {
						repStringAppend.append(replaceCharData);
					} else {
						keyCharData = a_key_data.charAt(i);
						repStringAppend.append(keyCharData);
					}
				}

				ls_repString = repStringAppend.toString();
				lb_exit = true;
			}
			if (lb_exit)
				break;
		}

		if (!lb_exit && lb_isPattarnFound) {
			ls_repString = a_key_data;
		} else if (!lb_exit) {
			ls_repString = StringUtils.rightPad("", a_key_data.length(), a_replace_char);
		}
		return ls_repString;
	}

	private static JSONObject of_replaceKeysData(JSONObject as_inputJson, JSONObject asjsonReplaceObj)
			throws Exception {

		JSONObjectImpl jsonRequesData = null;
		Set<String> keysList;
		JSONObject jsonReplaceObj;
		JSONArray arrayObjectJlist;
		long subJsonArrayCnt;
		JSONObject JsonSubObj;
		JSONObject jsonSubObjRet;
		JSONArray subJsonArrayjlist;
		Object object;
		String ls_replaceValue;
		String ls_replaceDataValue;
		Object valueObject;

		jsonRequesData = new JSONObjectImpl(as_inputJson.toString());
		keysList = jsonRequesData.keySet();

		jsonReplaceObj = asjsonReplaceObj;

		/* GET THE LIST OF KEYS AND COUNT .LOOP KEY WISE */
		for (String keyName : keysList) {

			/* CHECK IN KEY NAME STRING. */
			if (jsonReplaceObj.has(keyName)) {

				ls_replaceDataValue = jsonReplaceObj.getString(keyName);

				/* GET THE KEY VALUE FROM INPUT JSON */
				object = jsonRequesData.get(keyName);
				if (object instanceof JSONObject || object instanceof JSONArray) {
					ls_replaceValue = "";
				} else {
					ls_replaceValue = String.valueOf(object);
				}
				/* PUT THE REPLACE VALUE IN REQUEST JSON OBJECT */
				if (StringUtils.isAnyBlank(ls_replaceDataValue, ls_replaceValue)) {
					jsonRequesData.put(keyName, "");
				} else {
					jsonRequesData.put(keyName, ofReplaceData(ls_replaceDataValue, ls_replaceValue));
				}
			}

			object = jsonRequesData.get(keyName);

			/* GET THE JSON OBJECT FROM JSON */
			if (object instanceof JSONObject) {
				/*
				 * RE CALL SAME FUNCTION TO REPLACE THE VALUE IN OBJECT AND GET OBJECT FROM
				 * FUNCTION.
				 */
				jsonSubObjRet = of_replaceKeysData((JSONObject) object, asjsonReplaceObj);

				/* PUT THE REPLACED JSON OBJECT INTO MAIN JSON. */
				jsonRequesData.put(keyName, jsonSubObjRet);

			} else if (object instanceof JSONArray) {
				/* GET THE JSON ARRAY FOR KEY */
				arrayObjectJlist = (JSONArray) object;
				subJsonArrayCnt = arrayObjectJlist.length();
				subJsonArrayjlist = new JSONArray();

				for (int i = 0; i < subJsonArrayCnt; i++) {

					// get the object from array.
					valueObject = arrayObjectJlist.get(i);

					if (valueObject instanceof JSONObject) {
						JsonSubObj = (JSONObject) valueObject;
						/*
						 * RE CALL SAME FUNCTION TO REPLACE THE VALUE IN OBJECT AND GET OBJECT FROM
						 * FUNCTION.
						 */
						jsonSubObjRet = of_replaceKeysData(JsonSubObj, asjsonReplaceObj);

						/* Append the object into list */
						subJsonArrayjlist.put(jsonSubObjRet);
					} else {
						/* Append the object into list */
						subJsonArrayjlist.put(valueObject);
					}
				}
				/* PUT THE REPLACED JSON OBJECT INTO MAIN JSON. */
				jsonRequesData.put(keyName, subJsonArrayjlist);
			}
		}
		return jsonRequesData;
	}

	/**
	 * This function used to replace the keys data from requested json object.
	 * 
	 * @param asInputData - input JSON in which values want to replaced.
	 * @return return the string data with replaced values.
	 */
	public static String ofReplaceData(String asInputData, JSONObject replaceKeysListJson) throws Exception {

		String ls_inputData = asInputData;
		JSONObject inputjson = null;
		JSONArray inputJlist = null;
		JSONArray retDataJlist = null;
		int arrayCnt = 0;

		if (ls_inputData.trim().startsWith("[")) {
			retDataJlist = new JSONArray();
			inputJlist = new JSONArray(ls_inputData);
			arrayCnt = inputJlist.length();

			for (int i = 0; i < arrayCnt; i++) {
				inputjson = inputJlist.getJSONObject(i);
				JSONObject retDataJson = of_replaceKeysData(inputjson, replaceKeysListJson);
				retDataJlist.put(retDataJson);
			}
			return retDataJlist.toString();

		} else if (ls_inputData.trim().startsWith("{")) {
			inputjson = new JSONObject(ls_inputData);
			return of_replaceKeysData(inputjson, replaceKeysListJson).toString();
		} else {
			return ls_inputData;
		}
	}

	/**
	 * This method used for get the json object from string json/jsonArray object.
	 **/
	public static JSONObjectImpl ofGetJsonObject(String data) {

		if (data.trim().substring(0, 1).equals("[")) {
			return new JSONArrayImpl(data).getJSONObject(0);
		} else {
			return new JSONObjectImpl(data);
		}
	}

	public static String GetCurrentDate(String format) {
		try {
			DateFormat df = new SimpleDateFormat(format);
			Date today = Calendar.getInstance().getTime();
			String dateToString = df.format(today);
			return dateToString;
		} catch (Exception e) {
			return "";
		}
	}

	public static String DecimalFormatter(String ls_data, String ls_flag) {
		Double le_amt = null;
		try {
			le_amt = Double.parseDouble(ls_data);
		} catch (NumberFormatException exception) {
			return ls_data;
		}
		if (le_amt == 0 && "Y".equalsIgnoreCase(ls_flag)) {
			return "";
		}
		DecimalFormat decimalFormatter = new DecimalFormat("#,##,##,##,##0.00");
		return decimalFormatter.format(le_amt);
	}

	public static Object getDataAtIndex(int index, Object... object) {

		int lenght = object == null ? 0 : object.length;
		if (index <= lenght - 1) {
			return object[index];
		} else {
			return null;
		}
	}

	public static String getStringDataAtIndex(int index, Object... object) {
		Object indexValue = getDataAtIndex(index, object);
		if (indexValue == null) {
			return "";
		} else {
			return String.valueOf(indexValue);
		}
	}

	public static Object getDataAtIndex(Object... object) {
		int lenght = object == null ? 0 : object.length;
		if (lenght > 0) {
			return object;
		} else {
			return null;
		}
	}

	/**
	 * This method is used to get the error code from json.
	 **/
	public static String getAbabilErrorResCode(String errorData) {
		if (StringUtils.isBlank(errorData)) {
			return "";
		}

		if (!errorData.trim().substring(0, 1).equals("{")) {
			return "";
		}

		JSONObject erroDataJson = ofGetJsonObject(errorData);
		String ls_errorCode = erroDataJson.optString("code");
		return ls_errorCode;
	}

	/**
	 * This method is used to common response for all API.
	 **/
	public static JSONObject ofGetResponseJson(JSONArray responseJlist, String responseCode, String responseMessage,
			Object... values) throws Exception {

		Object objectJson;
		String ls_statusCodeObj;
		String ls_colorObj;
		String ls_messageObj;

		Object statusCodeObj;
		Object colorObj;
		Object messageObj;

		JSONObject responseJsonObject = null;

		objectJson = getDataAtIndex(3, values);

		if (objectJson != null) {
			responseJsonObject = new JSONObject(objectJson.toString());
		} else {
			responseJsonObject = new JSONObject();
		}

		statusCodeObj = getDataAtIndex(0, values);
		colorObj = getDataAtIndex(1, values);
		messageObj = getDataAtIndex(2, values);

		ls_statusCodeObj = statusCodeObj == null || String.valueOf(statusCodeObj).equals("") ? CommonBase.ST0
				: String.valueOf(statusCodeObj);
		ls_colorObj = colorObj == null || String.valueOf(colorObj).equals("") ? "G" : String.valueOf(colorObj);
		ls_messageObj = messageObj == null ? "" : String.valueOf(messageObj);

		responseJsonObject.put("STATUS", ls_statusCodeObj);
//		responseJsonObject.put("COLOR",ls_colorObj);
		responseJsonObject.put("RESPONSE", responseJlist == null ? new JSONArray() : responseJlist);
		responseJsonObject.put("MESSAGE", ls_messageObj);
		responseJsonObject.put("RESPONSECODE", StringUtils.isBlank(responseCode) ? "" : responseCode);
		responseJsonObject.put("RESPONSEMESSAGE", StringUtils.isBlank(responseMessage) ? "" : responseMessage);

		/*
		 * if (!getDataAtIndex(3,values).equals(null) ||
		 * !getDataAtIndex(3,values).equals("") ) { JSONObjectImpl jExtraFields = new
		 * JSONObjectImpl(values[3].toString()); ls_keyName =
		 * JSONObject.getNames(jExtraFields);
		 * 
		 * for (String keyName : ls_keyName) { responseJsonObject.put(keyName,
		 * jExtraFields.get(keyName)); } ls_response = responseJsonObject.toString(); }
		 */

		return responseJsonObject;
	}

	public static JSONObject ofGetResponseJson(JSONArrayImpl responseJlist, String responseCode, String responseMessage,
			Object... values) throws Exception {

		Object objectJson;
		String ls_statusCodeObj;
		String ls_colorObj;
		String ls_messageObj;

		Object statusCodeObj;
		Object colorObj;
		Object messageObj;

		JSONObject responseJsonObject = null;

		objectJson = getDataAtIndex(3, values);

		if (objectJson != null) {
			responseJsonObject = new JSONObject(objectJson.toString());
		} else {
			responseJsonObject = new JSONObject();
		}

		statusCodeObj = getDataAtIndex(0, values);
		colorObj = getDataAtIndex(1, values);
		messageObj = getDataAtIndex(2, values);

		ls_statusCodeObj = statusCodeObj == null || String.valueOf(statusCodeObj).equals("") ? CommonBase.ST0
				: String.valueOf(statusCodeObj);
		ls_colorObj = colorObj == null || String.valueOf(colorObj).equals("") ? "G" : String.valueOf(colorObj);
		ls_messageObj = messageObj == null ? "" : String.valueOf(messageObj);

		responseJsonObject.put("STATUS", ls_statusCodeObj);
//		responseJsonObject.put("COLOR",ls_colorObj);
		responseJsonObject.put("RESPONSE", responseJlist == null ? new JSONArrayImpl() : responseJlist);
		responseJsonObject.put("MESSAGE", ls_messageObj);
		responseJsonObject.put("RESPONSECODE", StringUtils.isBlank(responseCode) ? "" : responseCode);
		responseJsonObject.put("RESPONSEMESSAGE", StringUtils.isBlank(responseMessage) ? "" : responseMessage);

		/*
		 * if (!getDataAtIndex(3,values).equals(null) ||
		 * !getDataAtIndex(3,values).equals("") ) { JSONObjectImpl jExtraFields = new
		 * JSONObjectImpl(values[3].toString()); ls_keyName =
		 * JSONObject.getNames(jExtraFields);
		 * 
		 * for (String keyName : ls_keyName) { responseJsonObject.put(keyName,
		 * jExtraFields.get(keyName)); } ls_response = responseJsonObject.toString(); }
		 */

		return responseJsonObject;
	}

	public static String ofGetSource(String ls_data) {

		if (StringUtils.isNotBlank(ls_data)) {
			if (ls_data.startsWith("178")) {
				return ConstantValue.getAbabil();
			} else {
				return ConstantValue.getFinacle();
			}
		} else {
			return ls_data;
		}
	}

	/**
	 * This method used to get the API URL.
	 * 
	 * @param as_APIName     -Name of API.
	 * @param as_serviceName -Name of service which belong to service name.
	 */
	public static String getPathKeyValue(String as_APIName, String as_serviceName, String as_basePath,
			String as_keyName) throws Exception {

		String ls_resAPIPathName = "";
		String ls_comAPIPathName = "";
		String ls_basePath = "root>FINACLE_API";
		String ls_apiUrl = "";

		if (StringUtils.isNotBlank(as_basePath)) {
			ls_basePath = as_basePath;
		}
		if (StringUtils.isNotBlank(as_serviceName)) {
			ls_basePath = ls_basePath + ">" + as_serviceName;
		}

		if (StringUtils.isNotBlank(as_APIName)) {
			ls_resAPIPathName = ls_basePath + ">" + as_APIName + ">" + as_keyName;
		} else {
			ls_resAPIPathName = ls_basePath + ">" + as_keyName;
		}

		ls_comAPIPathName = ls_basePath + ">" + as_keyName;

		ls_apiUrl = readXML.getXmlData(ls_resAPIPathName);

		if (StringUtils.isBlank(ls_apiUrl)) {
			ls_apiUrl = readXML.getXmlData(ls_comAPIPathName);
		}

		return ls_apiUrl;
	}

	public static Double getDecFormatValue(Double ae_double) {
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		return Double.parseDouble(decimalFormat.format(ae_double));
	}

	/**
	 * used this method for get value as formated("0.00") decimal string value.
	 * 
	 * @param as_double string format decimal value.
	 * @return This method return decimal value.
	 */
	public static Double getDecFormatValue(String as_double) {
		DecimalFormat decimalFormat = new DecimalFormat("0.00");

		if (StringUtils.isBlank(as_double) || "null".equals(as_double)) {
			return new Double("0.00");
		}
		return Double.parseDouble(decimalFormat.format(new Double(as_double)));
	}

	/**
	 * used this method for get value as formated("0.00") decimal string value.
	 * 
	 * @param ae_double decimal value.
	 * @return This method return String value.
	 */
	public static String getDecFormatValueStr(Double ae_double) {
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		// double le_value=getDecFormatValue(ae_double);
		return decimalFormat.format(ae_double);
	}

	/**
	 * used this method for get value as formated("0.00") decimal string value.
	 * 
	 * @param as_double string format decimal value.
	 * @return This method return String value.
	 */
	public static String getDecFormatValueStr(String as_double) {
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		double le_value = getDecFormatValue(as_double);
		return decimalFormat.format(le_value);
	}

	/**
	 * used this method for get value in multiply with second argument and also in
	 * "0.00" format.
	 * 
	 * @param as_double   string format decimal value.
	 * @param al_mulValue multiply number.
	 * @return This method return decimal value.
	 */
	public static Double getDecFormatValueWithMul(String as_double, int al_mulValue) {
		double le_value = getDecFormatValue(as_double);

		if (le_value != 0) {
			return le_value * al_mulValue;
		} else {
			return le_value;
		}
	}

	/**
	 * used this method for get value in multiply with second argument and also in
	 * "0.00" format.
	 * 
	 * @param as_double   string format decimal value.
	 * @param al_mulValue multiply number.
	 * @return This method return string value.
	 */
	public static String getDecFormatValueWithMulStr(String as_double, int al_mulValue) {
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		double le_value = getDecFormatValueWithMul(as_double, al_mulValue);

		return decimalFormat.format(le_value);
	}

	/****
	 * This method used to replaced the key values form request data.
	 */
	public static String getReplacedKeyDataFromRequest(JSONObject requestData, String ls_values) {

		int strLength = ls_values.length();
		boolean lb_read = false;
		String ls_keyValue = "";
		ArrayList<String> keyList = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		JSONObject apiResponseData = null;
		LoggerImpl loggerImpl = new LoggerImpl();

		try {

			if (StringUtils.isBlank(ls_values) || !ls_values.contains("<")) {
				return ls_values;
			}

			if (requestData.has("VALUEREPLACEDATA")) {
				apiResponseData = requestData.getJSONArray("VALUEREPLACEDATA").getJSONObject(0);
			}

			for (int i = 0; i < strLength; i++) {

				if (ls_values.charAt(i) == '<') {
					if (lb_read) {
						buffer.delete(0, buffer.length());
					}
					lb_read = true;
				}

				if (ls_values.charAt(i) == '>') {
					lb_read = false;
					if (buffer.length() > 0) {
						keyList.add(buffer.substring(1).toString());
						// reset the object
						buffer.delete(0, buffer.length());
					}
				}

				if (lb_read) {
					buffer.append(ls_values.charAt(i));
				}
			}

			for (String KeyName : keyList) {
				if (apiResponseData != null) {
					ls_keyValue = apiResponseData.optString(KeyName, "").trim();
				} else {
					ls_keyValue = "";
				}

				ls_values = ls_values.replaceAll("<" + KeyName + ">", ls_keyValue);
			}

		} catch (Exception exception) {
			loggerImpl.error(LOGGER, ofGetTotalErrString(exception, ""), "IN:getReplacedKeyDataFromRequest");
		}

		return ls_values;
	}

	public static String ofGetAPIendPoint(String ls_url) throws MalformedURLException {

		URL url = new URL(ls_url);

		/// get the API endPoint From URL.
		return url.getPath().toString().substring(1);
	}

	public static boolean ofvalidateJSonArrayData(String ls_data) {

		switch (ls_data.trim().charAt(1)) {
		case '"':
		case '\'':
		case '{':
			return true;
		case '[':
			return ofvalidateJSonArrayData(ls_data.trim().substring(1));
		default:
			return false;
		}
	}

	/**
	 * This method are used to create main project name folder data in main api
	 * folder(tomcat base dir/API_DATA/)
	 * 
	 * @return This method return the project folder path(tomcat base
	 *         dir/API_DATA/${PROJECT_NAME}).
	 */
	public static String ofCreateAPiDataFolder() throws Exception {
		String tomcatBasePath = ConfigurationValues.API_DATA;// System.getProperty("API_DATA");
		String projectName = ConfigurationValues.PROJECT_NAME;// System.getProperty("PROJECT_NAME");
		String projectFolderName = "";

		File fileFolderobj = new File(tomcatBasePath + File.separator + "API_DATA");

		if (!fileFolderobj.exists()) {
			fileFolderobj.mkdir();
		}

		File projectFolderPath = new File(fileFolderobj.getAbsolutePath() + File.separator + projectName);
		if (!projectFolderPath.exists()) {
			projectFolderPath.mkdir();
		}

		File projectCommonFolderPath = new File(fileFolderobj.getAbsolutePath() + File.separator + "EnfinityAPI");
		if (!projectCommonFolderPath.exists()) {
			projectCommonFolderPath.mkdir();
		}
		ConfigurationValues.COMMON_PROJECT_FOLDER_PATH = projectCommonFolderPath.getAbsolutePath() + File.separator;

		projectFolderName = projectFolderPath.getAbsolutePath();

		return projectFolderName + File.separator;
	}

	/***
	 * This method used for set the project configuration detail.
	 * 
	 * @throws Exception
	 *****/
	public static void ofSetProjectConfigDtl() throws Exception {

		// above set parameter used in below method.
		ConfigurationValues.PROJECT_FOLDER_PATH = common.ofCreateAPiDataFolder();
		ConfigurationValues.SPRING_CONFIG_LOCATION = ConfigurationValues.PROJECT_FOLDER_PATH + "application.properties";
		System.setProperty(ConfigurationValues.PROJECT_NAME + "_PROJECT_FOLDER_PATH",
				ConfigurationValues.PROJECT_FOLDER_PATH);
		System.setProperty(ConfigurationValues.PROJECT_NAME + "_COM_PROJ_FDR_PATH",
				ConfigurationValues.COMMON_PROJECT_FOLDER_PATH);
	}

	public static String getFirstEmailId(String as_emailId) {
		String[] emailIDArray = as_emailId.split(",");
		return emailIDArray[0];
	}

	public static String getAPINameandType(String as_name) {
		String ls_apiName = "";
		String ls_apiType = "";
		int li_findResValue = 0;
		int li_findReqValue = 0;
		String ls_typeData = "";

		if (StringUtils.isNotBlank(as_name)) {
			if (as_name.startsWith("EX:")) {
				ls_apiType = "External";
				ls_apiName = as_name.substring(3);
			} else if (as_name.startsWith("IN:")) {
				ls_apiType = "Internal";
				ls_apiName = as_name.substring(3);
			} else {
				ls_apiType = "Internal";
				ls_apiName = as_name;
			}

			li_findReqValue = ls_apiName.indexOf("-Request");
			li_findResValue = ls_apiName.indexOf("-Response");

			if (li_findReqValue > 0) {
				ls_typeData = "Request";
				ls_apiName = ls_apiName.substring(0, li_findReqValue);
			} else if (li_findResValue > 0) {
				ls_typeData = "Response";
				ls_apiName = ls_apiName.substring(0, li_findResValue);
			}
			return ls_apiName + "\"" + ",\"API_TYPE\":\"" + ls_apiType + "\"" + ",\"LOGTYPE\":\"" + ls_typeData;
		} else {
			return as_name;
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println(getAPINameandType("EX:GEtaccountDetail"));
		System.out.println("---------------------");
		System.out.println(getAPINameandType("IN:GEtaccountDetail"));
		System.out.println("---------------------");
		System.out.println(getAPINameandType("GEtaccountDetail"));
		System.out.println("---------------------");
		// System.out.println(GetMBAndRmSpChar("123((45))@@@6(1234)147"));
	}

	public static boolean ofExcludeMediaType(String as_mediatype) {

		if (StringUtils.isNotBlank(as_mediatype) && as_mediatype.equals("PDF")) {
			return true;
		} else {
			return false;
		}

	}

	public static String getMediaTypeFromResType(String as_responseType) {

		if (StringUtils.isNotBlank(as_responseType)) {
			if (as_responseType.equals("PDF")) {
				return MediaType.APPLICATION_PDF_VALUE;
			} else {
				return MediaType.APPLICATION_JSON_VALUE;
			}
		} else {
			return MediaType.APPLICATION_JSON_VALUE;
		}
	}

	/**
	 * This method used to get the action name from URI.
	 * 
	 * @param request -http request for get the URI.
	 * 
	 */
	public static String getLastURIData(HttpServletRequest request) {
		String ls_uri = request.getRequestURI();
		String[] ls_uris = ls_uri.split("/");
		return ls_uris[ls_uris.length - 1];
	}

	/**
	 * Used this method for get value as formated("0.00") BigDecimal string value.
	 * 
	 * @param ae_bigDecimal bigDecimal value.
	 * @return This method return String value.
	 */
	public static String getBigDecFormatValueStr(BigDecimal ae_bigDecimal) {
		BigDecimal le_formatedValue = null;
		le_formatedValue = ae_bigDecimal.setScale(2, RoundingMode.HALF_DOWN);
		return le_formatedValue.toString();
	}

	public static byte[] compress(String as_response) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(as_response.getBytes());
		gzip.close();
		return out.toByteArray();
	}
}
