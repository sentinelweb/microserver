package uk.co.sentinelweb.microservice.cp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import java.io.IOException;

import uk.co.sentinelweb.microserver.server.MimeMap;
import uk.co.sentinelweb.microserver.server.RequestData;
import uk.co.sentinelweb.microserver.server.cp.CommandProcessor;

/**
 * Write out drawable
 * Created by robert on 25/02/2017.
 */
public class DrawableCommandProcessor extends CommandProcessor {
    public static final String TAG = DrawableCommandProcessor.class.getSimpleName();
    public static final String DEFAULT_TYPE_DRAWABLE = "drawable";
    Context _context;

    public DrawableCommandProcessor(final String path,final Context context) {
        super(path);
        _context = context;
        _handleHeaders = true;
    }

    @Override
    public String processCommand(final RequestData req) {
        try {
            final String[] theEls = req.getPath().split("/");
            if (theEls.length > 1) {
                String filePart;
                String type= DEFAULT_TYPE_DRAWABLE;
                if (theEls.length==4) {
                    filePart = theEls[3];
                    type = theEls[2];
                } else {
                    filePart = theEls[2];
                }

                final int dotIndex = filePart.lastIndexOf(".");
                if (dotIndex > -1) {
                    filePart = filePart.substring(0, dotIndex);
                }

                ////Log.d("TCP", "S: sp: '" + substring+" : "+ theEls[1] + "'");
                final int rid = _context.getResources().getIdentifier(filePart, type, _context.getPackageName());
                Log.d(TAG, "S: drawable rid: " + rid + " : " + filePart + " : ");
                getHeaderUtils().writeHeaders(req.getOutputStream(), MimeMap.MIME_IMAGE_PNG, server.getConfig().getCacheTimeSecs());
                final Bitmap bitmapOrg = BitmapFactory.decodeResource(_context.getResources(), rid);
                new BitmapDrawable(_context.getResources(), bitmapOrg).getBitmap().compress(Bitmap.CompressFormat.PNG, 90, req.getOutputStream());
                return "";
            }
            getHeaderUtils().write404(req.getOutputWriter(), req.getPath());
        } catch (final IOException e) {
            Log.d(DrawableCommandProcessor.class.getSimpleName(), "Error writing drawable:" + req.getPath(), e);
        }
        return "";
    }

    @Override
    public void release() {

    }
}
