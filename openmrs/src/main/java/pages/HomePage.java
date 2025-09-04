package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
public class HomePage {

	protected WebDriver driver;
	
	public HomePage(WebDriver driver){
		this.driver = driver;
		if (!driver.getTitle().equals("Home")) {
			throw new IllegalStateException("This is not Home Page," + " current page is: " + driver.getCurrentUrl());
		}
	}
	public void navigateToAModule(String moduleName)
	{
		//appointmentschedulingui-homeAppLink-appointmentschedulingui-homeAppLink-extension
		driver.findElement(By.xpath( "//a[@id='" + moduleName + "']")).click();
	}

}
