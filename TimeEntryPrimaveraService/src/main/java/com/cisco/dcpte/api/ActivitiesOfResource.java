package com.cisco.dcpte.api;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Properties;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.STRUCT;

import org.apache.log4j.Logger;

import com.cisco.dcpte.dao.DAOCommonUtils;
import com.cisco.dcpte.model.ErrorDetail;
import com.cisco.dcpte.model.ObjectFactory;
import com.cisco.dcpte.model.ProjectProfileResponse;
import com.cisco.dcpte.model.ProjectSnapShot;
import com.cisco.dcpte.util.DCPConstants;
import com.primavera.ServerException;
import com.primavera.integration.client.ClientException;
import com.primavera.integration.network.NetworkException;

/**
 * @author egandi
 * Class Name: ActivitiesOfResource 
 * Aim: Implementing logic for getActivities webservice
 */
public class ActivitiesOfResource {

	private static final Logger LOGGER = Logger
			.getLogger(ActivitiesOfResource.class);

	private ObjectFactory objFactory = new ObjectFactory();
	
	//Modified by gdeshmuk for DE1726
	private static String activityThresholdvalue;
	
	private static String timeZone;
	static
 {
		Properties prop = new Properties();
		InputStream in = null;

		try {
			String propFile = "config.properties";
			in = new ActivitiesOfResource().getClass()
					.getClassLoader().getResourceAsStream(propFile);
			prop.load(in);
			//Modified by gdeshmuk for DE1726
			activityThresholdvalue  = prop.getProperty("thresholdForActivity");
			timeZone = prop.getProperty("timeZone");

			LOGGER.info("The TimeZone value getting from config.properties file is : "
							+ timeZone);
			//Modified by gdeshmuk for DE1726
			LOGGER.info("The Activity Counter getting from config.properties file is : "
					+ activityThresholdvalue);
		} catch (Exception e) {
			LOGGER.error( "Exception",e);
		}finally{
			try{
				if(in!= null){
					in.close();
				}
			}catch(IOException ioe){
				LOGGER.error("Exception",ioe);
			}
		} 

	}

	/**
	 * Method name: getActivitiesForResource 
	 * 
	 * @param projectProfileResponse
	 * @param projectNumber
	 * @param taskTypes
	 * @param excludeWbsCodes 
	 * @param activityName 
	 * @param myActivityFlag 
	 * @throws ServerException, NetworkException, ClientException
	 * 
	 * @return projectProfileResponse	 
	 */
	
