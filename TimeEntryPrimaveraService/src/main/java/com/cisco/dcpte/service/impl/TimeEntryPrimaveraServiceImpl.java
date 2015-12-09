package com.cisco.dcpte.service.impl;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.cisco.dcpte.api.ActivitiesOfResource;
import com.cisco.dcpte.api.GetActivityDetailsForWbs;
import com.cisco.dcpte.api.GetActivityDetailsForWbsAPI;
import com.cisco.dcpte.api.GetExceptionTasks;
import com.cisco.dcpte.api.GetTopLevelWbs;
import com.cisco.dcpte.api.GetTopLevelWbsAPI;
import com.cisco.dcpte.api.UpdateResourceLevelActivity;
import com.cisco.dcpte.eman.EmanServiceDetails;
import com.cisco.dcpte.model.ErrorDetail;
import com.cisco.dcpte.model.ObjectFactory;
import com.cisco.dcpte.model.ProjectProfileResponse;
import com.cisco.dcpte.model.ProjectSnapShot;
import com.cisco.dcpte.service.ITimeEntryPrimaveraService;
import com.cisco.dcpte.util.CommonDCPUtils;
import com.cisco.dcpte.util.DCPConstants;
import com.cisco.framework.AbstractService;
import com.cisco.framework.ServiceConstants;
import com.cisco.framework.ServiceContext;
import com.cisco.framework.TransactionContext;
import com.cisco.framework.rest.core.BasicResponseHolder;
import com.cisco.framework.rest.core.HttpVerb;

/**
 * Class Name: DCPTimeEntryProfileImpl Extends: com.cisco.framework.AbstractService
 * Implements: IDCPTimeEntryProfileService
 * Aim: Implementing class for DCPTimeEntryProfile
 */

public class TimeEntryPrimaveraServiceImpl extends AbstractService implements ITimeEntryPrimaveraService {
	

		private static final Logger LOGGER = Logger.getLogger(TimeEntryPrimaveraServiceImpl.class);
		
		private GetActivityDetailsForWbsAPI getActivityDetailsForWbsAPI = new GetActivityDetailsForWbsAPI();
		
		private GetActivityDetailsForWbs getActivityDetailsForWbs = new GetActivityDetailsForWbs();
	
		private ObjectFactory objFactory = new ObjectFactory();
		
		private ProjectProfileResponse projectProfileResponse = objFactory.createProjectProfileResponse();
					
		private String contentType = "application/json";
		
		private String transId = "";
		
