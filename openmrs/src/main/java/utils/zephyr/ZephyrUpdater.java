package utils.zephyr;

import java.io.IOException;
import java.util.List;

public interface ZephyrUpdater {
	List<String> fetchTestCases() throws IOException;
	String createTestCycle(String cycleName) throws IOException;
	void addTestCasesToCycle(String cycleKey, List<String> testCases) throws IOException;
	void updateExecutions(String cycleKey, List<String> testCases) throws IOException;
	void updateExecutionForTestcase(String cycleKey, String testCase, String status) throws IOException;
    boolean testCaseExists(String testCaseKey);

}
