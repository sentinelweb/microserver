package uk.co.sentinelweb.microserver.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by robert on 23/02/2017.
 */

public class FileUtils {
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
            System.err.println("TCP:file:" + assetPath + ":" + e.getMessage());
            e.printStackTrace(System.err);
        }
        return -1;
    }

    boolean checkParent(final File templateFolder, final File f) {
        final String[] pathSplit = f.getAbsolutePath().split("/");
        File realFile = new File("/");
        for (final String filePart: pathSplit) {
            if (!"..".equals(filePart)) {
                realFile = new File(realFile, filePart);
            } else {
                realFile = realFile.getParentFile();
            }
        }
        while (realFile.getParentFile() != null) {
            if (realFile.getParentFile().equals(templateFolder)) {
                return true;
            } else {
                realFile = realFile.getParentFile();
            }
        }
        return false;
    }
}
