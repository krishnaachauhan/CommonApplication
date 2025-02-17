package com.easynet.dao;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.easynet.bean.GetRequestUniqueData;
import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.GetDataDB;

import oracle.jdbc.OracleTypes;

@Repository
public class GetFuctionData extends GetDataDB {

	static Logger LOGGER = LoggerFactory.getLogger(GetFuctionData.class);

	@Autowired
	private GetRequestUniqueData getRequestUniqueData;

	public String getClobData(String as_func_name, Object... as_input) {

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

			callableStatement = connection.prepareCall("{? = call " + as_func_name + "(" + parameter + ")}");

			callableStatement.registerOutParameter(1, OracleTypes.CLOB);

			ofsetQueryParameter(parameterIndex, callableStatement, as_input);

			// execute stored Function
			callableStatement.execute();

			clob_data = callableStatement.getClob(1);
			if (clob_data == null) {
				return ofGetFailedMSg("common.exception", ST999,
						"Null response get from function " + as_func_name + ".", "(ENP007)");
			} else {
				ls_return = clob_data.getSubString(1, (int) clob_data.length());
			}

		} catch (SQLException sQLException) {
			ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:getClobData", "(ENP628)",
					"SQLException : ", null);

		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getClobData", "(ENP629)");

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(connection, callableStatement);
		}
		return ls_return;
	}
	
	//ADDED METHOD TO GET BLOB DATA 
	
	public String getBlobData(String as_func_name, Object... as_input) {

	    String ls_return = null;
	    Connection connection = null;
	    CallableStatement callableStatement = null;
	    LoggerImpl loggerImpl = null;
	    int parameterIndex = 1;
	    String parameter = StringUtils.EMPTY;
	    Blob blob_data;

	    try {
	        loggerImpl = new LoggerImpl();

	        if (as_input != null) {
	            parameter = StringUtils.repeat("?", ",", as_input.length);
	        }

	        connection = getDbConnection();

	        callableStatement = connection.prepareCall("{? = call " + as_func_name + "(" + parameter + ")}");

	        callableStatement.registerOutParameter(1, OracleTypes.BLOB);

	        ofsetQueryParameter(parameterIndex, callableStatement, as_input);

	        // execute stored Function
	        callableStatement.execute();

	        blob_data = callableStatement.getBlob(1);
	        if (blob_data == null) {
	            return ofGetFailedMSg("common.exception", ST999,
	                    "Null response get from function " + as_func_name + ".", "(ENP007)");
	        } else {
	            // Convert the Blob to a byte array
	            byte[] blobBytes = blob_data.getBytes(1, (int) blob_data.length());
	            // Convert the byte array to a String (if necessary)
	            ls_return = new String(blobBytes, "UTF-8"); // Adjust the encoding as needed
	        }

	    } catch (SQLException sQLException) {
	        ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:getBlobData", "(ENP008)",
	                "SQLException : ", null);

	    } catch (Exception exception) {
	        ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getBlobData", "(ENP009)");

	    } finally {
	        // It's important to close the statement when you are done with
	        closeDbObject(connection, callableStatement);
	    }
	    return ls_return;
	}
	
	
	public String getCursorData(String as_func_name, Object... as_input) {

	    String ls_return = null;
	    Connection connection = null;
	    CallableStatement callableStatement = null;
	    ResultSet resultSet = null;
	    LoggerImpl loggerImpl = null;
	    int parameterIndex = 1;
	    String parameter = StringUtils.EMPTY;

	    try {
	        loggerImpl = new LoggerImpl();

	        if (as_input != null) {
	            parameter = StringUtils.repeat("?", ",", as_input.length);
	        }

	        connection = getDbConnection();

	        callableStatement = connection.prepareCall("{? = call " + as_func_name + "(" + parameter + ")}");

	        // Register the output parameter as a cursor
	        callableStatement.registerOutParameter(1, OracleTypes.CURSOR);

	        ofsetQueryParameter(parameterIndex, callableStatement, as_input);

	        // Execute the stored function
	        callableStatement.execute();

	        resultSet = (ResultSet) callableStatement.getObject(1);

			JSONArrayImpl response_dataJList = getResultSetData(resultSet);


			return ofGetResponseJson(response_dataJList, "", "", ST0, "G", "").toString();

	    } catch (SQLException sQLException) {
	        ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:getCursorData", "(ENP008)",
	                "SQLException : ", null);

	    } catch (Exception exception) {
	        ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getCursorData", "(ENP009)");

	    } finally {
	        // It's important to close the statement when you are done with
	        closeDbObject(connection,resultSet, callableStatement);
	    }
	    return ls_return;
	}

	public String getDecimalValue(String as_func_name, Object... as_input) {
		String ls_return = null;
		Connection connection = null;
		CallableStatement callableStatement = null;
		LoggerImpl loggerImpl = null;
		String parameter = StringUtils.EMPTY;
		int parameterIndex = 2;

		try {
			loggerImpl = new LoggerImpl();

			if (as_input != null) {
				parameter = StringUtils.repeat("?", ",", as_input.length);
			}

			connection = getDbConnection();

			callableStatement = connection.prepareCall("{? = call " + as_func_name + "(" + parameter + ")}");
			callableStatement.registerOutParameter(1, OracleTypes.DECIMAL);

			ofsetQueryParameter(parameterIndex, callableStatement, as_input);

			// execute stored Function
			callableStatement.execute();

			return callableStatement.getString(1);

		} catch (SQLException sQLException) {
			ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:getDecimalValue", "(ENP010)",
					"SQLException : ", null);

		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getDecimalValue", "(ENP011)");

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(connection, callableStatement);
		}
		return ls_return;
	}

	public Object getAllTypeReturnValue(String as_func_name, int ai_outParaNumer, Object... as_input) throws Exception {

		Connection connection = null;
		try {
			connection = getDbConnection();

			return getAllTypeReturnValue(connection, as_func_name, ai_outParaNumer, as_input);

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(connection);
		}
	}

	public Object getAllTypeReturnValue(Connection connection, String as_func_name, int ai_outParaNumer,
			Object... as_input) throws Exception {

		CallableStatement callableStatement = null;
		String parameter = StringUtils.EMPTY;
		int parameterIndex = 1;

		try {

			if (as_input != null) {
				parameter = StringUtils.repeat("?", ",", as_input.length);
			}

			callableStatement = connection.prepareCall("{? = call " + as_func_name + "(" + parameter + ")}");
			callableStatement.registerOutParameter(1, ai_outParaNumer);

			ofsetQueryParameter(parameterIndex, callableStatement, as_input);

			// execute stored Function
			callableStatement.execute();

			if (ai_outParaNumer == OracleTypes.CLOB) {
				return callableStatement.getClob(1);
			} else if (ai_outParaNumer == OracleTypes.CHAR) {
				return callableStatement.getString(1);
			} else if (ai_outParaNumer == OracleTypes.DECIMAL) {
				return callableStatement.getBigDecimal(1);
			} else if (ai_outParaNumer == OracleTypes.DATE) {
				return callableStatement.getDate(1);
			} else if (ai_outParaNumer == OracleTypes.INTEGER) {
				return callableStatement.getInt(1);
			} else if (ai_outParaNumer == OracleTypes.VARCHAR) {
				return callableStatement.getString(1);
			} else if (ai_outParaNumer == OracleTypes.NUMBER) {
				return callableStatement.getInt(1);
			} else if (ai_outParaNumer == OracleTypes.TIMESTAMP) {
				return callableStatement.getTimestamp(1);
			} else if (ai_outParaNumer == OracleTypes.CURSOR) {
				return callableStatement.getTimestamp(1);
			} else {
				return callableStatement.getObject(1);
			}

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(callableStatement);
		}
	}

	@Override
	public String ofGetResponseData(String input) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This Method is use for Get Message from Database
	 * 
	 * @param connection       Connection object for execution.
	 * @param display_language Display Language
	 * @param messageCode      Configured Message Code
	 * @return String Message
	 * @throws Exception
	 */
	public String getDBConfiguredMessage(Connection connection, int messageCode) throws Exception {
		return getDBConfiguredMessage(connection, messageCode, "");
	}

	/**
	 * This method is use for get message from database
	 * 
	 * @param connection       Connection object for execution.
	 * @param display_language Display Language
	 * @param messageCode      Configured Message Code
	 * @param defaultMessage   Default Message if Configuration not found
	 * @return String Message
	 * @throws Exception
	 */
	public String getDBConfiguredMessage(Connection connection, int messageCode, String defaultMessage)
			throws Exception {
		return StringEscapeUtils.unescapeJava((String) getAllTypeReturnValue(connection, "ENFINITY.FUNC_GET_LANG_MSG",
				OracleTypes.VARCHAR, messageCode, getRequestUniqueData.getLangCode(), defaultMessage));
	}

	/**
	 * This method is use for get message from database
	 * 
	 * @param display_language Display Language
	 * @param messageCode      Configured Message Code
	 * @return String Message
	 * @throws Exception
	 */
	public String getDBConfiguredMessage(int messageCode) throws Exception {
		return getDBConfiguredMessage(messageCode, "");
	}

	/**
	 * This method is use for get message from database
	 * 
	 * @param display_language Display Language
	 * @param messageCode      Configured Message Code
	 * @param defaultMessage   Default Message if Configuration not found
	 * @return String Message
	 * @throws Exception
	 */
	public String getDBConfiguredMessage(int messageCode, String defaultMessage) throws Exception {

		Connection connection = null;
		connection = getDbConnection();
		try {
			return getDBConfiguredMessage(connection, messageCode, defaultMessage);
		} catch (Exception exception) {
			throw exception;
		} finally {
			closeDbObject(connection);
		}
	}

}
