package uk.co.sentinelweb.microserver.server;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by robert on 22/02/2017.
 */
public class WebServerTest {
    private final String PING_URL = "http://127.0.0.1:"+C.SERVERPORT_DEF+"/c/ping";

    WebServer server;

    @Test
    public void testStartup() {
        server = new WebServer(C.SERVERPORT_DEF);
        server.start();
        boolean success = false;
        while (!server.serverRunning) {
            try {Thread.sleep(50);}catch ( final Exception e){}
        }

        try {
            final URL url = new URL(PING_URL);
            final URLConnection urlConnection = url.openConnection();
            final byte[] buf = new byte[1000];
            int read = -1;
            final InputStream inputStream = urlConnection.getInputStream();
            final StringWriter sw = new StringWriter();
            while ((read = inputStream.read(buf,0,buf.length))>-1) {
                sw.write(new String(buf,0,read));
            }
            inputStream.close();
            final String s = sw.getBuffer().toString();
            Assert.assertTrue(s.contains("hello"));
            success = true;
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        try {
            server.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(success);
    }

}