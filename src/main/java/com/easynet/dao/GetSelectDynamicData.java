package com.easynet.dao;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.GetDataDB;
import com.easynet.util.common;
import static com.easynet.util.ConstantKeyValue.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class GetSelectDynamicData extends GetDataDB {

	static Logger LOGGER = LoggerFactory.getLogger(GetSelectDynamicData.class);

//	private final static String LS_PROCQUERY="PROC_GETAPI_CONFIG_PARA";

	private static final String GETQUERY = "SELECT ID,PAGINATION,GET_QUERY, GET_TYPE ,IS_COMPRESSED \n"
			+ "        FROM GET_API_CONFIG_MST\n" + "        WHERE\n" + "        ACTION = ?";

	private static final String GETQUERYDTL = " SELECT REQ_PARA, REQ_PARA_TYPE, MUTLI_VALUE, WHERE_SEQ_ID\n"
			+ "        FROM GET_API_CONFIG_PARA_DTL\n" + "        WHERE\n" + "        GET_API_CONFIG_ID = ? \n"
			+ "        ORDER BY WHERE_SEQ_ID ASC";

	public static final String LS_UPDATE = "UPDATE";
	public static final String LS_SELECT = "SELECT";
	public static final String LS_PROC = "PROCEDURE";
	public static final String LS_PROCWITHCURSOR = "PROCWITHCURSOR";
	public static final String LS_FUNCWITHCURSOR = "FUNCWITHCURSOR";
	public static final String LS_PROCEXECUTE = "PROCEXECUTE";

	@Autowired
	private GetProcDataWithArg getDynamicData;

//	@Autowired
//	private GetProcData getProcData;
	
	@Autowired
	private SelectData selectData;

	public String getDynamicConfig(String as_action, String as_reqData) {

		LoggerImpl loggerImpl = null;
		String ls_emptyResponseData = StringUtils.EMPTY;
		String ls_dbResponseData;
		JSONObjectImpl configDataJson;
		String ls_query = StringUtils.EMPTY;
		String ls_config = StringUtils.EMPTY;
		String ls_type = StringUtils.EMPTY;
		String ls_status = StringUtils.EMPTY;
		String ls_isCompressed = StringUtils.EMPTY;
		String ls_pagination = StringUtils.EMPTY;
		
		try {

			loggerImpl = new LoggerImpl();

			ls_emptyResponseData = doCheckBlankData(as_action, as_reqData);
			if (StringUtils.isNotBlank(ls_emptyResponseData))
				return ls_emptyResponseData;

//			ls_dbResponseData = getProcData.getClobData(LS_PROCQUERY, as_action);

			ls_dbResponseData = getDbData(as_action);

			configDataJson = new JSONObjectImpl(common.ofGetJsonObject(ls_dbResponseData));
			ls_status = configDataJson.getString(STATUS);

			if (isSuccessStCode(ls_status)) {

				ls_query = configDataJson.getString("QUERY");
				ls_config = configDataJson.getString("CONFIG");
				ls_type = configDataJson.getString("GETTYPE");
				ls_isCompressed = configDataJson.getString("IS_COMPRESSED");
				ls_pagination= configDataJson.getString("ENABLE_PAGINATION");

				return getDynamicData.getDataFromConfig(ls_query, as_reqData, ls_config, ls_type, ls_isCompressed,ls_pagination);
			} else {
				return ls_dbResponseData;
			}
		} catch (Exception exception) {
			return getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getDynamicConfig", "(ENP021)");
		}
	}

	@Override
	public String ofGetResponseData(String input) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDbData(String as_action_nm) {

		LoggerImpl loggerImpl = null;
		String ls_return = StringUtils.EMPTY;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		int parameterIndex = 0;
		int li_resParaIndex;
		ResultSet resultSet = null;
		JSONObjectImpl responseDataJobj = null;
		JSONObjectImpl responseDataDtlJobj = null;
		JSONObjectImpl responseJObj = null;
		JSONObjectImpl responseDtlJObj = null;
		JSONArrayImpl responseDataJlist = null;
		JSONArrayImpl responseDataDtlJlist = null;
		JSONArrayImpl responseDtlJList = null;
		String ls_id = StringUtils.EMPTY;
		String ls_query = StringUtils.EMPTY;
		String ls_getType = StringUtils.EMPTY;
		String ls_para = StringUtils.EMPTY;
		String ls_paraType = StringUtils.EMPTY;
		String ls_sqId = StringUtils.EMPTY;
		String ls_isCompressed = StringUtils.EMPTY;
		String ls_page=StringUtils.EMPTY;;
	    JSONObjectImpl resDataJson;
		try {

			loggerImpl = new LoggerImpl();

			connection = getDbConnection();
			preparedStatement = connection.prepareStatement(GETQUERY);

			ofsetQueryParameter(parameterIndex, preparedStatement, as_action_nm);

			resultSet = preparedStatement.executeQuery();

			responseDataJlist = getResultSetData(resultSet);
			responseDataJobj = responseDataJlist.getJSONObject(0);
			ls_query = responseDataJobj.getString("GET_QUERY");
			ls_getType = responseDataJobj.getString("GET_TYPE");
			ls_isCompressed = responseDataJobj.getString("IS_COMPRESSED");
			ls_page= responseDataJobj.getString("PAGINATION");
			
			responseJObj = new JSONObjectImpl();
			responseJObj.put("ACTION", as_action_nm);
			responseJObj.put("QUERY", ls_query);
			responseJObj.put("GETTYPE", ls_getType);
			responseJObj.put("IS_COMPRESSED", ls_isCompressed);
			responseJObj.put("ENABLE_PAGINATION", ls_page);
			
			try {
				ls_id = responseDataJobj.getString("ID");

				preparedStatement = connection.prepareStatement(GETQUERYDTL);

				ofsetQueryParameter(parameterIndex, preparedStatement, ls_id);

				resultSet = (ResultSet) preparedStatement.executeQuery();
				responseDataDtlJlist = getResultSetData(resultSet);

			} catch (SQLException sqlexception) {
				ls_return = getExceptionMSg(sqlexception, LOGGER, loggerImpl, "IN:getDbData", "(ENP180)",
						"SQLException : ", null);
			}

			li_resParaIndex = responseDataDtlJlist.length();
			responseDtlJList = new JSONArrayImpl();

			for (int i = 0; i < li_resParaIndex; i++) {

				responseDataDtlJobj = responseDataDtlJlist.getJSONObject(i);
				ls_para = responseDataDtlJobj.getString("REQ_PARA");
				ls_paraType = responseDataDtlJobj.getString("REQ_PARA_TYPE");
				ls_sqId = responseDataDtlJobj.getString("WHERE_SEQ_ID");

				responseDtlJObj = new JSONObjectImpl();
				responseDtlJObj.put("REQ_PARA", ls_para);
				responseDtlJObj.put("REQ_PARA_TYPE", ls_paraType);
				responseDtlJObj.put("WHERE_SEQ_ID", ls_sqId);
				responseDtlJList.put(responseDtlJObj);
			}

			responseJObj.put("CONFIG", responseDtlJList);
			responseJObj.put("STATUS", ST0);

			return responseJObj.toString();

		} catch (SQLException sqlexception) {
			ls_return = getExceptionMSg(sqlexception, LOGGER, loggerImpl, "IN:getDbData", "(ENP180)", "SQLException : ",
					null);
		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getDbData", "(ENP180)");
		} finally {
			closeDbObject(connection, preparedStatement);
		}
		return ls_return;
	}
}
