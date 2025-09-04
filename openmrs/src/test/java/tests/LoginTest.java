package tests;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import libs.FrameworkLibrary;
import pages.HomePage;
import pages.LoginPage;
import pages.ManageServiceTypesPage;
import utils.CustomRandomUtils;
import utils.zephyr.ZephyrCase;

public class LoginTest extends FrameworkLibrary {

	HashMap<String, String> expectedHMap = null;
	HashMap<String, String> actualHMap = null;

	@Test(enabled = false, priority = 1)
	@ZephyrCase("SCRUM-T1") 
	public void testManageServiceTypes() {
		try {
			SoftAssert softAssert = new SoftAssert();
			System.out.println("================ Login Start================================================\n");
			launchApplication(prop.getProperty("mrs_url"));
			LoginPage loginPage = new LoginPage(driver);
			loginPage.loginValidUser(prop.getProperty("mrs_admin_username"), prop.getProperty("mrs_admin_password"), prop.getProperty("mrs_session_location"));
			softAssert.assertTrue(driver.getTitle().contains("Home"));
			
			System.out.println("================ Home Start ================================================\n");
			HomePage homePage = new HomePage(driver);
			homePage.navigateToAModule("appointmentschedulingui-homeAppLink-appointmentschedulingui-homeAppLink-extension");
			softAssert.assertTrue(driver.getTitle().contains("OpenMRS Electronic Medical Record") || driver.getTitle().contains("Appointment Scheduling"));

			homePage.navigateToAModule("appointmentschedulingui-manageAppointmentTypes-app");
			softAssert.assertTrue(driver.getTitle().contains("OpenMRS Electronic Medical Record"));
			
			System.out.println("================ Manage Service Start ================================================\n");
			ManageServiceTypesPage manageServiceTypesPage = new ManageServiceTypesPage(driver);
			manageServiceTypesPage.clickOnNewServiceType();
			
			System.out.println("================ New Service Type Start ================================================\n");
			String ranChar = CustomRandomUtils.generateRandomString(3);
			String serviceName = "Gynecology Consultation Ven-"+ranChar;
			String duration = String.valueOf(CustomRandomUtils.generateRandomNumber(10,99));
			String description = "Gynecology Consultation Description Ven-"+ranChar;
			expectedHMap = manageServiceTypesPage.saveServiceTypeDetails(serviceName, duration, description);
			System.out.println("Save Expected HMap::" +expectedHMap);

			boolean serviceTypeFound = manageServiceTypesPage.searchRecordWithNextButton(expectedHMap.get("serviceName"));
			softAssert.assertTrue(serviceTypeFound, "Created Service Type details are not matching!");
			//boolean serviceTypeFoundInPage = manageServiceTypesPage.searchRecordWithPageNumbers(expectedHMap.get("serviceName"));
			//softAssert.assertTrue(serviceTypeFoundInPage, "Created Service Type details are not matching!");
			System.out.println("================ Edit Service Type Start ================================================\n");
			
			String ranCharEdit = CustomRandomUtils.generateRandomString(3);
			String serviceNameE = "Gynecology Consultation Ven-"+ranCharEdit;
			String durationE = String.valueOf(CustomRandomUtils.generateRandomNumber(10,99));
			String descriptionE = "Gynecology Consultation Description Ven-"+ranCharEdit;
			expectedHMap = manageServiceTypesPage.editServiceTypeDetails(serviceNameE, durationE, descriptionE, serviceName);
			System.out.println("Edit Expected HMap::" +expectedHMap);
			boolean b = manageServiceTypesPage.searchRecordWithNextButton(expectedHMap.get("serviceName"));
			softAssert.assertTrue(b, "Edited Service Type details are not matching!");
			
			System.out.println("================ Delete Service Type Start ================================================\n");
			boolean isDeleted = manageServiceTypesPage.deleteServiceTypeDetails(serviceNameE);
			System.out.println("Deleted? " +isDeleted);
			softAssert.assertTrue(isDeleted, "Deleted Service Type details are still present!");
			boolean b1 = manageServiceTypesPage.searchRecordWithNextButton(serviceNameE);
			softAssert.assertFalse(b1, "Deleted Service Type details are still avaiable!");
			softAssert.assertAll();
			System.out.println("================ Delete Service Type End ================================================\n");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Test failed due to exception: " + e.getMessage());
		}
	}
}

