package libs;

import java.text.SimpleDateFormat;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReportUtil {

	private static ExtentReports extent;
	
	public static ExtentReports getInstance() {
		
		if (extent == null) {
			System.out.println("ExtentReportUtil getInstance called");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			// Create a new ExtentSparkReporter with a unique file name
			ExtentSparkReporter spark = new ExtentSparkReporter("target/ExtentReport_"+sdf.format(new java.util.Date())+".html");
			extent = new ExtentReports();
			extent.attachReporter(spark);
		}
		return extent;
	}	
	 
}
