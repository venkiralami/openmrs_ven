package utils.zephyr;

public class ZephyrClientFactory {
    public static ZephyrUpdater getClient() {
        String type = ConfigReader.getProperty("zephyr.type").toUpperCase();
        if ("SCALE".equals(type)) {
            return new ZephyrScaleUpdater();
        } else {
            return new ZephyrSquadUpdater();
        }
    }
}
