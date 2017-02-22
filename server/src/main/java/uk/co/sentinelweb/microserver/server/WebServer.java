package uk.co.sentinelweb.microserver.server;

import org.apache.commons.io.input.ReaderInputStream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import uk.co.sentinelweb.microserver.server.cp.PingCommandProcessor;
import uk.co.sentinelweb.microserver.server.cp.ProxyCommandProcessor;
import uk.co.sentinelweb.microserver.server.cp.StreamCommandProcessor;

/**
 * see not at bottom for tip on how to use https
 *
 * @author robert
 */
public class WebServer extends Thread implements InterruptibleChannel {
    ServerSocket serverSocket;
    public boolean doRun = true;
    int port = C.SERVERPORT_DEF;
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
    public ArrayList<RequestProcessor> requestThreads = new ArrayList<>();
    public ArrayList<RequestProcessor> waitingRequests = new ArrayList<>();
    boolean serverRunning = false;
    public WebServer(final int port) {
        super();
        setName("Webserver");
        //wvu = new WebViewUtil(act);
        this.port = port;
        setPriority(C.WEB_SVR_PRIORITY);
        try {
//            new EpisodeCommandProcessor(act);
//            new ToolsCommandProcessor(act);
            new PingCommandProcessor();
//            new PodcastCommandProcessor(act);
//            new PlayerCommandProcessor(act);
            new StreamCommandProcessor();
//            new DashBoardCommandProcessor(act);
            new ProxyCommandProcessor();
//            new DeviceCommandProcessor(act);
//            new UpdateCommandProcessor(act);
//            new EpisodeJSONCommandProcessor(act);
//            new DownloadCommandProcessor(act);
//            new ImportCommandProcessor(act);
//            new HPROFCommandProcessor(act);
        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void run() {
        try {
            System.out.println("TCPServer: start");
            // dont sseem to be abe to coonect when this is enabled must be more to it,
//              ServerSocketFactory sslf = SSLServerSocketFactory.getDefault();
//              serverSocket = sslf.createServerSocket(this.port);
            serverSocket = new ServerSocket(this.port);
            while (doRun) {
                serverRunning = true;
                System.out.println( "TCPServer: running:"+serverSocket.getInetAddress().toString()+":"+serverSocket.getLocalPort()+":"+serverSocket.getLocalSocketAddress().toString());
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

    private void writeHeaders(final BufferedWriter out, final String type, final int cacheSec) throws IOException {
        final StringWriter sw = new StringWriter();
        sw.write("HTTP/1.1 200 OK" + "\r\n");
        sw.write("Date: " + sdf.format(new Date()) + " GMT\r\n");
        sw.write("Server: MyPOD Android" + "\r\n");
        sw.write("Last-Modified: " + sdf.format(new Date()) + " GMT\r\n");
        sw.write("Keep-Alive: timeout=15, max=100" + "\r\n");
        sw.write("Connection: Keep-Alive" + "\r\n");
        sw.write("Content-Type:" + type + "\r\n");
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

    public static void sendForward(final OutputStream out, final String path) throws IOException {
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");//"Tue, 16 Feb 2010 22:01:40 GMT"
        final StringWriter sw = new StringWriter();
        sw.write("HTTP/1.1 302 Found" + "\r\n");
        sw.write("Date: " + sdf.format(new Date()) + " GMT\r\n");
        sw.write("Server: MyPOD Android" + "\r\n");
        sw.write("Location: " + path + "\r\n");
        sw.write("Content-Type:" + MimeMap.get(path).mimeType + "\r\n");
        sw.write("\r\n");
        out.write(sw.toString().getBytes());
        out.flush();
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.InterruptibleChannel#close()
     */
    @Override
    public void close() throws IOException {
        for (final RequestProcessor rp : requestThreads) {
            try {
                rp.interrupt();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        serverSocket.close();
    }

    @Override
    public boolean isOpen() {
        return !serverSocket.isClosed();
    }

    public class RequestProcessor extends Thread implements InterruptibleChannel {
        private final Socket client;
        public boolean cancelled = false;
        private CommandProcessor currentCommand = null;

        public RequestProcessor( final Socket client) {
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
                String str = null;
                boolean headersProcessed = false;
                //Parsing headers, path method string
                final RequestData req = new RequestData();
                final SocketAddress addr = client.getRemoteSocketAddress();

                while (!headersProcessed) {//.indexOf("Connection:")==-1
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
                    ////Log.d(Globals.TAG, "S: Received: '" + str + "'");
                }


                System.out.println(new StringBuffer("S: path: '").append(req.getPath()).append("'").toString());
                if (req.getBasePath() != null && !"".equals(req.getBasePath())) {
                    try {
                        if (req.getPath().toLowerCase().endsWith("favicon.ico")) {
                            req.setPath(C.FAVICON);
                        }
                        // parsing GET garameters
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
                        String command = req.getParams().get("command");
                        if (command != null || req.getBasePath().startsWith("/c/")) {
                            // parsing POS parameters
                            final String contType = req.getHeaders().get("content-type");
                            req.setContentType(contType);
                            boolean isFormData = false;
                            if (contType != null) {
                                isFormData = contType.indexOf("multipart/form-data") == 0;
                            }
                            final String contLen = req.getHeaders().get("content-length");
                            int len = 0;
                            try {
                                len = Integer.parseInt(contLen);
                            } catch (final Exception e1) {
                            }
                            req.setContentLength(len);
                            final StringBuffer postRequest = new StringBuffer();

                            ////Log.d("TCP", "S: Content: '" + contLen + ":"+ post +"'");
                            if (!isFormData) {
                                if (contLen != null && req.isPost()) {
                                    //int len = Integer.parseInt(contLen);
                                    final char[] buf = new char[1000];
                                    int block = 0;
                                    while (len > 0) {//len>0
                                        try {
                                            if (!in.ready()) {
                                                break;
                                            }
                                            block = in.read(buf, 0, len > 1000 ? 1000 : len);
                                            str = new String(buf, 0, block);
                                            postRequest.append(str);
                                            len -= block;
                                        } catch (final Exception e) {
                                            System.err.println("POST Exception :::: " + postRequest.toString());
                                            e.printStackTrace(System.err);
                                        }
                                    }
                                    final String postParams = postRequest.toString();
                                    //Log.d(Globals.TAG,"POST :::: "+postParams);
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
                            final String outputType = "text/html; charset=utf-8";
                            if (req.getBasePath().startsWith("/c/")) {
                                final String[] pathSplit = req.getBasePath().split("/");
                                if (pathSplit.length > 2) {
                                    command = pathSplit[2];
                                }
                            }
                            CommandProcessor c = CommandProcessor.getCommandProcessor(command);
                            if (!c.singleton) {
                                final Constructor<CommandProcessor> cons = (Constructor<CommandProcessor>) c.getClass().getConstructor();
                                c = cons.newInstance();
                            }
                            currentCommand = c;
                            c.setInputStream(new ReaderInputStream(in, Charset.defaultCharset()));
                            c.setOutputStream(outputStream);
                            c.setRequest(this);
                            if (c != null) {
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
                                    System.err.println( "SocketException:" + e.getMessage());
                                    e.printStackTrace(System.err);
                                }
                            } else {
                                outWriter.write("({\"error\":\"Command not found\"})");
                            }
                            if (!c.singleton) {
                                c.release();
                            }
                            currentCommand = null;
                            outWriter.flush();
                        } else {
                            outWriter.write("({\"error\":\"not handled\"})");
                        }
                    } catch (final Exception e) {
                        System.err.println( "S: Error:" + req.getPath());
                        e.printStackTrace(System.err);
                    }
                }/*  req.getPath()!=null  */
            } catch (final Exception e) {
                System.err.println( "S: Error: "+e.getMessage());
                e.printStackTrace(System.err);
            } finally {
                try {
                    client.close();
                } catch (final Exception e) {
                    System.err.println( "S: Exception closing client:" );
                    e.printStackTrace(System.err);
                }
            }
            requestThreads.remove(this);
        }

        @Override
        public void close() throws IOException {
            try {
                requestThreads.remove(this);
                client.close();
            } catch (final Exception e) {
                System.err.println( "TCP:Exception closing client");
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

    private int writOutInputStream(final OutputStream out, final InputStream sr) throws IOException {
        final byte[] ch = new byte[1000];
        int read = 0;
        int pos = 0;
        while (read > -1) {
            out.write(ch, 0, read);
            read = sr.read(ch, 0, 1000);
            pos += read;
        }
        sr.close();
        return pos;
    }

    public int writeFile(final File assetPath, final OutputStream out) {
        try {
            final FileInputStream sr = new FileInputStream(assetPath);
            return writOutInputStream(out, sr);
        } catch (final IOException e) {
            System.err.println( "TCP:file:" + assetPath + ":" + e.getMessage());
            e.printStackTrace(System.err);
        }
        return -1;
    }

    private boolean checkParent(final File templateFolder, final File f) {
        final boolean res = false;
        File test = f;
        while (test.getParentFile() != null) {
            if (test.getParentFile().equals(templateFolder)) {
                return true;
            } else {
                test = test.getParentFile();
            }
        }
        return false;
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