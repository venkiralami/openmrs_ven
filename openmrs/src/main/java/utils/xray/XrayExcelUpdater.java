package utils.xray;

        import io.restassured.RestAssured;
        import io.restassured.response.Response;
        import org.apache.poi.ss.usermodel.*;
        import org.apache.poi.xssf.usermodel.XSSFWorkbook;
        import org.json.JSONArray;
        import org.json.JSONObject;

        import java.io.FileInputStream;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

        public class XrayExcelUpdater {

            private static final String CLIENT_ID_XRAY = "21B9D71A81AA462485AFDF93958F356E";
            private static final String CLIENT_SECRET_XRAY = "17afd4a53e767471ae1ecebf924dd86468e6171e0598e6a6cbb6d6da6c1a11bc";
            private static final String PROJECT_KEY = "SCRUM";
            
            public static void main(String[] args) throws Exception {

                // 1️⃣ Authenticate to Xray
                String token = authenticate();
                System.out.println("Authenticated. Token: " + token);

                // 2️⃣ Read Excel results
                List<Map<String, String>> results = readExcelResults("resources\\results.xlsx");

                // 3️⃣ Upload execution results (creates Test Execution automatically)
                importExecution(token, results);
                System.out.println("Test execution uploaded successfully!");
            }

            // Authenticate to Xray Cloud
            private static String authenticate() {
                JSONObject authBody = new JSONObject()
                        .put("client_id", CLIENT_ID_XRAY)
                        .put("client_secret", CLIENT_SECRET_XRAY);

                Response response = RestAssured.given()
                        .baseUri("https://xray.cloud.getxray.app/api/v2")
                        .header("Content-Type", "application/json")
                        .body(authBody.toString())
                        .post("/authenticate");

                if(response.statusCode() != 200){
                    throw new RuntimeException("Authentication failed! Response: " + response.getBody().asString());
                }

                return response.getBody().asString().replace("\"", "");
            }

            // Read test results from Excel
            private static List<Map<String, String>> readExcelResults(String filePath) throws Exception {
                List<Map<String, String>> results = new ArrayList<>();
                FileInputStream fis = new FileInputStream(filePath);
                Workbook workbook = new XSSFWorkbook(fis);
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    Map<String, String> rowData = new HashMap<>();
                    for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                        String key = headerRow.getCell(j).getStringCellValue();
                        Cell cell = row.getCell(j);
                        String value = (cell != null) ? cell.toString() : "";
                        rowData.put(key, value);
                    }
                    results.add(rowData);
                }

                workbook.close();
                return results;
            }

            // Upload execution results to Xray (creates Test Execution automatically)
            private static void importExecution(String token, List<Map<String, String>> results) {
                JSONArray testsArray = new JSONArray();
                for (Map<String, String> row : results) {
                	System.out.println("Row Data: " + row);
                	
                    JSONObject testObj = new JSONObject()
                            .put("testKey", row.get("TestCaseKey"))
                            .put("status", row.get("Status").toUpperCase())
                            .put("comment", row.get("Comment"));
                    testsArray.put(testObj);
                }

                JSONObject info = new JSONObject()
                        .put("summary", "Automation Execution from Java")
                        .put("project", PROJECT_KEY);

                JSONObject payload = new JSONObject()
                        .put("info", info)
                        .put("tests", testsArray);
                System.out.println("Execution payload: " + payload.toString());
                Response response = RestAssured.given()
                        .baseUri("https://xray.cloud.getxray.app/api/v2")
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .body(payload.toString())
                        .post("/import/execution");

                System.out.println("Execution Response: " + response.getBody().asString());

                if(response.statusCode() != 200){
                    throw new RuntimeException("Failed to import execution: " + response.getBody().asString());
                }
            }
        }
