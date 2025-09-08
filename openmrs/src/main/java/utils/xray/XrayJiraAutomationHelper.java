package utils.xray;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import utils.zephyr.ConfigReader;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class XrayJiraAutomationHelper {

    private static final String JIRA_BASE = ConfigReader.getProperty("jira.url");
    private static final String XRAY_BASE = ConfigReader.getProperty("xray.baseUri");
    private static final String PROJECT_KEY = ConfigReader.getProperty("project.key");

    /**
     * Create a Jira Cloud Bug for failed test case
     */
    public static String createBug(String apiToken, String summary, String description) {
        JSONObject payload = new JSONObject()
                .put("fields", new JSONObject()
                        .put("project", new JSONObject().put("key", PROJECT_KEY))
                        .put("summary", summary)
                        .put("description", description)
                        .put("issuetype", new JSONObject().put("name", "Bug"))
                );

        Response response = RestAssured.given()
                .baseUri(JIRA_BASE)
                .header("Authorization", "Basic " + apiToken)
                .header("Content-Type", "application/json")
                .body(payload.toString())
                .post("/rest/api/3/issue");

        if (response.statusCode() != 201) {
            throw new RuntimeException("❌ Failed to create Bug: " + response.getBody().asString());
        }

        String bugKey = response.jsonPath().getString("key");
        System.out.println("✅ Bug created: " + bugKey);
        return bugKey;
    }

    /**
     * Attach screenshot to Jira Cloud Bug
     */
    public static void attachScreenshotToBug(String apiToken, String bugKey, String screenshotPath) throws Exception {
        File screenshot = new File(screenshotPath);
        if (!screenshot.exists()) {
            throw new RuntimeException("❌ Screenshot file not found: " + screenshot.getAbsolutePath());
        }

        Response response = RestAssured.given()
                .baseUri(JIRA_BASE)
                .header("Authorization", "Basic " + apiToken)
                .header("X-Atlassian-Token", "no-check")
                .multiPart("file", screenshot)
                .post("/rest/api/3/issue/" + bugKey + "/attachments");

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("❌ Failed to attach screenshot: " + response.getBody().asString());
        }

        System.out.println("✅ Screenshot attached to " + bugKey);
    }

    /**
     * Upload execution results to Xray Cloud (with optional evidence)
     */
    public static void importExecutionWithEvidence(String xrayToken, String testExecutionKey, List<Map<String, String>> results) throws Exception {
        JSONArray testsArray = new JSONArray();

        for (Map<String, String> row : results) {
            JSONObject testObj = new JSONObject()
                    .put("testKey", row.get("TestCaseKey"))
                    .put("status", row.get("Status").toUpperCase());

            if (row.containsKey("Comment")) {
                testObj.put("comment", row.get("Comment"));
            }

            if (row.containsKey("Screenshot")) {
                File screenshot = new File(row.get("Screenshot"));
                if (screenshot.exists()) {
                    byte[] fileContent = new FileInputStream(screenshot).readAllBytes();
                    String encoded = Base64.getEncoder().encodeToString(fileContent);

                    JSONArray evidences = new JSONArray();
                    evidences.put(new JSONObject()
                            .put("filename", screenshot.getName())
                            .put("data", encoded));
                    testObj.put("evidences", evidences);
                } else {
                    System.out.println("⚠️ Screenshot not found: " + screenshot.getAbsolutePath());
                }
            }

            testsArray.put(testObj);
        }

        JSONObject payload = new JSONObject()
                .put("testExecutionKey", testExecutionKey)
                .put("tests", testsArray);

        Response response = RestAssured.given()
                .baseUri(XRAY_BASE)
                .header("Authorization", "Bearer " + xrayToken)
                .header("Content-Type", "application/json")
                .body(payload.toString())
                .post("/import/execution");

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("❌ Failed to import execution: " + response.getBody().asString());
        }

        System.out.println("✅ Xray Execution Imported: " + testExecutionKey);
    }
}
