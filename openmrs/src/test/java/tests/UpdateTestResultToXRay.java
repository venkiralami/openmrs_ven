package tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import libs.FrameworkLibrary;
import utils.xray.XRayUpdater;
import utils.xray.XrayJiraAutomationHelper;
import utils.zephyr.ConfigReader;
import utils.zephyr.JiraTestKey;
import utils.zephyr.ZephyrClientFactory;
import utils.zephyr.ZephyrUpdater;

public class UpdateTestResultToXRay extends FrameworkLibrary {

	XRayUpdater xrayUpdater = new XRayUpdater();
	private String testExecutionKey = null;


	@BeforeClass
	private String getTestExecutionKey() throws IOException {
		testExecutionKey = ConfigReader.getProperty("xray.testExecutionKey");
		if (testExecutionKey == null || testExecutionKey.isEmpty()) {
			
			System.out.println("[XRay] , Creating a new test Execution Key as 'xray.testExecutionKey' not configured in config: "+testExecutionKey);
			
			testExecutionKey = "SCRUM-17"; //xrayClient.getLatestExecutionKey();

		}
		System.out.println("Test Execution Key present in config : " + testExecutionKey);
		return testExecutionKey;
	}	


	@DataProvider(name = "testResults")
	public Object[][] getLoginData() {
		return new Object[][]{
			// TestKey, Status, Comments
			{"SCRUM-13", "PASSED", "Test result updated in test Execution Key: "+testExecutionKey,null,testExecutionKey},  
			{"SCRUM-14", "FAILED", "Test result updated in test Execution Key: "+testExecutionKey,"screenshots\\testManageServiceTypes_2025-09-03_18-23-55.png",testExecutionKey},     
			{"SCRUM-15", "PASSED", "Test result updated in test Execution Key: "+testExecutionKey,null,testExecutionKey},   
			{"SCRUM-16", "PASSED", "Test result updated in test Execution Key: "+testExecutionKey,null,testExecutionKey},   
			{"SCRUM-18", "FAILED", "Test result updated in test Execution Key: "+testExecutionKey,"screenshots\\testManageServiceTypes_2025-09-03_18-34-48.png",testExecutionKey}     
		};
	}

	@Test(dataProvider = "testResults")
	@JiraTestKey("SCRUM-15")
	private void updateXrayResults(String testKey, String status, String comments, String screenshotPath, String executionKey) {
		try {
			System.out.println("Testing with: " + testKey + " | " + status+ " | " + comments+ " | " + screenshotPath+ " | " + executionKey);
			if (testKey != null) {

				xrayUpdater.updateTestStatus1(testKey, status, comments, screenshotPath, executionKey);
				System.out.println("[XRay] Updated " + testKey + " -> " + status);

			}
		} catch (Exception e) {
			System.err.println("[XRay] Failed to update result: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	@JiraTestKey("SCRUM-18")
	public  void testXray() throws Exception {

		// 2️⃣ Read Excel results
		List<Map<String, String>> results = readExcelResults("resources\\results.xlsx");

		// 2. For failed test, create bug
		String bugKey = xrayUpdater.createBug("SCRUM-16", "Login failed", "Login button not working");
		System.out.println("Created Bug: " + bugKey);
		
		//3. Attach screenshot
		xrayUpdater.attachScreenshot(bugKey, "screenshots/SCRUM-16_20250907_001941.png");
		// 📝 Update that execution in Xray
		
		xrayUpdater.importExecution(results, testExecutionKey);
		//xrayClient.linkBugToTest(jiraUser, jiraApiToken, bugKey, "SCRUM-16");
		
		xrayUpdater.linkBugToTest("SCRUM-16", bugKey, "Relates");
		System.out.println("Test execution uploaded successfully!");
	}

	// Read test results from Excel
	private static List<Map<String, String>> readExcelResults(String filePath) throws Exception {
		List<Map<String, String>> results = new ArrayList<>();
		FileInputStream fis = new FileInputStream(filePath);
		Workbook workbook = new XSSFWorkbook(fis);
		Sheet sheet = workbook.getSheetAt(0);
		Row headerRow = sheet.getRow(0);

		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row == null) continue;

			Map<String, String> rowData = new HashMap<>();
			for (int j = 0; j < headerRow.getLastCellNum(); j++) {
				String key = headerRow.getCell(j).getStringCellValue();
				Cell cell = row.getCell(j);
				String value = (cell != null) ? cell.toString() : "";
				rowData.put(key, value);
			}
			results.add(rowData);
		}

		workbook.close();
		return results;
	}
}