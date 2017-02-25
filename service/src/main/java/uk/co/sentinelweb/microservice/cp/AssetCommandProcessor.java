package uk.co.sentinelweb.microservice.cp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import uk.co.sentinelweb.microserver.server.MimeMap;
import uk.co.sentinelweb.microserver.server.RequestData;
import uk.co.sentinelweb.microserver.server.cp.CommandProcessor;
import uk.co.sentinelweb.microserver.server.util.FileUtils;

/**
 * Created by robert on 25/02/2017.
 */

public class AssetCommandProcessor extends CommandProcessor{
    private static final String TAG = AssetCommandProcessor.class.getSimpleName();
    final Context _context;
    FileUtils f = new FileUtils();

    public AssetCommandProcessor(final String path,final Context context) {
        super(path);
        _context = context;
        _handleHeaders = true;
    }

    @Override
    public String processCommand(final RequestData req) {
        try {
            String actualPath;
            actualPath = req.getBasePath().substring(getCommandPath().length());
            actualPath = RequestData.basePath(actualPath);
            final OutputStream outputStream = req.getOutputStream();
            final MimeMap.MimeData mimeData = MimeMap.get(actualPath);
            Log.d(TAG,"Assetpath: "+actualPath+":"+ req.getBasePath());
            final boolean binary = mimeData==null || mimeData._binary == MimeMap.Binary.YES;
            final String mimeType = mimeData!=null ? mimeData.mimeType : MimeMap.MIME_TEXT_HTML;
            getHeaderUtils().writeHeaders(outputStream, mimeType + "; charset=utf-8", server.getConfig().getCacheTimeSecs());
            if (!req.isHead()) {
                writeAssetByType(outputStream, req.getOutputWriter(), actualPath, binary);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void release() {

    }

    private void writeAssetByType(final OutputStream outputStream, final BufferedWriter outWriter, final String actualPath, final boolean binary) throws IOException {
        if (binary) {
            writeAsset(actualPath, outputStream);
        } else {
            outWriter.write(getAssetString(actualPath));
        }
    }

    public int writeAsset(final String assetPath, final OutputStream out) {
        try {
            final InputStream sr = _context.getAssets().open(assetPath);
            return writeOutInputStream(out, sr);
        } catch (final IOException e) {
            Log.d(TAG, "HTTP:asset:" + assetPath + ":" + e.getMessage(), e);
        }
        return -1;
    }

    private int writeOutInputStream(final OutputStream out, final InputStream sr) throws IOException {
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

    private String getAssetString(final String assetPath) {
        try {
            final InputStream sr = _context.getAssets().open(assetPath);
            final StringWriter sw = new StringWriter();
            final byte[] ch = new byte[1000];
            int pos = 0;
            while (pos > -1) {
                sw.write(new String(ch, 0, pos));
                pos = sr.read(ch, 0, 1000);

            }
            sr.close();
            final String out = sw.toString();
            return out;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
