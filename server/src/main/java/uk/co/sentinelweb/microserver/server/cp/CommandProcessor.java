package uk.co.sentinelweb.microserver.server.cp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import uk.co.sentinelweb.microserver.server.IServer;
import uk.co.sentinelweb.microserver.server.RequestData;
import uk.co.sentinelweb.microserver.server.WebServer;
import uk.co.sentinelweb.microserver.server.util.HeaderUtils;

public abstract class CommandProcessor {
    //public static HashMap<Class,CommandProcessor> commands = new HashMap<>();

    private static final String EX_KEY_EXCEPTION = "exception";
    private static final String EX_KEY_TYPE = "type";
    private static final String EX_KEY_STACK = "stack";
    private static final String EX_KEY_MSG = "msg";
    private final String _commandPath;

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

    public boolean _handleHeaders = false;
    public boolean singleton = true;
    public boolean cancel = false;
    private WebServer.RequestProcessor _requestProcessor;
    protected Gson gson;
    protected IServer server;

    public CommandProcessor(final String commandPath) {
        super();
        this._commandPath = commandPath;
        final GsonBuilder gsonb = new GsonBuilder();
        gson = gsonb.create();
    }

    public void setServer(final IServer server) {
        this.server = server;
    }

    public abstract String processCommand(RequestData req);

    public abstract void release();

    public void setRequestProcessor(final WebServer.RequestProcessor req) {
        _requestProcessor = req;
    }

    public WebServer.RequestProcessor getRequestProcessor() {
        return _requestProcessor;
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

    protected HeaderUtils getHeaderUtils(){
        return server.getHeaderUtils();
    }

    public String getCommandPath() {
        return _commandPath;
    }

}
