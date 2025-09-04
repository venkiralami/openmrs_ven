package listeners;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import libs.ExtentReportUtil;
import libs.FrameworkLibrary;
import libs.ScreenshotUtil;
import utils.zephyr.ConfigReader;
import utils.zephyr.ZephyrCase;
import utils.zephyr.ZephyrClientFactory;
import utils.zephyr.ZephyrUpdater;

public class ListenerUtility implements org.testng.ITestListener {

	private ExtentReports extent= ExtentReportUtil.getInstance();
	private ExtentTest test;

	@Override
	public void onTestStart(org.testng.ITestResult result) {
		System.out.println("Test started: ListenerUtility : " + result.getName());
		// TODO Auto-generated method stub
		test = extent.createTest(result.getMethod().getMethodName());
		test.assignCategory("Regression");
		test.assignAuthor("Venkat");
		test.assignDevice("Windows 10, Chrome");
		test.log(com.aventstack.extentreports.Status.INFO, "Test started");
	}
	public void onTestSuccess(org.testng.ITestResult result) {
		System.out.println("Test passed:  ListenerUtility : " + result.getName());
		updateZephyr(result, "Pass");
		test.log(com.aventstack.extentreports.Status.PASS, "Test passed");
	}
	public void onTestFailure(org.testng.ITestResult result) {
		System.out.println("Test failed:  ListenerUtility : " + result.getName());	 
		updateZephyr(result, "Fail");
		test.log(com.aventstack.extentreports.Status.FAIL, "Test failed");
		Object currentObject = result.getInstance();
		WebDriver driver = ((FrameworkLibrary) currentObject).driver; 
		try {
			ScreenshotUtil.takeScreenshot(result.getName(), driver);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void onTestSkipped(org.testng.ITestResult result) {
		System.out.println("Test skipped:  ListenerUtility : " + result.getName());
		updateZephyr(result, "Skipped");
		test.log(com.aventstack.extentreports.Status.SKIP, "Test skipped");
	}
	public void onFinish(ITestContext context) {
		if (extent != null) {
			extent.flush();
		}
		System.out.println("ListenerUtility : Extent report flushed successfully.");
	}
	
	private void updateZephyr(ITestResult result, String status) {
        try {
        	
            Method method = result.getMethod().getConstructorOrMethod().getMethod();
            ZephyrCase annotation = method.getAnnotation(ZephyrCase.class);
            if (annotation != null) {
                String testCaseKey = annotation.value(); // MUST be an existing test case key
                ZephyrUpdater client = ZephyrClientFactory.getClient();
                String testCycleKey = ConfigReader.getProperty("zephyr.scale.testCycleKey");
                System.out.println("Test Cycle Key from config: " + testCycleKey);
                if (testCycleKey == null || testCycleKey.isEmpty()) {
    				System.out.println("[Zephyr] Test cycle key not configured. Create a test cycle in Zephyr Scale and set 'zephyr.scale.testCycleKey' in config.");
					/*
					 * String cycleName = "Automation Cycle by TestNG Listner " + new
					 * Date().toString(); testCycleKey = client.createTestCycle(cycleName);
					 */
    			}
                // Only update existing test case
               // if (client.testCaseExists(testCaseKey)) {
                    client.updateExecutionForTestcase(testCycleKey, testCaseKey, status);
                    System.out.println("[Zephyr] Updated " + testCaseKey + " -> " + status);
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
