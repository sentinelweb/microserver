package uk.co.sentinelweb.microserver.server.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import uk.co.sentinelweb.microserver.server.HTTP;
import uk.co.sentinelweb.microserver.server.MimeMap;
import uk.co.sentinelweb.microserver.server.WebServerConfig;

/**
 * Utility for writing headers
 * TODO cleanup
 * Created by robert on 25/02/2017.
 */
public class HeaderUtils {
    final WebServerConfig _config;

    private final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

    public HeaderUtils(final WebServerConfig config) {
        this._config = config;
    }

    public HashMap<String, String> getCacheControlHeaders(final int cacheSec) {
        final HashMap<String, String> headers = new HashMap<>();
        if (cacheSec == -1) {
            headers.put(HTTP.HEADER_EXPIRES, "-1");
            headers.put(HTTP.HEADER_CACHE_CONTROL, "private, max-age=0");
        } else {
            headers.put(HTTP.HEADER_EXPIRES, sdf.format(new Date(System.currentTimeMillis() - cacheSec * 1000)) + " GMT");
            headers.put(HTTP.HEADER_CACHE_CONTROL, "max-age=" + cacheSec);
        }
        return headers;
    }

    public void outputHeaders(final OutputStream out, final HashMap<String, String> headers, final StringWriter sw) throws IOException {
        for (final String name : headers.keySet()) {
            sw.write(name + ": " + headers.get(name) + HTTP.NEWLINE);
        }
        sw.write(HTTP.NEWLINE);

        out.write(sw.toString().getBytes());
        out.flush();
    }

    public void writeHeaders(final BufferedWriter out, final String mimeType, final int cacheSec) throws IOException {
        final StringWriter sw = new StringWriter();
        sw.write(HTTP.HTTP_1_1_200_OK +HTTP.NEWLINE);
        sw.write(HTTP.HEADER_DATE+": " + sdf.format(new Date()) + " GMT"+HTTP.NEWLINE);
        sw.write(HTTP.HEADER_SERVER+": " + _config.getName() + "\r\n");
        sw.write("Last-Modified: " + sdf.format(new Date()) + " GMT"+HTTP.NEWLINE);
        sw.write("Keep-Alive: timeout=15, max=100" +HTTP.NEWLINE);
        sw.write("Connection: Keep-Alive" + "\r\n");
        sw.write(HTTP.HEADER_CONTENT_TYPE+":" + mimeType + "\r\n");
        if (cacheSec == -1) {
            sw.write("Expires: -1" + "\r\n");
            sw.write("Cache-Control: private, max-age=0" + "\r\n");
        } else {
            sw.write("Expires: " + sdf.format(new Date(System.currentTimeMillis() - cacheSec * 1000)) + " GMT\r\n");
            sw.write("Cache-Control: private, max-age=" + cacheSec + "\r\n");
        }
        sw.write("\r\n");

        out.write(sw.toString());
        out.flush();
    }

    public void sendForward(final OutputStream out, final String path) throws IOException {
        final StringWriter sw = new StringWriter();
        sw.write(HTTP.HTTP_1_1_302_FOUND + "\r\n");
        sw.write(HTTP.HEADER_DATE+": " + sdf.format(new Date()) + " GMT\r\n");
        sw.write(HTTP.HEADER_SERVER+": " + _config.getName() + "\r\n");
        sw.write(HTTP.HEADER_LOCATION+": " + path + "\r\n");
        final MimeMap.MimeData mimeData = MimeMap.get(path);
        if (mimeData!=null) {
            sw.write(HTTP.HEADER_CONTENT_TYPE + ":" + mimeData.mimeType + "\r\n");
        }
        sw.write("\r\n");
        out.write(sw.toString().getBytes());
        out.flush();
    }

    public void write404(final BufferedWriter outWriter, final String path) throws IOException {
        final StringWriter sw = new StringWriter();
        sw.write(HTTP.HTTP_1_1_404_NOT_FOUND + "\r\n");
        sw.write(HTTP.HEADER_DATE+": " + sdf.format(new Date()) + " GMT\r\n");
        sw.write(HTTP.HEADER_SERVER+": " + _config.getName() + "\r\n");
        sw.write("\r\n");

        outWriter.write(sw.toString());
    }

    public HashMap<String, String> getDefaultHeaders() {
        final HashMap<String, String> headers = new HashMap<>();
        headers.put(HTTP.HEADER_DATE, new Date().toGMTString() + " GMT");
        headers.put(HTTP.HEADER_SERVER, "MicroServer java");
        headers.put(HTTP.HEADER_LAST_MODIFIED, new Date().toGMTString() + " GMT");
        headers.put(HTTP.HEADER_KEEP_ALIVE, "timeout=15, max=100");
        headers.put(HTTP.HEADER_CONNECTION, "Keep-Alive");
        headers.put(HTTP.HEADER_EXPIRES, "-1");
        headers.put(HTTP.HEADER_CACHE_CONTROL, "private, max-age=0");
        return headers;
    }

    public void writeHeaders(final OutputStream out, final String type) throws IOException {
        final HashMap<String, String> headers = getDefaultHeaders();
        headers.put(HTTP.HEADER_CONTENT_TYPE, type);
        writeHeaders(out, type, headers);
    }

    public void writeHeaders(final OutputStream out, final String type, final HashMap<String, String> extras) throws IOException {
        final HashMap<String, String> headers = getDefaultHeaders();
        headers.put(HTTP.HEADER_CONTENT_TYPE, type);
        headers.putAll(extras);
        writeHeaders(out, headers);
    }

    public void writeHeaders(final OutputStream out, final String type, final int cacheSec) throws IOException {
        final HashMap<String, String> headers = getDefaultHeaders();
        headers.put(HTTP.HEADER_CONTENT_TYPE, type);
        headers.putAll(getCacheControlHeaders(cacheSec));
        writeHeaders(out, headers);
    }

    protected void writeHeaders(final OutputStream out, final HashMap<String, String> headers) throws IOException {
        final StringWriter sw = new StringWriter();
        sw.write(HTTP.HTTP_1_1_200_OK + HTTP.NEWLINE);
        outputHeaders(out, headers, sw);
    }

}

