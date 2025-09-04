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

public class ManageServiceTypesTest extends FrameworkLibrary {

	HashMap<String, String> expectedHMap = null;
	HashMap<String, String> actualHMap = null;
	
	String ranChar = CustomRandomUtils.generateRandomString(3);
	String serviceNameNew = "Gynecology Consultation Ven-"+ranChar;
	String durationNew = String.valueOf(CustomRandomUtils.generateRandomNumber(10,99));
	String descriptionNew = "Gynecology Consultation Description Ven-"+ranChar;

	String ranCharEdit = CustomRandomUtils.generateRandomString(3);
	String serviceNameEdit = "Gynecology Consultation Ven-"+ranCharEdit;
	String durationEdit = String.valueOf(CustomRandomUtils.generateRandomNumber(10,99));
	String descriptionEdit = "Gynecology Consultation Description Ven-"+ranCharEdit;
	
	@Test(priority = 1)
	@ZephyrCase("SCRUM-T4") 
	public void testOpenMrsLogin() {
		try {
			SoftAssert softAssert = new SoftAssert();
			System.out.println("\n================ Login Start================================================");
			launchApplication(prop.getProperty("mrs_url"));
			LoginPage loginPage = new LoginPage(driver);
			loginPage.loginValidUser(prop.getProperty("mrs_admin_username"), prop.getProperty("mrs_admin_password"), prop.getProperty("mrs_session_location"));
			softAssert.assertTrue(driver.getTitle().contains("Home"));

			System.out.println("\n================ Home Start ================================================");
			HomePage homePage = new HomePage(driver);
			homePage.navigateToAModule("appointmentschedulingui-homeAppLink-appointmentschedulingui-homeAppLink-extension");
			softAssert.assertTrue(driver.getTitle().contains("OpenMRS Electronic Medical Record") || driver.getTitle().contains("Appointment Scheduling"));

			homePage.navigateToAModule("appointmentschedulingui-manageAppointmentTypes-app");
			softAssert.assertTrue(driver.getTitle().contains("OpenMRS Electronic Medical Record"));
						
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Test failed due to exception: " + e.getMessage());
		}
	}

	@Test(priority = 2)
	@ZephyrCase("SCRUM-T5") 
	public void testCreateManageServiceTypes() {
		try {
			SoftAssert softAssert = new SoftAssert();
			System.out.println("\n================ Manage Service Start ================================================");
			ManageServiceTypesPage manageServiceTypesPage = new ManageServiceTypesPage(driver);
			manageServiceTypesPage.clickOnNewServiceType();

			System.out.println("\n================ New Service Type Start ================================================");
			
			expectedHMap = manageServiceTypesPage.saveServiceTypeDetails(serviceNameNew, durationNew, descriptionNew);
			System.out.println("Save Expected HMap::" +expectedHMap);

			boolean serviceTypeFound = manageServiceTypesPage.searchRecordWithNextButton(expectedHMap.get("serviceName"));
			softAssert.assertTrue(serviceTypeFound, "Created Service Type details are not matching!");
			//boolean serviceTypeFoundInPage = manageServiceTypesPage.searchRecordWithPageNumbers(expectedHMap.get("serviceName"));
			//softAssert.assertTrue(serviceTypeFoundInPage, "Created Service Type details are not matching!");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Test failed due to exception: " + e.getMessage());
		}
	}

	@Test(priority = 3)
	@ZephyrCase("SCRUM-T6") 
	public void testEditManageServiceTypes() {
		try {
			SoftAssert softAssert = new SoftAssert();
			ManageServiceTypesPage manageServiceTypesPage = new ManageServiceTypesPage(driver);
			System.out.println("\n================ Edit Service Type Start ================================================");

			expectedHMap = manageServiceTypesPage.editServiceTypeDetails(serviceNameEdit, durationEdit, descriptionEdit, serviceNameNew);
			System.out.println("Edit Expected HMap::" +expectedHMap);
			boolean b = manageServiceTypesPage.searchRecordWithNextButton(serviceNameEdit);
			softAssert.assertTrue(b, "Edited Service Type details are not matching!");
			boolean b1 = manageServiceTypesPage.searchRecordWithNextButton(serviceNameNew);
			softAssert.assertFalse(b1, "Edited Service Type details are not correct!");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Test failed due to exception: " + e.getMessage());
		}
	}
	
	@Test(priority = 4)
	@ZephyrCase("SCRUM-T7") 
	public void testDeleteManageServiceTypes() {
		try {
			SoftAssert softAssert = new SoftAssert();
			ManageServiceTypesPage manageServiceTypesPage = new ManageServiceTypesPage(driver);
			System.out.println("\n================ Delete Service Type Start ================================================"+serviceNameEdit);
			boolean b1 = manageServiceTypesPage.searchRecordWithNextButton(serviceNameEdit);
			softAssert.assertTrue(b1, "Deleted Service Type details are still avaiable!");
			boolean isDeleted = manageServiceTypesPage.deleteServiceTypeDetails(serviceNameEdit);
			System.out.println("Deleted? " +isDeleted);
			softAssert.assertTrue(isDeleted, "Deleted Service Type details are still present!");
			Thread.sleep(5000);
			boolean b2 = manageServiceTypesPage.searchRecordWithNextButton(serviceNameEdit);
			softAssert.assertFalse(b2, "Deleted Service Type details are still avaiable!");
			softAssert.assertAll();
			System.out.println("\n================ Delete Service Type End ================================================\n");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Test failed due to exception: " + e.getMessage());
		}
	}

}

