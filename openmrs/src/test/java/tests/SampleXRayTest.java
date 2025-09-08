package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import libs.FrameworkLibrary;
import utils.zephyr.JiraTestKey;

public class SampleXRayTest  extends FrameworkLibrary  {

    @Test
    @JiraTestKey("SCRUM-14")
    public void loginTest() {
        // test code
    	System.out.println("Login test executed loginTest");
    	Assert.assertTrue(true);
    }

    @Test
    @JiraTestKey("SCRUM-16")
    public void failedTest() {
        // throw new RuntimeException("Login button missing");
    	System.out.println("Failed test executed failedTest");
    	Assert.assertTrue(false);
    }
}
