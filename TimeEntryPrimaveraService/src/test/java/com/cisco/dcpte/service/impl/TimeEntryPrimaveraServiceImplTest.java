package com.cisco.dcpte.service.impl;

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.cisco.dcpte.model.ProjectProfileResponse;
import com.cisco.dcpte.model.ProjectSnapShot;
import com.cisco.framework.rest.core.BasicResponseHolder;
import com.cisco.framework.rest.core.HttpVerb;
import com.primavera.ServerException;
import com.primavera.integration.client.ClientException;
import com.primavera.integration.network.NetworkException;

public class TimeEntryPrimaveraServiceImplTest {

	/**
	 * Constructor for test class.
	 * 
	 * @author Parasoft Jtest 9.5
	 */
	public TimeEntryPrimaveraServiceImplTest() {
		

	}

	/**
	 * Used to set up the test. This method is called by JUnit before each of
	 * the tests are executed.
	 * 
	 * @throws Exception
	 *             the exception
	 * @author Parasoft Jtest 9.5
	 */
	@Before
	public void setUp() throws Exception {
		
		 
		// jtest.Repository.putTemporary("name", object);

	}

	/**
	 * testgetActivitiesOfWBS
	 * 
	 * @throws ServerException
	 *             the server exception
	 * @throws NetworkException
	 *             the network exception
	 * @throws ClientException
	 *             the client exception
	 * @throws ParseException
	 *             the parse exception
	 */
	@Test
	public void testgetActivitiesOfWBS() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();
		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput = new ProjectProfileResponse();
		String wsbId="104141";
		String taskTypes="Travel";
		String cecId ="sashwin";
		String wbsCode ="1000.3";
		responseHolder.setContentType("application/json");
		ProjectSnapShot projectSnapShot = new ProjectSnapShot();
		projectSnapShot.setWbsId(wsbId);
		projectSnapShot.setInclusionTaskType(taskTypes);
		projectSnapShot.setCecId(cecId);
		projectSnapShot.setExclusionWbsCode(wbsCode);
		HttpVerb httpVerb = HttpVerb.POST;
		projectProfileResponseinput.getProjectSnapShot().add(projectSnapShot);
		
		responseHolder = timeEntrySrvImpl.getActivitiesOfWBS(httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}
	
	@Test
	public void testgetActivitiesOfWBSMandatoryInput() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();
		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput = new ProjectProfileResponse();
		String wsbId="";
		String taskTypes="Travel";
		String cecId ="sashwin";
		String wbsCode ="1000.3";
		responseHolder.setContentType("application/json");
		ProjectSnapShot projectSnapShot = new ProjectSnapShot();
		projectSnapShot.setWbsId(wsbId);
		projectSnapShot.setInclusionTaskType(taskTypes);
		projectSnapShot.setCecId(cecId);
		projectSnapShot.setExclusionWbsCode(wbsCode);
		HttpVerb httpVerb = HttpVerb.POST;
		projectProfileResponseinput.getProjectSnapShot().add(projectSnapShot);
		
		responseHolder = timeEntrySrvImpl.getActivitiesOfWBS(httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}
	
	
	@Test
	public void testgetActivitiesOfWBSNoInput() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();
		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput = null;
		
		responseHolder.setContentType("application/json");
	
		HttpVerb httpVerb = HttpVerb.POST;
		responseHolder = timeEntrySrvImpl.getActivitiesOfWBS(httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}

	/**
	 * testGetExceptionTasksList
	 * 
	 * @throws ServerException
	 *             the server exception
	 * @throws NetworkException
	 *             the network exception
	 * @throws ClientException
	 *             the client exception
	 * @throws ParseException
	 *             the parse exception
	 */
	@Test
	public void testGetExceptionTasksList() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput = new ProjectProfileResponse();
		String projectNumber="781331";
		String taskTypes="Exception";
		String cecId ="deepaks3";
		responseHolder.setContentType("application/json");
		ProjectSnapShot projectSnapShot = new ProjectSnapShot();
		projectSnapShot.setProjectNumber(projectNumber);
		projectSnapShot.setInclusionTaskType(taskTypes);
		projectSnapShot.setCecId(cecId);
		HttpVerb httpVerb = HttpVerb.POST;
		projectProfileResponseinput.getProjectSnapShot().add(projectSnapShot);
		
		responseHolder = timeEntrySrvImpl.getExceptionTasksList (httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}
	

