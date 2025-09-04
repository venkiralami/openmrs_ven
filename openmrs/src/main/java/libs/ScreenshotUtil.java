package libs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotUtil {
	/**
	 * Takes a screenshot of the current screen and saves it to the specified file path.
	 *
	 * @param filePath The path where the screenshot will be saved.
		 * @throws IOException 
	 */
	public static void takeScreenshot(String screenshotName,WebDriver driver) throws IOException {
		if (driver == null) {
			throw new IllegalArgumentException("WebDriver cannot be null");
		}
		 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String filePath = "screenshots/" + screenshotName + "_" + sdf.format(new java.util.Date()) + ".png";
		File destFile = new File(filePath);
		TakesScreenshot tsh = (TakesScreenshot) driver;
		File scrFile = tsh.getScreenshotAs(OutputType.FILE);
		System.out.println("SrcFile Screenshot saved at: " + scrFile.getAbsolutePath());	
        FileUtils.copyFile(scrFile, destFile);
		System.out.println("DestFile Screenshot saved at: " + destFile.getAbsolutePath());	
	}
}
