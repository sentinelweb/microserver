package uk.co.sentinelweb.microserver.server;

import org.apache.commons.io.input.ReaderInputStream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.nio.channels.InterruptibleChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import uk.co.sentinelweb.microserver.server.cp.CommandProcessor;

/**
 * see not at bottom for tip on how to use https
 *
 * @author robert
 */
public class WebServer extends Thread implements InterruptibleChannel {
    ServerSocket serverSocket;
    public boolean doRun = true;
    final int port;
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
    public ArrayList<RequestProcessor> requestThreads = new ArrayList<>();

    private boolean serverRunning = false;

    final WebServerConfig config;

    FileUtils _fileUtils = new FileUtils();

    public WebServer(final WebServerConfig config) {
        super();
        this.config = config;
        setName(config.getName());
        this.port = config.getPort();
        setPriority(config.getPriority());
    }

    public void run() {
        try {
            System.out.println("TCPServer: start");
            serverSocket = new ServerSocket(this.port);
            while (doRun) {
                serverRunning = true;
                System.out.println("TCPServer: running:" + serverSocket.getInetAddress().toString() + ":" + serverSocket.getLocalPort() + ":" + serverSocket.getLocalSocketAddress().toString());
                final Socket client = serverSocket.accept();
                try {
                    final RequestProcessor rp = new RequestProcessor(client);
                    requestThreads.add(rp);
                    rp.start();
                } catch (final Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            serverSocket.close();
        } catch (final Exception e) {
            System.err.println("S: Error" + e.getLocalizedMessage());
            e.printStackTrace(System.err);
            System.err.println("Could not start server:" + e.getMessage());
        }
        serverRunning = false;
    }

    public boolean isServerRunning() {
        return serverRunning;
    }

    /*
         * (non-Javadoc)
         * @see java.nio.channels.InterruptibleChannel#close()
         */
    @Override
    public void close()  {
        for (final RequestProcessor rp : requestThreads) {
            try {
                rp.cancel();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            try {
                rp.interrupt();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        for (final CommandProcessor cp : config.getCommandProcessorMap().values()) {
            try {
                cp.release();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        try {
            serverSocket.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        doRun = false;
    }

    @Override
    public boolean isOpen() {
        return !serverSocket.isClosed();
    }

    public class RequestProcessor extends Thread implements InterruptibleChannel {
        private final Socket client;
        public boolean cancelled = false;
        private CommandProcessor currentCommand = null;

        public RequestProcessor(final Socket client) {
            super();
            this.client = client;
            setName("RequestProcessor:" + requestThreads.size());
        }

        @Override
        public void run() {
            try {
                final BufferedReader in;

                final OutputStream outputStream;
                final BufferedWriter outWriter;
                in = new BufferedReader(new InputStreamReader(client.getInputStream()), 8 * 1024);
                outputStream = client.getOutputStream();
                outWriter = new BufferedWriter(new OutputStreamWriter(outputStream), 8 * 1024);// may not be used
                //Parsing headers, path method string
                final RequestData req = new RequestData();
                final SocketAddress addr = client.getRemoteSocketAddress();

                processHeaders(in, req);

                System.out.println(new StringBuffer("S: path: '").append(req.getPath()).append("'").toString());
                if (req.getBasePath() != null && !"".equals(req.getBasePath())) {
                    try {
//                        if (req.getPath().toLowerCase().endsWith("favicon.ico")) {
//                            req.setPath(C.FAVICON);
//                        }
                        // parsing GET garameters
                        processGetParams(req);
                        // parsing POST parameters
                        processPostParameters(in, req);
                        final String outputType = "text/html; charset=utf-8";
                        CommandProcessor c = getCommandProcessor(req);
                        if (c != null) {
                            if (!c.singleton) {
                                final Constructor<CommandProcessor> cons = (Constructor<CommandProcessor>) c.getClass().getConstructor();
                                c = cons.newInstance();
                            }
                            currentCommand = c;
                            c.setInputStream(new ReaderInputStream(in, Charset.defaultCharset()));
                            c.setOutputStream(outputStream);
                            c.setRequest(this);

                            try {
                                if (!c.handleHeaders) {
                                    writeHeaders(outWriter, outputType, -1);
                                }
                                final String processCommand = c.processCommand(req);
                                if (processCommand != null) {
                                    outWriter.write(processCommand);
                                } else {
                                    outWriter.write("");
                                }
                                //DebugLog.log(DebugLog.wsLog,TCPServer.class.getCanonicalName(), "Output of cmd::"+processCommand.toString());
                            } catch (final SocketException e) {
                                System.err.println("SocketException:" + e.getMessage());
                                e.printStackTrace(System.err);
                            }
                            if (!c.singleton) {
                                c.release();
                            }
                            currentCommand = null;
                        } else if (config.getWebRoot() != null) {
                            final File f = new File(config.getWebRoot(), req.getPath());
                            if (_fileUtils.checkParent(config.getWebRoot(), f) && f.exists()) {
                                final MimeMap.MimeData mimeData = MimeMap.get(req.getPath());
                                final String mimeType = mimeData != null ? mimeData.mimeType : MimeMap.MIME_APPLICATION_OCTET_STREAM;
                                writeHeaders(outWriter, mimeType, config.getCacheTimeSecs());
                                outWriter.flush();
                                _fileUtils.writeFile(f, outputStream);
                            } else {
                                write404(outWriter, req.getPath());
                            }
                        } else {
                            outWriter.write("({\"error\":\"Command not found\"})");
                        }

                        outWriter.flush();
                    } catch (final Exception e) {
                        System.err.println("S: Error:" + req.getPath());
                        e.printStackTrace(System.err);
                    }
                }/*  req.getPath()!=null  */
            } catch (final Exception e) {
                System.err.println("S: Error: " + e.getMessage());
                e.printStackTrace(System.err);
            } finally {
                try {
                    client.close();
                } catch (final Exception e) {
                    System.err.println("S: Exception closing client:");
                    e.printStackTrace(System.err);
                }
            }
            requestThreads.remove(this);
        }

        private void processHeaders(final BufferedReader in, final RequestData req) throws IOException {
            String str;
            boolean headersProcessed = false;
            while (!headersProcessed) {
                str = in.readLine();
                //Log.d(Globals.TAG, "S: Request: '" + str + "'");
                if (str != null && str.indexOf("GET ") != -1) {
                    req.setMethod("GET");
                    req.setPath(str.substring("GET ".length(), str.length() - " HTTP/1.1".length()));
                } else if (str != null && str.indexOf("POST ") != -1) {
                    req.setMethod("POST");
                    req.setPath(str.substring("POST ".length(), str.length() - " HTTP/1.1".length()));
                    //post=true;
                } else if (str != null && str.indexOf("HEAD ") != -1) {
                    req.setMethod("HEAD");
                    req.setPath(str.substring("HEAD ".length(), str.length() - " HTTP/1.1".length()));
                } else if (str != null) {
                    final String[] header = str.split(":");
                    if (header.length == 2) {
                        if (header[0].trim().toLowerCase().equals("cookie")) {
                            final HashMap<String, String> cookie = new HashMap<>();
                            final String[] cookieDataSplit = header[1].split(";");
                            for (final String cookieData : cookieDataSplit) {
                                final String[] valSplit = cookieData.split(":");
                                if (valSplit.length > 1) {
                                    cookie.put(valSplit[0], valSplit[1]);
                                }
                            }
                        } else {
                            req.getHeaders().put(header[0].trim().toLowerCase(), header[1].trim());
                            //DebugLog.log(DebugLog.wsLog,TCPServer.class.getCanonicalName() ,"S: Received Header: '" + header[0].trim().toLowerCase()+" = "+header[1].trim() + "'");
                        }
                    }
                }
                if ("".equals(str) || str == null) {
                    headersProcessed = true;
                }
                //System.out.println("Header - '" + str + "'");
            }
        }

        private void processGetParams(final RequestData req) {
            if (req.getPath().indexOf("?") > -1) {
                final String queryString = req.getPath().substring(req.getPath().indexOf("?") + 1);
                //Log.e("TCP","command : queryString="+queryString);
                final String[] params = queryString.split("&");
                ////Log.e("TCP","params="+params.length);

                for (final String pair : params) {
                    ////Log.e("TCP","pair="+pair);
                    final String[] paramSplit = pair.split("=");
                    if (paramSplit.length > 1) {
                        req.getParams().put(paramSplit[0], URLDecoder.decode(paramSplit[1]));
                    }
                }
            }
        }

        private void processPostParameters(final BufferedReader in, final RequestData req) {
            String str;
            final String contType = req.getHeaders().get("content-type");
            req.setContentType(contType);
            boolean isFormData = false;
            if (contType != null) {
                isFormData = contType.indexOf("multipart/form-data") == 0;
            }
            final String contentLengthString = req.getHeaders().get("content-length");
            int contentLength = 0;
            try {
                contentLength = Integer.parseInt(contentLengthString);
            } catch (final Exception e1) {

            }
            req.setContentLength(contentLength);
            final StringBuilder postRequest = new StringBuilder();

            ////Log.d("TCP", "S: Content: '" + contentLengthString + ":"+ post +"'");
            if (!isFormData) {
                if (contentLength > 0 && req.isPost()) {
                    final char[] buf = new char[1000];
                    int block = 0;
                    while (contentLength > 0) {
                        try {
                            if (!in.ready()) {
                                break;
                            }
                            block = in.read(buf, 0, contentLength > 1000 ? 1000 : contentLength);
                            str = new String(buf, 0, block);
                            postRequest.append(str);
                            contentLength -= block;
                        } catch (final Exception e) {
                            System.err.println("POST Exception :::: " + postRequest.toString());
                            e.printStackTrace(System.err);
                        }
                    }
                    final String postParams = postRequest.toString();
                    System.out.println("POST :::: " + postParams);
                    if (!postParams.startsWith("{") && !postParams.startsWith("[")) {// crude test for JSON posting - how can i do it better?
                        final String[] params = postParams.split("&");
                        for (final String pair : params) {
                            final String[] paramSplit = pair.split("=");
                            req.getParams().put(paramSplit[0].toLowerCase(), URLDecoder.decode(paramSplit[1]));
                        }
                    } else {
                        req.setPostData(postParams);
                    }
                }
            } else {
                // next ttest for boundary : "boundary=----WebKitFormBoundaryFgZcxCfWUB2BdVQl"
                // ignoring for now

            }
        }

        private CommandProcessor getCommandProcessor(final RequestData req) {
            int maxSize = -1;
            CommandProcessor found = null;
            for (final String commandPath : config.getCommandProcessorMap().keySet()) {
                if (req.getPath().startsWith(commandPath) && commandPath.length() > maxSize) {
                    maxSize = commandPath.length();
                    found = config.getCommandProcessorMap().get(commandPath);
                }
            }
            return found;
        }

        @Override
        public void close() throws IOException {
            try {
                requestThreads.remove(this);
                client.close();
            } catch (final Exception e) {
                System.err.println("TCP:Exception closing client");
                e.printStackTrace(System.err);
            }
        }

        @Override
        public boolean isOpen() {
            // TODO Auto-generated method stub
            return false;
        }

        public void cancel() {
            cancelled = true;
            if (currentCommand != null) {
                currentCommand.cancel = true;
            }
        }
    }

    private void writeHeaders(final BufferedWriter out, final String mimeType, final int cacheSec) throws IOException {
        final StringWriter sw = new StringWriter();
        sw.write("HTTP/1.1 200 OK" + "\r\n");
        sw.write("Date: " + sdf.format(new Date()) + " GMT\r\n");
        sw.write("Server: " + config.getName() + "\r\n");
        sw.write("Last-Modified: " + sdf.format(new Date()) + " GMT\r\n");
        sw.write("Keep-Alive: timeout=15, max=100" + "\r\n");
        sw.write("Connection: Keep-Alive" + "\r\n");
        sw.write("Content-Type:" + mimeType + "\r\n");
        if (cacheSec == -1) {
            sw.write("Expires: -1" + "\r\n");
            sw.write("Cache-Control: private, max-age=0" + "\r\n");
        } else {
            sw.write("Expires: " + sdf.format(new Date(System.currentTimeMillis() - cacheSec * 1000)) + " GMT\r\n");
            sw.write("Cache-Control: private, max-age=" + cacheSec + "\r\n");
        }
        sw.write("\r\n");

        out.write(sw.toString());
        //Log.d(Globals.TAG,sw.toString());
        out.flush();
    }

    private void sendForward(final OutputStream out, final String path) throws IOException {
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");//"Tue, 16 Feb 2010 22:01:40 GMT"
        final StringWriter sw = new StringWriter();
        sw.write("HTTP/1.1 302 Found" + "\r\n");
        sw.write("Date: " + sdf.format(new Date()) + " GMT\r\n");
        sw.write("Server: " + config.getName() + "\r\n");
        sw.write("Location: " + path + "\r\n");
        sw.write("Content-Type:" + MimeMap.get(path).mimeType + "\r\n");
        sw.write("\r\n");
        out.write(sw.toString().getBytes());
        out.flush();
    }

    private void write404(final BufferedWriter outWriter, final String path) throws IOException {
        final StringWriter sw = new StringWriter();
        sw.write("HTTP/1.1 404 Not Found" + "\r\n");
        sw.write("Date: " + sdf.format(new Date()) + " GMT\r\n");
        sw.write("Server: " + config.getName() + "\r\n");
        sw.write("\r\n");

        outWriter.write(sw.toString());
    }



	
	/* ************* https code  *************************************
     * make a key store using : keytool -genkey -alias alias -keypass simulator -keystore lig.keystore -storepass simulator
	 * 
	 * http://stackoverflow.com/questions/2308479/simple-java-https-server
	 * 
	  try
        {
            // setup the socket address
            InetSocketAddress address = new InetSocketAddress ( InetAddress.getLocalHost (), config.getHttpsPort () );

            // initialise the HTTPS server
            HttpsServer httpsServer = HttpsServer.create ( address, 0 );
            SSLContext sslContext = SSLContext.getInstance ( "TLS" );

            // initialise the keystore
            char[] password = "simulator".toCharArray ();
            KeyStore ks = KeyStore.getInstance ( "JKS" );
            FileInputStream fis = new FileInputStream ( "lig.keystore" );
            ks.load ( fis, password );

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance ( "SunX509" );
            kmf.init ( ks, password );

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance ( "SunX509" );
            tmf.init ( ks );

            // setup the HTTPS context and parameters
            sslContext.init ( kmf.getKeyManagers (), tmf.getTrustManagers (), null );
            httpsServer.setHttpsConfigurator ( new HttpsConfigurator( sslContext )
            {
                public void configure ( HttpsParameters params )
                {
                    try
                    {
                        // initialise the SSL context
                        SSLContext c = SSLContext.getDefault ();
                        SSLEngine engine = c.createSSLEngine ();
                        params.setNeedClientAuth ( false );
                        params.setCipherSuites ( engine.getEnabledCipherSuites () );
                        params.setProtocols ( engine.getEnabledProtocols () );

                        // get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters ();
                        params.setSSLParameters ( defaultSSLParameters );
                    }
                    catch ( Exception ex )
                    {
                        ILogger log = new LoggerFactory ().getLogger ();
                        log.exception ( ex );
                        log.error ( "Failed to create HTTPS port" );
                    }
                }
            } );
            
            // RM: dont need this just te serversocket
            // LigServer server = new LigServer ( httpsServer );
            //  joinableThreadList.add ( server.getJoinableThread () );
        }
        catch ( Exception exception )
        {
            log.exception ( exception );
            log.error ( "Failed to create HTTPS server on port " + config.getHttpsPort () + " of localhost" );
        }
	 */
} 