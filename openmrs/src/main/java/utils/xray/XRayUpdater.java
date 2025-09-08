package utils.xray;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import utils.zephyr.ConfigReader;

public class XRayUpdater  {

	private static final String BASE_URI_XRAY = ConfigReader.getProperty("xray.baseUri");
	private static final String CLIENT_ID_XRAY = ConfigReader.getProperty("xray.clientId");
    private static final String CLIENT_SECRET_XRAY = ConfigReader.getProperty("xray.clientSecret");
    private static final String XRAY_AUTH_TOKEN = getXRayAuthToken();
    private static final String JIRA_BASE_URI = ConfigReader.getProperty("jira.url");
    private static final String JIRA_EMAIL = ConfigReader.getProperty("jira.email");
    private static final String JIRA_API_TOKEN = ConfigReader.getProperty("jira.token");
    private static final String JIRA_AUTH_TOKEN = getJiraAuthToken(JIRA_EMAIL, JIRA_API_TOKEN);
    
    private static final String PROJECT_KEY = ConfigReader.getProperty("project.key"); // Jira project key;
    private static final String BASE_URL = ConfigReader.getProperty("zephyr.scale.url");
    private static final String BEARER_TOKEN = ConfigReader.getProperty("zephyr.scale.bearerToken");
    
        // 2️⃣ Create test cycle
    public String createTestCycle(String cycleName) throws IOException {
        URL url = new URL(BASE_URL + "/testcycles");
        HttpURLConnection conn = buildConnection(url, "POST");

        String json = "{"
                + "\"projectKey\":\"" + PROJECT_KEY + "\","
                + "\"name\":\"" + cycleName + "\""
                + "}";

        writeRequest(conn, json);

        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        System.out.println("Create Cycle Response (" + responseCode + "): " + response);

        // extract cycle key
        int keyIdx = response.indexOf("\"key\":\"");
        if (keyIdx != -1) {
            keyIdx += 7;
            int end = response.indexOf("\"", keyIdx);
            return response.substring(keyIdx, end);
        }
        return null;
    }

    // 3️⃣ Add test cases to cycle
    public void addTestCasesToCycle(String cycleKey, List<String> testCases) throws IOException {
        URL url = new URL(BASE_URL + "/testcycles/" + cycleKey + "/testcases");
        HttpURLConnection conn = buildConnection(url, "POST");

        StringBuilder casesJson = new StringBuilder("[");
        for (int i = 0; i < testCases.size(); i++) {
            casesJson.append("\"").append(testCases.get(i)).append("\"");
            if (i < testCases.size() - 1) casesJson.append(",");
        }
        casesJson.append("]");

        String json = "{ \"additions\": " + casesJson + " }";

        writeRequest(conn, json);

        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        System.out.println("Add Test Cases Response (" + responseCode + "): " + response);
    }
      
