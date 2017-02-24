package uk.co.sentinelweb.microserver.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Set;

import uk.co.sentinelweb.microserver.server.cp.PingCommandProcessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by robert on 22/02/2017.
 */
public class WebServerTest {

    public static final String WEB_BASE = "http://127.0.0.1:" + C.SERVERPORT_DEF;
    WebServer server;

    private final String PING_URL = WEB_BASE + "/c/ping";
    public static final String FILE_CONTENT = "Some multi-line\n file content\nshould come back the same";

    @Test
    public void testPing() {
        final PingCommandProcessor pingCommandProcessor = new PingCommandProcessor();
        final WebServerConfig config = new WebServerConfig.Builder()
                .addProcessor("/c/ping", pingCommandProcessor)
                .build();
        pingCommandProcessor.setConfig(config);
        startServer(config);

        try {
            final String getParams = "?p1=v1&p2=v2";
            final URL url = new URL(PING_URL+ getParams);
            final URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);
            final OutputStreamWriter poster = new OutputStreamWriter(urlConnection.getOutputStream());
            final String testPostParams = "post1=value1&post2=value2";
            poster.write(testPostParams);
            poster.close();
            final String s = readContentString(urlConnection.getInputStream());
            System.out.println(s);
            final Gson gson = new GsonBuilder().create();
            final HashMap<String, Object> map = gson.fromJson(s, HashMap.class);
            final Set<String> keys = map.keySet();
            assertTrue(keys.contains("hello"));
            assertTrue(keys.contains("ip"));
            assertEquals(map.get("method").toString().toLowerCase(), "post");
            assertEquals(map.get("hello"), "world");
            assertEquals(map.get("basepath"), "/c/ping");
            assertEquals(map.get("path"), "/c/ping"+getParams);
            assertEquals((map.get("port")).toString(), Double.toString(C.SERVERPORT_DEF));

            // get params
            assertNotNull((map.get("params")));
            final LinkedTreeMap<String, String> getMap = (LinkedTreeMap<String, String>) map.get("params");
            assertEquals(getMap.get("p1"), "v1");
            assertEquals(getMap.get("p2"), "v2");

            // post params
            assertEquals(map.get("contentlength").toString(), Float.toString(testPostParams.length()));
            assertEquals(getMap.get("post1"), "value1");
            assertEquals(getMap.get("post2"), "value2");

        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            killServer();
        }
    }

    private final String FILE_URL = WEB_BASE + "/file.txt";

    @Test
    public void testFile() {
        final File webRoot = new File("/tmp");
        final WebServerConfig config = new WebServerConfig.Builder()
                .withWebRoot(webRoot)
                .build();
        startServer(config);
        try {
            final File testFile = new File(webRoot, "file.txt");
            final FileWriter writer = new FileWriter(testFile);
            writer.write(FILE_CONTENT);
            writer.close();
            final URL url = new URL(FILE_URL);
            final URLConnection urlConnection = url.openConnection();
            final String s = readContentString(urlConnection.getInputStream());
            assertEquals(FILE_CONTENT, s);
            assertEquals(urlConnection.getHeaderField("Content-Type"),"text/plain");
            testFile.delete();
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            killServer();
        }
    }

    @Test
    public void testFile404() {
        final File webRoot = new File("/tmp");
        final WebServerConfig config = new WebServerConfig.Builder()
                .withWebRoot(webRoot)
                .build();
        startServer(config);
        try {
            final URL url = new URL(FILE_URL);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            final int code = connection.getResponseCode();

            assertEquals(404, code);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            killServer();
        }
    }

    /**
     * Test a file cant be accessed outside the web root (using ..)
     */
    @Test
    public void testFile404OutsideRoot() {
        final File webRoot = new File("/tmp/root");
        webRoot.mkdirs();
        final WebServerConfig config = new WebServerConfig.Builder()
                .withWebRoot(webRoot)
                .build();
        startServer(config);
        try {
            final File testFile = new File("/tmp", "file.txt");
            final FileWriter writer = new FileWriter(testFile);
            writer.write(FILE_CONTENT);

            final URL url = new URL(WEB_BASE + "/../file.txt");
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            final int code = connection.getResponseCode();

            assertEquals(404, code);
            testFile.delete();
            webRoot.delete();

        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            killServer();
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

        while (!server.isServerRunning()) {
            sleep(20);
        }
    }

    private void killServer() {
        server.close();
        while (server.isServerRunning()) {
            sleep(20);
        }

    }

    private void sleep(final int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (final Exception e) {
        }
    }


}