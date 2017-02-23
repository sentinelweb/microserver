package uk.co.sentinelweb.microserver.server.cp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;

import uk.co.sentinelweb.microserver.server.RequestData;
import uk.co.sentinelweb.microserver.server.WebServer;

public abstract class CommandProcessor {
    public static final String NEWLINE = "\r\n";
    //public static HashMap<Class,CommandProcessor> commands = new HashMap<>();

    private static final String EX_KEY_EXCEPTION = "exception";
    private static final String EX_KEY_TYPE = "type";
    private static final String EX_KEY_STACK = "stack";
    private static final String EX_KEY_MSG = "msg";
    //Context _cxt;

    //JSONData json = new JSONData();
    public static enum ErrorTags {
        ERROR, WARNING, INFO, SUCCESS
    }

    public static final String RESULT = "result";
    public static final String MESSAGE = "message";
    public static final String CODE = "code";
    public static final String RESPONSE = "response";

    public static final int CODE_S_OK = 0;
    public static final String MSG_OK = "OK";
    public static final int CODE_E_GENERAL_ERROR = 1;
    public static final String MSG_GENERAL_ERROR = "General Error ...";
    public static final int CODE_E_EXCEPTION = 2;
    public static final String MSG_EXCEPTION_ERROR = "Exception ...";
    public static final int CODE_E_UNKNOWN_PARAMETER = 3;// can be sent if a parameter doesn't exist too
    public static final String MSG_UNKNOWN_PARAMETER = "Unknown parameter ...";
    public static final int CODE_E_NO_DATA = 4;
    public static final String MSG_NO_DATA = "No Data";
    public static final int CODE_E_NOID = 5;
    public static final String MSG_NOID = "No ID";
    public static final int CODE_E_BAD_ID = 6;
    public static final String MSG_BAD_ID = "Bad ID";

    public static final String DATA = "data";

    protected OutputStream outputStream;
    protected InputStream inputStream;
    public boolean handleHeaders = false;
    public boolean singleton = true;
    public boolean cancel = false;
    private WebServer.RequestProcessor _request;
    protected Gson gson;

    public CommandProcessor() {
        super();
        final GsonBuilder gsonb = new GsonBuilder();
        gson = gsonb.create();

    }


    public abstract String processCommand(RequestData req);

    public abstract void release();

    public abstract String getCommand();

    protected OutputStream getOutputStream() {
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

    public void setRequest(final WebServer.RequestProcessor req) {
        _request = req;
    }

    public HashMap<String, String> getDefaultHeaders() {
        final HashMap<String, String> headers = new HashMap<>();
        headers.put("Date", new Date().toGMTString() + " GMT");
        headers.put("Server", "MicroServer java");
        headers.put("Last-Modified", new Date().toGMTString() + " GMT");
        headers.put("Keep-Alive", "timeout=15, max=100");
        headers.put("Connection", "Keep-Alive");
        headers.put("Expires", "-1");
        headers.put("Cache-Control", "private, max-age=0");
        return headers;
    }

    public WebServer.RequestProcessor getRequest() {
        return _request;
    }

    protected void writeHeaders(final OutputStream out, final String type) throws IOException {
        final HashMap<String, String> headers = getDefaultHeaders();
        headers.put("Content-Type", type);
        writeHeaders(out, type, headers);
    }

    protected void writeHeaders(final OutputStream out, final String type, final HashMap<String, String> extras) throws IOException {
        final HashMap<String, String> headers = getDefaultHeaders();
        headers.put("Content-Type", type);
        headers.putAll(extras);
        writeHeaders(out, headers);
    }

    protected void writeHeaders(final OutputStream out, final HashMap<String, String> headers) throws IOException {
        final StringWriter sw = new StringWriter();
        sw.write("HTTP/1.1 200 OK" + NEWLINE);
        for (final String name : headers.keySet()) {
            sw.write(name + ": " + headers.get(name) + NEWLINE);
        }
        sw.write(NEWLINE);

        out.write(sw.toString().getBytes());
        //Log.d(Globals.TAG,sw.toString());
        out.flush();
    }

    protected void setResultOK(final HashMap<String, Object> response) {
        setResult(response, ErrorTags.SUCCESS, MSG_OK, CODE_S_OK);
    }

    protected void setResult(final HashMap<String, Object> response, final ErrorTags result, final String msg, final int code) {
        final HashMap<String, Object> resultMap = makeResult(result, msg, code);
        response.put(RESPONSE, resultMap);
    }

    protected String sendResponse(final ErrorTags result, final String msg, final int code) {
        final HashMap<String, Object> resultMap = makeResult(result, msg, code);
        final HashMap<String, Object> response = new HashMap<>();
        response.put(RESPONSE, resultMap);
        return gson.toJson(response);
    }

    private HashMap<String, Object> makeResult(final ErrorTags result, final String msg, final int code) {
        final HashMap<String, Object> response = new HashMap<>();
        response.put(RESULT, result);
        response.put(MESSAGE, msg);
        response.put(CODE, code);
        return response;
    }

    //	{
//		response : {
//			result: {
//				result: "ERROR"|"WARNING"|"INFO"|"SUCCESS",
//				msg : "message ...",
//				code : num,
//				exception:{
//					msg:"ex msg",
//					type:"ex.className",
//					stack:"stackTrace"
//				}
//			}
//		}
//	}
    protected String sendExceptionResponse(final Throwable e) {
        // write log messages
        System.err.println(this.getClass().getName() + "exception:" + e.getMessage());
        e.printStackTrace(System.err);

        final HashMap<String, Object> response = new HashMap<>();
        setResult(response, ErrorTags.ERROR, MSG_EXCEPTION_ERROR, CODE_E_EXCEPTION);
        final HashMap<String, Object> result = (HashMap<String, Object>) response.get(RESPONSE);
        final HashMap<String, Object> exMap = new HashMap<>();
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        exMap.put(EX_KEY_MSG, e.getMessage() != null ? e.getMessage() : "null");
        exMap.put(EX_KEY_STACK, sw.toString());
        exMap.put(EX_KEY_TYPE, e.getClass().getSimpleName());
        result.put(EX_KEY_EXCEPTION, exMap);
        return gson.toJson(response);
    }
}
