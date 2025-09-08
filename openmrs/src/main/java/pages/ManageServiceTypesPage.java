package pages;

import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ManageServiceTypesPage {

	protected WebDriver driver;
	HashMap<String, String> expectedHMap = null;

	//button[normalize-space()='New service type']
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

	@FindBy(css ="table#appointmentTypesTable tbody tr")
	private List<WebElement> tableRowsList;
	
	// Locators
	private By tableRows = By.cssSelector("table#appointmentTypesTable tbody tr");
	private By nextButton = By.xpath("//a[@id='appointmentTypesTable_next']");
	private By firstButton = By.xpath("//a[@id='appointmentTypesTable_first']");
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

	// ✅ Check if First button enabled
	public boolean isFirstButtonEnabled() {
	    try {
	        WebElement firstBtn = driver.findElement(firstButton);

	        boolean isEnabled = firstBtn.isDisplayed()
	                && firstBtn.isEnabled()
	                && !firstBtn.getAttribute("class").contains("disabled");
	        return isEnabled;
	        
	    } catch (NoSuchElementException e) {
	        System.out.println("⚠️ First button not found on page");
	        return false;
	    }
	}
	
	// ✅ Reset to first page if not already there
	private void goToFirstPage() {
	    if (isFirstButtonEnabled()) {
	        WebElement firstBtn = driver.findElement(firstButton);
	        firstBtn.click();
	        waitFor(1000); // small wait to reload table
	    } else {
	        System.out.println("ℹ️ Already at first page, no action needed");
	    }
	}

	// ✅ Click First
	public void clickFirst() {
		driver.findElement(firstButton).click();
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
			
			System.out.println("Tabel rows: getTableRows: " + getTableRows().size());
			System.out.println("Tabel rows: tableRowsList: " + tableRowsList.size());
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
		while (true) {
			for (WebElement row : getTableRows()) {
				if (serviceName!=null && row.getText().contains(serviceName)) {
					System.out.println("Record found and Deleting record " + row.getText());
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

	// ✅ Search using Next button pagination always starts from Page 1 
	public boolean searchRecordWithNextButton(String searchText) {
		if (searchText == null || searchText.isEmpty()) {
			throw new IllegalArgumentException("❌ Search text cannot be null or empty");
		}

		// ✅ Ensure we start at the first page
		goToFirstPage();

		while (true) {
			for (WebElement row : getTableRows()) {
				String rowText = row.getText();
				if (rowText != null && rowText.contains(searchText)) {
					System.out.println("✅ Next Record found: " + rowText);
					return true;
				}
			}

			if (isNextButtonEnabled()) {
				clickNext();
			} else {
				break; // no more pages
			}
		}

		System.out.println("❌ Next Record not found: " + searchText);
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
