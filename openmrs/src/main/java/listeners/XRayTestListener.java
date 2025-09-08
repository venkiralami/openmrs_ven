package listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.xray.XRayUpdater;
import utils.zephyr.JiraTestKey;
import utils.zephyr.ZephyrClientFactory;
import utils.zephyr.ZephyrUpdater;

import java.io.File;
import java.lang.reflect.Method;

public class XRayTestListener implements ITestListener {
	XRayUpdater xrayClient = new XRayUpdater();
	String latestExecKey = "SCRUM-75";//xrayClient.getLatestExecutionKey();
	
	@Override
	public void onTestSuccess(ITestResult result) {
		String testKey = getXrayTestKey(result);
		System.out.println("XRayTestListener : onTestSuccess Test Key: " + testKey);
		///  XRayUpdater.updateTestStatus(testKey, "PASSED", null);
		String status = "PASSED";
		String bugDescription = "Test Failed in " + result.getMethod().getMethodName();
		String screenshotPath = "screenshots/SCRUM-16_20250907_001941.png";
		//String latestExecKey = latestExecKey;
		System.out.println("XRayTestListener : Latest Execution Key: onTestSuccess " + latestExecKey);
		xrayClient.updateTestStatus1(testKey, status, null, null, latestExecKey);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		String testKey = getXrayTestKey(result);
		System.out.println("XRayTestListener : onTestFailure Test Key: " + testKey);
		String screenshotPath = "screenshots/SCRUM-16_20250907_001941.png";//captureScreenshot(result);
		String bugDescription = result.getThrowable() != null ? result.getThrowable().toString() : "Failed test";

		// Update Xray and create bug automatically
		String status = "FAILED";
		//String latestExecKey = xrayClient.getLatestExecutionKey();
		System.out.println("XRayTestListener : Latest Execution Key: onTestFailure " + latestExecKey);

		xrayClient.updateTestStatus1(testKey, status, bugDescription, screenshotPath, latestExecKey);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		String testKey = getXrayTestKey(result);

		System.out.println("XRayTestListener : onTestSkipped Test Key: " + testKey);
		// Update Xray and create bug automatically
		String status = "TODO";
		//String latestExecKey = xrayClient.getLatestExecutionKey();
		System.out.println("XRayTestListener : Latest Execution Key: onTestSkipped " + latestExecKey);
		xrayClient.updateTestStatus1(testKey, status, null, null, latestExecKey);
	}

	@Override
	public void onStart(ITestContext context) {}

	@Override
	public void onFinish(ITestContext context) {}

	// ---------------- Helpers ----------------
	private String getXrayTestKey(ITestResult result) {
		String testCaseKey = null;
		Method method = result.getMethod().getConstructorOrMethod().getMethod();
		JiraTestKey annotation = method.getAnnotation(JiraTestKey.class);
		if (annotation != null) {
			testCaseKey = annotation.value(); // MUST be an existing test case key
		}
		return testCaseKey;
	}

	private String captureScreenshot(ITestResult result) {
		try {
			String path = "screenshots/" + result.getMethod().getMethodName() + ".png";
			File screenshotFile = new File(path);
			if(!screenshotFile.getParentFile().exists()){
				screenshotFile.getParentFile().mkdirs();
			}
			// Your Selenium screenshot code here
			// ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			return path;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

