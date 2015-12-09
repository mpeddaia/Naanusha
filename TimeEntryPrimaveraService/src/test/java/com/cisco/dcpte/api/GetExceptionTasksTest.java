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

public class GetExceptionTasksTest {


	/**
	 * Constructor for test class.
	 * 
	 * @author Parasoft Jtest 9.5
	 */
	public GetExceptionTasksTest() {
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
				.main("com.cisco.primavera.api.GetExceptionTasksTest");
	}
	
	/**
	 * testGetExceptionTasksByPid.
	 * @throws ClientException 
	 * @throws NetworkException 
	 * @throws ServerException 
	 */
	@Test
	public void testGetExceptionTasksByPid() throws ServerException, NetworkException, ClientException {
		
		String projectNumber ="714214";//pid 731765--dv1spm for customer info  //
		String cecId = "sashwin";
		String taskType = "Pre-Sales,Travel";
		String transId = "";
		ProjectProfileResponse response = GetExceptionTasks.getExceptionTasks(projectNumber,cecId,taskType, transId);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	
	@Test
	public void testGetErrorDetailsForInvalidPid() throws ServerException, NetworkException, ClientException {
		
		String projectNumber ="71421457";//pid 731765--dv1spm for customer info  //
		String cecId = "sashwin";
		String taskType = null;
		String transId = "";
		ProjectProfileResponse response = GetExceptionTasks.getExceptionTasks(projectNumber,cecId,taskType, transId);	
		ErrorDetail errorDetail = response.getErrorDetail().get(0);
		
		if ((errorDetail) instanceof ErrorDetail) {
			Assert.assertNotNull( response.getErrorDetail().get(0));

		}
	}
	
	@Test
	public void testGetErrorDetailsForExceptionTaskbyPid() throws ServerException, NetworkException, ClientException {
		
		String projectNumber ="753775";//pid 731765--dv1spm for customer info  //
		String cecId = "sashwin";
		String taskType =null;
		String transId = "";
        ProjectProfileResponse response = GetExceptionTasks.getExceptionTasks(projectNumber,cecId,taskType,transId);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	
	@Test
	public void testGetErrorDetailsForPidNull() throws ServerException, NetworkException, ClientException {
		
		String projectNumber =null;
		String cecId = "sashwin";
		String taskType = "Pre-Sales,Travel";
		String transId = "";
		ProjectProfileResponse response = GetExceptionTasks.getExceptionTasks(projectNumber,cecId,taskType,transId);	
		ErrorDetail errorDetail = response.getErrorDetail().get(0);
		
		if ((errorDetail) instanceof ErrorDetail) {
			Assert.assertNotNull( response.getErrorDetail().get(0));

		}
	}
	/**
	 * Get the class object of the class which will be tested.
	 * 
	 * @return the class which will be tested
	 * @author Parasoft Jtest 9.5
	 */
	public Class<GetExceptionTasksTest> getTestedClass() {
		return GetExceptionTasksTest.class;
	}

}