	public ProjectProfileResponse getActivitiesForResource(String cecId, String projectNumber, String taskTypes,String excludeWbsCodes,String activityName, String myActivityFlag)
			throws ServerException, NetworkException, ClientException {
		

		 

		ErrorDetail edetail = null;
		ProjectProfileResponse projectProfileResponse = objFactory.createProjectProfileResponse();
		ProjectSnapShot prjSnapShot = null;
		
		try {
			//get Primavera DB connection
			
			Connection conn = null;
			OracleCallableStatement callableStmt = null;
			try{
				conn = DAOCommonUtils.getP6DBConnection();
				if (conn != null){
					//call the DB procedure here
					callableStmt = (OracleCallableStatement) conn.prepareCall("{call ADMUSER.XXCAS_PRJ_TE_WS_PKG.get_proj_activity_dtls(?, ?, ? ,?, ?, ?, ?, ?) }");
					
					callableStmt.setString(1, cecId);
					callableStmt.setString(2, projectNumber);
					
//					if(taskTypes != null && !taskTypes.isEmpty()){
						callableStmt.setString(3, taskTypes);
//					}

//					if(activityName != null && !activityName.isEmpty()){
						callableStmt.setString(4, activityName);
//					}
					
//					if(excludeWbsCodes != null && !excludeWbsCodes.isEmpty()){
						callableStmt.setString(5, excludeWbsCodes);
//					}	
					
//					if(myActivityFlag != null && !myActivityFlag.isEmpty()){
						callableStmt.setString(6, myActivityFlag);
//					}
					/* Output parameters */
					callableStmt.registerOutParameter(7, OracleTypes.ARRAY,"XXCAS_O.XXCAS_PRJ_TE_ACT_TAB");
					callableStmt.registerOutParameter(8, OracleTypes.ARRAY,"XXCAS_O.XXCAS_PRJ_ERROR_DETAIL_TAB");
					
					callableStmt.execute();	
					
					//set response parameters
					
					ARRAY prjTeActTblType = (oracle.sql.ARRAY) callableStmt.getARRAY(7);
					ARRAY prjErrorDetailsTab = (oracle.sql.ARRAY)  callableStmt.getARRAY(8);
					
					//check if there is an error
					String errorStatus="";

					ErrorDetail errorDet = null;
					if (prjErrorDetailsTab != null && prjErrorDetailsTab.length() != 0) {

						errorDet=getErrorsList(prjErrorDetailsTab);
						errorStatus = errorDet.getReturnStatus();
						
						projectProfileResponse.getErrorDetail().add(errorDet);
					}
					
					if (prjTeActTblType!= null && DCPConstants.STATUS_SUCCESS.equalsIgnoreCase(errorStatus)){
						
						projectProfileResponse = getProjectProfileSnapShot(prjTeActTblType);
						projectProfileResponse.setServiceName(DCPConstants.ALL_ACT_ERROR_MSG_MAP_STRING);
						
					}else{
						prjSnapShot = new ProjectSnapShot();
						edetail = new ErrorDetail();
						edetail.setErrorCode(errorDet.getErrorCode());
						edetail.setErrorMessage(errorDet.getErrorMessage());
						edetail.setReturnStatus(errorDet.getReturnStatus());
						projectProfileResponse.getErrorDetail().add(edetail);
						projectProfileResponse.getProjectSnapShot().add(prjSnapShot);
					}
					
				}
			} catch (SQLException se) {
				se.printStackTrace();
				LOGGER.error("SQL Exception:",se);
				ErrorDetail error = new ErrorDetail();
				error.setErrorCode(DCPConstants.STORED_PROC_EXCEPTION_ERROR_CODE);
				error.setErrorMessage(se.getMessage());
				error.setReturnStatus(DCPConstants.STATUS_ERROR);
				projectProfileResponse = objFactory.createProjectProfileResponse();
				projectProfileResponse.getErrorDetail().add(error);
				return projectProfileResponse;
			} finally {
				if (conn != null) {
					try {
						conn.close();
						callableStmt.close();
						
					} catch (SQLException e) {
						LOGGER.error("SQL Exception: ",e);
					}
				}
			}
		} catch (Exception e) {
				LOGGER.error("Exception :" ,e);
				edetail = new ErrorDetail();
				edetail.setErrorMessage(e.getMessage());
				edetail.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
				edetail.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse.getErrorDetail().add(edetail);
				return projectProfileResponse;
		}
		
		return projectProfileResponse;
		
	}
	
	private ErrorDetail getErrorsList(ARRAY errorDetails) throws SQLException {
		String errorMessage = "";
		String errorCode="";
		String errorStatus="";

		Datum[] errorCodeValues = errorDetails.getOracleArray();
		if (errorCodeValues!=null){
			if(errorCodeValues[0]!=null){
				
				if((((STRUCT) errorCodeValues[0]).getAttributes()[0])!=null){
					errorStatus = (((String)((STRUCT) errorCodeValues[0]).getAttributes()[0]));
					LOGGER.debug("Value for errorStatus:"+ errorStatus);
				}else{
					errorStatus = null;
				}
				
				if((((STRUCT) errorCodeValues[0]).getAttributes()[1])!=null){
					errorMessage = (((String)((STRUCT) errorCodeValues[0]).getAttributes()[1].toString()));
					LOGGER.debug("Value for errorMessage:"+ errorMessage);
				}else{
					errorMessage = null;
				}

				if((((STRUCT) errorCodeValues[0]).getAttributes()[2])!=null){
					errorCode = ((((STRUCT) errorCodeValues[0]).getAttributes()[2]).toString());
					LOGGER.debug("Value for errorCode:"+ errorCode);
				}else{
					errorCode = null;
				}

			}else{
				errorMessage = "Error while fetching data from DB";
				errorStatus = DCPConstants.STATUS_ERROR;
			}
		}

		ErrorDetail errorDet = objFactory.createErrorDetail();
		errorDet.setErrorMessage(errorMessage);
		errorDet.setErrorCode(errorCode);
		errorDet.setReturnStatus(errorStatus);
		return errorDet;
		
	}
	
