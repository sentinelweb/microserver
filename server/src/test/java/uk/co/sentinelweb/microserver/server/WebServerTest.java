package uk.co.sentinelweb.microserver.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Set;

import uk.co.sentinelweb.microserver.server.cp.PingCommandProcessor;

/**
 * Created by robert on 22/02/2017.
 */
public class WebServerTest {

    WebServer server;

    private final String PING_URL = "http://127.0.0.1:" + C.SERVERPORT_DEF + "/c/ping";
    public static final String FILE_CONTENT = "Some multi-line\n file content\nshould come back the same";

    @Test
    public void testPing() {
        final WebServerConfig config = new WebServerConfig.Builder()
                .addProcessor("/c/ping", new PingCommandProcessor())
                .build();
        boolean success = false;
        startServer(config);

        try {
            final URL url = new URL(PING_URL);
            final URLConnection urlConnection = url.openConnection();
            final String s = readContentString(urlConnection.getInputStream());
            final Gson gson = new GsonBuilder().create();
            final HashMap<String, String> map = gson.fromJson(s, HashMap.class);
            final Set<String> keys = map.keySet();
            Assert.assertTrue(keys.contains("hello"));
            Assert.assertEquals(map.get("hello"), "world");
            success = true;
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            killServer();
            Assert.assertTrue(success);
        }
    }

    private final String FILE_URL = "http://127.0.0.1:" + C.SERVERPORT_DEF + "/file.txt";

    @Test
    public void testFile() {
        final File webRoot = new File("/tmp");
        final WebServerConfig config = new WebServerConfig.Builder()
                .withWebRoot(webRoot)
                .build();
        boolean success = false;
        startServer(config);
        try {
            final File testFile = new File(webRoot, "file.txt");
            final FileWriter writer = new FileWriter(testFile);
            writer.write(FILE_CONTENT);
            writer.close();
            final URL url = new URL(FILE_URL);
            final URLConnection urlConnection = url.openConnection();
            final String s = readContentString(urlConnection.getInputStream());
            Assert.assertEquals(FILE_CONTENT, s);
            testFile.delete();
            success = true;
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            killServer();
            Assert.assertTrue(success);
        }
    }

    @Test
    public void testFile404() {
        final File webRoot = new File("/tmp");
        final WebServerConfig config = new WebServerConfig.Builder()
                .withWebRoot(webRoot)
                .build();
        boolean success = false;
        startServer(config);
        try {
            final URL url = new URL(FILE_URL);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            final int code = connection.getResponseCode();

            Assert.assertEquals(404, code);
            success = true;
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            killServer();
            Assert.assertTrue(success);
        }
    }

    private final String FILE_DOTDOT_URL = "http://127.0.0.1:" + C.SERVERPORT_DEF + "/../file.txt";

    @Test
    public void testFile404OutsideRoot() {
        final File webRoot = new File("/tmp/root");
        webRoot.mkdirs();
        final WebServerConfig config = new WebServerConfig.Builder()
                .withWebRoot(webRoot)
                .build();
        boolean success = false;
        startServer(config);
        try {
            final File testFile = new File("/tmp", "file.txt");
            final FileWriter writer = new FileWriter(testFile);
            writer.write(FILE_CONTENT);

            final URL url = new URL(FILE_DOTDOT_URL);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            final int code = connection.getResponseCode();

            Assert.assertEquals(404, code);
            testFile.delete();
            webRoot.delete();

            success = true;
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            killServer();
            Assert.assertTrue(success);
        }


    }

    private String readContentString(final InputStream inputStream) throws IOException {
        final byte[] buf = new byte[1000];
        int read = -1;
        final StringWriter sw = new StringWriter();
        while ((read = inputStream.read(buf, 0, buf.length)) > -1) {
            sw.write(new String(buf, 0, read));
        }
        inputStream.close();
        return sw.getBuffer().toString();
    }

    private void startServer(final WebServerConfig config) {
        server = new WebServer(config);
        server.start();

        while (!server.serverRunning) {
            try {
                Thread.sleep(50);
            } catch (final Exception e) {
            }
        }
    }

    private void killServer() {
        try {
            server.close();
            //new URL("http://127.0.0.1:" + C.SERVERPORT_DEF).openConnection().getInputStream().close();
            while (server.serverRunning) {
                try {
                    Thread.sleep(50);
                } catch (final Exception e) {
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }



}