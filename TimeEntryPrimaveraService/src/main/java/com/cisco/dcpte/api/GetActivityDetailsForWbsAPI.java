package com.cisco.dcpte.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.cisco.dcpte.dao.PrimaveraConnectionManager;
import com.cisco.dcpte.model.ErrorDetail;
import com.cisco.dcpte.model.ObjectFactory;
import com.cisco.dcpte.model.ProjectProfileResponse;
import com.cisco.dcpte.model.ProjectSnapShot;
import com.cisco.dcpte.p6.util.PrimaveraUtil;
import com.cisco.dcpte.util.CommonDCPUtils;
import com.cisco.dcpte.util.DCPConstants;
import com.cisco.dcpte.util.P6Constants;
import com.primavera.ServerException;
import com.primavera.integration.client.ClientException;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.BOIterator;
import com.primavera.integration.client.bo.BusinessObjectException;
import com.primavera.integration.client.bo.enm.UDFIndicator;
import com.primavera.integration.client.bo.object.Activity;
import com.primavera.integration.client.bo.object.ActivityCodeAssignment;
import com.primavera.integration.client.bo.object.Project;
import com.primavera.integration.client.bo.object.UDFValue;
import com.primavera.integration.client.bo.object.WBS;
import com.primavera.integration.network.NetworkException;

/**
 * @author egandi
 * Class Name: GetActivityDetailsForWbs 
 * Aim: Implementing logic for getActivityDetailsForWbs webservice
 */
public class GetActivityDetailsForWbsAPI {

	private static final Logger LOGGER = Logger
			.getLogger(GetActivityDetailsForWbsAPI.class);

	public static int wbsLevelCount = 0;
	private ObjectFactory objFactory = new ObjectFactory();
	
	private static List<WBS> singletonWbs = null;
	
