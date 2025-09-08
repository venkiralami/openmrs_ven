package utils.zephyr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZephyrScaleUpdater implements ZephyrUpdater {

	private static final String BASE_URL = ConfigReader.getProperty("zephyr.scale.url");
    private static final String BEARER_TOKEN = ConfigReader.getProperty("zephyr.scale.bearerToken");
    private static final String PROJECT_KEY = ConfigReader.getProperty("project.key"); // Jira project key

    // 1️⃣ Fetch test cases
    public List<String> fetchTestCases() throws IOException {
        List<String> testCases = new ArrayList<>();
        URL url = new URL(BASE_URL + "/testcases?projectKey=" + PROJECT_KEY + "&maxResults=5"); // fetch 5 for demo
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

    // 4️⃣ Update executions
    public void updateExecutions(String cycleKey, List<String> testCases) throws IOException {
        String[] statuses = {"Pass", "Fail", "Blocked"};
        Random rand = new Random();

        for (String testCase : testCases) {
            String status = statuses[rand.nextInt(statuses.length)];

            URL url = new URL(BASE_URL + "/testexecutions");
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

            URL url = new URL(BASE_URL + "/testexecutions");
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
