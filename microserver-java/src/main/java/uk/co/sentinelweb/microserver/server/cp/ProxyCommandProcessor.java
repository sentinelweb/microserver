package uk.co.sentinelweb.microserver.server.cp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

import uk.co.sentinelweb.microserver.server.HTTP;
import uk.co.sentinelweb.microserver.server.RequestData;

/**
 * A Proxy to another URL (e.g. on another domain)
 */
public class ProxyCommandProcessor extends CommandProcessor {
	private static final int CONN_TIMEOUT = 10;//sec
	
	public ProxyCommandProcessor(final String path) {
		super(path);
		_handleHeaders = true;
		// TODO Auto-generated constructor stub
	}


	@Override
	public String processCommand(final RequestData req) {
		final HashMap<String, String> params = req.getParams();
		final String url = params.get("url");
		if (url!=null) {
			final String urlDecode = URLDecoder.decode(url);
			try {
				final URL urlDownload = new URL(urlDecode) ;
				final HttpURLConnection huc= (HttpURLConnection)urlDownload.openConnection();
				huc.setConnectTimeout(CONN_TIMEOUT*1000);
				final InputStream is = huc.getInputStream();
				String type = params.get("type");
				if (type==null) type="text/xml; charset=utf-8";
				writeHeaders(new OutputStreamWriter(req.getOutputStream()), type, huc.getContentLength());
				final byte[] b=new byte[1000];
				int pos =0; 
				int bytesRead=0;
				
				while ((bytesRead = is.read(b,0,1000))>-1 && !cancel) {
                    req.getOutputStream().write(b,0,bytesRead);
					pos+=bytesRead;
				}
				is.close();
			} catch (final MalformedURLException e) {
				System.err.println("bad url:"+url);
			} catch (final IOException e) {
                System.err.println("ioexception:"+url);
			}
		} 
		return "";
	}

	
	private void writeHeaders(final Writer out, final String type, final long contlen) throws IOException {
		final StringWriter sw = new StringWriter();
		
		sw.write("HTTP/1.1 200 OK"+ HTTP.NEWLINE);
		sw.write("Date: Tue, 16 Feb 2010 22:01:40 GMT"+HTTP.NEWLINE);
		sw.write("Server: Apache/2.2.12 (Ubuntu)"+HTTP.NEWLINE);
		sw.write("Last-Modified: Tue, 16 Feb 2010 21:57:52 GMT"+HTTP.NEWLINE);
		sw.write("Accept-Ranges: bytes"+HTTP.NEWLINE);
		sw.write("Content-Length: "+contlen+HTTP.NEWLINE);
		sw.write("Keep-Alive: timeout=15, max=100"+HTTP.NEWLINE);
		sw.write("Connection: Keep-Alive"+HTTP.NEWLINE);
		sw.write("Content-Type:"+type+HTTP.NEWLINE);
		sw.write(HTTP.NEWLINE);
		

		out.write(sw.toString());
		out.flush();
	}

	@Override
	public void release() {

	}
	
}