    // 🔧 Helpers
    private static HttpURLConnection buildConnection(URL url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + BEARER_TOKEN);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        return conn;
    }

    private static void writeRequest(HttpURLConnection conn, String json) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        InputStream stream = (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300)
                ? conn.getInputStream() : conn.getErrorStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line);
        in.close();
        return response.toString();
    }

	public boolean testCaseExists(String testCaseKey) {
		// TODO Auto-generated method stub
		return false;
	}
	

	 // Get JiranAuth Token
	    public static String getJiraAuthToken(String jiraEmail, String jiraApiToken) {
	        String auth = jiraEmail + ":" + jiraApiToken;
	        return Base64.getEncoder().encodeToString(auth.getBytes());
	    }
	    
	 // Authenticate to Xray Cloud and get Bearer token
    public static String getXRayAuthToken() {
        JSONObject authBody = new JSONObject()
                .put("client_id", CLIENT_ID_XRAY)
                .put("client_secret", CLIENT_SECRET_XRAY);

        Response response = RestAssured.given()
                .baseUri(BASE_URI_XRAY)
                .header("Content-Type", "application/json")
                .body(authBody.toString())
                .post("/authenticate");

        if(response.statusCode() != 200){
            throw new RuntimeException("Authentication failed! Response: " + response.getBody().asString());
        }

        return response.getBody().asString().replace("\"", "");
    }
    
 // 🔎 Get the latest Test Execution Key from Jira
    public String getLatestExecutionKey() {
        String jql = "project=" + PROJECT_KEY + " AND issuetype='Test Execution' ORDER BY created DESC";

        Response response = RestAssured.given()
                .baseUri(JIRA_BASE_URI)
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((JIRA_EMAIL + ":" + JIRA_API_TOKEN).getBytes()))
                .header("Content-Type", "application/json")
                .queryParam("jql", jql)
                .queryParam("maxResults", 1)
                .get("/rest/api/2/search");

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch execution key: " + response.getBody().asString());
        }

        String key = response.jsonPath().getString("issues[0].key");
        System.out.println("Latest Execution Key: " + key);
        return key;
    }
          
 // 🚀 Import execution results into Xray
    public static void importExecution(List<Map<String, String>> results, String executionKey) {
        JSONArray testsArray = new JSONArray();

        for (Map<String, String> row : results) {
            System.out.println("Row Data: " + row);

            String status = row.getOrDefault("Status", "TODO").toUpperCase();

            JSONObject testObj = new JSONObject()
                    .put("testKey", row.get("TestCaseKey"))
                    .put("status", status);

            if (row.containsKey("Comment") && row.get("Comment") != null) {
                testObj.put("comment", row.get("Comment"));
            }

            testsArray.put(testObj);
        }

        JSONObject info = new JSONObject()
                .put("summary", "Automation Execution from Java")
                .put("project", PROJECT_KEY)
                .put("description", "Automated execution run on " + LocalDate.now());

        JSONObject payload = new JSONObject()
                .put("info", info)
                .put("tests", testsArray);

        System.out.println("Execution payload: " + payload.toString(2));

        // 👇 Use correct endpoint based on executionKey
        String endpoint;
        if (executionKey != null && !executionKey.isEmpty()) {
            endpoint = "/import/execution?testExecutionKey=" + executionKey; // ✅ Update existing
        } else {
            endpoint = "/import/execution"; // ✅ Create new
        }

        Response response = RestAssured.given()
                .baseUri(BASE_URI_XRAY)
                .header("Authorization", "Bearer " + XRAY_AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .body(payload.toString())
                .post(endpoint);

        System.out.println("Execution Response: " + response.statusCode() + " => " + response.getBody().asString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to import execution: " + response.getBody().asString());
        }
    }

    
 // 2️⃣ Create Bug in Jira Cloud
    public static String createBug(String testKey, String summary, String description) {

        JSONObject payload = new JSONObject();
        payload.put("fields", new JSONObject()
                .put("project", new JSONObject().put("key", PROJECT_KEY))
                .put("summary", summary+testKey)
                .put("issuetype", new JSONObject().put("name", "Bug"))
                .put("description", new JSONObject()
                        .put("type", "doc")
                        .put("version", 1)
                        .put("content", new JSONArray()
                                .put(new JSONObject()
                                        .put("type", "paragraph")
                                        .put("content", new JSONArray()
                                                .put(new JSONObject().put("type", "text").put("text", description)))
                                )
                        )
                )
        );

        Response response = RestAssured.given()
                .baseUri(JIRA_BASE_URI)
                .header("Authorization", "Basic " + JIRA_AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .body(payload.toString())
                .post("/rest/api/3/issue");

        if (response.statusCode() != 201) {
            throw new RuntimeException("❌ Failed to create Bug: " + response.getBody().asString());
        }

        return response.jsonPath().getString("key"); // Bug Key
    }
     
 // 3️⃣ Attach Screenshot
    public static void attachScreenshot( String issueKey, String filePath) throws Exception {
       
        File file = new File(filePath);
        if(!file.exists()){
            throw new RuntimeException("File not found: " + filePath);
        }

        Response response = RestAssured.given()
                .baseUri(JIRA_BASE_URI)
                .header("Authorization", "Basic " + JIRA_AUTH_TOKEN)
                .header("X-Atlassian-Token", "no-check")
                .multiPart("file", file)
                .post("/rest/api/3/issue/" + issueKey + "/attachments");

        if(response.statusCode() != 200 && response.statusCode() != 201){
            throw new RuntimeException("❌ Failed to attach screenshot: " + response.getBody().asString());
        }
    }
      
    /**
     * Links a Bug to a Test in Jira Cloud / Xray Cloud.
     *
     * @param testKey Xray Test Key (e.g., SCRUM-14)
     * @param bugKey  Bug Issue Key (e.g., SCRUM-49)
     * @param linkTypeName Name of the link type (e.g., "Tests" or "Relates")
     */
    public static void linkBugToTest(String testKey, String bugKey, String linkTypeName) {
        try {
        	
        	 String auth = getJiraAuthToken(JIRA_EMAIL, JIRA_API_TOKEN);
        	System.out.println("Linking Bug " + bugKey + " to Test " + testKey + " with link type: " + linkTypeName);
            JSONObject payload = new JSONObject();
            JSONObject type = new JSONObject();
            type.put("name", linkTypeName); // must exist in Jira Cloud
            payload.put("type", type);

            JSONObject inward = new JSONObject();
            inward.put("key", testKey); // The test
            payload.put("inwardIssue", inward);

            JSONObject outward = new JSONObject();
            outward.put("key", bugKey); // The bug
            payload.put("outwardIssue", outward);

            System.out.println("Link Payload: " + payload.toString(2));

            Response response = RestAssured.given()
                    .baseUri(JIRA_BASE_URI)
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .body(payload.toString())
                    .post("/rest/api/3/issueLink");

            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.getBody().asString());

            if (response.statusCode() != 201) {
                throw new RuntimeException("❌ Failed to link Bug to Test: " + response.getBody().asString());
            }

            System.out.println("✅ Successfully linked Bug [" + bugKey + "] to Test [" + testKey + "]");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Error linking Bug to Test: " + e.getMessage());
        }
    }

    
 // 🚀 Import execution results into Xray
    public static void updateTestStatus1(String testKey, String status, String bugDescription, String screenshotPath, String executionKey) {
        
        
     // 1️⃣ Update test execution status
        JSONObject payload = new JSONObject();
        JSONArray testsArray = new JSONArray();
        JSONObject testObj = new JSONObject();
        testObj.put("testKey", testKey);
        testObj.put("status", status);
        testsArray.put(testObj);
       

        JSONObject info = new JSONObject()
                .put("summary", "Automation Execution from Java")
                .put("project", PROJECT_KEY)
                .put("description", "Automated execution run on " + LocalDate.now());

        		payload.put("info", info);
        		payload.put("tests", testsArray);

        System.out.println("Execution payload: " + payload.toString(2));

        // 👇 Use correct endpoint based on executionKey
        String endpoint;
        if (executionKey != null && !executionKey.isEmpty()) {
        	System.out.println("Updating existing execution: " + executionKey);
            endpoint = "/import/execution?testExecutionKey=" + executionKey; // ✅ Update existing
        } else {
        	System.out.println("Creating new execution");
            endpoint = "/import/execution"; // ✅ Create new
        }

        Response response = RestAssured.given()
                .baseUri(BASE_URI_XRAY)
                .header("Authorization", "Bearer " + XRAY_AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .body(payload.toString())
                .post(endpoint);

        System.out.println("Execution Response: " + response.statusCode() + " => " + response.getBody().asString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to import execution: " + response.getBody().asString());
        }
        String bugKey = null;
     // 2️⃣ Create and link bug if failed
        if("FAILED".equalsIgnoreCase(status) && bugDescription != null) {
             bugKey = createBug(testKey, bugDescription, "Automated bug created for failed test: " + testKey);
            if(bugKey != null) {
                linkBugToTest(testKey, bugKey, "Relates");
                // 3️⃣ Attach screenshot if available
                if(screenshotPath != null) {
                   System.out.println("Attaching screenshot to bug: " + bugKey+ " from path: " + screenshotPath);
                        try {
        					attachScreenshot(bugKey, screenshotPath);
        				} catch (Exception e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
                }
            }
        }
    }
}
