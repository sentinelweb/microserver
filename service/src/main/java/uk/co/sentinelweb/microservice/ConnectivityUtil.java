package uk.co.sentinelweb.microservice;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Created by robert on 24/02/2017.
 */

public class ConnectivityUtil {

    /**
     * Tell android to keep wifi on.
     * @param context
     * @return
     */
    public static WifiManager.WifiLock obtainLock(final Context context) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            final WifiManager.WifiLock lock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, C.WIFI_LOCK_TAG);
            lock.acquire();
            return lock;
        }
        return null;
    }

    /**
     * Release wifi lock
     * @param wifiLock
     */
    public static void releaseWifiLock(final WifiManager.WifiLock wifiLock ){
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
    }
}
