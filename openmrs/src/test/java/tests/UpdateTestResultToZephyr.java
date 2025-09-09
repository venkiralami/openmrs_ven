package tests;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import libs.FrameworkLibrary;
import utils.zephyr.ConfigReader;
import utils.zephyr.JiraTestKey;
import utils.zephyr.ZephyrClientFactory;
import utils.zephyr.ZephyrUpdater;

public class UpdateTestResultToZephyr extends FrameworkLibrary {

	private String testCycleKey = null;
	ZephyrUpdater client = ZephyrClientFactory.getClient();
	
	@BeforeClass
	private String getTestCycleKey() throws IOException {
		testCycleKey = ConfigReader.getProperty("zephyr.scale.testCycleKey");
		//ZephyrUpdater client = ZephyrClientFactory.getClient();	
		if (testCycleKey == null || testCycleKey.isEmpty()) {
			String cycleName = "Automation Cycle by TestNG Listner " + new Date().toString(); 
			System.out.println("[Zephyr] , Creating a new test cycle as 'zephyr.scale.testCycleKey' not configured in config: "+cycleName);
			testCycleKey = client.createTestCycle(cycleName);

		}
		System.out.println("Test Cycle present in config Using Test Cycle Key: " + testCycleKey);
		return testCycleKey;
	}		


	@DataProvider(name = "testResults")
	public Object[][] getLoginData() {
		return new Object[][]{
			// TestKey, Status, Comments
			{"SCRUM-T1", "Pass", "TC Passed -> Test result updated in test cycle: "+testCycleKey},  
			{"SCRUM-T2", "Fail", "TC Failed -> Test result updated in test cycle: "+testCycleKey},     
			{"SCRUM-T3", "Pass", "TC Passed -> Test result updated in test cycle: "+testCycleKey},                           
		};
	}

	@Test(dataProvider = "testResults")
	@JiraTestKey("SCRUM-T2") 
	private void updateZephyr(String testCaseKey, String status, String comments) {
		try {
			System.out.println("Testing with: " + testCaseKey + " | " + status+ " | " + comments+ " | " + testCycleKey);
			if (testCaseKey != null) {
				

				client.updateExecutionForTestcase(testCycleKey, testCaseKey, status, comments);
				System.out.println("[Zephyr] Updated " + testCaseKey + " -> " + status);
				if(status.equalsIgnoreCase("Fail")) {
					String bugKey = client.createBug(testCaseKey, "New Service Type creation failed", "Steps to Reproduce: \n1. Login to application \n2. Navigate to Service Types \n3. Click on Add Service Type \n4. Enter details and Save \n\nExpected Result: Service Type should be added successfully\nActual Result: Service Type addition failed");
					
					//3. Attach screenshot
					client.attachScreenshot(bugKey, "screenshots/SCRUM-16_20250907_001941.png");
					//String bugKey = client.createBug(status, comments);
					System.out.println("[Zephyr] Bug Created " + testCaseKey + " -> " + bugKey);
					client.linkBugToTest(testCaseKey, bugKey, "Relates");
					System.out.println("[Zephyr] Bug Linked " + testCaseKey + " -> " + bugKey);
				}
			}
		}
		catch (Exception e) {
			System.err.println("[Zephyr] Failed to update result: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	//@Test
	@JiraTestKey("SCRUM-T5") 
	private void linkBugToTestdd() throws IOException {
		String testKey = "SCRUM-T2";
		 String bugKey = client.createBug(testKey, "New Service Type creation failed", "Steps to Reproduce: \n1. Login to application \n2. Navigate to Service Types \n3. Click on Add Service Type \n4. Enter details and Save \n\nExpected Result: Service Type should be added successfully\nActual Result: Service Type addition failed");
			
		 System.out.println("Testing with: " + testKey + " | " + bugKey);
		 List<String> list =  client.fetchTestCases();
		 list.forEach(t->System.out.println(t));
			
		 client.linkBugToTestCase(testKey, bugKey);
		//client.linkBugToTest(testKey, bugKey, "Relates");
		System.out.println("[Zephyr] Bug Linked " + testKey + " -> " + bugKey);
		
	}
	
	//@Test
	@JiraTestKey("SCRUM-T5") 
	private void linkBugToTest() {
		String testKey = "SCRUM-T4";
		 String bugKey =  client.createBug(testKey, "New Service Type creation failed", "Steps to Reproduce: \n1. Login to application \n2. Navigate to Service Types \n3. Click on Add Service Type \n4. Enter details and Save \n\nExpected Result: Service Type should be added successfully\nActual Result: Service Type addition failed");
			
		 System.out.println("Testing with: " + testKey + " | " + bugKey);
		
		 client.linkBugToTestCase(testKey, bugKey);
		//client.linkBugToTest(testKey, bugKey, "Relates");
		System.out.println("[Zephyr] Bug Linked " + testKey + " -> " + bugKey);
		
	}

}