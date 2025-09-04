package pages;

import java.util.HashMap;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ManageServiceTypesPage {

	protected WebDriver driver;
	HashMap<String, String> expectedHMap = null;

	@FindBy(xpath = "//button[@class='confirm appointment-type-label right']")
	private WebElement btnNewServiceType;

	@FindBy(id = "name-field")
	private WebElement txtName;

	@FindBy(id = "duration-field")
	private WebElement txtDuration;

	@FindBy(id = "description-field")
	private WebElement txtAreaDescription;

	@FindBy(id = "save-button")
	private WebElement btnSave;

	@FindBy(xpath = "//input[@class='cancel']")
	private WebElement btnCancel;

	// Locators
	private By tableRows = By.cssSelector("table#appointmentTypesTable tbody tr");
	private By nextButton = By.xpath("//a[@id='appointmentTypesTable_next']");
	private By pageNumbers = By.xpath("//a[@class='fg-button ui-button ui-state-default']");

	public ManageServiceTypesPage(WebDriver driver){
		this.driver = driver;
		PageFactory.initElements(driver, this);
		if (!driver.getTitle().equals("OpenMRS Electronic Medical Record")) {
			throw new IllegalStateException("This is not Manage Service Types Page," + " current page is: " + driver.getCurrentUrl());
		}
	}
	// ✅ Get all rows
	public List<WebElement> getTableRows() {
		return driver.findElements(tableRows);
	}

	// ✅ Check if Next button enabled
	public boolean isNextButtonEnabled() {
		WebElement nextBtn = driver.findElement(nextButton);
		return nextBtn.isDisplayed() && nextBtn.isEnabled() && !nextBtn.getAttribute("class").contains("disabled");
	}

	// ✅ Click Next
	public void clickNext() {
		driver.findElement(nextButton).click();
	}

	public void clickOnNewServiceType()
	{
		btnNewServiceType.click();
	}

	public HashMap<String, String> saveServiceTypeDetails(String name, String duration, String description)
	{
		txtName.clear();
		txtName.sendKeys(name);
		txtDuration.clear();
		txtDuration.sendKeys(duration);
		txtAreaDescription.clear();
		txtAreaDescription.sendKeys(description);

		expectedHMap = new HashMap<String, String>();
		expectedHMap.put("serviceName", txtName.getDomProperty("value"));
		expectedHMap.put("serviceDuration", txtDuration.getDomProperty("value"));
		expectedHMap.put("serviceDesc", txtAreaDescription.getDomProperty("value"));

		btnSave.click();
		return expectedHMap;
	}

	public HashMap<String, String> editServiceTypeDetails(String name, String duration, String description,String existingService){
		expectedHMap = new HashMap<String, String>();
		System.out.println("Editing the record: " + existingService);
		while (true) {
			for (WebElement row : getTableRows()) {
				if (existingService!=null && row.getText().contains(existingService)) {
					System.out.println("Record found: editServiceTypeDetails : " + row.getText());
					driver.findElement(By.xpath("//i[@id='appointmentschedulingui-edit-"+ existingService +"']")).click();
					txtName.clear();
					txtName.sendKeys(name);
					txtDuration.clear();
					txtDuration.sendKeys(duration);
					txtAreaDescription.clear();
					txtAreaDescription.sendKeys(description);

					expectedHMap = new HashMap<String, String>();
					expectedHMap.put("serviceName", txtName.getDomProperty("value"));
					expectedHMap.put("serviceDuration", txtDuration.getDomProperty("value"));
					expectedHMap.put("serviceDesc", txtAreaDescription.getDomProperty("value"));

					btnSave.click();
					return expectedHMap;
				}
			}

			if (isNextButtonEnabled()) {
				clickNext();
				waitFor(1000);
			} else {
				break;
			}
		}
		return expectedHMap;
	}

	public boolean deleteServiceTypeDetails(String serviceName){
		expectedHMap = new HashMap<String, String>();
		System.out.println("Deleting the record: " + serviceName);
		while (true) {
			for (WebElement row : getTableRows()) {
				if (serviceName!=null && row.getText().contains(serviceName)) {
					System.out.println("Record found: deleteServiceTypeDetails : " + row.getText());
					driver.findElement(By.xpath("//i[@id='appointmentschedulingui-delete-"+ serviceName +"']")).click();

					driver.findElement(By.xpath("//div[@class='simplemodal-wrap']//button[@class='confirm right'][normalize-space()='Yes']")).click();
					return true;
				}
			}

			if (isNextButtonEnabled()) {
				clickNext();
				waitFor(1000);
			} else {
				break;
			}
		}
		return false;
	}

	// ✅ Search using Next button pagination
	public boolean searchRecordWithNextButton(String searchText) {
		waitFor(5000);
		while (true) {
			for (WebElement row : getTableRows()) {
				if (searchText!=null && row.getText().toString().contains(searchText)) {
					System.out.println("Next : Record found: " + row.getText());
					return true;
				}
			}

			if (isNextButtonEnabled()) {
				clickNext();
				waitFor(1000);
			} else {
				break;
			}
		}
		System.out.println("Page : Record not found in this row: " + searchText);
		return false;
	}

	// ✅ Search using Page Number pagination
	public boolean searchRecordWithPageNumbers(String searchText) {
		List<WebElement> pages = driver.findElements(pageNumbers);
		//System.out.println("Total pages: " + pages.size());
		for (int i = 0; i < pages.size(); i++) {
			pages = driver.findElements(pageNumbers); // refresh list
			// System.out.println("Navigating to page: " + i );
			// System.out.println("Page : Clicking on page: " + (i + 1));
			// System.out.println("Navigating to page: " + i );
			pages.get(i).click();
			waitFor(3000);

			for (WebElement row : getTableRows()) {
				if (searchText!=null && row.getText().contains(searchText)) {
					System.out.println("Page : Record found: " + row.getText()+" on page: " + (i+1));
					return true;
				}
			}
		}
		System.out.println("Page : Record not found in this row: " + searchText);
		return false;
	}

	private void waitFor(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



}
