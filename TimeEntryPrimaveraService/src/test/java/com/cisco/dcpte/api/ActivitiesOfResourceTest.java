package com.cisco.dcpte.api;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cisco.dcpte.model.ErrorDetail;
import com.cisco.dcpte.model.ProjectProfileResponse;
import com.primavera.ServerException;
import com.primavera.integration.client.ClientException;
import com.primavera.integration.network.NetworkException;

public class ActivitiesOfResourceTest {


	/**
	 * Constructor for test class.
	 * 
	 * @author Parasoft Jtest 9.5
	 */
	public ActivitiesOfResourceTest() {
		/*
		 * This constructor should not be modified. Any initialization code
		 * should be placed in the setUp() method instead.
		 */

	}

	/**
	 * Used to set up the test. This method is called by JUnit before each of
	 * the tests are executed.
	 *
	 * @throws Exception the exception
	 * @author Parasoft Jtest 9.5
	 */
	@Before
	public void setUp() throws Exception {
		/*
		 * Add any necessary initialization code here (e.g., open a socket).
		 * Call Repository.putTemporary() to provide initialized instances of
		 * objects to be used when testing.
		 */

	}

	/**
	 * Used to clean up after the test. This method is called by JUnit after
	 * each of the tests have been completed.
	 *
	 * @throws Exception the exception
	 * @author Parasoft Jtest 9.5
	 */
	@After
	public void tearDown() throws Exception {
		try {
			/*
			 * Add any necessary cleanup code here (e.g., close a socket).
			 */
		} finally {
		}
	}

	/**
	 * Utility main method. Runs the test cases defined in this test class.
	 * 
	 * Usage: java WbsDetailsByPidTest
	 * 
	 * @param args
	 *            command line arguments are not needed
	 * @author Parasoft Jtest 9.5
	 */
	public static void main(String[] args) {
		// junit.textui.TestRunner will print the test results to stdout.

		org.junit.runner.JUnitCore
				.main("com.cisco.primavera.api.ActivitiesOfResourceTest");
	}
	
	/**
	 * testGetExceptionTasksByPid.
	 * @throws ClientException 
	 * @throws NetworkException 
	 * @throws ServerException 
	 */
	@Test
	public void testGetActivitiesOfResourceByPid() throws ServerException, NetworkException, ClientException {
		
		String projectNumber ="714214";//pid 731765--dv1spm for customer info  //
		String cecId = "prawasth";
//		String inclusionTaskType = "Pre-Sales,Travel";
		String inclusionTaskType = "";
		String exclusionWbsCodes ="";
		String activityName ="";
		String myActivityFlag ="";
		ActivitiesOfResource rsc = new ActivitiesOfResource();
		ProjectProfileResponse response = rsc.getActivitiesForResource(cecId, projectNumber, inclusionTaskType, exclusionWbsCodes, activityName,myActivityFlag);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	
	@Test
	public void testGetActivitiesOfResourceByActivityFlag() throws ServerException, NetworkException, ClientException {
		
		String projectNumber ="781985";//pid 731765--dv1spm for customer info  //
		String cecId = "sinpotnu";
		String inclusionTaskType = "";
		String exclusionWbsCodes ="";
		String activityName ="";
		String myActivityFlag ="y";
		ActivitiesOfResource rsc = new ActivitiesOfResource();
		ProjectProfileResponse response = rsc.getActivitiesForResource(cecId, projectNumber, inclusionTaskType, exclusionWbsCodes, activityName,myActivityFlag);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	
	@Test
	public void testGetErrorDetailsForInvalidPid() throws ServerException, NetworkException, ClientException {
		
		String projectNumber ="71421457";//pid 731765--dv1spm for customer info  //
		String cecId = "prawasth";
		String inclusionTaskType = "Pre-Sales,Travel";
		String exclusionWbsCodes ="";
		String activityName ="";
		String myActivityFlag ="";
		ActivitiesOfResource rsc = new ActivitiesOfResource();
		ProjectProfileResponse response = rsc.getActivitiesForResource(cecId, projectNumber, inclusionTaskType, exclusionWbsCodes, activityName,myActivityFlag);			
		ErrorDetail errorDetail = response.getErrorDetail().get(0);
		
		if ((errorDetail) instanceof ErrorDetail) {
			Assert.assertNotNull( response.getErrorDetail().get(0));

		}
	}
	
	@Test
	public void testGetActivitiesOfResourcebyPid() throws ServerException, NetworkException, ClientException {
		
		String projectNumber ="714214";
		String cecId = "prawasth";
		String inclusionTaskType = "T & E,Exception";
		String exclusionWbsCodes ="1000.2";
		String activityName ="Test";
		String myActivityFlag ="";
		ActivitiesOfResource rsc = new ActivitiesOfResource();
		ProjectProfileResponse response = rsc.getActivitiesForResource(cecId, projectNumber, inclusionTaskType, exclusionWbsCodes, activityName,myActivityFlag);			
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	
	@Test
	public void testGetActivitiesOfResourcebyExclusionWbsList() throws ServerException, NetworkException, ClientException {
		
		String projectNumber ="714214";
		String cecId = "prawasth";
		String inclusionTaskType = "T & E,Exception";
		String exclusionWbsCodes ="4000.2,4000.3";
		String activityName = null;
		String myActivityFlag ="";
		ActivitiesOfResource rsc = new ActivitiesOfResource();
		ProjectProfileResponse response = rsc.getActivitiesForResource(cecId, projectNumber, inclusionTaskType, exclusionWbsCodes, activityName,myActivityFlag);			
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	
	@Test
	public void testGetActivitiesOfResourcebyNoMatches() throws ServerException, NetworkException, ClientException {
		
		String projectNumber ="714214";
		String cecId = "prawasth1";
		String inclusionTaskType = "T & E,Exception";
		String exclusionWbsCodes ="1000.2";
		String activityName ="Test";
		String myActivityFlag ="";
		ActivitiesOfResource rsc = new ActivitiesOfResource();
		ProjectProfileResponse response = rsc.getActivitiesForResource(cecId, projectNumber, inclusionTaskType, exclusionWbsCodes, activityName,myActivityFlag);			
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	@Test
	public void testGetActivitiesOfResourcebyTskTypeEmpty() throws ServerException, NetworkException, ClientException {
		
		String projectNumber ="714214";
		String cecId = "prawasth1";
		String inclusionTaskType = "T & E,Exception,Hardware";
		String exclusionWbsCodes =null;
		String activityName =null;
		String myActivityFlag ="";
		ActivitiesOfResource rsc = new ActivitiesOfResource();
		ProjectProfileResponse response = rsc.getActivitiesForResource(cecId, projectNumber, inclusionTaskType, exclusionWbsCodes, activityName,myActivityFlag);			
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	
	
	
	/**
	 * Get the class object of the class which will be tested.
	 * 
	 * @return the class which will be tested
	 * @author Parasoft Jtest 9.5
	 */
	public Class<ActivitiesOfResourceTest> getTestedClass() {
		return ActivitiesOfResourceTest.class;
	}

}
