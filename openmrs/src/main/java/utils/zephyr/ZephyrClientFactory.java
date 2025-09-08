package utils.zephyr;

import utils.xray.XRayUpdater;

public class ZephyrClientFactory {
    public static ZephyrUpdater getClient() {
        String type = ConfigReader.getProperty("zephyr.type").toUpperCase();
        if ("SCALE".equals(type)) {
            return new ZephyrScaleUpdater();
        }if ("XRAY".equals(type)) {
          //  return new XRayUpdater();
        	 return new ZephyrSquadUpdater();
        }
        else {
            return new ZephyrSquadUpdater();
        }
    }
}
