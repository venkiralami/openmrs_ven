package utils.zephyr;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ZephyrCase {
    String value(); // testcase key / executionId
}
