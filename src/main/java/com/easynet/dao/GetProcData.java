package com.easynet.dao;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.GetDataDB;
import com.easynet.util.common;
import oracle.jdbc.OracleTypes;
import static com.easynet.util.ConstantKeyValue.*;

@Repository
public class GetProcData extends GetDataDB {

	static Logger LOGGER = LoggerFactory.getLogger(GetProcData.class);

	public String getCursorData(String as_proc_name, Object... as_input) {
		return getCursorData(as_proc_name, false, as_input);
	}

	public String getCursorData(String as_proc_name, boolean isProcNameWithArgument, Object... as_input) {

		String ls_return = null;
		Connection connection = null;
		CallableStatement callableStatement = null;
		LoggerImpl loggerImpl = null;
		JSONArray response_dataJList = null;
		int parameterIndex = 1;
		String parameter = StringUtils.EMPTY;
		ResultSet resultSet = null;

		try {
			loggerImpl = new LoggerImpl();

			if (as_input != null) {
				parameter = StringUtils.repeat("?", ",", as_input.length);
			}

			connection = getDbConnection();

			if (isProcNameWithArgument) {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "}");
			} else {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "(?," + parameter + ")}");
			}

			callableStatement.registerOutParameter(1, OracleTypes.CURSOR);

			ofsetQueryParameter(parameterIndex, callableStatement, as_input);

			// execute stored procedure
			callableStatement.execute();
			resultSet = (ResultSet) callableStatement.getObject(1);

			response_dataJList = getResultSetData(resultSet);

