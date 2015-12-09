package com.cisco.dcpte.api;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.cisco.dcpte.dao.PrimaveraConnectionManager;
import com.cisco.dcpte.model.ErrorDetail;
import com.cisco.dcpte.model.ProjectProfileResponse;
import com.cisco.dcpte.model.ProjectSnapShot;
import com.cisco.dcpte.util.DCPConstants;
import com.cisco.dcpte.util.P6Constants;
import com.cisco.framework.rest.core.BasicResponseHolder;
import com.primavera.ServerException;
import com.primavera.common.value.BeginDate;
import com.primavera.common.value.EndDate;
import com.primavera.common.value.ObjectId;
import com.primavera.common.value.Unit;
import com.primavera.integration.client.ClientException;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.BOIterator;
import com.primavera.integration.client.bo.BusinessObjectException;
import com.primavera.integration.client.bo.enm.ActivityStatus;
import com.primavera.integration.client.bo.object.Activity;
import com.primavera.integration.client.bo.object.ActivityCode;
import com.primavera.integration.client.bo.object.ActivityCodeAssignment;
import com.primavera.integration.client.bo.object.Resource;
import com.primavera.integration.client.bo.object.ResourceAssignment;
import com.primavera.integration.client.bo.object.UDFType;
import com.primavera.integration.client.bo.object.UDFValue;
import com.primavera.integration.client.bo.object.WBS;
import com.primavera.integration.network.NetworkException;

/**
 * @author zkhaliq
 * Class Name: UpdateResourceLevelActivity 
 * Aim: Implementing logic for updateTimeCardDetails webservice
 */
public class UpdateResourceLevelActivity {
	private static final Logger LOGGER = Logger.getLogger(UpdateResourceLevelActivity.class);
	
	/**
	 * @param args
	 * @throws Exception 
	 */

	private static String timeZone;
	private static String dateFormat1 ="yyyy/MM/dd hh:mm a";
	private static List<WBS> singletonWbs = null;
	