	private static String timeZone;
	static
 {
		Properties prop = new Properties();
		InputStream in = null;

		try {
			String propFile = "config.properties";
			in = new GetActivityDetailsForWbsAPI().getClass()
					.getClassLoader().getResourceAsStream(propFile);
			prop.load(in);
			timeZone = prop.getProperty("timeZone");

			LOGGER.info("The TimeZone value getting from config.properties file is : "
							+ timeZone);
		} catch (Exception fnfe) {
			LOGGER.error("Exception",fnfe);
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
	 * Method name: getActivityDetails 
	 * 
	 * @param projectProfileResponse2
	 * @param wsbId
	 * @param taskTypes
	 * @param loadedActivity 
	 * @throws ServerException, NetworkException, ClientException
	 * 
	 * @return projectProfileResponse	 
	 */
	
	public ProjectProfileResponse getActivityDetails(String cecId, String wsbId, String taskTypes,String excludeWbsCodes)
			throws ServerException, NetworkException, ClientException {

		ErrorDetail edetail = null;
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
	
		 EnterpriseLoadManager elm = null;
		 Session session = null;
		ProjectProfileResponse projectProfileResponse = objFactory.createProjectProfileResponse();

		try {
			// Using PrimaveraConnectionManager class to get the database
			// instance
			PrimaveraConnectionManager.getInstance();
			session = PrimaveraConnectionManager.getSession();
			elm = session.getEnterpriseLoadManager();

			String whereWbsId = "ObjectId='" + wsbId + "'";
			LOGGER.info("whereWbsId :"+whereWbsId);
			
			BOIterator<WBS> wbslist = null;
			
			WBS wbs = null;
			wbslist = elm.loadWBS(new String[] { "ObjectId", "Name","Code" },whereWbsId, null);
			if (wbslist.hasNext()) {
				wbs = wbslist.next();
				
				projectProfileResponse = getActivitiesForWbs(wbs,projectProfileResponse,cecId,taskTypes,excludeWbsCodes);
				return projectProfileResponse;
			}else {
				edetail = new ErrorDetail();
				edetail.setErrorCode(DCPConstants.ACTIVTIES_EXIST_FOR_WBS_ERROR_CODE);
				edetail.setErrorMessage(P6Constants.WBS_NOT_FOUND);
				edetail.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse.getErrorDetail().add(edetail);
				return projectProfileResponse;
			}
			
		} catch (Exception e) {
				LOGGER.error("Exception :" , e);
				edetail = new ErrorDetail();
				edetail.setErrorMessage(e.getMessage());
				edetail.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
				edetail.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse.getErrorDetail().add(edetail);
				return projectProfileResponse;
		}
		
	}

	/**
	 * Method name: getActivitiesForWbs 
	 * 
	 * @param projectProfileResponse
	 * @param wbs
	 * @param taskTypes
	 * @param cecId 
	 * @param excludeWbsCodes
	 * @throws ServerException, NetworkException, ClientException
	 * Aim : It will provide list of Chargeable Activities for given WBS.
	 * 
	 * @return projectProfileResponse	 
	 */
	private ProjectProfileResponse getActivitiesForWbs(WBS wbs,ProjectProfileResponse projectProfileResponse, String cecId, String taskTypes,String excludeWbsCodes) throws BusinessObjectException, ServerException, NetworkException {
		projectProfileResponse.getProjectSnapShot().clear();
		
		ErrorDetail edetail = null;
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
	
		Activity loadedActivity = null;
 	    BOIterator<Activity> activities = null;
		
		// Load All activities of the WBS

		   activities = wbs.loadAllActivities(new String[] { "WBSCode","ObjectId","Id", "Name", "Status", "ActualStartDate","ActualFinishDate", "RemainingEarlyStartDate", "RemainingEarlyFinishDate","ProjectId" }, null, null);

			while (activities.hasNext()) {
				loadedActivity = activities.next();
				
				String chargeableFlag = getChargeableFlagForActivity(loadedActivity);
				LOGGER.info("chargeableFlag :"+chargeableFlag);
			
				if(P6Constants.DCPRM_CHARGEABLE_USER.equalsIgnoreCase(cecId)){
					LOGGER.debug("DCPRM logic Called");
					projectProfileResponse = getActivityList(projectProfileResponse,cecId,taskTypes,loadedActivity,excludeWbsCodes);
				
				}else if(P6Constants.CHARGEABLE_FLAG_YES.equalsIgnoreCase(chargeableFlag)){
					
					projectProfileResponse = getActivityList(projectProfileResponse,cecId,taskTypes,loadedActivity,excludeWbsCodes);
				
				}
				
			}
			if(projectProfileResponse.getProjectSnapShot().size()==0 && edetail == null){
				edetail = new ErrorDetail();
				edetail.setErrorCode(DCPConstants.NOACTIVTIES_EXIST_FOR_WBS_ERROR_CODE);
				edetail.setErrorMessage(DCPConstants.NOACTIVTIES_EXIST_FOR_WBS_ERROR_MSG);
				edetail.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse.getErrorDetail().add(edetail);
				return projectProfileResponse;
			}
			return projectProfileResponse;
		
	}
	
	/**
	 * Method name: getActivityList 
	 * 
	 * @param projectProfileResponse
	 * @param cecId
	 * @param taskTypes
	 * @param loadedActivity 
	 * @param excludeWbsCodes
	 * @throws ServerException, NetworkException, BusinessObjectException
	 * Aim : It will provide list of list of activities.
	 * 
	 * @return projectProfileResponse	 
	 */
	private ProjectProfileResponse getActivityList(ProjectProfileResponse projectProfileResponse,
			String cecId, String taskTypes,Activity loadedActivity,String excludeWbsCodes) throws BusinessObjectException, ServerException, NetworkException {
		
		ProjectSnapShot projectSnapShot = new ProjectSnapShot();
		String lowestTaskNumber ="";
		String existingTaskType =getTaskTypeOfActivity(loadedActivity);
		//get lowest task
		Activity lowestTask = getLowestTaskOfWbs(loadedActivity);
		if(lowestTask != null){
		    lowestTaskNumber = lowestTask.getId();
		}
		List<String> taskTypesList = null;
		List<String> excludeWbsCodesList = null;
		String actWbsCode = loadedActivity.getWBSCode();
		if (excludeWbsCodes != null && !excludeWbsCodes.isEmpty()){
			excludeWbsCodesList = Arrays.asList(excludeWbsCodes.split(","));
	
			if(actWbsCode != null && excludeWbsCodesList != null && !excludeWbsCodesList.contains(actWbsCode)){
				
				
				if (taskTypes != null && !taskTypes.isEmpty()) {
					taskTypesList = Arrays.asList(taskTypes.split(","));
					
					if(taskTypesList.contains(existingTaskType)){
					projectSnapShot = setActivityDetails(loadedActivity,cecId,lowestTaskNumber,existingTaskType);
					if (projectSnapShot != null) {
						projectProfileResponse.getProjectSnapShot().add(projectSnapShot);
						}
					}
					
					String existingLowestTaskType =null;
					if(lowestTask != null){
						existingLowestTaskType=getTaskTypeOfActivity(lowestTask);
					}
					if(existingTaskType.isEmpty() && taskTypesList.contains(existingLowestTaskType)){
						projectSnapShot = setActivityDetails(loadedActivity,cecId,lowestTaskNumber,existingLowestTaskType);
						if (projectSnapShot != null) {
							projectProfileResponse.getProjectSnapShot().add(projectSnapShot);
						}
					}
					
				}else {
					projectSnapShot=setActivityDetails(loadedActivity,cecId, lowestTaskNumber,existingTaskType);
					if(projectSnapShot != null){
						projectProfileResponse.getProjectSnapShot().add(projectSnapShot);
					}
				}
				
			}
		}else{
					if (taskTypes != null && !taskTypes.isEmpty()){
							taskTypesList = Arrays.asList(taskTypes.split(","));
							
							if(taskTypesList.contains(existingTaskType)){
							projectSnapShot = setActivityDetails(loadedActivity,cecId,lowestTaskNumber,existingTaskType);
							if (projectSnapShot != null) {
							projectProfileResponse.getProjectSnapShot().add(projectSnapShot);
							}
							}
							String existingLowestTaskType =null;
							if(lowestTask != null){
								existingLowestTaskType=getTaskTypeOfActivity(lowestTask);
							}
							
							if(existingTaskType.isEmpty() && taskTypesList.contains(existingLowestTaskType)){
								projectSnapShot = setActivityDetails(loadedActivity,cecId,lowestTaskNumber,existingLowestTaskType);
								if (projectSnapShot != null) {
								projectProfileResponse.getProjectSnapShot().add(projectSnapShot);
								}
							}
						
					  }else{
							projectSnapShot=setActivityDetails(loadedActivity,cecId, lowestTaskNumber,existingTaskType);
							if(projectSnapShot != null){
							projectProfileResponse.getProjectSnapShot().add(projectSnapShot);
							}
					     }
	           }
	
		return projectProfileResponse;
		
	}

	/**
	 * Method name: getChargeableFlagForActivity 
	 * 
	 * @param activity	
	 * @throws ServerException, NetworkException, BusinessObjectException
	 * Aim : It will provide Chargeable Flag of given Activity.
	 * 
	 * @return String	 
	 */

	public String getChargeableFlagForActivity(Activity activity) throws BusinessObjectException, ServerException, NetworkException {

		 LOGGER.info("in getChargeableFlagForActivity");
		 String chargeble ="N";
	
		 String[] fields = { "ActivityCodeDescription",
		 "ActivityCodeObjectId", "ActivityCodeTypeName",
		 "ActivityCodeValue", "ActivityId", "ActivityName",
		 "ProjectId", "ProjectObjectId","ActivityCodeTypeScope" };
		 
		 
		 String sWhereClause = "ActivityCodeTypeName = '"+ P6Constants.CHARGEABLE + "' And ActivityCodeTypeScope = '"+P6Constants.ACTIVITY_CODE_SCOPE_GLOBAL+"'";
		 LOGGER.info("sWhereClause : " + sWhereClause);
		 BOIterator<ActivityCodeAssignment> activityCodeAssignmentBO = activity.loadActivityCodeAssignments(fields,sWhereClause, null);
		
		 if(activityCodeAssignmentBO.hasNext()) {
			 ActivityCodeAssignment activityCodeAssignment = activityCodeAssignmentBO.next();
			 
			 if(activityCodeAssignment.getActivityCodeValue() != null){
				 chargeble =activityCodeAssignment.getActivityCodeValue();
			 }
		 }
	 return chargeble;
	}

	/**
	 * Method name: getLowestTaskOfWbs 
	 * 
	 * @param activity	
	 * @throws ServerException, NetworkException, BusinessObjectException
	 * Aim : It will provide lowestTask of given Activity.
	 * 
	 * @return Activity	 
	 */

	private Activity getLowestTaskOfWbs(Activity activity) throws BusinessObjectException, ServerException, NetworkException {
		LOGGER.info("getLowestTaskOfWbs--Entry");
		List<WBS> wbsHeirarchyList = new ArrayList<WBS>();
		Activity lowestTask = null;
		String lowestTaskNumber ="";
		BOIterator<UDFValue> bOIterator = null;
		
		String whereClause = "UDFTypeTitle='"	+ P6Constants.LINKED + "' And Indicator='"+P6Constants.LINKED_FLAG+"'";
		bOIterator = activity.loadUDFValues(new String[] { "UDFTypeObjectId", "Indicator" }, whereClause, null);
		if (bOIterator.hasNext()) {
			lowestTask = activity;
		} else {
		
			WBS wbs = activity.loadWBS(new String[]{"ObjectId","Name"});
			LOGGER.info("wbs Name :"+wbs.getName());
			lowestTask= checkForLinkActivityId(wbs);
			if(lowestTask != null){
				return lowestTask; 
			} else{
			
				wbsHeirarchyList = getParentWbs(wbs);
		
				// Set the lowest OP task number
				singletonWbs = null;
				if (!wbsHeirarchyList.isEmpty()) {
					for (WBS parentWbs : wbsHeirarchyList){
						
						lowestTask= checkForLinkActivityId(parentWbs);
						if (lowestTask !=null){
							lowestTaskNumber =lowestTask.getId();
							LOGGER.info("lowestTaskNumber :"+lowestTaskNumber);
							break;
						}
					}
				}
			}
		
		}
		
		return lowestTask; 
	
	}


	/**
	 * Method name: getTaskTypeOfActivity 
	 * 
	 * @param activity	
	 * @throws ServerException, NetworkException, BusinessObjectException
	 * Aim : It will provide TaskType of given Activity.
	 * 
	 * @return String	 
	 */
	private String getTaskTypeOfActivity(Activity activity) throws BusinessObjectException, ServerException, NetworkException {
		String lowestTaskType ="";
		String udfWhereClause = "UDFTypeTitle ='" + P6Constants.TASK_TYPE+ "'";
		BOIterator<UDFValue> boiUDFvalues = activity.loadUDFValues(	new String[] { "Double", "UDFTypeTitle", "Text" },udfWhereClause, null);
		
		if (boiUDFvalues.hasNext()) {
			UDFValue udfValues = boiUDFvalues.next();
			
			if(udfValues.getText() != null){
				lowestTaskType = udfValues.getText();
			}

		}
		return lowestTaskType;
	}

	/**
	 * Method name: getParentWbs 
	 * 
	 * @param wbs	
	 * Aim : It will provide parent Wbs for a given wbs.
	 * 
	 * @return list	 
	 */
	public static List<WBS> getParentWbs(WBS wbs) {
		List<WBS> wbsList = getWbsList();
		wbsList.add(wbs);
		WBS parentWbs = null;
		try {
			// get associated parent wbs for given elementId
			parentWbs = wbs.loadParentWBS(new String[] { "Name", "ObjectId" });
			if (parentWbs == null) {
				wbs.setParentObjectId(null);
				return wbsList;
			} else {
				wbsList.add(parentWbs);
				getParentWbs(parentWbs);
			}
		}catch (Exception exception) {
			LOGGER.error("Exception :", exception);
		}
		return wbsList;
	}
	
	/**
	 * method for singletonWbs
	 * @return 
	 * 
	 */
	public static List<WBS> getWbsList() {
		if (singletonWbs == null) {
			singletonWbs = new ArrayList<WBS>();
		}
		return singletonWbs;
	}
	
	/**
	 * method for setting Linked Indicator and Linked Flag
	 * @return 
	 * 
	 */
	public Activity checkForLinkActivityId(WBS wbs) {
		 Activity lowestTask = null;
		try {
			 BOIterator<Activity> linkActivities = null;
			 Activity linkActivity = null;
			
			 BOIterator<UDFValue> linkedBoiUDFvalues=null;
		     UDFValue linkedUdfValue = null;
		     UDFIndicator indicator = null;
		     String linkedIndicator = null;
		     
			linkActivities = wbs.loadActivities(
					new String[] { "WBSCode","ObjectId","Id", "Name", "Status", "ActualStartDate","ActualFinishDate", "RemainingEarlyStartDate", "RemainingEarlyFinishDate","ProjectId" }, null, null);
			while (linkActivities.hasNext()) {
				linkActivity = linkActivities.next();

				String linkedwhereClause = "UDFTypeTitle='"	+ P6Constants.LINKED + "'";

				linkedBoiUDFvalues = linkActivity.loadUDFValues(new String[] {
						"UDFTypeTitle", "Indicator" }, linkedwhereClause, null);
				if (linkedBoiUDFvalues.hasNext()) {
					linkedUdfValue = linkedBoiUDFvalues.next();
					if (linkedUdfValue != null && linkedUdfValue.getIndicator() != null) {

							indicator = linkedUdfValue.getIndicator();
							linkedIndicator = indicator.getDescription();
							if (P6Constants.LINKED_FLAG.equalsIgnoreCase(linkedIndicator)) {
								lowestTask =linkActivity;
							}

					}
				}

			}
		} catch (Exception exception) {
			LOGGER.error("Exception :" , exception);

		}
		return lowestTask;
		
	}
	
	/**
	 * Method name: setActivityDetails 
	 * 
	 * @param activity
	 * @param cecId
	 * @param lowestTaskNumber
	 * @throws ServerException, NetworkException, BusinessObjectException
	 * 
	 * @return projectSnapShot	 
	 */
	
	private ProjectSnapShot setActivityDetails(Activity activity, String cecId, String lowestTaskNumber,String existingTaskType) throws BusinessObjectException, ServerException, NetworkException{
		LOGGER.info("activity Name :"+activity.getName());
		
		ProjectSnapShot projectSnapShot = objFactory.createProjectSnapShot();
		Project project = null;
		String activityObjectId = null;
		String activityId = null;
		String activityName = null;
		String chargeable = null;
		String effortLogged = null;
		
		projectSnapShot.setActivityId("");
		projectSnapShot.setActivityObjectId("");
		projectSnapShot.setActivityName("");
		projectSnapShot.setActivityChargeable("");
		projectSnapShot.setEffortLogged("0");
		projectSnapShot.setTeAllowedFlag("");
		
		if(P6Constants.TASK_TYPE_VALUE.equalsIgnoreCase(existingTaskType)){
			projectSnapShot.setTeAllowedFlag(P6Constants.ELIGIBLE_FOR_TE_E);
		}else{
			projectSnapShot.setTeAllowedFlag(P6Constants.ELIGIBLE_FOR_TE_Y);
		}
		
		if (activity.getObjectId() != null) {
			activityObjectId = activity.getObjectId().toString();
			projectSnapShot.setActivityObjectId(activityObjectId);
		
		}
		if (activity.getId() != null) {
			activityId = activity.getId().toString();
			projectSnapShot.setActivityId(activityId);
		
		}
		if (activity.getName() != null) {
			activityName = activity.getName();
			projectSnapShot.setActivityName(activityName);
		}

		// Set Activity Dates based on Activity Status
		if(activity.getStatus() != null){
			getActivtyDates(activity, projectSnapShot);
		}

		// Set Chargeable field
		chargeable = PrimaveraUtil.getChargeableFlag(activity);
		if(chargeable != null && !chargeable.isEmpty()){
			projectSnapShot.setActivityChargeable(chargeable);
		}
		
		// Set Efforst Logged for Given user
		effortLogged =PrimaveraUtil.getEffortedLoggedForResource(activity,cecId);
		
		if(effortLogged != null && !effortLogged.isEmpty()){
			projectSnapShot.setEffortLogged(effortLogged);
		}

		// Set Customer Details
		project = activity.loadProject(new String[] { "Id",	"ObjectId" });
		
		if (project != null) {
			PrimaveraUtil.getCustomerPartyInfo(project, projectSnapShot);
		}
		
		if(lowestTaskNumber == null){
			lowestTaskNumber="";
			projectSnapShot.setEffortLogged(effortLogged);
		}
		projectSnapShot.setLowestTaskNumber(lowestTaskNumber);
		
		return projectSnapShot;
	}
	

	/**
	 * Method name: getActivtyDates 
	 * 
	 * @param activity
	 * @param projectSnapShot
	 * @throws BusinessObjectException
	 */
	
	public static void getActivtyDates(Activity activity,
			ProjectSnapShot projectSnapShot) throws BusinessObjectException {
		projectSnapShot.setStartDate("");
		projectSnapShot.setEndDate("");
		
		if(DCPConstants.ACTIVITY_STATUS_NOT_STARTED.equalsIgnoreCase(activity.getStatus().toString())){
			
			if(activity.getRemainingEarlyStartDate() != null) { 
				projectSnapShot.setStartDate(activity.getRemainingEarlyStartDate().toString());
				LOGGER.info("Activity StartDate :"+activity.getRemainingEarlyStartDate());
			}
			
			if(activity.getRemainingEarlyFinishDate() != null)  {
				projectSnapShot.setEndDate(activity.getRemainingEarlyFinishDate().toString());
				LOGGER.info("Activity EndDate :"+activity.getRemainingEarlyFinishDate());
				
			}
			
		}else if(DCPConstants.ACTIVITY_STATUS_COMPLETED.equalsIgnoreCase(activity.getStatus().toString())){
			if(activity.getActualStartDate() != null) { 
				projectSnapShot.setStartDate(activity.getActualStartDate().toString());
				LOGGER.info("Activity StartDate :"+activity.getActualStartDate());
				
			}
			
			if(activity.getActualFinishDate() != null)  {
				projectSnapShot.setEndDate(activity.getActualFinishDate().toString());
				LOGGER.info("Activity EndDate :"+activity.getActualFinishDate());
				
			}
		}else if(DCPConstants.ACTIVITY_STATUS_IN_PROGRESS.equalsIgnoreCase(activity.getStatus().toString())){
			if(activity.getActualStartDate() != null) { 
				projectSnapShot.setStartDate(activity.getActualStartDate().toString()); 
				LOGGER.info("Activity StartDate :"+activity.getActualStartDate());
				
				
			}
			
			if(activity.getRemainingEarlyFinishDate() != null)  {
				projectSnapShot.setEndDate(activity.getRemainingEarlyFinishDate().toString());
				LOGGER.info("Activity EndDate :"+activity.getRemainingEarlyFinishDate());
			}
		}		
	}
	
}
