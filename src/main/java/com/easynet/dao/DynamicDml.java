package com.easynet.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.easynet.bean.GetRequestUniqueData;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.LoggerImpl;

@Repository
public class DynamicDml extends DynamicInsertData {

	static Logger LOGGER = LoggerFactory.getLogger(DynamicDml.class);

	@Autowired
	private GetRequestUniqueData getRequestUniqueData;

	public String MasterDetailDML(String input, JSONObjectImpl confJson) {

		LoggerImpl loggerImpl = null;
		Connection connection = null;
		String ls_response = null;
		JSONObjectImpl configJson;
		JSONObjectImpl reqJson;
		JSONObjectImpl userJson;
		int USERROLE;
		String ls_Action_Type = StringUtils.EMPTY;
		int li_seqPara;
		int ACCESS_INSERT;
		int ACCESS_UPDATE;
		int ACCESS_DELETE;
		boolean isNewRow;
		boolean isDeleteRow;
		String ls_MasterTableName = StringUtils.EMPTY;
		String ls_DetailTableName = StringUtils.EMPTY;

		JSONArrayImpl auditDataArray = null;

		try {
			loggerImpl = new LoggerImpl();

			configJson = confJson;
			reqJson = new JSONObjectImpl(input);
			auditDataArray = new JSONArrayImpl();
			userJson = getRequestUniqueData.getLoginUserDetailsJson();

			USERROLE = Integer.parseInt((String) userJson.get("USERROLE"));
			ls_Action_Type = configJson.getString("ACTION_TYPE");

			li_seqPara = configJson.getInt("SEQUENCE_PARA");
			ACCESS_INSERT = configJson.getJSONObject("ACCESS").optInt("I", -1);
			ACCESS_UPDATE = configJson.getJSONObject("ACCESS").optInt("U", -1);
			ACCESS_DELETE = configJson.getJSONObject("ACCESS").optInt("D", -1);
			connection = getDbConnection();

			if ("MD".equals(ls_Action_Type)) {

				isNewRow = reqJson.optBoolean("_isNewRow", false);
				isDeleteRow = reqJson.optBoolean("_isDeleteRow", false);
				ls_MasterTableName = configJson.getString("MASTER_TABLE");
				ls_DetailTableName = configJson.getString("DETAILS_TABLE");
				if (isNewRow) {
					if (ACCESS_INSERT <= USERROLE) {
						ls_response = InsertData(connection, reqJson, userJson, li_seqPara,
								configJson.optString("SEQUENCE_NAME"), ls_MasterTableName, ls_DetailTableName,
								auditDataArray);
					} else {
						ls_response = ofGetFailedMSg("common.insertrights.error", "",
								"You do not have access to insert a new row. Please contact the administrator", null);
					}
				} else if (isDeleteRow) {
					if (ACCESS_DELETE <= USERROLE) {
						ls_response = DeleteData(connection, reqJson, userJson, ls_MasterTableName, ls_DetailTableName,
								auditDataArray);
					} else {
						ls_response = ofGetFailedMSg("common.deleterights.error", "",
								"You do not have access to the row deletion. Please contact the administrator", null);
					}

				} else {
					if (ACCESS_UPDATE <= USERROLE) {
						ls_response = UpdateData(connection, reqJson, userJson, ls_MasterTableName, ls_DetailTableName,
								auditDataArray);
					} else {
						ls_response = ofGetFailedMSg("common.updaterights.error", "",
								"You do not have access to record updates. Please contact the administrator", null);
					}
				}
			} else {
				return ofGetFailedMSg("common.invalid.dynconfig.data", "",
						"Invalid configuration found. Please contact the administrator .", null);
			}
			// Insert Audit Data
			if (isInsertAuditDataAllowed) {
				insertAuditData(ls_response, connection, auditDataArray);
			}
			connection.commit();

		} catch (SQLException sQLException) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (sQLException.getErrorCode() == 00001) {
				try {
					return ofGetResponseJson(new JSONArrayImpl(), "", "Record Already Exists.", ST99, "R",
							"common.rec_exist").toString();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				return getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:MasterDetailDML", "(ENP284)");
			}
		} catch (Exception exception) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException sQLException) {
					sQLException.printStackTrace();
				}
			}
			return getExceptionMSg(exception, LOGGER, loggerImpl, "IN:MasterDetailDML", "(ENP004)");
		} finally {
			// close the database connections object.
			closeDbObject(connection);
		}
		return ls_response;
	}

	public String MasterDML(String input, JSONObjectImpl confJson) {

		LoggerImpl loggerImpl = null;
		Connection connection = null;
		String ls_response = null;
		JSONObjectImpl configJson;
		JSONObjectImpl reqJson;
		JSONObjectImpl userJson;
		int USERROLE;
		String ls_Action_Type = StringUtils.EMPTY;
		int li_seqPara;
		int ACCESS_INSERT;
		int ACCESS_UPDATE;
		int ACCESS_DELETE;

		boolean isNewRow;
		boolean isDeleteRow;
		String ls_MasterTableName;
		JSONArrayImpl auditDataArray;

		try {
			loggerImpl = new LoggerImpl();

			configJson = confJson;
			reqJson = new JSONObjectImpl(input);
			auditDataArray = new JSONArrayImpl();
			userJson = getRequestUniqueData.getLoginUserDetailsJson();

			USERROLE = Integer.parseInt((String) userJson.get("USERROLE"));
			ls_Action_Type = configJson.getString("ACTION_TYPE");

			li_seqPara = configJson.getInt("SEQUENCE_PARA");
			ACCESS_INSERT = configJson.getJSONObject("ACCESS").optInt("I", -1);
			ACCESS_UPDATE = configJson.getJSONObject("ACCESS").optInt("U", -1);
			ACCESS_DELETE = configJson.getJSONObject("ACCESS").optInt("D", -1);

			connection = getDbConnection();

			if ("M".equals(ls_Action_Type)) {
				isNewRow = reqJson.optBoolean("_isNewRow", false);
				isDeleteRow = reqJson.optBoolean("_isDeleteRow", false);
				ls_MasterTableName = configJson.getString("MASTER_TABLE");

				if (isNewRow) {
					if (ACCESS_INSERT <= USERROLE) {
						ls_response = InsertData(connection, reqJson, userJson, li_seqPara,
								configJson.optString("SEQUENCE_NAME"), ls_MasterTableName, auditDataArray);
					} else {
						ls_response = ofGetFailedMSg("common.insertrights.error", "",
								"You do not have access to insert a new row. Please contact the administrator", null);
					}
				} else if (isDeleteRow) {
					if (ACCESS_DELETE <= USERROLE) {
						ls_response = DeleteData(connection, reqJson, userJson, ls_MasterTableName, auditDataArray);
					} else {
						ls_response = ofGetFailedMSg("common.deleterights.error", "",
								"You do not have access to the row deletion.Please contact the administrator", null);
					}

				} else {
					if (ACCESS_UPDATE <= USERROLE) {
						ls_response = UpdateData(connection, reqJson, userJson, ls_MasterTableName, auditDataArray);
					} else {
						ls_response = ofGetFailedMSg("common.updaterights.error", "",
								"You do not have access to record updates. Please contact the administrator", null);
					}
				}
			} else {
				return ofGetFailedMSg("common.invalid.dynconfig.data", "",
						"Invalid configuration found. Please contact the administrator .", null);
			}
			// Insert Audit Data
			if (isInsertAuditDataAllowed) {
				insertAuditData(ls_response, connection, auditDataArray);
			}
			connection.commit();

		} catch (SQLException sQLException) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (sQLException.getErrorCode() == 00001) {
				try {
					return ofGetResponseJson(new JSONArrayImpl(), "", "Record Already Exists.", ST99, "R",
							"common.rec_exist").toString();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				return getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:MasterDML", "(ENP285)");
			}

		} catch (Exception exception) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException sQLException) {
					sQLException.printStackTrace();
				}
			}
			return getExceptionMSg(exception, LOGGER, loggerImpl, "IN:MasterDML", "(ENP005)");
		} finally {
			// close the database connections object.
			closeDbObject(connection);
		}
		return ls_response;
	}

	public String DetailDML(String input, JSONObjectImpl confJson) {

		LoggerImpl loggerImpl = null;
		Connection connection = null;
		String ls_response = null;
		JSONObjectImpl configJson;
		JSONObjectImpl reqJson;
		JSONObjectImpl userJson;
		int USERROLE;
		String ls_Action_Type = StringUtils.EMPTY;

		int ACCESS_INSERT;
		int ACCESS_UPDATE;
		int ACCESS_DELETE;

		String ls_DetailTableName = StringUtils.EMPTY;
		JSONObjectImpl jDetails;
		JSONArrayImpl jDeleteRow;
		JSONArrayImpl jUpdateRow;
		JSONArrayImpl jInsertRow;
		JSONArrayImpl auditDataArray;
		JSONObjectImpl jreq;

		try {
			loggerImpl = new LoggerImpl();

			configJson = confJson;
			reqJson = new JSONObjectImpl(input);
			auditDataArray = new JSONArrayImpl();
			userJson = getRequestUniqueData.getLoginUserDetailsJson();

			USERROLE = Integer.parseInt((String) userJson.get("USERROLE"));
			ls_Action_Type = configJson.getString("ACTION_TYPE");

			ACCESS_INSERT = configJson.getJSONObject("ACCESS").optInt("I", -1);
			ACCESS_UPDATE = configJson.getJSONObject("ACCESS").optInt("U", -1);
			ACCESS_DELETE = configJson.getJSONObject("ACCESS").optInt("D", -1);

			connection = getDbConnection();

			if ("D".equals(ls_Action_Type)) {
				ls_DetailTableName = configJson.getString("DETAILS_TABLE");
				jDetails = reqJson.getJSONObject("DETAILS_DATA");
				jDeleteRow = jDetails.getJSONArray("isDeleteRow");
				jUpdateRow = jDetails.getJSONArray("isUpdatedRow");
				jInsertRow = jDetails.getJSONArray("isNewRow");
				if (jDeleteRow.length() > 0 && ACCESS_DELETE > USERROLE) {
					ls_response = ofGetFailedMSg("common.deleterights.error", "",
							"You do not have access to the row deletion. Please contact the administrator", null);
				} else if (jUpdateRow.length() > 0 && ACCESS_UPDATE > USERROLE) {
					ls_response = ofGetFailedMSg("common.updaterights.error", "",
							"You do not have access to record updates. Please contact the administrator", null);
				} else if (jInsertRow.length() > 0 && ACCESS_INSERT > USERROLE) {
					ls_response = ofGetFailedMSg("common.insertrights.error", "",
							"You do not have access to insert a new row. Please contact the administrator", null);
				} else {
					ls_response = UpdateDetailsData(connection, jDetails, userJson, reqJson, ls_DetailTableName,
							auditDataArray);
				}

			} else {
				return ofGetFailedMSg("common.invalid.dynconfig.data", "",
						"Invalid configuration found. Please contact the administrator .", null);
			}
			// Insert Audit Data
			if (isInsertAuditDataAllowed) {
				insertAuditData(ls_response, connection, auditDataArray);
			}
			connection.commit();

		} catch (SQLException sQLException) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (sQLException.getErrorCode() == 00001) {
				try {
					return ofGetResponseJson(new JSONArrayImpl(), "", "Record Already Exists.", ST99, "R",
							"common.rec_exist").toString();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				return getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:DetailDML", "(ENP283)");
			}

		} catch (Exception exception) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException sQLException) {
					sQLException.printStackTrace();
				}
			}
			return getExceptionMSg(exception, LOGGER, loggerImpl, "IN:DetailDML", "(ENP006)");

		} finally {
			// close the database connections object.
			closeDbObject(connection);
		}
		return ls_response;
	}

}
