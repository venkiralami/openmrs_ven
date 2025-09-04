package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage {

    private WebDriver driver;

    // 🔹 WebElements
    @FindBy(id = "username")
    private WebElement txtUsername;

    @FindBy(id = "password")
    private WebElement txtPassword;

    @FindBy(xpath = "//li[@id='Inpatient Ward']")
    private WebElement selectSessionLocation;

    @FindBy(id = "loginButton")
    private WebElement btnLogin;
    
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        
        if (!driver.getTitle().equals("Login")) {
  	      throw new IllegalStateException("This is Login Page," + " current page is: " + driver.getCurrentUrl());
  	    }
    }

    public void selectSessionLocation(String sessionLocation) {
		
		driver.findElement(By.xpath("//li[@id='" + sessionLocation + "']")).click();
	}
	
    public HomePage loginValidUser(String username, String password, String sessionLocation) {
    	 txtUsername.clear();
         txtUsername.sendKeys(username);
         txtPassword.clear();
         txtPassword.sendKeys(password);
         selectSessionLocation(sessionLocation);
         btnLogin.click();
	    return new HomePage(driver);
	  }
}
