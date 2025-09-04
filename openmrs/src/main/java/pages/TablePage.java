package pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TablePage {
   
	private WebDriver driver;

    // Locators
    private By tableRows = By.cssSelector("table#example tbody tr");
    private By nextButton = By.xpath("//button[@aria-label='Next']");
    private By pageNumbers = By.xpath("//button[@class='dt-paging-button']");

    public TablePage(WebDriver driver) {
        this.driver = driver;
        if (!driver.getTitle().equals("DataTables example - Alternative pagination")) {
  	      throw new IllegalStateException("This is DataTables example Page," +
  	            " current page is: " + driver.getCurrentUrl());
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

    // ✅ Search using Next button pagination
    public boolean searchRecordWithNextButton(String searchText) {
        while (true) {
            for (WebElement row : getTableRows()) {
                if (row.getText().contains(searchText)) {
                System.out.println("Record found: " + row.getText());
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

    // ✅ Search using Page Number pagination
    public boolean searchRecordWithPageNumbers(String searchText) {
        List<WebElement> pages = driver.findElements(pageNumbers);
        System.out.println("Total pages: " + pages.size());
        for (int i = 0; i < pages.size(); i++) {
            pages = driver.findElements(pageNumbers); // refresh list
           // System.out.println("Navigating to page: " + i );
           System.out.println("Clicking on page: " + (i + 1));
          // System.out.println("Navigating to page: " + i );
           pages.get(i).click();
           waitFor(2000);

            for (WebElement row : getTableRows()) {
				if (row.getText().contains(searchText)) {
					System.out.println("Record found: " + row.getText()+" on page: " + (i+1));
					return true;
				}
			}
        }
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

