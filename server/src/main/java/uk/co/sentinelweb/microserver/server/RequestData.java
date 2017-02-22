package uk.co.sentinelweb.microserver.server;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestData {
	private HashMap<String,String> headers = new HashMap<String,String>();
	private String method = "GET";
	private String path = "";
	HashMap<String, String> params=new HashMap<String,String>();
	ArrayList<HashMap<String,String>> cookies = new ArrayList<HashMap<String,String>>();
	public String remoteAddress = null;
	private int contentLength = 0;
	private String contentType = "text/plain";
	private String postData =null;
	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public HashMap<String, String> getParams() {
		return params;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	public String getBasePath() {
		return basePath(this.path);
	}
	public static String basePath(String url) {
		String loc=url;
		if (loc.indexOf("?")>-1) {
			loc=loc.substring(0,loc.indexOf("?"));
		}
		if (loc.indexOf("#")>-1) {
			loc=loc.substring(0,loc.indexOf("#"));
		}
		return loc;
	}
	public boolean isPost() {return "POST".equals(method);}
	public boolean isGet() {return "GET".equals(method);}
	public boolean isHead() {return "HEAD".equals(method);}

	public ArrayList<HashMap<String, String>> getCookies() {
		return cookies;
	}

	public void setCookies(ArrayList<HashMap<String, String>> cookies) {
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
	public void setContentLength(int contentLength) {
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
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setPostData(String postParams) {
		this.postData=postParams;
		
	}

	public String getPostData() {
		return postData;
	}
	
	
}
