package uk.co.sentinelweb.microserver.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

public class MultiCastListener extends Thread {
    String ip = C.MULTICAST_IP_DEF;
    int port = C.MULTICAST_PORT_DEF;
    String _localIp = null;
    boolean _keepGoing = true;
    MulticastSocket _theSocket = null;
    int _webPort = C.SERVERPORT_DEF;
    InetAddress _broadcastAddress;
    OnAsyncListener _recieveListener;
    Gson gson;

    public MultiCastListener(final String multiCastIp, final String localIp, final int port, final int webPort) {
        super("MultiCastListener");
        this.ip = multiCastIp;
        this.port = port;
        _localIp = localIp;
        this._webPort = webPort;
        final GsonBuilder gsonb = new GsonBuilder();
        gson = gsonb.create();
    }

    @Override
    public void run() {
        try {
            _broadcastAddress = InetAddress.getByName(ip);
            _theSocket = new MulticastSocket(port);
            _theSocket.joinGroup(_broadcastAddress);

            sendBroadcast();

            final byte[] buffer = new byte[10 * 1024];
            final DatagramPacket data1 = new DatagramPacket(buffer, buffer.length);
            System.out.println("multi start: addr:" + _broadcastAddress);

            while (_keepGoing) {
                _theSocket.receive(data1);
                final String msg = new String(buffer, 0, data1.getLength(), "utf-8");
                System.out.println("multi Received: " + msg);
                if (_recieveListener != null) {
                    _recieveListener.onAsync(msg);
                }
            }
        } catch (final IOException e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        }
        System.err.println(this.getClass().getCanonicalName() + " :: exit");
    }

    public void sendBroadcast() {
        if (_theSocket != null && !_theSocket.isClosed()) {
            try {
                final HashMap<String, String> closeMsg = new HashMap<>();
                closeMsg.put("join", _localIp + ":" + _webPort);
                final String closeMsgStr = gson.toJson(closeMsg);
                final DatagramPacket data = new DatagramPacket(closeMsgStr.getBytes(), closeMsgStr.length(), _broadcastAddress, port);
                _theSocket.send(data);
                System.out.println("sendBroadcast .. done");
            } catch (final Exception e) {
                System.err.println(this.getClass().getCanonicalName() + "sendBroadcast err");
                e.printStackTrace(System.err);
            }
        }
    }


    public OnAsyncListener getRecieveListener() {
        return _recieveListener;
    }

    public void setRecieveListener(final OnAsyncListener _recieveListener) {
        this._recieveListener = _recieveListener;
    }

    public interface OnAsyncListener {
        public void onAsync(String s);
    }

    public boolean isKeepGoing() {
        return _keepGoing;
    }

    public void setKeepGoing(final boolean keepGoing) {
        this._keepGoing = keepGoing;
    }

    public void close() {
        this._keepGoing = false;
        if (_theSocket != null && !_theSocket.isClosed()) {
            // TODO note we might need to send a local exit message to stop blocking ...
            new Thread("MultiCastClose") {
                @Override
                public void run() {
                    try {
                        final InetAddress group = InetAddress.getByName(ip);
                        // send exit message
                        final HashMap<String, String> closeMsg = new HashMap<>();
                        closeMsg.put("close", _localIp);
                        final String closeMsgStr = gson.toJson(closeMsg);
                        final DatagramPacket data = new DatagramPacket(closeMsgStr.getBytes(), closeMsgStr.length(), group, port);
                        _theSocket.send(data);
                        System.out.println(this.getClass().getCanonicalName() + "multi closing: send lastcall");

                        final InetAddress local = InetAddress.getByName("localhost");
                        final DatagramPacket data1 = new DatagramPacket("".getBytes(), 0, local, port);
                        _theSocket.send(data1);
                        System.out.println(this.getClass().getCanonicalName() + "multi closing: ");

                        _theSocket.close();
                    } catch (final Exception e) {
                        System.err.println(this.getClass().getCanonicalName() + "multi close ex: ");
                        e.printStackTrace(System.err);
                    }
                }
            }.start();
        }
    }
}
