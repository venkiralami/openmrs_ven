package pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
public class LoginPage2 {
	
	  protected WebDriver driver;
	  private By usernameBy = By.id("username");
	  private By passwordBy = By.id("password");
	  private By inpatientWardBy = By.xpath("//li[@id='Inpatient Ward']");
	  private By signinBy = By.xpath("//input[@value='Sign In']");

	  public LoginPage2(WebDriver driver){
	    this.driver = driver;
	     if (!driver.getTitle().equals("Login")) {
	      throw new IllegalStateException("This is Login Page," +
	            " current page is: " + driver.getCurrentUrl());
	    }
	  }
	  public HomePage loginValidUser(String userName, String password) {
	    driver.findElement(usernameBy).sendKeys(userName);
	    driver.findElement(passwordBy).sendKeys(password);
	    driver.findElement(signinBy).click();
	    return new HomePage(driver);
	  }

}
  