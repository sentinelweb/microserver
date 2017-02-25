package uk.co.sentinelweb.microserver.server.cp;

import java.net.URLEncoder;
import java.util.HashMap;

import uk.co.sentinelweb.microserver.server.RequestData;
import uk.co.sentinelweb.microserver.server.WebServerUtil;

/**
 * Ping back server data
 */
public class PingCommandProcessor extends CommandProcessor{
	public PingCommandProcessor(final String path) {
		super(path);
        _handleHeaders =true;
	}

    @Override
	public String processCommand(final RequestData req) {
		final HashMap<String,Object> retVal = new HashMap<>();
        retVal.put("hello","world");
        retVal.put("ip", WebServerUtil.getIp().toString());
        retVal.put("method",req.getMethod());
        retVal.put("params",req.getParams());
        if (req.getPostData()!=null) {
            retVal.put("post", URLEncoder.encode(req.getPostData()));
        }
        retVal.put("cookies",req.getCookies());
        retVal.put("path",req.getPath());
        retVal.put("basepath",req.getBasePath());
        retVal.put("contentlength",req.getContentLength());
        retVal.put("port", server.getConfig().getPort());
        if (server.getConfig().getWebRoot()!=null) {
            retVal.put("root", server.getConfig().getWebRoot().getAbsolutePath());
        }
		try {
			getHeaderUtils().writeHeaders(req.getOutputStream(),"application/json" );
			return gson.toJson(retVal);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	
	@Override
	public void release() {

	}
	
}