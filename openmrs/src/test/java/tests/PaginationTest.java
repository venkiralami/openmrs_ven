package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import libs.FrameworkLibrary;
import pages.TablePage;
import utils.zephyr.JiraTestKey;

public class PaginationTest extends FrameworkLibrary {

	String appUrl = "https://datatables.net/examples/basic_init/alt_pagination.html";
	
    @Test
    @JiraTestKey("SCRUM-T2") // This is same as Zephyr TestCasekey
    public void verifyRecordUsingNextButtonPagination() {
        launchApplication(appUrl);
    	TablePage tablePage = new TablePage(driver);
        boolean recordFound = tablePage.searchRecordWithNextButton("Paul Byrd");
        Assert.assertTrue(recordFound, "Record not found using Next button pagination!");
    }

    @Test
    @JiraTestKey("SCRUM-T3") // 
    public void verifyRecordUsingPageNumberPagination() {
    	 launchApplication(appUrl);
        TablePage tablePage = new TablePage(driver);
        boolean recordFound = tablePage.searchRecordWithPageNumbers("Thor Walton");
        Assert.assertTrue(recordFound, "Record not found using Page number pagination!");
    }
    
    //@Test	
    @JiraTestKey("SCRUM-T1") // 
    public void verifyFailTest() {
    	
        Assert.assertTrue(false, "Record not found using Page number pagination!");
    }
}