			// return common.ofGetResponseJson(response_dataJList, "", "",
			// ST0,"G","").toString();
			return ofGetResponseJson(response_dataJList, "", "", ST0, "G", "").toString();

		} catch (SQLException sQLException) {
			ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:getCursorData", "(ENP012)",
					"SQLException : ", null);

		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getCursorData", "(ENP013)");

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(connection, resultSet, callableStatement);
		}
		return ls_return;
	}

	/**
	 * Used this method where want to used connection in cursor.
	 **/

	public String getCursorData(Connection connection, String as_proc_name, Object... as_input) {
		return getCursorData(connection, as_proc_name, false, as_input);
	}

	public String getCursorData(Connection connection, String as_proc_name, boolean isProcNameWithArgument,
			Object... as_input) {

		String ls_return = null;
		// Connection connection = null;
		CallableStatement callableStatement = null;
		LoggerImpl loggerImpl = null;
		JSONArrayImpl response_dataJList = null;
		int parameterIndex = 1;
		String parameter = StringUtils.EMPTY;
		ResultSet resultSet = null;

		try {
			loggerImpl = new LoggerImpl();

			if (as_input != null) {
				parameter = StringUtils.repeat("?", ",", as_input.length);
			}

			// connection = getDbConnection();

			if (isProcNameWithArgument) {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "}");
			} else {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "(?," + parameter + ")}");
			}

			callableStatement.registerOutParameter(1, OracleTypes.CURSOR);

			ofsetQueryParameter(parameterIndex, callableStatement, as_input);

			// execute stored procedure
			callableStatement.execute();
			resultSet = (ResultSet) callableStatement.getObject(1);

			response_dataJList = getResultSetData(resultSet);

			// return common.ofGetResponseJson(response_dataJList, "", "",
			// ST0,"G","").toString();
			return ofGetResponseJson(response_dataJList, "", "", ST0, "G", "").toString();

		} catch (SQLException sQLException) {
			ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:getCursorData", "(ENP012)",
					"SQLException : ", null);

		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getCursorData", "(ENP013)");

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(callableStatement, resultSet);
		}
		return ls_return;
	}

	@Override
	public String ofGetResponseData(String input) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getClobData(String as_proc_name, Object... as_input) {
		return getClobData(as_proc_name, false, as_input);
	}

	public String getClobData(String as_proc_name, boolean isProcNameWithArgument, Object... as_input) {

		String ls_return = null;
		Connection connection = null;
		CallableStatement callableStatement = null;
		LoggerImpl loggerImpl = null;
		int parameterIndex = 1;
		String parameter = StringUtils.EMPTY;
		Clob clob_data;

		try {
			loggerImpl = new LoggerImpl();

			if (as_input != null) {
				parameter = StringUtils.repeat("?", ",", as_input.length);
			}

			connection = getDbConnection();

			if (isProcNameWithArgument) {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "}");
			} else {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "(?," + parameter + ")}");
			}
			callableStatement.registerOutParameter(1, OracleTypes.CLOB);

			ofsetQueryParameter(parameterIndex, callableStatement, as_input);

			// execute stored procedure
			callableStatement.execute();

			clob_data = callableStatement.getClob(1);
			if (clob_data == null) {
				return ofGetFailedMSg("common.exception", ST999,
						"Null response get from Procedure " + as_proc_name + ".", "(ENP014)");
			} else {
				ls_return = clob_data.getSubString(1, (int) clob_data.length());
			}

		} catch (SQLException sQLException) {
			ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:getClobData", "(ENP015)",
					"SQLException : ", null);

		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getClobData", "(ENP016)");

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(connection, callableStatement);
		}
		return ls_return;
	}

	public String procExecuteOnly(String as_proc_name, Object... as_input) {
		return procExecuteOnly(as_proc_name, false, as_input);
	}

	/**
	 * Used this method where want to used connection in cursor and save DD /NEFT
	 * DETAILS
	 **/

	public String savePaymentTransferDetails(Connection connection, Object... as_input) {
		return getCursorData(connection, "PACK_GEN_OTH_MODULE_DD_NEFT.PROC_GEN_ENTRY", false, as_input);
	}

	public String procExecuteOnly(String as_proc_name, boolean isProcNameWithArgument, Object... as_input) {

		String ls_return = null;
		Connection connection = null;
		LoggerImpl loggerImpl = null;
		String ls_procResData = StringUtils.EMPTY;
		JSONObjectImpl procResDataJson;

		try {
			loggerImpl = new LoggerImpl();
			connection = getDbConnection();

			ls_procResData = procExecuteOnly(connection, as_proc_name, isProcNameWithArgument, as_input);

			procResDataJson = common.ofGetJsonObject(ls_procResData);

			if (isSuccessStCode(procResDataJson.getString(STATUS))) {
				connection.commit();
			} else {
				connection.rollback();
			}
			return ls_procResData;

		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:procExecuteOnly", "(ENP017)");

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(connection);
		}
		return ls_return;
	}

	public String procExecuteOnly(Connection connection, String as_proc_name, Object... as_input) {
		return procExecuteOnly(connection, as_proc_name, false, as_input);
	}

	public String procExecuteOnly(Connection connection, String as_proc_name, boolean isProcNameWithArgument,
			Object... as_input) {

		String ls_return = null;
		CallableStatement callableStatement = null;
		LoggerImpl loggerImpl = null;
		int parameterIndex = 0;
		String parameter = StringUtils.EMPTY;

		try {
			loggerImpl = new LoggerImpl();

			if (as_input != null) {
				parameter = StringUtils.repeat("?", ",", as_input.length);
			}

			if (isProcNameWithArgument) {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "}");
			} else {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "(" + parameter + ")}");
			}

			ofsetQueryParameter(parameterIndex, callableStatement, as_input);

			// execute stored procedure
			callableStatement.execute();

			// return common.ofGetResponseJson(new JSONArrayImpl(), "", "",
			// ST0,"G","").toString();
			return ofGetResponseJson(new JSONArrayImpl(), "", "", ST0, "G", "").toString();

		} catch (SQLException sQLException) {
			ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:procExecuteOnly", "(ENP018)",
					"SQLException : ", null);

		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:procExecuteOnly", "(ENP019)");

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(callableStatement);
		}
		return ls_return;
	}

	public String getJsonData(String as_proc_name, Object... as_input) {
		return getJsonData(as_proc_name, false, as_input);
	}

	public String getJsonData(String as_proc_name, boolean isProcNameWithArgument, Object... as_input) {

		String ls_return = null;
		Connection connection = null;
		CallableStatement callableStatement = null;
		LoggerImpl loggerImpl = null;
		int parameterIndex = 2;
		String parameter = StringUtils.EMPTY;
		JSONObjectImpl json_data;

		try {
			loggerImpl = new LoggerImpl();

			if (as_input != null) {
				parameter = StringUtils.repeat("?", ",", as_input.length);
			}

			connection = getDbConnection();

			if (isProcNameWithArgument) {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "}");
			} else {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "(?," + parameter + ")}");
			}

			callableStatement.registerOutParameter(1, OracleTypes.OTHER);
