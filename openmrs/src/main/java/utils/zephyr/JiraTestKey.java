package utils.zephyr;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JiraTestKey {
    String value(); // Jira testcase key / executionId
}
