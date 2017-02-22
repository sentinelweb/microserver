package uk.co.sentinelweb.microserver.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class WebServerUtil {
//	public boolean startService(Context c) {
//		int port = Globals.SERVERPORT_DEF;
//
//		Intent i = new Intent(c,WebServerService.class);
//        i.setAction(Globals.INTENT_WEB_SERVICE_START);
//        i.putExtra(Globals.INTENT_PARAM_WEB_SERVER_CFG_PORT, port);
//        c.startService(i);
//        WebServerService wService = WebServerService.get();
//        return wService!=null;
//
//	}
//
//	public static void startServerIntent(Context c) {
//		Intent i =  new Intent(c,WebServerService.class);
//		i.setAction(Globals.INTENT_WEB_SERVICE_START);
//		//i.putExtra(Globals.INTENT_PARAM_WEB_SERVER_CFG_PORT, Globals.SERVERPORT_DEF);
//		c.startService(i);
//	}
//	public static void checkStartServerIntent(Context c) {
//		Intent i =  new Intent(c,WebServerService.class);
//		i.setAction(Globals.INTENT_WEB_SERVICE_CHECK_START);
//		c.startService(i);
//	}
//	public static void stopServerIntent(Context c) {
//		Intent i =  new Intent(c,WebServerService.class);
//		i.setAction(Globals.INTENT_WEB_SERVICE_STOP);
//		c.startService(i);
//	}
//	public static void checkStopServerIntent(Context c) {
//		Intent i =  new Intent(c,WebServerService.class);
//		i.setAction(Globals.INTENT_WEB_SERVICE_CHECK_STOP);
//		c.startService(i);
//	}

    public static List<String> getIp() {

        final List<String> addresses = new ArrayList<>();
        try {
            //System.out.println("Your Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
            final Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            while (n.hasMoreElements()) {
                final NetworkInterface e = n.nextElement();

                final Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements(); ) {
                    final InetAddress addr = a.nextElement();
                    System.out.println("  " + addr.getHostAddress());
                    addresses.add(addr.getHostAddress());
                }
            }

//        } catch (final UnknownHostException e) {
//            e.printStackTrace();
        } catch (final SocketException e) {
            e.printStackTrace();
        }
        return addresses;
    }
}
