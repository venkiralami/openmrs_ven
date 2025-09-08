package tests;

import java.io.IOException;
import java.util.Date;

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

	@BeforeClass
	private String getTestCycleKey() throws IOException {
		testCycleKey = ConfigReader.getProperty("zephyr.scale.testCycleKey");
		ZephyrUpdater client = ZephyrClientFactory.getClient();	
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
			{"SCRUM-T1", "Pass", "Test result updated in test cycle: "+testCycleKey},  
			{"SCRUM-T2", "Fail", "Test result updated in test cycle: "+testCycleKey},     
			{"SCRUM-T3", "Pass", "Test result updated in test cycle: "+testCycleKey},                           
		};
	}

	@Test(dataProvider = "testResults")
	@JiraTestKey("SCRUM-T2") 
	private void updateZephyr(String testCaseKey, String status, String comments) {
		try {
			System.out.println("Testing with: " + testCaseKey + " | " + status+ " | " + comments+ " | " + testCycleKey);
			if (testCaseKey != null) {
				ZephyrUpdater client = ZephyrClientFactory.getClient();

				client.updateExecutionForTestcase(testCycleKey, testCaseKey, status, comments);
				System.out.println("[Zephyr] Updated " + testCaseKey + " -> " + status);

			}
		} catch (Exception e) {
			System.err.println("[Zephyr] Failed to update result: " + e.getMessage());
			e.printStackTrace();
		}
	}

}