package utils.zephyr;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

	private static Properties properties = new Properties();

	static {
		try {
			FileInputStream fis = new FileInputStream("config/jira_zephyr_config.properties");
			properties.load(fis);
		} catch (IOException e) {
			throw new RuntimeException("❌ Failed to load jira_zephyr_config.properties file", e);
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
}
