package libs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class FrameworkLibrary {

	public WebDriver driver;
	protected Properties prop = new Properties();
    @BeforeClass
	public void setUp() throws IOException {
		readPropertyFile("mrs_global.properties");

		System.out.println("Browser:: "+ prop.getProperty("browsertype") + " Environment:: "+ prop.getProperty("environment"));

		switch (prop.getProperty("browsertype").toLowerCase()) {
		case "chrome":
			driver = new ChromeDriver();
			break;
		case "firefox":
			driver = new FirefoxDriver();
			break;
		}
		
		switch (prop.getProperty("environment").toLowerCase()) {
		case "qa":
			readPropertyFile("mrs_qa.properties");
			break;
		case "dev":
			readPropertyFile("mrs_dev.properties");
			break;
		}
		
		//launchApplication(prop.getProperty("patient_url"));

	}

	public  void readPropertyFile(String fileName) throws IOException {

		String filePath = System.getProperty("user.dir") + "/config/" + fileName;
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		prop.load(fis);
		System.out.println("Prop file loaded successfully: " + filePath);
	}
	
	public  void launchApplication(String url) {
		driver.get(url);
		driver.manage().window().maximize();
		driver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(3));
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

	}

	@AfterClass
	public void tearDown() throws Exception {
		if(driver!=null) {
			Thread.sleep(5000);
			driver.quit();
		}
	}
	
}