		private static String timeZone;
		static
	 {
			Properties prop = new Properties();
			InputStream in = null;

			try {
				String propFile = "config.properties";
				//Modified by gdeshmuk
				in = new TimeEntryPrimaveraServiceImpl().getClass()
						.getClassLoader().getResourceAsStream(propFile);
				prop.load(in);
				timeZone = prop.getProperty("timeZone");

				LOGGER.info("The TimeZone value getting from config.properties file is : "
								+ timeZone);
			} catch (Exception fnfe) {
				LOGGER.error("FileNotFoundException",fnfe);
			} finally{
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
		 * Method name: getHeaderDetails 
		 * Aim: This method reads the header details from the webservice input 
		 * and sets content type into response holder.
		 * 
		 * @param BasicResponseHolder
		 * 
		 */
		
		public void getHeaderDetails(BasicResponseHolder responseHolder){
			CommonDCPUtils commonUtils = new CommonDCPUtils(); 
			
			TransactionContext transcContext = ServiceContext.getTransactionContext();    
	        Map<String, String> httpHeaders = new HashMap<String, String>();
	        httpHeaders = (Map<String, String>)transcContext.getProperty(ServiceConstants.TRANSPORT_IN_HEADERS);     
	        LOGGER.debug("HttpHeader:"+httpHeaders);
	        
	        /* test for null values */
	        if(httpHeaders != null){
	        	contentType = commonUtils.getContentType(httpHeaders);
	        	transId = commonUtils.getTransactionId(httpHeaders);
	        }	       
	        LOGGER.debug("ContentType:"+contentType);
	        responseHolder.setContentType(contentType);	
		}
		
		/**
		 * Method name: getActivitiesOfWBS 
		 * Aim: This method is called to get Activities of WBS
		 * validates the input and passes the parameters to another Class.
		 * 
		 * @param httpVerb
		 * @param projectProfileResponse
		 * @return BasicResponseHolder
		 */
		
	public BasicResponseHolder getActivitiesOfWBSAPI(HttpVerb verb,
				ProjectProfileResponse projectProfileResponseinput) {

		/* Initialize variables */
		BasicResponseHolder responseHolder = new BasicResponseHolder();
			
		ErrorDetail errorDet = new ErrorDetail();
		String errorMessage = "";

		List<ProjectSnapShot> lstProjectSnapShots =null;
		String cecId = null;
		String wbsId = null;
		String inclusionTaskType = null;
		String excludeWbsCodes = null;
		
		LOGGER.debug("In getActivitiesOfWBSAPI method");
		
		getHeaderDetails(responseHolder);
		
		if (projectProfileResponseinput != null){
			try{
				/* Initialize variables */
				/* get POST data */			
				lstProjectSnapShots = projectProfileResponseinput.getProjectSnapShot();
				for (ProjectSnapShot projectSnapShot : lstProjectSnapShots){
					if (projectSnapShot != null){  
						
						if(projectSnapShot.getCecId() != null && !projectSnapShot.getCecId().isEmpty()){
						     cecId = projectSnapShot.getCecId();	
						 }
						if(projectSnapShot.getWbsId() != null  && !projectSnapShot.getWbsId().isEmpty()){
							wbsId = projectSnapShot.getWbsId();
						}
						if(projectSnapShot.getExclusionWbsCode() != null && !projectSnapShot.getExclusionWbsCode().isEmpty()){
							excludeWbsCodes = projectSnapShot.getExclusionWbsCode();
						}
						if(projectSnapShot.getInclusionTaskType() != null){
							inclusionTaskType = projectSnapShot.getInclusionTaskType();
						}
					}	
				}
				if (wbsId != null && !wbsId.isEmpty() && cecId !=null && !cecId.isEmpty()){
								
						LOGGER.debug("Input cecId:"+cecId);
						LOGGER.debug("Input wbsId:"+wbsId);
						projectProfileResponse = getActivityDetailsForWbsAPI.getActivityDetails(cecId, wbsId, inclusionTaskType,excludeWbsCodes);				
				}else{
						throw new Exception("All mandatory Input fields are Required");
				}
			}catch(Exception e){
				
					errorMessage = e.getMessage();
					LOGGER.error("errorMessage :",e);
					errorDet.setErrorMessage(errorMessage);
					errorDet.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
					errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
					projectProfileResponse.getErrorDetail().add(errorDet);
					responseHolder.setData(projectProfileResponse);
					return responseHolder;
				}
			}else{
				errorDet.setErrorCode(DCPConstants.NO_INPUT_ERROR_CODE);
				errorDet.setErrorMessage(DCPConstants.ERROR461);
				errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse = objFactory.createProjectProfileResponse();
				projectProfileResponse.getErrorDetail().add(errorDet);
				responseHolder.setData(projectProfileResponse);
				return responseHolder;	
			}	
			responseHolder.setData(projectProfileResponse);
			LOGGER.debug("Completed");
			return responseHolder;
		}
	
	public BasicResponseHolder getActivitiesOfWBS(HttpVerb verb,
			ProjectProfileResponse projectProfileResponseinput) {

	/* Initialize variables */
	BasicResponseHolder responseHolder = new BasicResponseHolder();
		
	ErrorDetail errorDet = new ErrorDetail();
	String errorMessage = "";

	List<ProjectSnapShot> lstProjectSnapShots =null;
	String cecId = null;
	String wbsId = null;
	String inclusionTaskType = null;
	String excludeWbsCodes = null;
	
	LOGGER.debug("In getActivitiesOfWBS method");
	
	getHeaderDetails(responseHolder);
	
	if (projectProfileResponseinput != null){
		try{
			/* Initialize variables */
			/* get POST data */			
			lstProjectSnapShots = projectProfileResponseinput.getProjectSnapShot();
			for (ProjectSnapShot projectSnapShot : lstProjectSnapShots){
				if (projectSnapShot != null){  
					
					if(projectSnapShot.getCecId() != null && !projectSnapShot.getCecId().isEmpty()){
					     cecId = projectSnapShot.getCecId();	
					 }
					if(projectSnapShot.getWbsId() != null  && !projectSnapShot.getWbsId().isEmpty()){
						wbsId = projectSnapShot.getWbsId();
					}
					if(projectSnapShot.getExclusionWbsCode() != null && !projectSnapShot.getExclusionWbsCode().isEmpty()){
						excludeWbsCodes = projectSnapShot.getExclusionWbsCode();
					}
					if(projectSnapShot.getInclusionTaskType() != null && !projectSnapShot.getInclusionTaskType().isEmpty()){
						inclusionTaskType = projectSnapShot.getInclusionTaskType();
					}
				}	
			}
			if (wbsId != null && !wbsId.isEmpty() && cecId !=null && !cecId.isEmpty()){
							
					LOGGER.debug("Input cecId:"+cecId);
					LOGGER.debug("Input wbsId:"+wbsId);
					projectProfileResponse = getActivityDetailsForWbs.getActivityDetails(cecId, wbsId, inclusionTaskType,excludeWbsCodes);				
			}else{
					throw new Exception("All mandatory Input fields are Required");
			}
		}catch(Exception e){
			
				errorMessage = e.getMessage();
				LOGGER.error("errorMessage :",e);
				errorDet.setErrorMessage(errorMessage);
				errorDet.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
				errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse.getErrorDetail().add(errorDet);
				responseHolder.setData(projectProfileResponse);
				return responseHolder;
			}
		}else{
			errorDet.setErrorCode(DCPConstants.NO_INPUT_ERROR_CODE);
			errorDet.setErrorMessage(DCPConstants.ERROR461);
			errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
			projectProfileResponse = objFactory.createProjectProfileResponse();
			projectProfileResponse.getErrorDetail().add(errorDet);
			responseHolder.setData(projectProfileResponse);
			return responseHolder;	
		}	
		responseHolder.setData(projectProfileResponse);
		LOGGER.debug("Completed");
		return responseHolder;
	}
	
		/**
		 * Method name: getExceptionTasksList 
		 * Aim: This method is called to get List of Exception tasks
		 * validates the input and passes the parameters to another Class.
		 * 
		 * @param httpVerb
		 * @param projectProfileResponse
		 * @return BasicResponseHolder
		 */
	
		public BasicResponseHolder getExceptionTasksList (HttpVerb verb, ProjectProfileResponse projectProfileResponseinput) {
			
			ErrorDetail errorDet = new ErrorDetail();
			String errorMessage = "";
			BasicResponseHolder responseHolder = new BasicResponseHolder();
			String projectNumber = null;
			String cecId = null;
			String taskTypes = null;
			getHeaderDetails(responseHolder);			
			LOGGER.debug(":"+transId+":Start of getExceptionTasksList method");
		
			List<ProjectSnapShot> lstProjectSnapShots;
			lstProjectSnapShots = projectProfileResponseinput.getProjectSnapShot();
			for (ProjectSnapShot projectSnapShot : lstProjectSnapShots){
			if (projectSnapShot != null) {
				try {
					
					if (projectSnapShot.getProjectNumber() != null && projectSnapShot.getProjectNumber().length() > 0) {
						projectNumber =projectSnapShot.getProjectNumber() ;
					}
					if (projectSnapShot.getCecId() != null && projectSnapShot.getCecId().length() > 0) {
						cecId =projectSnapShot.getCecId() ;
					}
					if (projectSnapShot.getInclusionTaskType()!= null && projectSnapShot.getInclusionTaskType().length() > 0) {
						taskTypes =projectSnapShot.getInclusionTaskType() ;
					}
					
					if( projectNumber == null || projectNumber.isEmpty() || cecId == null || cecId.isEmpty() || taskTypes == null || taskTypes.isEmpty()){
						throw new Exception("All mandatory fields are required ");
					
					}else{
						projectProfileResponse = GetExceptionTasks.getExceptionTasks(projectNumber,cecId,taskTypes,transId);
						if(projectProfileResponse != null){
							responseHolder.setData(projectProfileResponse);
						}else{
							throw new Exception("Response is Empty");
						}
					}
				}catch (Exception e) {
					LOGGER.error(":"+transId+":Exception ",e);
					projectProfileResponse.getErrorDetail().clear();
					errorMessage = e.getMessage();
					errorDet.setErrorMessage(errorMessage);
					errorDet.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
					errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
					projectProfileResponse.getErrorDetail().add(errorDet);
					responseHolder.setData(projectProfileResponse);
					return responseHolder;
				}

			}
		}
		LOGGER.debug(":"+transId+":End of getExceptionTasksList method");
		return responseHolder;
			
	}
		
	/**
	 * Method name: updateTimeCardDetails 
	 * Aim: This method is called to Update Time card details
	 * This Method passes the parameters to another Class.
	 * 
	 * @param httpVerb
	 * @param projectProfileResponse
	 * @return BasicResponseHolder
	 */
		
	public BasicResponseHolder updateTimeCardDetails(HttpVerb verb,
				ProjectProfileResponse projectProfileResponseinput) {
		
		BasicResponseHolder responseHolder = new BasicResponseHolder();
		getHeaderDetails(responseHolder);
		responseHolder = UpdateResourceLevelActivity.updateTimeCardDetails(projectProfileResponseinput);
			
		LOGGER.info("updated Successfully");
		return responseHolder;
	}
	
	/**
	 * Method name: getTopLevelWBS 
	 * Aim: This method is called to get top Level WBS and filter them according to WBS Codes
	 * validates the input and passes the parameters to another Class.
	 * 
	 * @param httpVerb
	 * @param projectProfileResponse
	 * @return BasicResponseHolder
	 */
	
public BasicResponseHolder getTopLevelWBSAPI(HttpVerb verb,
			ProjectProfileResponse projectProfileResponseinput) {

	/* Initialize variables */
	BasicResponseHolder responseHolder = new BasicResponseHolder();
		
	ErrorDetail errorDet = new ErrorDetail();
	String errorMessage = "";

	List<ProjectSnapShot> lstProjectSnapShots =null;
	
	String projectNumber = null;
	String exclusionWbsCode = null;
	String wbsName = null;
	
	LOGGER.debug("In getTopLevelWBS method");
	
	getHeaderDetails(responseHolder);
	
	if (projectProfileResponseinput != null){
		try{
			/* Initialize variables */
			/* get POST data */			
			lstProjectSnapShots = projectProfileResponseinput.getProjectSnapShot();
			for (ProjectSnapShot projectSnapShot : lstProjectSnapShots){
				
				if (projectSnapShot != null){
						
					if (projectSnapShot.getProjectNumber() != null && projectSnapShot.getProjectNumber().length() > 0) {
						projectNumber =projectSnapShot.getProjectNumber() ;
					}
					if(projectSnapShot.getExclusionWbsCode() != null && projectSnapShot.getExclusionWbsCode().length() > 0 ){
						exclusionWbsCode = projectSnapShot.getExclusionWbsCode();
					}
					if(projectSnapShot.getWbsName() != null && projectSnapShot.getWbsName().length() > 0 ){
						wbsName = projectSnapShot.getWbsName();
					}
				}	
			}
			if (projectNumber != null && !projectNumber.isEmpty()){
							
					LOGGER.debug("Input wbsId:"+projectNumber);
					LOGGER.debug("wbs Code:"+exclusionWbsCode);
					projectProfileResponse = GetTopLevelWbsAPI.getTopLevelWbs(projectNumber, exclusionWbsCode,wbsName);				
			}else{
					errorDet.setErrorCode(DCPConstants.NO_INPUT_ERROR_CODE);
					errorDet.setErrorMessage(DCPConstants.ERROR491);
					errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
					projectProfileResponse = objFactory.createProjectProfileResponse();
					projectProfileResponse.getErrorDetail().add(errorDet);
					responseHolder.setData(projectProfileResponse);
					return responseHolder;	
			}
		}catch(Exception e){
				errorMessage = e.getMessage();
				LOGGER.error("errorMessage :",e);
				errorDet.setErrorMessage(errorMessage);
				errorDet.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
				errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse.getErrorDetail().add(errorDet);
				responseHolder.setData(projectProfileResponse);
				return responseHolder;
			}
		}else{
			errorDet.setErrorCode(DCPConstants.NO_INPUT_ERROR_CODE);
			errorDet.setErrorMessage(DCPConstants.ERROR461);
			errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
			projectProfileResponse = objFactory.createProjectProfileResponse();
			projectProfileResponse.getErrorDetail().add(errorDet);
			responseHolder.setData(projectProfileResponse);
			return responseHolder;	
		}	
		responseHolder.setData(projectProfileResponse);
		LOGGER.debug("Completed");
		return responseHolder;
	}

	/**
	 * Method name: getTopLevelWBS 
	 * Aim: This method is called to get top Level WBS and filter them according to WBS Codes
	 * validates the input and passes the parameters to another Class.
	 * 
	 * @param httpVerb
	 * @param projectProfileResponse
	 * @return BasicResponseHolder
	 */

	public BasicResponseHolder getTopLevelWBS(HttpVerb verb,
			ProjectProfileResponse projectProfileResponseinput) {
	
	/* Initialize variables */
	BasicResponseHolder responseHolder = new BasicResponseHolder();
		
	ErrorDetail errorDet = new ErrorDetail();
	String errorMessage = "";
	
	List<ProjectSnapShot> lstProjectSnapShots =null;
	
	String projectNumber = null;
	String exclusionWbsCode = null;
	String wbsName = null;
	
	LOGGER.debug("In getTopLevelWBS method");
	
	getHeaderDetails(responseHolder);
	
	if (projectProfileResponseinput != null){
		try{
			/* Initialize variables */
			/* get POST data */			
			lstProjectSnapShots = projectProfileResponseinput.getProjectSnapShot();
			for (ProjectSnapShot projectSnapShot : lstProjectSnapShots){
				
				if (projectSnapShot != null){
						
					if (projectSnapShot.getProjectNumber() != null && projectSnapShot.getProjectNumber().length() > 0) {
						projectNumber =projectSnapShot.getProjectNumber() ;
					}
					if(projectSnapShot.getExclusionWbsCode() != null && projectSnapShot.getExclusionWbsCode().length() > 0 ){
						exclusionWbsCode = projectSnapShot.getExclusionWbsCode();
					}
					if(projectSnapShot.getWbsName() != null && projectSnapShot.getWbsName().length() > 0 ){
						wbsName = projectSnapShot.getWbsName();
					}
				}	
			}
			if (projectNumber != null && !projectNumber.isEmpty()){
							
					LOGGER.debug("Input wbsId:"+projectNumber);
					LOGGER.debug("wbs Code:"+exclusionWbsCode);
					projectProfileResponse = GetTopLevelWbs.getTopLevelWbs(projectNumber, exclusionWbsCode,wbsName);				
			}else{
					errorDet.setErrorCode(DCPConstants.NO_INPUT_ERROR_CODE);
					errorDet.setErrorMessage(DCPConstants.ERROR491);
					errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
					projectProfileResponse = objFactory.createProjectProfileResponse();
					projectProfileResponse.getErrorDetail().add(errorDet);
					responseHolder.setData(projectProfileResponse);
					return responseHolder;	
			}
		}catch(Exception e){
				errorMessage = e.getMessage();
				LOGGER.error("errorMessage :",e);
				errorDet.setErrorMessage(errorMessage);
				errorDet.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
				errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse.getErrorDetail().add(errorDet);
				responseHolder.setData(projectProfileResponse);
				return responseHolder;
			}
		}else{
			errorDet.setErrorCode(DCPConstants.NO_INPUT_ERROR_CODE);
			errorDet.setErrorMessage(DCPConstants.ERROR461);
			errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
			projectProfileResponse = objFactory.createProjectProfileResponse();
			projectProfileResponse.getErrorDetail().add(errorDet);
			responseHolder.setData(projectProfileResponse);
			return responseHolder;	
		}	
		responseHolder.setData(projectProfileResponse);
		LOGGER.debug("Completed");
		return responseHolder;
	}
	/**
	 * Method name: getActivities 
	 * Aim: This method is called to get the activities based on cecID.
	 * validates the input and passes the parameters to another Class.
	 * 
	 * @param httpVerb
	 * @param projectProfileResponse
	 * @return BasicResponseHolder
	 */
	
	public BasicResponseHolder getActivities(HttpVerb verb,
			ProjectProfileResponse projectProfileResponseinput) {
		
		long startTime = System.currentTimeMillis();

		/* Initialize variables */
		BasicResponseHolder responseHolder = new BasicResponseHolder();
			
		ErrorDetail errorDet = new ErrorDetail();
		String errorMessage = "";

		List<ProjectSnapShot> lstProjectSnapShots =null;
		String cecId = null;
		String projectId = null;
		String taskType = null;
		String excludeWbsCodes = null;
		String activityName = null;
		String myActivityFlag = null;
		
		LOGGER.debug("In getActivities method");
		
		getHeaderDetails(responseHolder);
		
		if (projectProfileResponseinput != null){
			try{
				/* Initialize variables */
				/* get POST data */			
				lstProjectSnapShots = projectProfileResponseinput.getProjectSnapShot();
				for (ProjectSnapShot projectSnapShot : lstProjectSnapShots){
					if (projectSnapShot != null){
						if(projectSnapShot.getCecId() != null){
							cecId = projectSnapShot.getCecId();
						}
						if( projectSnapShot.getProjectNumber() != null &&  projectSnapShot.getProjectNumber().length() >0){
							projectId = projectSnapShot.getProjectNumber();
						}
						
						if(projectSnapShot.getExclusionWbsCode() != null && projectSnapShot.getExclusionWbsCode().length()>0){
							excludeWbsCodes = projectSnapShot.getExclusionWbsCode();
						}
						
						if(projectSnapShot.getInclusionTaskType() != null && !projectSnapShot.getInclusionTaskType().isEmpty()){
							taskType = projectSnapShot.getInclusionTaskType();
						}	
						if(projectSnapShot.getActivityName() != null && !projectSnapShot.getActivityName().isEmpty()){
							activityName = projectSnapShot.getActivityName();
						}
						
						if(projectSnapShot.getMyActivtyFlag() != null && !projectSnapShot.getMyActivtyFlag().isEmpty()){
							myActivityFlag = projectSnapShot.getMyActivtyFlag();
						}
						
												
						LOGGER.debug("Input cecId:"+cecId);
						LOGGER.debug("Input projectId:"+projectId);
						ActivitiesOfResource actOfRsc = new ActivitiesOfResource();
						
						if( projectId == null || projectId.isEmpty() || cecId == null || cecId.isEmpty()){
							throw new Exception("All mandatory fields are required ");
						}else{
							projectProfileResponse = actOfRsc.getActivitiesForResource(cecId, projectId, taskType, excludeWbsCodes,activityName,myActivityFlag);
						}
				}/*else{
						errorDet.setErrorCode(DCPConstants.NO_INPUT_ERROR_CODE);
						errorDet.setErrorMessage(DCPConstants.ERROR461);
						errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
						projectProfileResponse = objFactory.createProjectProfileResponse();
						projectProfileResponse.getErrorDetail().add(errorDet);
						responseHolder.setData(projectProfileResponse);
						return responseHolder;	
				}*/
				}
			}catch(Exception e){
					errorMessage = e.getMessage();
					LOGGER.error("errorMessage :",e);
					errorDet.setErrorMessage(errorMessage);
					errorDet.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
					errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
					projectProfileResponse.getErrorDetail().add(errorDet);
					responseHolder.setData(projectProfileResponse);
					return responseHolder;
				}
			}else{
				errorDet.setErrorCode(DCPConstants.NO_INPUT_ERROR_CODE);
				errorDet.setErrorMessage(DCPConstants.ERROR461);
				errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse = objFactory.createProjectProfileResponse();
				projectProfileResponse.getErrorDetail().add(errorDet);
				responseHolder.setData(projectProfileResponse);
				return responseHolder;	
			}	
			responseHolder.setData(projectProfileResponse);
			long endTime = System.currentTimeMillis();
			LOGGER.debug("Completed time="+(endTime-startTime));
			return responseHolder;
		
	}
	
	
	
	/**
	 * Method name: getEmanStatus 
	 * Aim: This method is called to get the eman status
	 * This Method passes the parameters to another Class.
	 * 
	 * @param httpVerb
	 * @param projectProfileResponse
	 * @return BasicResponseHolder
	 */
	
	public BasicResponseHolder getEmanStatus(){

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		getHeaderDetails(responseHolder);
		ProjectProfileResponse projectProfileResponse = EmanServiceDetails.getEmanDetails();
		if(projectProfileResponse != null){
		  responseHolder.setData(projectProfileResponse) ;
		}
		return responseHolder;
		}
}