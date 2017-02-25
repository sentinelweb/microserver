package uk.co.sentinelweb.microserver.server.util;

/**
 * Created by robert on 24/02/2017.
 */

public class ThreadUtil {
    public static void sleep(final int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (final Exception e) {
        }
    }
}
