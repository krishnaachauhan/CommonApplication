package com.easynet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.GetDataDB;
import com.easynet.util.common;

@Repository
public class SelectData extends GetDataDB{

	static Logger LOGGER=LoggerFactory.getLogger(SelectData.class);

	public String getSelectData(String Query , Object... as_input){

		String ls_return = null;       
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		LoggerImpl loggerImpl=null;
		JSONArrayImpl responseDataJlist = new JSONArrayImpl() ;
		int parameterIndex=0;
		ResultSet resultSet=null;

		try {
			loggerImpl=new LoggerImpl();

			connection=getDbConnection();
			preparedStatement=connection.prepareStatement(Query);

			ofsetQueryParameter(parameterIndex, preparedStatement, as_input);

			resultSet =preparedStatement.executeQuery();

			responseDataJlist=getResultSetData(resultSet);

			return common.ofGetResponseJson(responseDataJlist, "", "", ST0,"G","").toString();
//			return ofGetResponseJson(responseDataJlist, "", "Success", ST0, "G", "common.success_msg")
//					.toString();
			

		} catch (SQLException sQLException) {
			ls_return= getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:getSelectData", "(ENP025)","SQLException : ",null );

		} catch (Exception exception) {
			ls_return= getExceptionMSg(exception, LOGGER, loggerImpl, "IN:getSelectData", "(ENP026)" );

		} finally {
			//It's important to close the statement when you are done with
			closeDbObject(connection,resultSet,preparedStatement);	
		}

		return  ls_return;
	}
	public JSONArrayImpl getSelectData(Connection connection,String Query , Object... as_input) throws Exception{
	      
		PreparedStatement preparedStatement = null;
		int parameterIndex=0;
		ResultSet resultSet=null;

		try {
			preparedStatement=connection.prepareStatement(Query);

			ofsetQueryParameter(parameterIndex, preparedStatement, as_input);

			resultSet =preparedStatement.executeQuery();

			return getResultSetData(resultSet);

		} finally {
			//It's important to close the statement when you are done with
			closeDbObject(preparedStatement,resultSet);	
		}
	}

	public final Long getSeqNumber(Connection connection,String sequenceName) throws Exception
	{
		String REGSEQQUERY = "SELECT TO_NUMBER("+sequenceName+".NEXTVAL) AS GEN_SEQ_NO FROM DUAL";
		return getSelectData(connection,REGSEQQUERY).getJSONObject(0).getBigDecimal("GEN_SEQ_NO").longValue();
	}
	@Override
	public String ofGetResponseData(String input) {
		// TODO Auto-generated method stub
		return null;
	}

}