	static
 {
		Properties prop = new Properties();
		InputStream in = null;

		try {
			String propFile = "config.properties";
			in = new UpdateResourceLevelActivity().getClass()
					.getClassLoader().getResourceAsStream(propFile);
			prop.load(in);
			timeZone = prop.getProperty("timeZone");

			LOGGER.info("The TimeZone value getting from config.properties file is : "
							+ timeZone);
		} catch (Exception fnfe) {
			LOGGER.error( "FileNotFoundException",fnfe);
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
	 * Method name: updateTimeCardDetails 
	 * 
	 * @param projectProfileResponse 
	 * @return BasicResponseHolder	 
	 */
	public static synchronized BasicResponseHolder updateTimeCardDetails(ProjectProfileResponse projectProfileResponseinput){
		LOGGER.info("updateTimeCardDetails --entry");
		String cecId = null;
		String activityStartDate = null;
		String activityEndDate = null;
		String projectNumber = null;
		String taskNumber = null;
		String totalHours = null;
		String latestChargedDate = null;
		String earliestChargedDate = null;
		List<Activity> activityUpdateList = new ArrayList<Activity>() ;
		List<ResourceAssignment> resourceUpdateList= new ArrayList<ResourceAssignment>() ;
		List<ResourceAssignment>  resourceCreateList = new ArrayList<ResourceAssignment>() ;
		
		
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		ErrorDetail errorDet = new ErrorDetail();
		String errorMessage = "";
		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponse = new ProjectProfileResponse();
		List<ProjectSnapShot> lstProjectSnapShots;
		lstProjectSnapShots = projectProfileResponseinput.getProjectSnapShot();
		for (ProjectSnapShot projectSnapShot : lstProjectSnapShots){
			if (projectSnapShot != null) {
				try {
				
					if (projectSnapShot.getProjectNumber() != null && projectSnapShot.getProjectNumber().length() > 0) {
						projectNumber =projectSnapShot.getProjectNumber() ;
					}
					LOGGER.info("projectNumber :"+projectNumber);
					if (projectSnapShot.getCecId() != null){
						cecId = projectSnapShot.getCecId();		
					}	
					LOGGER.info("cecId :"+cecId);
					if (projectSnapShot.getActivityStartDate() != null){
						activityStartDate = projectSnapShot.getActivityStartDate();						
					}
					LOGGER.info("activityStartDate :"+activityStartDate);
					
					if (projectSnapShot.getActivityEndDate() != null){
						activityEndDate =projectSnapShot.getActivityEndDate();						
					}	
					LOGGER.info("activityEndDate :"+activityEndDate);
					if (projectSnapShot.getTaskNumber() != null){
						taskNumber = projectSnapShot.getTaskNumber();		
					}
					LOGGER.info("taskNumber :"+taskNumber);
					if (projectSnapShot.getTotalHours() != null){
						totalHours = projectSnapShot.getTotalHours();		
					}
					LOGGER.info("totalHours :"+totalHours);
					if(projectSnapShot.getMinChargedDate() != null){
						earliestChargedDate = projectSnapShot.getMinChargedDate().toString();
					}
					LOGGER.info("earliestChargedDate :"+earliestChargedDate);
				
					if(projectSnapShot.getLastestChargedDate() != null){
						latestChargedDate = projectSnapShot.getLastestChargedDate().toString();
					}
					LOGGER.info("latestChargedDate :"+latestChargedDate);			
					if( projectNumber == null || projectNumber.isEmpty() ||  taskNumber == null || taskNumber.isEmpty() || cecId == null || cecId.isEmpty()){
						throw new Exception("All mandatory fields are required ");
				
					}else{
					
						PrimaveraConnectionManager.getInstance();
					//	String whereActivityId = "Id = '"+taskNumber+"' and ProjectId ='"+projectNumber+"'";
						
						// activity Object id changes 
						String whereActivityId = "ObjectId = '"+taskNumber+"' and ProjectId ='"+projectNumber+"'";
					
						EnterpriseLoadManager elm =null;
						BOIterator<Activity> activities;
						Session session = PrimaveraConnectionManager.getSession();
						LOGGER.info("session :"+session);
						elm = session.getEnterpriseLoadManager(); 
						activities = elm.loadActivities(new String[] { "Id","Status","ObjectId", "ProjectId", "Name" ,"ActualStartDate","ActualFinishDate","RemainingEarlyStartDate","RemainingEarlyFinishDate","StartDate","FinishDate"} , whereActivityId , null);
						LOGGER.info("Activities :" +activities.hasNext()); 
						if(!activities.hasNext()){

							ErrorDetail errorDet2 = new ErrorDetail();
							errorDet2.setErrorCode(DCPConstants.ACTIVITY_EXISTS_ERROR_CODE);
							errorDet2.setErrorMessage(DCPConstants.ACTIVITY_EXISTS_ERROR_MSG);
							errorDet2.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
							projectProfileResponse.getErrorDetail().add(errorDet2);
							responseHolder.setData(projectProfileResponse);
							return responseHolder;
							
						}else if(activities.hasNext()){
							
							Activity activity = activities.next();
							activityUpdateList.add(activity);
							
							if(earliestChargedDate != null && earliestChargedDate.length() >0){
								updateEarliestChargedDate(activity,earliestChargedDate);
									
							}
								
							if(latestChargedDate != null && latestChargedDate.length() >0){
								updateLatestChargedDate(activity,latestChargedDate);
							}
					
							projectProfileResponse =updateResourceLevelActivityHours(cecId.toLowerCase(), activity, totalHours, activityStartDate,activityEndDate, resourceUpdateList, resourceCreateList);

							List<WBS> wbsHeirarchyList = null;
							BOIterator<Activity> bOActivity = null;
							Activity activity1;
							BOIterator<UDFValue> bOIterator = null;
							String activityFields[]={ "Id","Status","ObjectId", "ProjectId", "Name" ,"ActualStartDate","ActualFinishDate","RemainingEarlyStartDate","RemainingEarlyFinishDate","StartDate","FinishDate"};
							String whereClause = "Indicator='"+P6Constants.LINKED_FLAG.trim()+"'";
							WBS wbs = activity.loadWBS(new String[] { "Name" });

							//added by gdeshmuk for infinite loop issue
							boolean foundLowestLevelTask = false;
							if (wbs != null) {
								
								bOActivity = wbs.loadActivities(activityFields, null, null);
								
								while (bOActivity.hasNext()) {
									activity1 = bOActivity.next();
									
									bOIterator = activity1.loadUDFValues(new String[] { "UDFTypeObjectId" }, whereClause, null);
									if (bOIterator.hasNext()) {
										  activityUpdateList.add(activity1);
										// --update lowest task status  also as "in progress" if not started--
										// 6.if lowest task start and end date are not in range of activity getting charged.
										
										  //Commenting code to Update Lowest task dates as per new requirement on 23/05
									//	UpdateResourceLevelActivity.updateActivtyDates(activity1,activityStartDate,activityEndDate);
										
										// If lowest task is not chargeable make it chargeable
										UpdateResourceLevelActivity.setLowestLevelTaskAsChargable(activity1,session);
										
										break;
										
									}else{
										
										// Load All WBS
										wbsHeirarchyList = getParentWbs(wbs);
										
										if (wbsHeirarchyList.size() > 0) {
											for (WBS parentWbs : wbsHeirarchyList) {
												//added by gdeshmuk for infinite loop issue
												if(foundLowestLevelTask){
													break;
												}
												bOActivity = parentWbs.loadActivities(activityFields, null, null);
												while (bOActivity.hasNext()) {
													activity1 = bOActivity.next();
													bOIterator = activity1.loadUDFValues(new String[] { "UDFTypeObjectId" }, whereClause, null);
													if (bOIterator.hasNext()) {
															  activityUpdateList.add(activity1);
														// --update lowest task status  also as "in progress" if not started--
														// 6.if lowest task start and end date are not in range of activity getting charged.
													//Commenting code to Update Lowest task dates as per new requirement on 23/05
												/*	if(activity1.getStatus() != null && activityStartDate != null && activityEndDate.length() > 0 && activityStartDate != null && activityEndDate.length() > 0){
															
															UpdateResourceLevelActivity.updateActivtyDates(activity1,activityStartDate,activityEndDate);
														}*/
														
														// If lowest task is not chargeable make it chargeable
														UpdateResourceLevelActivity.setLowestLevelTaskAsChargable(activity1,session);
														//added by gdeshmuk for infinite loop issue
														foundLowestLevelTask = true;
														break;
													}
												 }
											
											}
											wbsHeirarchyList.clear();
										}
										
									}// load All WBS block
									//added by gdeshmuk for infinite loop issue
									if(foundLowestLevelTask){
										break;
									}
								}
							}
					
				}
				}
			}catch (Exception e) {
			
				projectProfileResponse.getErrorDetail().clear();
				errorMessage = e.getMessage();
				LOGGER.error("errorMessage :",e);
				errorDet.setErrorMessage(errorMessage);
				errorDet.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
				errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse.getErrorDetail().add(errorDet);
				responseHolder.setData(projectProfileResponse);
				return responseHolder;
			}

		}
	  }
		if(projectProfileResponse != null){
			String failedActivtyId ="";
			try {
				LOGGER.info("value " +projectProfileResponse.getErrorDetail().size());
				
				LOGGER.info("Resource Create list "+resourceCreateList.size());
			if (projectProfileResponse.getErrorDetail().size() == 0){
				
				//remove duplicate Id's
				LOGGER.info("activityUpdateList  list "+activityUpdateList.size());
				Set<Activity> activityUpdateList2 = new HashSet<Activity>(activityUpdateList);
				
				LOGGER.info("activityUpdateList  list2 "+activityUpdateList2.size());
		      
				for (Activity listActivity : activityUpdateList2){
					
					failedActivtyId = listActivity.getId();
					listActivity.update();
					LOGGER.info("activity "+failedActivtyId+" updated");
				}
								
				LOGGER.info("Resource update list "+resourceUpdateList.size());
				Set<ResourceAssignment> resourceUpdateList2 = new HashSet<ResourceAssignment>(resourceUpdateList);
				LOGGER.info("Resource update list2 "+resourceUpdateList2.size());
				for (ResourceAssignment resAsignmentupdate : resourceUpdateList2){
					resAsignmentupdate.update();
				}
								
				Set<ResourceAssignment> resourceCreateList2 = new HashSet<ResourceAssignment>(resourceCreateList);
				LOGGER.info("Resource Create list2 "+resourceCreateList2.size());
				for (ResourceAssignment resAsignmentcreate : resourceCreateList2){
					try{
						ObjectId id = resAsignmentcreate.create();
						resAsignmentcreate.setObjectId(id);
						}catch (Exception e){
							LOGGER.error("Exception :",e);
							String errMessage=e.getMessage();
							String duplicateErrorTrigger="Code:DUPLICATE_RESOURCE";
							LOGGER.info(e.getMessage());
							if (errMessage.toLowerCase().contains(duplicateErrorTrigger.toLowerCase())){
								LOGGER.info("Resource Duplicate Trigger Caught");
								resAsignmentcreate.update();								
							}
						}
					
				}
							
			}else{
				projectProfileResponse.getProjectSnapShot().clear();
				responseHolder.setData(projectProfileResponse);
				return responseHolder;
			}
			}catch (Exception e){
				LOGGER.error("Exception :",e);
				projectProfileResponse.getErrorDetail();
				errorDet = new ErrorDetail();
				errorMessage ="activty Updation failed" ;
				errorDet.setErrorMessage(errorMessage);
				errorDet.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
				errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				projectProfileResponse.getErrorDetail().add(errorDet);
				responseHolder.setData(projectProfileResponse);
				return responseHolder;
			}finally{
				if(resourceCreateList != null){
					resourceCreateList.clear();
				}
				if(resourceUpdateList != null){
					resourceUpdateList.clear();
				}
				if(activityUpdateList != null){
					activityUpdateList.clear();
				}
			}
			if(projectProfileResponse.getErrorDetail().size()==0){
				ProjectSnapShot projectSnapShot = new ProjectSnapShot();
				projectSnapShot.setResultMessage(P6Constants.UPDATE_STATUS_SUCCESS);
				projectProfileResponse.getProjectSnapShot().add(projectSnapShot);
				LOGGER.info("updated Successfully");
			}
			responseHolder.setData(projectProfileResponse);
		}
		
		return responseHolder;
}

	/**
	 * Method name: updateEarliestChargedDate 
	 * 
	 * @param activity
	 * @param earliestChargedDate
	 * @throws ServerException, NetworkException, ClientException, ParseException
	 * 	 
	 */	
private static void updateEarliestChargedDate(Activity activity,String earliestChargedDate) throws ParseException, ServerException, NetworkException, ClientException {
	

	
	SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormat1);
    Date inputEarlierChargedDate = dateFormat.parse(earliestChargedDate.concat(P6Constants.START_TIME));
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
	Date existingEarliestChargedDate = null;
	
	String udfWhereClause ="UDFTypeTitle ='"+P6Constants.EARLIEST_CHARGED_DATE+"'";
	BOIterator<UDFValue> boiUDFvalues=activity.loadUDFValues(new String[] {"StartDate","UDFTypeTitle","Text","FinishDate"}, udfWhereClause, null);

		if (!boiUDFvalues.hasNext()) {
			String udfTypeObjectId = getUDFTypeObjectId(
					P6Constants.EARLIEST_CHARGED_DATE, "Activity");
			ObjectId id = ObjectId.fromString(udfTypeObjectId);
			Session session = PrimaveraConnectionManager.getSession();
			UDFValue udf = new UDFValue(session);
			udf.setUDFTypeObjectId(id);
			udf.setForeignObjectId(activity.getObjectId());
			BeginDate insertDate = new BeginDate(inputEarlierChargedDate);
			udf.setStartDate(insertDate);
			udf.create();
			LOGGER.info("Earliest ChargedDate Created");
			
		}else if(boiUDFvalues.hasNext()) {
				UDFValue udfValue = boiUDFvalues.next();
				if(udfValue.getFinishDate() != null){
					existingEarliestChargedDate = udfValue.getFinishDate();
					
					if(existingEarliestChargedDate.after(inputEarlierChargedDate)){
						BeginDate insertDate = new BeginDate(inputEarlierChargedDate);
						udfValue.setStartDate(insertDate);
						udfValue.update();
					}
				}
				LOGGER.info("Earliest ChargedDate UDF Updated");
				
			}

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
		LOGGER.error("Exception :" , exception);
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
	* Method name: updateResourceLevelActivityHours 
	* 
	* @param cecId
	* @param activity
	* @param totalHours
	* @param actStartDate 
	* @param actEndDate 
 	* @param resourceUpdateList
 	* @param resourceCreateList
 	* @throws Exception
 	* 
 	* @return projectProfileResponse	 
 	*/
	public static ProjectProfileResponse updateResourceLevelActivityHours(String cecId, Activity activity, String totalHours,String actStartDate,String actEndDate, List<ResourceAssignment> resourceUpdateList, List<ResourceAssignment> resourceCreateList) throws Exception {
		ProjectProfileResponse projectProfileResponse = null;
		// Using PrimaveraConnectionManager class to get the database instance
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormat1);
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
				
				LOGGER.info("act ObjId :"+activity.getObjectId());
				LOGGER.info("Status :"+activity.getStatus());
				

				//1 . change from not stated status to inprogress
				if(activity.getStatus() != null){
					if(DCPConstants.ACTIVITY_STATUS_NOT_STARTED.equalsIgnoreCase(activity.getStatus().toString())){
						LOGGER.info("in progress status");
						activity.setStatus(ActivityStatus.IN_PROGRESS);
						
						// 2 .Update activity actual start date if not available
						if(activity.getActualStartDate() == null && actStartDate != null && actStartDate.length() >0){
							LOGGER.info("update actual start date");

							String startDateWithTimeStamp = actStartDate.concat(P6Constants.START_TIME);
							Date inputActActualStart = dateFormat.parse(startDateWithTimeStamp);
							LOGGER.info("inputActActualStart date    :" +inputActActualStart);
							BeginDate actActualBeginDate = new BeginDate(inputActActualStart.getTime());
							activity.setActualStartDate(actActualBeginDate);
							
						}
					}else if(DCPConstants.ACTIVITY_STATUS_COMPLETED.equalsIgnoreCase(activity.getStatus().toString()) && actEndDate !=null && actEndDate.length() >0 && activity.getFinishDate() != null){
						
						// Update activity actual start date if not available.  if activity status is completed then update activity end date.(if charged date is greater  than end Date)

						LOGGER.info("in completed block");
						// For inputActFinishDate
						String endDateWithTimeStamp = actEndDate.concat(P6Constants.END_TIME);
						Date formatedInputActEndDate = dateFormat.parse(endDateWithTimeStamp);
						EndDate inputActActualEnDate = new EndDate(formatedInputActEndDate.getTime());
						
						// For existingActFinishDate
						String formatedDate = dateFormat.format(activity.getFinishDate());
						Date existActEndDate= dateFormat.parse(formatedDate);
						LOGGER.info("actActualEnDate :"+formatedInputActEndDate);
						LOGGER.info("existActEndDate :"+existActEndDate);
						
						if(existActEndDate != null && formatedInputActEndDate.after(existActEndDate)){
							activity.setActualFinishDate(inputActActualEnDate);
							LOGGER.info("end dates update block");
						}
						
					}
				}
				
				// 3 .If activity level resource assignment exists   Update actual hours at activity level for each resource  else   create assignment and update actual hours 
				projectProfileResponse = updateResourceAssignment(activity, cecId, totalHours, resourceUpdateList, resourceCreateList );
						
		return projectProfileResponse;
		}
	
	/**
	* Method name: updateResourceAssignment 
	* 
	* @param cecId
	* @param activity
	* @param totalHours
 	* @param resourceUpdateList
 	* @param resourceCreateList
 	* @throws Exception
 	* 
 	* @return projectProfileResponse	 
 	*/
	private static ProjectProfileResponse updateResourceAssignment(Activity activity,String cecId,String totalHours, List<ResourceAssignment> resourceUpdateList, List<ResourceAssignment> resourceCreateList)
			throws Exception {
		LOGGER.info("in updateResourceAssignment");
		
		ProjectProfileResponse projectProfileResponse = new ProjectProfileResponse();
		ErrorDetail errorDet = null;
		
		Unit userTotalHours = null;
		if(totalHours != null && !(totalHours.equals("0"))){
			userTotalHours = new Unit(Double.parseDouble(totalHours));
		}
		String resourceId = cecId;
		
		String[] fields = { "ActivityId", "ActivityObjectId",
				"IsPrimaryResource", "ObjectId", "ResourceId",
				"ResourceName", "ResourceObjectId", "ResourceType",
				"RoleId", "RoleName", "RoleObjectId","ActualUnits","PlannedUnits",
				"RemainingUnits","RemainingStartDate","RemainingFinishDate", "PlannedStartDate", 
				"PlannedFinishDate", "ActualStartDate", "ActualFinishDate" };
			
			String sWhereClause = "ResourceId ='"+resourceId.trim()+"'";
			
			LOGGER.info("sWhereClause : " + sWhereClause);
			
			BOIterator<ResourceAssignment> resourecAssgBOI = activity.loadResourceAssignments(fields, sWhereClause, null);
			
			// create assignment and set actual hours
			if(!resourecAssgBOI.hasNext()){
				LOGGER.info("creating resource");
				String[] resFields = { "EmployeeId", "Id", "Name", "ObjectId",
						"PrimaryRoleId", "PrimaryRoleName",
						"PrimaryRoleObjectId", "UserName", "UserObjectId","IsActive"};
				String resWhereClause = "Id = '"+resourceId+ "'";
				
				LOGGER.info("resWhereClause : " + resWhereClause);
				Session session =PrimaveraConnectionManager.getSession();
				EnterpriseLoadManager enterpriseLoadManager = session.getEnterpriseLoadManager();
				BOIterator<Resource> boIterator = enterpriseLoadManager.loadResources(resFields, resWhereClause, null);
				enterpriseLoadManager = null;
				if(!boIterator.hasNext()){
					errorDet = new ErrorDetail();
					errorDet.setErrorCode(DCPConstants.UPDATE_RESOURC_ERROR_CODE);
					errorDet.setErrorMessage(DCPConstants.UPDATE_RESOURC_ERROR_MSG);
					errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
					projectProfileResponse.getErrorDetail().add(errorDet);
					return projectProfileResponse;
				}else {
						LOGGER.info("resource found in P6");
						Resource resource = (Resource) boIterator.next();
						 ResourceAssignment resourceAssignment = new ResourceAssignment(session);
						
						 if (resource.getIsActive()) {
							resourceAssignment.setResourceObjectId(resource.getObjectId());
							
							// fore creating new rescAsgnment with planned and remaining units as zero
							Unit plannedUnits = new Unit(0.0);
							resourceAssignment.setPlannedUnits(plannedUnits);
							resourceAssignment.setRemainingUnits(plannedUnits);
							
							if (userTotalHours != null) {
								resourceAssignment.setActualUnits(userTotalHours);
							}
							resourceAssignment.setActivityObjectId(activity.getObjectId());
							resourceCreateList.add(resourceAssignment);
							}else{
							errorDet = new ErrorDetail();
							errorDet.setErrorCode(DCPConstants.CERATE_INACTIVE_RESOURC_ERROR_CODE);
							errorDet.setErrorMessage(DCPConstants.CERATE_INACTIVE_RESOURC_ERROR_MSG);
							errorDet.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
							projectProfileResponse.getErrorDetail().add(errorDet);
							return projectProfileResponse;
						}
						resource = null;
						resourceAssignment = null;
					}
				boIterator = null;
				
			}else if(resourecAssgBOI.hasNext()) {
				// resource assignment exists   Update actual hours at activity level
					LOGGER.info("resource exists");
					ResourceAssignment resAssg = resourecAssgBOI.next();
					if(userTotalHours != null ){
						
						if(resAssg.getActualUnits() != null){
							//get existing hours
							Unit existingHours = resAssg.getActualUnits();
							
							double value1 =Double.parseDouble(userTotalHours.toString());
							double value2 =Double.parseDouble(existingHours.toString()); 
							double totHrs =value1 + value2;
							if(totHrs >0){
							 userTotalHours =  new Unit(totHrs);
							}else{
								userTotalHours =new Unit(0.0);
							}
							
						}
						resAssg.setActualUnits(userTotalHours);
					}
					resourceUpdateList.add(resAssg);
					resAssg = null;
			}

			return projectProfileResponse;

		}

	/**
	* Method name: updateLatestChargedDate 
	* 
	* @param activity
	* @param latestChargedDate
 	* @param resourceUpdateList
 	* @param resourceCreateList
 	* @throws ServerException, NetworkException, ParseException, ClientException
 	*  
 	*/
private static void updateLatestChargedDate(Activity activity, String latestChargedDate) throws ServerException, NetworkException, ParseException, ClientException {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormat1);
	    Date inputChargedDate = dateFormat.parse(latestChargedDate.concat(P6Constants.END_TIME));
	    TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		Date existingChargedDate = null;
		
		String udfWhereClause ="UDFTypeTitle ='"+P6Constants.LATEST_CHARGED_DATE+"'";
		BOIterator<UDFValue> boiUDFvalues=activity.loadUDFValues(new String[] {"StartDate","UDFTypeTitle","Text","FinishDate"}, udfWhereClause, null);

			if (!boiUDFvalues.hasNext()) {
				String udfTypeObjectId = getUDFTypeObjectId(
						P6Constants.LATEST_CHARGED_DATE, "Activity");
				ObjectId id = ObjectId.fromString(udfTypeObjectId);
				Session session = PrimaveraConnectionManager.getSession();
				UDFValue udf = new UDFValue(session);
				udf.setUDFTypeObjectId(id);
				udf.setForeignObjectId(activity.getObjectId());
				EndDate insertDate = new EndDate(inputChargedDate);
				udf.setFinishDate(insertDate);
				udf.create();
				LOGGER.info("Latest ChargedDate Created");
			}else if(boiUDFvalues.hasNext()) {
					UDFValue udfValue = boiUDFvalues.next();
					if(udfValue.getFinishDate() != null){
						existingChargedDate = udfValue.getFinishDate();
						
						if(existingChargedDate.before(inputChargedDate)){
							EndDate insertDate = new EndDate(inputChargedDate);
							udfValue.setFinishDate(insertDate);
							udfValue.update();
						}
					}
					LOGGER.info("Latest ChargedDate UDF Updated");
					
				}
	}
	