	/**
	 * testGetExceptionTasksList
	 * 
	 * @throws ServerException
	 *             the server exception
	 * @throws NetworkException
	 *             the network exception
	 * @throws ClientException
	 *             the client exception
	 * @throws ParseException
	 *             the parse exception
	 */
	@Test
	public void testGetExceptionTasksListEmptyRes() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput = new ProjectProfileResponse();
		String projectNumber="714214";
		String taskTypes="Exception1";
		String cecId ="sashwin";
		responseHolder.setContentType("application/json");
		ProjectSnapShot projectSnapShot = new ProjectSnapShot();
		projectSnapShot.setProjectNumber(projectNumber);
		projectSnapShot.setInclusionTaskType(taskTypes);
		projectSnapShot.setCecId(cecId);
		HttpVerb httpVerb = HttpVerb.POST;
		projectProfileResponseinput.getProjectSnapShot().add(projectSnapShot);
		
		responseHolder = timeEntrySrvImpl.getExceptionTasksList (httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}
	
	@Test
	public void testGetExceptionTasksListNoInput() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput = new ProjectProfileResponse();
		String projectNumber="";
		String taskTypes="Pre-Sales,Travel";
		String cecId ="sashwin";
		responseHolder.setContentType("application/json");
		ProjectSnapShot projectSnapShot = new ProjectSnapShot();
		projectSnapShot.setProjectNumber(projectNumber);
		projectSnapShot.setInclusionTaskType(taskTypes);
		projectSnapShot.setCecId(cecId);
		HttpVerb httpVerb = HttpVerb.POST;
		projectProfileResponseinput.getProjectSnapShot().add(projectSnapShot);
		
		responseHolder = timeEntrySrvImpl.getExceptionTasksList (httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}
	
	
	
	/**
	 * testgetTopLevelWBS
	 * 
	 * @throws ServerException
	 *             the server exception
	 * @throws NetworkException
	 *             the network exception
	 * @throws ClientException
	 *             the client exception
	 * @throws ParseException
	 *             the parse exception
	 */
//	@Test
//	public void testGetTopLevelWBS() throws ServerException,
//			NetworkException, ClientException, ParseException {
//		
//		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();
//
//		BasicResponseHolder responseHolder = new BasicResponseHolder();
//		ProjectProfileResponse projectProfileResponseinput = new ProjectProfileResponse();
//		String projectNumber="714214";
//		String exclusionWbsCode="1000.0";
//		String wbsName ="000";
//		responseHolder.setContentType("application/json");
//		ProjectSnapShot projectSnapShot = new ProjectSnapShot();
//		projectSnapShot.setProjectNumber(projectNumber);
//		projectSnapShot.setExclusionWbsCode(exclusionWbsCode);
//		projectSnapShot.setWbsName(wbsName);
//		HttpVerb httpVerb = HttpVerb.POST;
//		projectProfileResponseinput.getProjectSnapShot().add(projectSnapShot);
//		
//		responseHolder = timeEntrySrvImpl.getTopLevelWBS (httpVerb, projectProfileResponseinput);
//		Assert.assertNotNull(responseHolder);
//	}
	
	@Test
	public void testGetTopLevelWBSNoInput() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput = null;
		
		responseHolder.setContentType("application/json");
		HttpVerb httpVerb = HttpVerb.POST;
		responseHolder = timeEntrySrvImpl.getTopLevelWBS (httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}

	@Test
	public void testGetTopLevelWBSNoInputPid() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput = new ProjectProfileResponse();
		String projectNumber1="";
		String exclusionWbsCode="1000.0";
		String wbsName ="000";
		responseHolder.setContentType("application/json");
		ProjectSnapShot projectSnapShot = new ProjectSnapShot();
		projectSnapShot.setProjectNumber(projectNumber1);
		projectSnapShot.setExclusionWbsCode(exclusionWbsCode);
		projectSnapShot.setWbsName(wbsName);
		HttpVerb httpVerb = HttpVerb.POST;
		projectProfileResponseinput.getProjectSnapShot().add(projectSnapShot);
		
		responseHolder = timeEntrySrvImpl.getTopLevelWBS (httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}

