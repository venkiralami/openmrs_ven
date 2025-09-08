package listeners;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.BeforeClass;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import libs.ExtentReportUtil;
import libs.FrameworkLibrary;
import libs.ScreenshotUtil;
import utils.zephyr.ConfigReader;
import utils.zephyr.JiraTestKey;
import utils.zephyr.ZephyrClientFactory;
import utils.zephyr.ZephyrUpdater;

public class ZephyrTestListener implements org.testng.ITestListener {

	private ExtentReports extent= ExtentReportUtil.getInstance();
	private ExtentTest test;
	private String testCycleKey = null;
	ZephyrUpdater client = ZephyrClientFactory.getClient();	

	@Override
	public void onTestStart(org.testng.ITestResult result) {

		test = extent.createTest(result.getMethod().getMethodName());
		test.assignCategory("Regression");
		test.assignAuthor("Venkat");
		test.assignDevice("Windows 10, Chrome");
		test.log(com.aventstack.extentreports.Status.INFO, "Test started"  + result.getName());
		try {
			testCycleKey = ConfigReader.getProperty("zephyr.scale.testCycleKey");
			if (testCycleKey == null || testCycleKey.isEmpty()) {
				String cycleName = "Automation Cycle by TestNG Listner " + new Date().toString(); 
				System.out.println("[Zephyr] , Creating a new test cycle as 'zephyr.scale.testCycleKey' not configured in config: "+cycleName);
				testCycleKey = client.createTestCycle(cycleName);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Test Cycle present in config Using Test Cycle Key: " + testCycleKey);
	}

	public void onTestSuccess(org.testng.ITestResult result) {
		updateZephyr(result, "Pass","Test passed:  ListenerUtility : " + result.getName());
		test.log(com.aventstack.extentreports.Status.PASS, "Test passed"+ result.getName());
	}
	public void onTestFailure(org.testng.ITestResult result) {
		updateZephyr(result, "Fail","Test failed:  ListenerUtility : " + result.getName());
		test.log(com.aventstack.extentreports.Status.FAIL, "Test failed"+ result.getName());
		Object currentObject = result.getInstance();
		WebDriver driver = ((FrameworkLibrary) currentObject).driver; 
		try {
			ScreenshotUtil.takeScreenshot(result.getName(), driver);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void onTestSkipped(org.testng.ITestResult result) {
		updateZephyr(result, "Skipped", "Test skipped:  ListenerUtility : " + result.getName());
		test.log(com.aventstack.extentreports.Status.SKIP, "Test skipped"+ result.getName());
	}
	public void onFinish(ITestContext context) {
		if (extent != null) {
			extent.flush();
		}
		System.out.println("ListenerUtility : Extent report flushed successfully.");
	}

	private void updateZephyr(ITestResult result, String status, String comments) {
		try {

			Method method = result.getMethod().getConstructorOrMethod().getMethod();
			JiraTestKey annotation = method.getAnnotation(JiraTestKey.class);
			if (annotation != null) {
				String testCaseKey = annotation.value(); // MUST be an existing test case key
				ZephyrUpdater client = ZephyrClientFactory.getClient();

				// Only update existing test case
				// if (client.testCaseExists(testCaseKey)) {
				client.updateExecutionForTestcase(testCycleKey, testCaseKey, status, comments);
				System.out.println("[Zephyr] Updated " + testCaseKey + " -> " + status+ " in cycle: "+testCycleKey+" with comments: "+comments);
				// } else {
				//    System.out.println("[Zephyr] Skipping update: Test case " + testCaseKey + " does not exist.");
				//}
			}
		} catch (Exception e) {
			System.err.println("[Zephyr] Failed to update result: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
