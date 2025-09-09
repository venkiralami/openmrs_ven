package utils.zephyr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ZephyrScaleUpdater implements ZephyrUpdater {

	private static final String JIRA_BASE_URL = ConfigReader.getProperty("jira.url");
	private static final String JIRA_EMAIL = ConfigReader.getProperty("jira.email");
	private static final String JIRA_API_TOKEN = ConfigReader.getProperty("jira.token");
	 private static final String JIRA_AUTH_TOKEN = getJiraAuthToken(JIRA_EMAIL, JIRA_API_TOKEN);
	private static final String ZEPHYR_BASE_URL = ConfigReader.getProperty("zephyr.scale.url");
    private static final String BEARER_TOKEN = ConfigReader.getProperty("zephyr.scale.bearerToken");
    private static final String PROJECT_KEY = ConfigReader.getProperty("project.key"); // Jira project key

    private static final String ZEPHYR_API = ZEPHYR_BASE_URL + "/rest/zephyr-scale/latest";
    //private static final String JIRA_API = JIRA_BASE_URL + "/rest/api/3";
    private static final String TOKEN = BEARER_TOKEN.startsWith("Bearer ") ? BEARER_TOKEN : "Bearer " + BEARER_TOKEN;
    
    public static String getJiraAuthToken(String jiraEmail, String jiraApiToken) {
        String auth = jiraEmail + ":" + jiraApiToken;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }
    
    // 1️⃣ Fetch test cases
    public List<String> fetchTestCases() throws IOException {
        List<String> testCases = new ArrayList<>();
        URL url = new URL(ZEPHYR_BASE_URL + "/testcases?projectKey=" + PROJECT_KEY + "&maxResults=5"); // fetch 5 for demo
        HttpURLConnection conn = buildConnection(url, "GET");

        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        System.out.println("Fetch Test Cases Response (" + responseCode + "): " + response);

        int idx = 0;
        while ((idx = response.indexOf("\"key\":\"", idx)) != -1) {
            idx += 7;
            int end = response.indexOf("\"", idx);
            testCases.add(response.substring(idx, end));
            idx = end;
        }

        System.out.println("✅ Found Test Cases: " + testCases);
        return testCases;
    }

    // 2️⃣ Create test cycle
    public String createTestCycle(String cycleName) throws IOException {
        URL url = new URL(ZEPHYR_BASE_URL + "/testcycles");
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
        URL url = new URL(ZEPHYR_BASE_URL + "/testcycles/" + cycleKey + "/testcases");
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

    // 4️⃣ Update executions
    public void updateExecutions(String cycleKey, List<String> testCases) throws IOException {
        String[] statuses = {"Pass", "Fail", "Blocked"};
        Random rand = new Random();

        for (String testCase : testCases) {
            String status = statuses[rand.nextInt(statuses.length)];

            URL url = new URL(ZEPHYR_BASE_URL + "/testexecutions");
            HttpURLConnection conn = buildConnection(url, "POST");

            String json = "{"
                    + "\"projectKey\":\"" + PROJECT_KEY + "\","
                    + "\"testCaseKey\":\"" + testCase + "\","
                    + "\"testCycleKey\":\"" + cycleKey + "\","
                    + "\"statusName\":\"" + status + "\""
                    + "}";

            writeRequest(conn, json);

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);
            System.out.println("Update Execution: " + testCase + " -> " + status
                    + " (" + responseCode + "): " + response);
        }
    }

    // 5 Update execution for a single test case
    public void updateExecutionForTestcase(String cycleKey, String testCase, String status, String comment) throws IOException {

            URL url = new URL(ZEPHYR_BASE_URL + "/testexecutions");
            HttpURLConnection conn = buildConnection(url, "POST");

            String json = "{"
                    + "\"projectKey\":\"" + PROJECT_KEY + "\","
                    + "\"testCaseKey\":\"" + testCase + "\","
                    + "\"testCycleKey\":\"" + cycleKey + "\","
                     + "\"comment\":\"" + comment + "\","
                    + "\"statusName\":\"" + status + "\""
                    + "}";

            writeRequest(conn, json);

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);
            System.out.println("[Zephyr] Update Execution: " + testCase + " -> " + status
                    + " (" + responseCode + "): " + response);
        
    }
    
 // 2️⃣ Create Bug in Jira Cloud
    public String createBug(String testKey, String summary, String description) {
System.out.println("Creating Bug for Test Case: " + testKey);
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
                .baseUri(JIRA_BASE_URL)
                .header("Authorization", "Basic " + JIRA_AUTH_TOKEN)
                .header("Content-Type", "application/json")
                .body(payload.toString())
                .post("/rest/api/3/issue");

        if (response.statusCode() != 201) {
            throw new RuntimeException("❌ Failed to create Bug: " + response.getBody().asString());
        }

        return response.jsonPath().getString("key"); // Bug Key
    }

        public String createBug(String projectKey, String summary) {
            // build JSON payload
            String payload = "{ \"fields\": { " +
                    "\"project\": { \"key\": \"" + projectKey + "\" }, " +
                    "\"summary\": \"" + summary + "\", " +
                    "\"issuetype\": { \"name\": \"Bug\" } } }";

            // call Jira REST API
            Response resp = given()
                    .header("Authorization", "Bearer " + JIRA_API_TOKEN)
                    .header("Content-Type", "application/json")
                    .body(payload)
                    .post(JIRA_BASE_URL+"/rest/api/3/issue");

            // assert & extract
            resp.then().statusCode(201);
            return resp.jsonPath().getString("key");  // ✅ return String, not the chain
        }
    
     // 3️⃣ Attach Screenshot
        public void attachScreenshot( String issueKey, String filePath) throws Exception {
           
            File file = new File(filePath);
            if(!file.exists()){
                throw new RuntimeException("File not found: " + filePath);
            }

            Response response = RestAssured.given()
                    .baseUri(JIRA_BASE_URL)
                    .header("Authorization", "Basic " + JIRA_AUTH_TOKEN)
                    .header("X-Atlassian-Token", "no-check")
                    .multiPart("file", file)
                    .post("/rest/api/3/issue/" + issueKey + "/attachments");

            if(response.statusCode() != 200 && response.statusCode() != 201){
                throw new RuntimeException("❌ Failed to attach screenshot: " + response.getBody().asString());
            }
        }

     // Link Bug to Test Case
        
       
        public void linkBugToTestCase1(String testCaseKey, String bugKey) {
           
        	 JSONObject payload = new JSONObject();
			 payload.put("issueIdOrKey", bugKey);

			Response response = RestAssured.given()
					.baseUri(ZEPHYR_BASE_URL)
					.header("Authorization", "Bearer " + BEARER_TOKEN)
					.header("Content-Type", "application/json")
					.body(payload.toString())
					.post("/v2/testcases/" + testCaseKey + "/links/issues");

			System.out.println("Response Status: " + response.statusCode());
			System.out.println("Response Body: " + response.getBody().asString());

			if (response.statusCode() != 201) {
				throw new RuntimeException("❌ Failed to link Bug to Test: " + response.getBody().asString());
			}

			System.out.println("✅ Successfully linked Bug [" + bugKey + "] to Test [" + testCaseKey + "]");
        	
        	
        }
        
        public void linkBugToTestCase(String testCaseKey, String bugKey) {
            given()
                .log().all() // Logs request details
                .header("Authorization", "Bearer " + BEARER_TOKEN) // Zephyr Scale API token
                .header("Content-Type", "application/json")
                .body("{ \"issueIdOrKey\": \"SCRUM-100\"}")
                .when()
                .post("https://api.zephyrscale.smartbear.com/v2/testcases/" + testCaseKey + "/links/issues")
                .then()
                .log().all() // Logs response details
                .statusCode(200);
        }



        public void linkBugToTest(String testKey, String bugKey, String linkTypeName) {
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
                        .baseUri(ZEPHYR_BASE_URL)
                        .header("Authorization", TOKEN)
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

	@Override
	public boolean testCaseExists(String testCaseKey) {
		// TODO Auto-generated method stub
		return false;
	}
     
}
