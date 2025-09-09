package utils.zephyr;

import java.io.IOException;
import java.util.List;

public interface ZephyrUpdater {
	List<String> fetchTestCases() throws IOException;
	String createTestCycle(String cycleName) throws IOException;
	String createBug(String projectKey, String summary);
	String createBug(String testKey, String summary, String description);
	void attachScreenshot( String issueKey, String filePath) throws Exception;
	void linkBugToTestCase(String testCaseKey, String bugKey);
	void linkBugToTest(String testKey, String bugKey, String linkTypeName);
	void addTestCasesToCycle(String cycleKey, List<String> testCases) throws IOException;
	void updateExecutions(String cycleKey, List<String> testCases) throws IOException;
	void updateExecutionForTestcase(String cycleKey, String testCase, String status, String comment) throws IOException;
    boolean testCaseExists(String testCaseKey);

}