//			JSONObject data = new JSONObject();
//			data.put(key, value)

			ofsetQueryParameter(parameterIndex, callableStatement, as_input);

			// execute stored procedure

			ResultSet rs = callableStatement.executeQuery();

			ResultSetMetaData rsMetaData = rs.getMetaData();
			int count = rsMetaData.getColumnCount();

			while (rs.next()) {
				JSONObjectImpl jo_obj = new JSONObjectImpl();
				for (int i = 1; i <= count; i++) {
					jo_obj.put(rsMetaData.getColumnName(i), rs.getString(i));

				}
			}

			json_data = (JSONObjectImpl) callableStatement.getObject(1);
			if (json_data == null) {
				return ofGetFailedMSg("common.exception", ST999,
						"Null response get from Procedure " + as_proc_name + ".", "(ENP144)");
			} else {
				ls_return = json_data.toString();

			}
		} catch (SQLException sQLException) {
			ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:getJsonData", "(ENP145)",
					"SQLException : ", null);

		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getJsonData", "(ENP146)");

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(connection, callableStatement);
		}
		return ls_return;
	}

	/**
	 * This method is used for call the procedure and get the multiple output from
	 * procedure.
	 * <p>
	 * Limitation
	 * <ul>
	 * <li>This method will not work in case of procedure conatin same parameter as
	 * In OUT.</li>
	 * <li>Parameter sequence should be 1st all aregument of IN and then Out.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param as_proc_name Procedure Name
	 * @param outParaList  List of out parameter.
	 * @param as_input     Input parameter list
	 * @return Return the object list of object which pass.
	 * @throws Exception Caller should handle the exception
	 * 
	 */
	public ArrayList<Object> getprocedureAllOutData(String as_proc_name, ArrayList<Integer> outParaList,
			Object... as_input) throws Exception {
		return getprocedureAllOutData(as_proc_name, false, outParaList, as_input);
	}

	/**
	 * This method is used for call the procedure and get the multiple output from
	 * procedure.
	 * <p>
	 * Limitation
	 * <ul>
	 * <li>This method will not work in case of procedure conatin same parameter as
	 * In OUT.</li>
	 * <li>Parameter sequence should be 1st all aregument of IN and then Out.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param as_proc_name           Procedure Name
	 * @param isProcNameWithArgument If true then procedure name should contain the
	 *                               argument list.
	 * @param outParaList            List of out parameter.
	 * @param as_input               Input parameter list
	 * @return return the Object list of object which pass.
	 * @throws Exception Caller should handle the exception
	 * 
	 */
	public ArrayList<Object> getprocedureAllOutData(String as_proc_name, boolean isProcNameWithArgument,
			ArrayList<Integer> outParaList, Object... as_input) throws Exception {

		Connection connection = null;

		try {
			connection = getDbConnection();
			return getprocedureAllOutData(connection, as_proc_name, isProcNameWithArgument, outParaList, as_input);
		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(connection);
		}
	}

	/**
	 * This method is used for call the procedure and get the multiple output from
	 * procedure.
	 * <p>
	 * Limitation
	 * <ul>
	 * <li>This method will not work in case of procedure conatin same parameter as
	 * In OUT.</li>
	 * <li>Parameter sequence should be 1st all aregument of IN and then Out.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param as_proc_name           Procedure Name
	 * @param isProcNameWithArgument If true then procedure name should contain the
	 *                               argument list.
	 * @param outParaList            List of out parameter.
	 * @param as_input               Input parameter list
	 * @return return the Object list of object which pass.
	 * @throws Exception Caller should handle the exception
	 * 
	 */
	public ArrayList<Object> getprocedureAllOutData(Connection connection, String as_proc_name,
			boolean isProcNameWithArgument, ArrayList<Integer> outParaList, Object... as_input) throws Exception {

		CallableStatement callableStatement = null;
		int parameterIndex = 0;
		String ls_parameter = StringUtils.EMPTY;
		String ls_outParameter = StringUtils.EMPTY;
		ResultSet resultSet = null;
		int li_outParaList = outParaList.size();
		ArrayList<Object> outParaDataList = new ArrayList<Object>();
		int outParaNumber;
		int outParaStartIndex = 0;

		try {
			if (as_input != null) {
				ls_parameter = StringUtils.repeat("?", ",", as_input.length);
			}

			if (outParaList != null) {
				ls_outParameter = "," + StringUtils.repeat("?", ",", outParaList.size());
			}

			if (isProcNameWithArgument) {
				callableStatement = connection.prepareCall("{CALL " + as_proc_name + "}");
			} else {
				callableStatement = connection
						.prepareCall("{CALL " + as_proc_name + "(" + ls_parameter + ls_outParameter + ")}");
			}

			parameterIndex = ofsetQueryParameter(parameterIndex, callableStatement, as_input);

			if (outParaList != null) {
				outParaStartIndex = parameterIndex;
				parameterIndex = ofsetProcOutParameter(parameterIndex, callableStatement, outParaList.toArray());
			}

			// execute stored procedure
			callableStatement.execute();

			for (int i = 0; i < li_outParaList; i++) {
				outParaNumber = outParaList.get(i);

				outParaStartIndex++;

				if (outParaNumber == OracleTypes.CURSOR) {
					resultSet = (ResultSet) callableStatement.getObject(outParaStartIndex);
					outParaDataList.add(getResultSetData(resultSet));
				} else if (outParaNumber == OracleTypes.CLOB) {
					Clob clobObj = callableStatement.getClob(outParaStartIndex);
					if (Objects.isNull(clobObj)) {
						outParaDataList.add("");
					} else {
						outParaDataList.add(clobObj.getSubString(1, (int) clobObj.length()));
					}
				} else if (outParaNumber == OracleTypes.CHAR) {
					outParaDataList.add(callableStatement.getString(outParaStartIndex));
				} else if (outParaNumber == OracleTypes.DECIMAL) {
					outParaDataList.add(callableStatement.getBigDecimal(outParaStartIndex));
				} else if (outParaNumber == OracleTypes.DATE) {
					outParaDataList.add(callableStatement.getDate(outParaStartIndex));
				} else if (outParaNumber == OracleTypes.INTEGER) {
					outParaDataList.add(callableStatement.getInt(outParaStartIndex));
				} else if (outParaNumber == OracleTypes.VARCHAR) {
					outParaDataList.add(callableStatement.getString(outParaStartIndex));
				} else if (outParaNumber == OracleTypes.NUMBER) {
					outParaDataList.add(callableStatement.getInt(outParaStartIndex));
				} else if (outParaNumber == OracleTypes.TIMESTAMP) {
					outParaDataList.add(callableStatement.getTimestamp(outParaStartIndex));
				} else {
					outParaDataList.add(callableStatement.getObject(outParaStartIndex));
				}
			}

			return outParaDataList;
		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(callableStatement, resultSet);
		}
	}

	/**
	 * This method is used to save data in audit table ENC_EXCEPTION_MST after
	 * delete data and call the procedure procedure.
	 * 
	 * @param loginUserDetailObject login user detail object from request
	 * @param as_deleteQuery        delete query.
	 * @param as_whereParainput     string array of input parameters which pass in
	 *                              delete query.
	 * @param as_proc_name          Procedure Name
	 * @param outParaList           output parameter list of procedure
	 * @param as_ProcParainput      Input parameter list
	 * @return Return the response message.
	 * @throws Exception Caller should handle the exception
	 * 
	 */

	public String insertAuditEntryOnDeletion(Connection connection, String as_deleteQuery, String[] as_whereParainput,
			String as_proc_name, ArrayList<Integer> outParaList, Object... as_input) {

		String ls_resStatus = null;
		String ls_resMessage = null;
		String response_data = null;

		PreparedStatement deleteStmt = null;
		JSONObjectImpl mainResJson = new JSONObjectImpl();
		ArrayList<Object> returnDataList;
		LoggerImpl loggerImpl = null;
//		int parameterIndex = 2;
		int parameter = 0;
		int rowcount;
		JSONObjectImpl json_data;

		try {
			loggerImpl = new LoggerImpl();

			// 1. Execute DELETE query to remove the data
			deleteStmt = connection.prepareStatement(as_deleteQuery);

			ofsetQueryParameter(parameter, deleteStmt, as_whereParainput);

			rowcount = deleteStmt.executeUpdate();

			if (rowcount == 0) {
				return ofGetResponseJson(new JSONArrayImpl(), "", "Data Deletion Fail...", ST999, "G",
						"common.data_deletion_fail").toString();
			}

			returnDataList = getprocedureAllOutData(connection, as_proc_name, false, outParaList, as_input);

			ls_resStatus = (String) returnDataList.get(0);
			ls_resMessage = (String) returnDataList.get(1);

			if (ls_resStatus.equals("0")) {
				// connection.commit();
				return ofGetResponseJson(new JSONArrayImpl(), "", "", ST0, "G", "").toString();

			} else {
				// connection.rollback();
				mainResJson.put("STATUS", ls_resStatus);
				mainResJson.put("MESSAGE", ls_resMessage);
				return response_data = ofGetResponseJson(new JSONArrayImpl().put(mainResJson), "", "", ST99, "R", "")
						.toString();
			}

		} catch (SQLException sQLException) {
			return response_data = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:insertAuditEntryOnDeletion",
					"(ENP559)", "SQLException : ", null);

		} catch (Exception exception) {
			return response_data = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:insertAuditEntryOnDeletion",
					"(ENP560)");

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(deleteStmt);
		}

	}

}