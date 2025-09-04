package utils.zephyr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;

public class ZephyrSquadUpdater implements ZephyrUpdater {
    

	@Override
	public boolean testCaseExists(String testCaseKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> fetchTestCases() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createTestCycle(String cycleName) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTestCasesToCycle(String cycleKey, List<String> testCases) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateExecutions(String cycleKey, List<String> testCases) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateExecutionForTestcase(String cycleKey, String testCase, String status) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