	private ProjectProfileResponse getProjectProfileSnapShot(
			ARRAY prjTeActTblType) {
		ProjectProfileResponse projectProfileResponse = new ProjectProfileResponse();
		ProjectSnapShot prjSnapShot = null;
		Format formatter = new SimpleDateFormat(
				"dd-MMM-yyyy");
		if (prjTeActTblType != null){
			try {
				Datum[] actDetailsArray = prjTeActTblType.getOracleArray();
				for (int i = 0; i < actDetailsArray.length; i++) {
					prjSnapShot = new ProjectSnapShot();
					
					if (actDetailsArray[i] != null){
						Object[] prjSnapShotObj = ((STRUCT) actDetailsArray[i]).getAttributes();
						
						if(prjSnapShotObj[0] !=  null){
							prjSnapShot.setActivityId((String) prjSnapShotObj[0].toString());
						}
						if(prjSnapShotObj[1] !=  null){
							prjSnapShot.setActivityName((String) prjSnapShotObj[1].toString());
						}
						if(prjSnapShotObj[2] !=  null){
							prjSnapShot.setActivityChargeable(((String) prjSnapShotObj[2].toString()));
						}
						if(prjSnapShotObj[3] !=  null){
							prjSnapShot.setActivityObjectId((String) prjSnapShotObj[3].toString());
						}
						if(prjSnapShotObj[4] !=  null){
							prjSnapShot.setEffortLogged((String) prjSnapShotObj[4].toString());
						}
						else {
							prjSnapShot.setEffortLogged("0");
						}
						if(prjSnapShotObj[5] !=  null){
							prjSnapShot.setLowestTaskNumber((String) prjSnapShotObj[5].toString());
						}
						if(prjSnapShotObj[6] !=  null){
							String starDate = formatter.format(prjSnapShotObj[6]);
							prjSnapShot.setStartDate(starDate);
						}
						if(prjSnapShotObj[7] !=  null){
							String endDate = formatter.format(prjSnapShotObj[7]);
							prjSnapShot.setEndDate(endDate);
						}
						if(prjSnapShotObj[8] !=  null){
							prjSnapShot.setEndCustomerId((String) prjSnapShotObj[8].toString());
						}
						if(prjSnapShotObj[9] !=  null){
							prjSnapShot.setEndCustomerName((String) prjSnapShotObj[9].toString());
						}
						if(prjSnapShotObj[10] !=  null){
							prjSnapShot.setPrimaryCustomerId((String) prjSnapShotObj[10].toString());
						}
						if(prjSnapShotObj[11] !=  null){
							prjSnapShot.setPrimaryCustomerName((String) prjSnapShotObj[11].toString());
						}
						if(prjSnapShotObj[12] !=  null){
							prjSnapShot.setTeAllowedFlag((String) prjSnapShotObj[12].toString());
						}
						//add to response object
						projectProfileResponse.getProjectSnapShot().add(prjSnapShot);
						
					}
					else {
						   prjSnapShot = new com.cisco.dcpte.model.ProjectSnapShot();
						   projectProfileResponse.getProjectSnapShot().add(prjSnapShot);
					}
				}
			} catch (SQLException e) {
				LOGGER.error("Error in getting data--",e);
			}
		}
		return projectProfileResponse;
	}
}