	/**
	* Method name: getUDFTypeObjectId 
	* 
	* @param udfTypeTitleId
	* @param subjectArea
	* @throws ServerException, NetworkException, ClientException
	* 
	* @return string 
	*/
	public static String getUDFTypeObjectId(String udfTypeTitleId,  String subjectArea) 
			 throws ServerException, NetworkException, ClientException{
		
		PrimaveraConnectionManager.getInstance();
		Session session;
		session = PrimaveraConnectionManager.getSession();
		EnterpriseLoadManager elm = session.getEnterpriseLoadManager();
		String where = "Title = '" + udfTypeTitleId + "' and SubjectArea = '"+ subjectArea + "'";
		LOGGER.info("inside getUDFTypeObjectId where = "+where);
		BOIterator<UDFType> boiUDFTypes = elm.loadUDFTypes(new String[] {"ObjectId", "Title", "SubjectArea" }, where, null);

		UDFType udfType = null;
		String udfObjectId = "";
		if (boiUDFTypes.hasNext()) {
			udfType = boiUDFTypes.next();
			if (udfType != null) {
				LOGGER.info(" udfType object id : " + udfType.getObjectId());
				udfObjectId = udfType.getObjectId().toString();
			}
		}
		return udfObjectId;
	}
			
	/**
	* Method name: updateActivtyDates 
	* 
	* @param activity
	* @param activityInputStartDate
	* @param activityInputFinishDate
	* @throws BusinessObjectException, ParseException
	* 
	*/
	//Commenting as this method is not user anywhere
	/*public static void updateActivtyDates(Activity activity,
			String activityInputStartDate, String activityInputFinishDate) throws BusinessObjectException, ParseException {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormat1);
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		
		// format existing Actual dates
		String existActActualStart = null ;
		Date activityExistingStartDate = null;
		
		if(activity.getActualStartDate() != null){
			existActActualStart=dateFormat.format(activity.getActualStartDate() );
			activityExistingStartDate = dateFormat.parse(existActActualStart);
		}
		
		String existActActualEnd = null;
		Date activityExistingEndDate = null;
		
		if(activity.getActualFinishDate() != null){
			existActActualEnd = dateFormat.format(activity.getActualFinishDate());
			activityExistingEndDate = dateFormat.parse(existActActualEnd);
		}
		
		// format existing Rem dates
		String extstActRemStart = null;
		Date activityExistingRemStartDate = null;
		if(activity.getRemainingEarlyStartDate() != null){
			extstActRemStart = dateFormat.format(activity.getRemainingEarlyStartDate());
			activityExistingRemStartDate = dateFormat.parse(extstActRemStart);
		}
		
		String extActRemEnd = null;
		Date activityExistingRemEndDate = null;
		
		if(activity.getRemainingEarlyFinishDate() != null){
			extActRemEnd = dateFormat.format(activity.getRemainingEarlyFinishDate());
			activityExistingRemEndDate = dateFormat.parse(extActRemEnd);
		}
		
		// For inputActStartDate
		 Date formatedInputActStartDate = null;
		 BeginDate inputActActualStartDate = null;
		
		if(activityInputStartDate != null && activityInputStartDate.length() >0){
			String inputStratDateWithTimeStamp = activityInputStartDate.concat(P6Constants.START_TIME);
			formatedInputActStartDate = dateFormat.parse(inputStratDateWithTimeStamp);
			inputActActualStartDate = new BeginDate(formatedInputActStartDate.getTime());
		}
		
		// For inputActFinishDate
		Date formatedInputActEndDate = null;
		EndDate inputActActualEnDate = null;
		if(activityInputFinishDate != null && activityInputFinishDate.length() >0 ){
			
			String actEndDateWithTimeStamp = activityInputFinishDate.concat(P6Constants.END_TIME);
			formatedInputActEndDate = dateFormat.parse(actEndDateWithTimeStamp);
			inputActActualEnDate = new EndDate(formatedInputActEndDate.getTime());
		}
		
		
		if(DCPConstants.ACTIVITY_STATUS_NOT_STARTED.equalsIgnoreCase(activity.getStatus().toString())){
			LOGGER.info("NotStarted");
			activity.setStatus(ActivityStatus.IN_PROGRESS);

			//	Update activity actual start date if not available
				if(activity.getActualStartDate() == null && activityInputStartDate != null && activityInputStartDate.length() >0){
					activity.setActualStartDate(inputActActualStartDate);

					}
			
			
			if(activityExistingRemStartDate != null && formatedInputActStartDate != null && activityExistingRemStartDate.after(formatedInputActStartDate)) {
				activity.setRemainingEarlyStartDate(inputActActualStartDate);
				LOGGER.info("Activity StartDate :"+activity.getRemainingEarlyStartDate());
			}
			
			if(activityExistingRemEndDate != null && formatedInputActEndDate != null && activityExistingRemEndDate.before(formatedInputActEndDate)  )  {
				
				activity.setRemainingEarlyFinishDate(inputActActualEnDate);
				LOGGER.info("Activity EndDate :"+activity.getRemainingEarlyFinishDate());
				
			}
			
		}else if(DCPConstants.ACTIVITY_STATUS_COMPLETED.equalsIgnoreCase(activity.getStatus().toString())){
			
			LOGGER.info("Completed");
			if(activityExistingStartDate != null && formatedInputActStartDate != null && activityExistingStartDate.after(formatedInputActStartDate)) { 
				activity.setActualStartDate(inputActActualStartDate);
				LOGGER.info("Activity StartDate :"+activity.getActualStartDate());
				
			}
			
			if(activityExistingEndDate != null && formatedInputActEndDate != null && activityExistingEndDate.before(formatedInputActEndDate))  {
				activity.setActualFinishDate(inputActActualEnDate);
				LOGGER.info("Activity EndDate :"+activity.getActualFinishDate());
				
			}
		}else if(DCPConstants.ACTIVITY_STATUS_IN_PROGRESS.equalsIgnoreCase(activity.getStatus().toString())){
			
			LOGGER.info("Inprogress");
			if(activityExistingStartDate != null && formatedInputActStartDate != null && activityExistingStartDate.after(formatedInputActStartDate)) {
				
				activity.setActualStartDate(inputActActualStartDate);
				LOGGER.info("Activity StartDate :"+activity.getActualStartDate());
				
			}
			if(activityExistingRemEndDate != null && formatedInputActEndDate != null && activityExistingRemEndDate.before(formatedInputActEndDate)  )  {
				
				activity.setRemainingEarlyFinishDate(inputActActualEnDate);
				LOGGER.info("Activity EndDate :"+activity.getRemainingEarlyFinishDate());
				
			}
		}		
		
	}*/