	/**
	 * testUpdateTimeCardDetails
	 * 
	 * @throws ServerException
	 *             the server exception
	 * @throws NetworkException
	 *             the network exception
	 * @throws ClientException
	 *             the client exception
	 * @throws ParseException
	 *             the parse exception
	 */
//	@Test
//	public void testUpdateTimeCardDetails() throws ServerException,
//			NetworkException, ClientException, ParseException {
//		
//		   TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();
//			BasicResponseHolder responseHolder = new BasicResponseHolder();
//			ProjectProfileResponse projectProfileResponseinput = new ProjectProfileResponse();
//			
//			String projectNumber="742704";
//			String taskNumber="A1000.23";
//			String activtyStartDate="2014/01/05";
//			String activtyEndDate="2014/01/12";
//			String cecId ="sashwin";
//			String totalHours ="55.25";
//			
//			responseHolder.setContentType("application/json");
//			ProjectSnapShot projectSnapShot = new ProjectSnapShot();
//			projectSnapShot.setProjectNumber(projectNumber);
//			projectSnapShot.setTaskNumber(taskNumber);
//			projectSnapShot.setCecId(cecId);
//			projectSnapShot.setActivityStartDate(activtyStartDate);
//			projectSnapShot.setActivityEndDate(activtyEndDate);
//			projectSnapShot.setTotalHours(totalHours);
//			HttpVerb httpVerb = HttpVerb.POST;
//			projectProfileResponseinput.getProjectSnapShot().add(projectSnapShot);
//			
//			responseHolder = timeEntrySrvImpl.updateTimeCardDetails(httpVerb, projectProfileResponseinput);
//			Assert.assertNotNull(responseHolder);
//			}
	
	/**
	 * Utility main method. Runs the test cases defined in this test class.
	 * 
	 * Usage: java TimeEntryPrimaveraServiceImplTest
	 * 
	 * @param args
	 *            command line arguments are not needed
	 * @author Parasoft Jtest 9.5
	 */
	public static void main(String[] args) {
		// junit.textui.TestRunner will print the test results to stdout.

		org.junit.runner.JUnitCore
				.main("com.cisco.dcpte.service.impl.TimeEntryPrimaveraServiceImplTest");
	}
	
	@Test
	public void testGetMyActivitiesNoInputData() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput =null;
	
		responseHolder.setContentType("application/json");
				
		HttpVerb httpVerb = HttpVerb.POST;
		responseHolder = timeEntrySrvImpl.getActivities (httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}
	
	@Test
	public void testGetMyActivities() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput = new ProjectProfileResponse();
		String projectNumber="714214";
		String exclusionWbsCode="1000.2";
		String cecId ="prawasth";
		String inclusionTaskType ="T & E,Exception";
		String activityName = "Test";	
		
		responseHolder.setContentType("application/json");
		ProjectSnapShot projectSnapShot = new ProjectSnapShot();
		projectSnapShot.setProjectNumber(projectNumber);
		projectSnapShot.setExclusionWbsCode(exclusionWbsCode);
		projectSnapShot.setCecId(cecId);
		projectSnapShot.setInclusionTaskType(inclusionTaskType);
		projectSnapShot.setActivityName(activityName);
		
		HttpVerb httpVerb = HttpVerb.POST;
		projectProfileResponseinput.getProjectSnapShot().add(projectSnapShot);
		
		responseHolder = timeEntrySrvImpl.getActivities (httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}

	@Test
	public void testGetMyActivitiesMadatoryFields() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		ProjectProfileResponse projectProfileResponseinput = new ProjectProfileResponse();
		String projectNumber="";
		String exclusionWbsCode="1000.2";
		String cecId ="prawasth";
		String inclusionTaskType ="T & E,Exception";
		String activityName = "Test";		
		
		responseHolder.setContentType("application/json");
		ProjectSnapShot projectSnapShot = new ProjectSnapShot();
		projectSnapShot.setProjectNumber(projectNumber);
		projectSnapShot.setExclusionWbsCode(exclusionWbsCode);
		projectSnapShot.setCecId(cecId);
		projectSnapShot.setInclusionTaskType(inclusionTaskType);
		projectSnapShot.setActivityName(activityName);
		
		HttpVerb httpVerb = HttpVerb.POST;
		projectProfileResponseinput.getProjectSnapShot().add(projectSnapShot);
		
		responseHolder = timeEntrySrvImpl.getActivities (httpVerb, projectProfileResponseinput);
		Assert.assertNotNull(responseHolder);
	}
	
	@Test
	public void testGetEmanStatus() throws ServerException,
			NetworkException, ClientException, ParseException {
		
		TimeEntryPrimaveraServiceImpl timeEntrySrvImpl = new TimeEntryPrimaveraServiceImpl();

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		responseHolder = timeEntrySrvImpl.getEmanStatus ();
		Assert.assertNotNull(responseHolder);
	}


	/**
	 * Get the class object of the class which will be tested.
	 * 
	 * @return the class which will be tested
	 * @author Parasoft Jtest 9.5
	 */
	public Class getTestedClass() {
		return TimeEntryPrimaveraServiceImplTest.class;
	}
}
