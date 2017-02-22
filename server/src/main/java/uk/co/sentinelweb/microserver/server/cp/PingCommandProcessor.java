package uk.co.sentinelweb.microserver.server.cp;

import java.util.HashMap;

import uk.co.sentinelweb.microserver.server.RequestData;
import uk.co.sentinelweb.microserver.server.WebServerUtil;

public class PingCommandProcessor extends CommandProcessor{
	public static String command = "ping";
	public PingCommandProcessor() {
		super();
		handleHeaders=true;
	}
	
	@Override
	public String processCommand(final RequestData req) {
		final HashMap<String,String> params = req.getParams();
		final HashMap<String,Object> retVal = new HashMap<>();
        retVal.put("hello","world");
        retVal.put("ip", WebServerUtil.getIp().toString());
		try {
			writeHeaders(outputStream,"application/json" );
			return gson.toJson(retVal);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	
	@Override
	public void release() {

	}

	@Override
	public String getCommand() {
		return command;
	}
	
}