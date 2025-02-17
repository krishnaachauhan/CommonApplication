package com.easynet.dao;

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
import static com.easynet.util.ConstantKeyValue.*;

@Repository
public class InsertData extends GetDataDB{

	static Logger LOGGER=LoggerFactory.getLogger(InsertData.class);

	public String insertDataWithObject(String as_query ,Object... as_input) throws SQLException
	{
		String ls_return = null;       
		Connection connection = null;
		PreparedStatement  preparedStatement = null;
		LoggerImpl loggerImpl=null;
		String ls_insertResData =StringUtils.EMPTY;
		JSONObjectImpl insertResDataJson;
		
		try {
			loggerImpl=new LoggerImpl();	
			connection=getDbConnection();

			ls_insertResData=insertDataWithObj(connection,as_query,as_input);			
			insertResDataJson=common.ofGetJsonObject(ls_insertResData);

			if (isSuccessStCode(insertResDataJson.getString(STATUS))) {
				connection.commit();									
			}else {
				connection.rollback();
			}			
			return ls_insertResData;

		} catch (Exception exception) {
			connection.rollback();
			ls_return= getExceptionMSg(exception, LOGGER, loggerImpl, "IN:insertDataWithObject", "(ENP022)" );

		} finally {
			//It's important to close the statement when you are done with			
			closeDbObject(connection,preparedStatement);			
		}	
		return  ls_return;
	}


//		public String insertDataWithObject(Connection con, String as_query, Object... as_input) throws Exception {
//	
//			PreparedStatement preparedStatement = null;
//			LoggerImpl loggerImpl = null;
//			int parameterIndex = 1;
//	
//			try {
//				loggerImpl = new LoggerImpl();
//				preparedStatement = con.prepareStatement(as_query);
//	
//				ofsetQueryParameter(parameterIndex, preparedStatement, as_input);
//	
//				preparedStatement.executeUpdate();
//				return "";
//	
//			} catch (SQLException e) {
//				if(e.getErrorCode() == 00001){
//					return common.ofGetResponseJson(new JSONArray(), "", "Record Already Exists.", ST99,"R","Record Already Exists.").toString();
//	
//				}else{
//					return getExceptionMSg(e, logger, loggerImpl, "IN:insertDataWithObject", "(ENP757)");
//				}	
//			} catch (Exception e) {
//				return getExceptionMSg(e, logger, loggerImpl, "IN:insertDataWithObject", "(ENP757)");
//			} finally {			
//				closeDbObject(preparedStatement);			
//			}
//		}

	public String insertDataWithObj(Connection con,String as_query ,Object... as_input) throws Exception
	{
		String ls_return = null;       
		PreparedStatement  preparedStatement = null;
		LoggerImpl loggerImpl=null;
		int parameterIndex=0;

		try {
			loggerImpl=new LoggerImpl();

			preparedStatement=con.prepareStatement(as_query);

			ofsetQueryParameter(parameterIndex, preparedStatement, as_input);

			preparedStatement.executeUpdate();

			//return common.ofGetResponseJson(new JSONArrayImpl(), "", "", ST0,"G","").toString();
			return ofGetResponseJson(new JSONArrayImpl(), "", "", ST0,"G","").toString();


		}catch (SQLException sQLException) {
			if(sQLException.getErrorCode() == 00001){
				//return common.ofGetResponseJson(new JSONArrayImpl(), "", "Record Already Exists.", ST99,"R","Record Already Exists.").toString();
				return ofGetResponseJson(new JSONArrayImpl(), "", "Record Already Exists.", ST99,"R","common.rec_exist").toString();

			}else{
				return getExceptionMSg(sQLException, LOGGER, loggerImpl, "IN:insertDataWithObject", "(ENP23)");
			}	
		}catch (Exception exception) {
			ls_return= getExceptionMSg(exception, LOGGER, loggerImpl, "IN:insertDataWithObj", "(ENP024)" );

		} finally {
			//It's important to close the statement when you are done with		
			closeDbObject(preparedStatement);			
		}

		return  ls_return;
	}

	@Override
	public String ofGetResponseData(String input) {
		// TODO Auto-generated method stub
		return null;
	}

}
