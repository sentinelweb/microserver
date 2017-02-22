package uk.co.sentinelweb.microserver.server.cp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import uk.co.sentinelweb.microserver.server.C;
import uk.co.sentinelweb.microserver.server.MimeMap;
import uk.co.sentinelweb.microserver.server.RequestData;

public class StreamCommandProcessor extends CommandProcessor {
    public static String command = "stream";
    private boolean close = false;
    static ArrayList<StreamCommandProcessor> activeRequests = new ArrayList<>();


    public StreamCommandProcessor() {
        super();
        handleHeaders = true;
        singleton = false;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public String processCommand(final RequestData req) {
        activeRequests.add(this);
        final HashMap<String, String> params = req.getParams();
        String path = null;
        try {
            path = params.get("path");
            path = path.replaceAll("..", "");
        } catch (final Exception e) {

        }
        final boolean dl = params.get("dl") != null;

        final File file = new File(C.STREAM_BASE + path);
        if (file == null || !file.exists()) {// not found
            final ArrayList<String> extraHeaders = new ArrayList<>();
            final OutputStream out = getOutputStream();
            extraHeaders.add("HTTP/1.1 " + 404 + " file not found" + "\r\n");
            try {
                writeHeaders(out, extraHeaders);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            return "";
        } else {//process
            final MimeMap.MimeData mimeRec = MimeMap.get(file.getName());
            try {
                getRequest().setName("SCP:" + path);
                System.out.println("StreamCP:>>>>>>>>>>>>>>new request >>>>>>>>>>>" + this.hashCode() + "(active:" + activeRequests.size() + ")" + " first:" + activeRequests.get(0).hashCode());

                System.out.println("StreamCP:method:" + req.getMethod());
                for (final String key : req.getHeaders().keySet()) {
                    System.out.println("StreamCP:hdr:" + key + "=" + req.getHeaders().get(key));
                }
                final long startTime = System.currentTimeMillis();
                int status = 200;
                final ArrayList<String> extraHeaders = new ArrayList<>();
                extraHeaders.add("HTTP/1.1 " + status + " " + (status == 200 ? "OK" : "Partial Content") + "\r\n");

                final String etag = "" + file.getPath().length();
                extraHeaders.add("ETag:\"" + etag + "-" + file.lastModified() + "\"" + "\r\n");
                final String connHeader = req.getHeaders().get("connection");
                //if (connHeader==null) {connHeader = req.getHeaders().get("connection");}
                if (connHeader != null && connHeader.toLowerCase().trim().indexOf("keep-alive") == 0) {
                    extraHeaders.add("Keep-Alive: timeout=15, max=100" + "\r\n");
                    extraHeaders.add("Connection: Keep-Alive" + "\r\n");
                } else if (connHeader != null && connHeader.toLowerCase().trim().indexOf("close") == 0) {
                    extraHeaders.add("Connection: close" + "\r\n");
                }
                long contlen = file.length();
                final OutputStream out = getOutputStream();
                String type = mimeRec.mimeType;
                if (req.isHead()) {
                    extraHeaders.add("Date: Thu, 18 Feb 2010 17:01:17 GMT\r\n");
                    extraHeaders.add("Server: MyPOD Android" + "\r\n");
                    extraHeaders.add("Last-Modified:" + new Date().toGMTString() + "\r\n");
                    extraHeaders.add("Content-Length:" + contlen + "\r\n");
                    if (dl) {
                        type = "application/x-octet-stream";
                        extraHeaders.add("Content-Type:" + type + "\r\n");
                    } else {
                        extraHeaders.add("Content-Type:" + type + "\r\n");
                    }
                    writeHeaders(out, extraHeaders);
                } else {
                    long start = 0;
                    long end = file.length();
                    String rangeHeader = req.getHeaders().get("range");//Range: bytes=7864320-7999999

                    if (connHeader != null && connHeader.toLowerCase().trim().indexOf("close") == 0) {
                        System.out.println(this.hashCode() + "(" + activeRequests.size() + ")" + "close req");
                        if (activeRequests.size() > 1) {
                            System.out.println("StreamCP:" + this.hashCode() + "(" + activeRequests.size() + ")" + "closing " + activeRequests.get(0).hashCode());
                            activeRequests.get(0).cancel();
                            end = 100;
                        }
                    } else if (rangeHeader != null && !dl) {// assumed to be in bytes
                        // range header parsing
                        status = 206;
                        rangeHeader = rangeHeader.trim();
                        final String[] eqSplit = rangeHeader.split("=");
                        if (eqSplit.length == 2) {
                            final String[] dashSplit = eqSplit[1].split("-");
                            if (dashSplit.length > 0) {
                                try {
                                    start = Long.parseLong(dashSplit[0]);
                                    if (dashSplit.length > 1) {
                                        end = Long.parseLong(dashSplit[1]);
                                    } else {
                                        end = file.length() - 1;//last byte
                                    }
                                    contlen = end - start + 1;


                                } catch (final NumberFormatException e) {
                                    System.out.println("-" + this.hashCode() + "StreamCP: couln't parse Range header '" + rangeHeader + "'");
                                }
                            }
                        }
                    }
                    extraHeaders.clear();
                    if (dl) {
                        extraHeaders.add("HTTP/1.1 200 OK" + "\r\n");
                        type = "application/octet-stream";
                        extraHeaders.add("Content-Type:" + type + "\r\n");
                        final String fileName = file.getName();
                        final String ua = req.getHeaders().get("user-agent");
                        if (ua != null && ua.toLowerCase().indexOf("playstation") > -1) {

                        } else {
                            extraHeaders.add("Content-Length:" + contlen + "\r\n");
                        }

                        System.out.println("StreamCP:filename:" + fileName);
                        extraHeaders.add("Content-disposition:attachment; filename=" + fileName + "\r\n");
                    } else {
                        extraHeaders.add("HTTP/1.1 " + status + " " + (status == 200 ? "OK" : "Partial Content") + "\r\n");
                        extraHeaders.add("ETag: \"" + etag + "-" + file.lastModified() + "\"" + "\r\n");
                        extraHeaders.add("Date: " + (new Date()).toGMTString() + "\r\n");
                        extraHeaders.add("Server: MyPOD Android" + "\r\n");
                        extraHeaders.add("Last-Modified: " + new Date(file.lastModified()).toGMTString() + "\r\n");
                        extraHeaders.add("Accept-Ranges:bytes" + "\r\n");
                        extraHeaders.add("Content-Length: " + contlen + "\r\n");
                        extraHeaders.add("Content-Type: " + type + "\r\n");
                        extraHeaders.add("Connection: close\r\n");
                        if (rangeHeader != null) {
                            extraHeaders.add("Content-Range:bytes " + start + "-" + end + "/" + (file.length()) + "\r\n");
                        }
                    }
                    writeHeaders(out, extraHeaders);
                    // set thread name for testing
                    getRequest().setName("SCP:" + file.getName() + ":" + status);

                    // write stream
                    final long openTime = System.currentTimeMillis();
                    final int buffSize = 100000;
                    final byte[] b = new byte[buffSize];
                    long pos = start;
                    long written = 0;
                    int bytesRead = 0;
                    //String firstBytes=null;
                    final InputStream is = new FileInputStream(file);

                    if (start > 0) {
                        is.skip(start);
                    }


                    System.out.println("StreamCP:start writing out data:" + this.hashCode() + "(" + activeRequests.size() + ")" + "filesize=" + file.length() + ": start:" + start + ": en:" + end + " status:" + status);
                    try {
                        while ((bytesRead = is.read(b, 0, buffSize)) > -1 && !close && !cancel) {
                            int bytesToWrite = bytesRead;
                            if (end - pos < bytesRead) {//WTF does this do? seems to stop writing out past end of file
                                bytesToWrite = (int) (end - pos + 1);
                            }
                            out.write(b, 0, bytesToWrite);
                            pos += bytesToWrite;
                            written += bytesToWrite;
                            getOutputStream().flush();
                            if (pos > end) {
                                break;
                            }
                        }
                    } catch (final Exception e) {
                        System.err.println("StreamCP:" + this.hashCode() + "(" + activeRequests.size() + ")" + e.getMessage());
                        e.printStackTrace(System.err);
                    }
                    final long endTime = System.currentTimeMillis();
                    System.out.println("StreamCP:" + this.hashCode() + "(" + activeRequests.size() + ")" + "finished... pos:" + pos + " written:" + written + " opentime:" + (openTime - startTime) + ": serveTime:" + (endTime - openTime));

                    is.close();
                }
            } catch (final FileNotFoundException e) {
                System.err.println("StreamCP:" + this.hashCode() + "(" + activeRequests.size() + ")" + e.getMessage());
                e.printStackTrace(System.err);
            } catch (final IOException e) {
                System.err.println("StreamCP:" + this.hashCode() + "(" + activeRequests.size() + ")" + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        System.out.println("StreamCP: reqmove active:" + this.hashCode() + "(" + activeRequests.size() + ")");
        activeRequests.remove(this);
        return "";
    }


    private void writeHeaders(final OutputStream out, final ArrayList<String> extra) throws IOException {
        final StringWriter sw = new StringWriter();
        for (final String extr : extra) {
            sw.write(extr);
            System.out.println("StreamCP:outheader:" + extr);
        }

        sw.write("\r\n");
        sw.flush();
        out.write(sw.toString().getBytes());
        sw.close();
        out.flush();
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub

    }

    public void cancel() {
        try {
            outputStream.close();
            getRequest().close();
            System.out.println("StreamCP: closed:" + this.hashCode() + "(" + activeRequests.size() + ")");
        } catch (final IOException e) {
            System.err.println("StreamCP: cancel:" + this.hashCode() + "(" + activeRequests.size() + ")" + e.getMessage());
            e.printStackTrace(System.err);
        }
        close = true;
    }

}
