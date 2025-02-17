package com.easynet.dao;

import static com.easynet.util.ConstantKeyValue.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.GetDataDB;
import com.easynet.util.common;

@Repository
public class DeleteData extends GetDataDB {

	static Logger LOGGER = LoggerFactory.getLogger(DeleteData.class);

	@Override
	public String ofGetResponseData(String input) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toDeleteRow(String Query, Object... as_input) throws SQLException {
		String ls_return = null;
		Connection connection = null;
		LoggerImpl loggerImpl = null;
		String ls_deleteResData = StringUtils.EMPTY;
		JSONObjectImpl deleteResDataJson = null;

		try {
			loggerImpl = new LoggerImpl();

			connection = getDbConnection();

			ls_deleteResData = toDeleteRow(connection, Query, as_input);
			deleteResDataJson = common.ofGetJsonObject(ls_deleteResData);

			if (isSuccessStCode(deleteResDataJson.getString(STATUS))) {
				connection.commit();
			} else {
				connection.rollback();
			}
			return ls_deleteResData;

		} catch (Exception exception) {
			connection.rollback();
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:toDeleteRow", "(ENP001)");

		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(connection);
		}
		return ls_return;

	}

	public String toDeleteRow(Connection connection, String Query, Object... as_input) throws SQLException {
		String ls_return = null;
		PreparedStatement preparedStatement = null;
		LoggerImpl loggerImpl = null;
		int parameterIndex = 0;
		int rowcount;

		try {
			loggerImpl = new LoggerImpl();

			preparedStatement = connection.prepareStatement(Query);

			ofsetQueryParameter(parameterIndex, preparedStatement, as_input);

			rowcount = preparedStatement.executeUpdate();

			if (rowcount == 0) {
				return ofGetResponseJson(new JSONArrayImpl(), "", "Data Deletion Fail...", ST999, "G",
						"common.data_deletion_fail").toString();
			}

			return ofGetResponseJson(new JSONArrayImpl(), "", "", ST0, "G", "").toString();

		} catch (SQLException sQLException) {
			ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:toDeleteRow", "(ENP002)",
					"SQLException : ", null);
		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:toDeleteRow", "(ENP003)");
		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(preparedStatement);
		}
		return ls_return;
	}

	public String deleteData(Connection connection, String deleteQuery, Object... as_input) throws SQLException {
		String ls_return = null;
		PreparedStatement preparedStatement = null;
		LoggerImpl loggerImpl = null;
		int parameterIndex = 0;
		try {
			loggerImpl = new LoggerImpl();

			preparedStatement = connection.prepareStatement(deleteQuery);

			ofsetQueryParameter(parameterIndex, preparedStatement, as_input);

			// Execute the DELETE statement
			int rowsDeleted = preparedStatement.executeUpdate();

			return ofGetResponseJson(new JSONArrayImpl(), "", "", ST0, "G", "").toString();

		} catch (SQLException sQLException) {
			ls_return = getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:deleteData", "(ENP494)",
					"SQLException : ", null);
		} catch (Exception exception) {
			ls_return = getExceptionMSg(exception, LOGGER, loggerImpl, "IN:deleteData", "(ENP493)");
		} finally {
			// It's important to close the statement when you are done with
			closeDbObject(preparedStatement);
		}
		return ls_return;
	}

}