	/**
	* Method name: setLowestLevelTaskAsChargable 
	* 
	* @param activity
	* @param session
	* @throws Exception
	* 
	*/
	 public static  void setLowestLevelTaskAsChargable(Activity activity, Session session)
			 throws Exception {
		 LOGGER.info("in updateChargebleFlag");
	
		 String[] fields = { "ActivityCodeDescription",
		 "ActivityCodeObjectId", "ActivityCodeTypeName",
		 "ActivityCodeValue", "ActivityId", "ActivityName",
		 "ProjectId", "ProjectObjectId","ActivityCodeTypeScope" };
		 
		 
		 String sWhereClause = "ActivityCodeTypeName = '"+ P6Constants.CHARGEABLE + "' And ActivityCodeTypeScope = '"+P6Constants.ACTIVITY_CODE_SCOPE_GLOBAL+"'";
		 LOGGER.info("sWhereClause : " + sWhereClause);
		 BOIterator<ActivityCodeAssignment> activityCodeAssignmentBO = activity.loadActivityCodeAssignments(fields,sWhereClause, null);
		 LOGGER.info("activityCodeAssignmentBO.hasNext() :"+activityCodeAssignmentBO.hasNext());
		 if(!activityCodeAssignmentBO.hasNext()){
		
			 ActivityCode activityCode =loadActivityCode();
			 ActivityCodeAssignment actCodeAsgnmnt = new ActivityCodeAssignment(session,activity.getObjectId(),activityCode.getCodeTypeObjectId());
			 actCodeAsgnmnt.setActivityCodeObjectId(activityCode.getObjectId());
			 actCodeAsgnmnt.create();
			 
		 }else if(activityCodeAssignmentBO.hasNext()) {
		 ActivityCodeAssignment activityCodeAssignment = activityCodeAssignmentBO.next();
		 
		 LOGGER.info("ActivityCodeValue :"+activityCodeAssignment.getActivityCodeValue());
		 if(activityCodeAssignment.getActivityCodeValue() != null && !(P6Constants.CHARGEABLE_FLAG_YES.equalsIgnoreCase(activityCodeAssignment.getActivityCodeValue().toString()))){
			 LOGGER.info("updating activity as  chargable");
			 ActivityCode activityCode =loadActivityCode();
			 if (activityCode != null) {
				 activityCodeAssignment.setActivityCodeObjectId(activityCode.getObjectId());
				 activityCodeAssignment.setActivityObjectId(activity.getObjectId());
				 activityCodeAssignment.update();
			 }
		 }

	
		 }
	}
	 /**
		 * Load activity code.
		 *
		 * @param codeTypeName the code type name
		 * @param codeValue the code value
		 * @param codeTypeScope the code type scope
		 * @return the activity code
		 */
		private static  ActivityCode loadActivityCode() {
			ActivityCode activityCode = null;
			Session session=null;
			try{
			
			session = PrimaveraConnectionManager.getSession();
			
			EnterpriseLoadManager loadManager = session.getEnterpriseLoadManager();
			String[] fields = { "CodeTypeName", "CodeTypeObjectId",
					"CodeTypeScope", "CodeValue", "Description", "ObjectId",
			"ParentObjectId","CodeTypeObjectId" };
			String sWhereClause = "CodeTypeName = '"+P6Constants.CHARGEABLE+"' and CodeTypeScope = '"+P6Constants.ACTIVITY_CODE_SCOPE_GLOBAL+"' and CodeValue = '"+P6Constants.CHARGEABLE_FLAG_YES+"'";
	
			
				BOIterator<ActivityCode> codeBO = loadManager.loadActivityCodes(
						fields, sWhereClause, null);
				while (codeBO.hasNext()) {
					activityCode = codeBO.next();
				}

			} catch (BusinessObjectException e) {
				LOGGER.error("BusinessObjectException :",e);
			} catch (ServerException e) {
				LOGGER.error("ServerException",e);
			} catch (NetworkException e) {
				LOGGER.error("NetworkException",e);
			}catch (ClientException e1) {
				LOGGER.error("ClientException",e1);
			}
			return activityCode;
		}
		
			
	}

