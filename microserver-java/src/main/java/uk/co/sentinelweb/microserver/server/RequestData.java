package uk.co.sentinelweb.microserver.server;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class RequestData {
    private HashMap<String, String> headers = new HashMap<>();
    private String method = "GET";
    private String path = "";
    HashMap<String, String> params = new HashMap<>();
    ArrayList<HashMap<String, String>> cookies = new ArrayList<>();
    public String remoteAddress = null;
    private int contentLength = 0;
    private String contentType = "text/plain";
    private String postData = null;
    protected OutputStream outputStream;
    protected InputStream inputStream;
    private BufferedWriter _outputWriter;

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(final HashMap<String, String> headers) {
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(final HashMap<String, String> params) {
        this.params = params;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getBasePath() {
        return basePath(this.path);
    }

    public static String basePath(final String url) {
        String loc = url;
        if (loc.indexOf("?") > -1) {
            loc = loc.substring(0, loc.indexOf("?"));
        }
        if (loc.indexOf("#") > -1) {
            loc = loc.substring(0, loc.indexOf("#"));
        }
        return loc;
    }

    public boolean isPost() {
        return "POST".equals(method);
    }

    public boolean isGet() {
        return "GET".equals(method);
    }

    public boolean isHead() {
        return "HEAD".equals(method);
    }

    public ArrayList<HashMap<String, String>> getCookies() {
        return cookies;
    }

    public void setCookies(final ArrayList<HashMap<String, String>> cookies) {
        this.cookies = cookies;
    }

    /**
     * @return the contentLength
     */
    public int getContentLength() {
        return contentLength;
    }

    /**
     * @param contentLength the contentLength to set
     */
    public void setContentLength(final int contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public void setPostData(final String postParams) {
        this.postData = postParams;

    }

    public String getPostData() {
        return postData;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(final OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * @return the inputStream
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * @param inputStream the inputStream to set
     */
    public void setInputStream(final InputStream inputStream) {
        this.inputStream = inputStream;
    }


    public void setOutputWriter(final BufferedWriter outputWriter) {
        _outputWriter = outputWriter;
    }

    public BufferedWriter getOutputWriter() {
        return _outputWriter;
    }
}
