package uk.co.sentinelweb.microserver.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A simple mime type map - to add more add new {@link MimeData} records to {@link #mimeTypes} and call {@link #initExtensionMap()}
 */

public class MimeMap {
    public static final String MIME_AUDIO_MPEG3 = "audio/mpeg3";
    public static final String MIME_AUDIO_MP3 = "audio/mp3";
    public static final String MIME_X_AUDIO_MP3 = "x-audio/mp3";
    public static final String MIME_AUDIO_MPEG = "audio/mpeg";
    public static final String MIME_VIDEO_AVI = "video/avi";
    public static final String MIME_VIDEO_X_MS_WMV = "video/x-ms-wmv";
    public static final String MIME_VIDEO_3GPP = "video/3gpp";
    public static final String MIME_AUDIO_3GPP = "audio/3gpp";
    public static final String MIME_VIDEO_MP4 = "video/mp4";
    public static final String MIME_AUDIO_MP4 = "audio/mp4";
    public static final String MIME_AUDIO_X_MS_WMA = "audio/x-ms-wma";
    public static final String MIME_AUDIO_X_M4A = "audio/x-m4a";
    public static final String MIME_VIDEO_X_M4V = "video/x-m4v";
    public static final String MIME_VIDEO_OGG = "video/ogg";
    public static final String MIME_AUDIO_OGG = "audio/ogg";
    public static final String MIME_APPLICATION_OGG = "application/ogg";
    public static final String MIME_AUDIO_X_WAV = "audio/x-wav";
    public static final String MIME_IMAGE_GIF = "image/gif";
    public static final String MIME_IMAGE_PNG = "image/png";
    public static final String MIME_IMAGE_JPEG = "image/jpeg";
    public static final String MIME_IMAGE_BMP = "image/bmp";
    public static final String MIME_IMAGE_ICON = "image/icon";
    public static final String MIME_VIDEO_QUICKTIME = "video/quicktime";
    public static final String MIME_AUDIO_AAC = "audio/aac";
    public static final String MIME_AUDIO_FLAC = "audio/flac";
    public static final String MIME_APPLICATION_ZIP = "application/zip";
    public static final String MIME_APPLICATION_X_GZIP = "application/x-gzip";
    public static final String MIME_APPLICATION_X_SHOCKWAVE_FLASH = "application/x-shockwave-flash";
    public static final String MIME_VIDEO_VND_OBJECTVIDEO = "video/vnd.objectvideo";
    public static final String MIME_AUDIO_VND_OBJECTAUDIO = "audio/vnd.objectaudio";
    public static final String MIME_VIDEO_MPEG2 = "video/mpeg2";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String MIME_APPLICATION_PDF = "application/pdf";
    public static final String MIME_APPLICATION_X_PDF = "application/x-pdf";
    public static final String MIME_TEXT_HTML = "text/html";
    public static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";

    public static final String EXT_MP3 = "mp3";
    public static final String EXT_AVI = "avi";
    public static final String EXT_WMV = "wmv";
    public static final String MIME_3GP = "3gp";
    public static final String EXT_3GP = "3gp";
    public static final String EXT_3GPP = "3gpp";
    public static final String EXT_MP4 = "mp4";
    public static final String EXT_WMA = "wma";
    public static final String EXT_M4A = "m4a";
    public static final String EXT_M4V = "m4v";
    public static final String EXT_OGG = "ogg";
    public static final String EXT_OGA = "oga";
    public static final String EXT_OGV = "ogv";
    public static final String EXT_WAV = "wav";
    public static final String EXT_GIF = "gif";
    public static final String EXT_PNG = "png";
    public static final String EXT_JPG = "jpg";
    public static final String EXT_JPEG = "jpeg";
    public static final String EXT_BMP = "bmp";
    public static final String EXT_ICO = "ico";
    public static final String EXT_MOV = "mov";
    public static final String EXT_AAC = "aac";
    public static final String EXT_FLAC = "flac";
    public static final String EXT_ZIP = "zip";
    public static final String EXT_GZIP = "gzip";
    public static final String EXT_GZ = "gz";
    public static final String EXT_SWF = "swf";
    public static final String EXT_HTML = "html";
    public static final String EXT_TXT = "txt";
    public static final String EXT_PDF = "pdf";

    public static final String AUDIO = "a";
    public static final String AUDIO_VIDEO = "av";
    public static final String VIDEO = "v";
    public static final String IMAGE = "i";
    public static final String WEB = "w";
    public static final String OTHER = "o";
    public static final String FLASH = "f";
    public static final String DOC = "d";
    public static final String MIME_IMAGE_ANY = "image/*";

    public enum Supported{YES, NO}
    public enum Binary{ YES,NO}

    public static class MimeData {
        public final String ext;
        public final String mimeType;
        public final String mediaType;
        public final Supported _supported;
        public final Binary _binary;

        public MimeData(final String ext, final String mimeType,final String mediaType, final Supported supported, final Binary binary) {
            _supported = supported;
            this.ext = ext;
            this.mediaType = mediaType;
            this.mimeType = mimeType;
            _binary=binary;
        }
    }

    public static final String MIME_TEXT_CSS = "text/css";
    public static final String EXT_CSS = "css";
    public static final String MIME_APPLICATION_JAVASCRIPT = "application/javascript";
    public static final String EXT_JS = "js";
    public static final String EXT_MKV = "mkv";
    public static final String MIME_VIDEO_X_MATROSKA = "video/x-matroska";
    public static final List<MimeData> mimeTypes = Arrays.asList(
            new MimeData(EXT_MP3, MIME_AUDIO_MPEG3, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_MP3, MIME_AUDIO_MP3, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_MP3, MIME_X_AUDIO_MP3, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_MP3, MIME_AUDIO_MPEG, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_AVI, MIME_VIDEO_AVI, VIDEO, Supported.NO, Binary.YES),
            new MimeData(EXT_WMV, MIME_VIDEO_X_MS_WMV, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_3GP, MIME_VIDEO_3GPP, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_3GP, MIME_AUDIO_3GPP, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_3GPP, MIME_VIDEO_3GPP, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_MP4, MIME_VIDEO_MP4, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_MP4, MIME_AUDIO_MP4, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_WMA, MIME_AUDIO_X_MS_WMA, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_M4A, MIME_AUDIO_MP4, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_M4A, MIME_AUDIO_X_M4A, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_M4V, MIME_VIDEO_MP4, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_M4V, MIME_VIDEO_X_M4V, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_OGG, MIME_AUDIO_OGG, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_OGG, MIME_VIDEO_OGG, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_OGA, MIME_AUDIO_OGG, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_OGG, MIME_APPLICATION_OGG, AUDIO_VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_OGV, MIME_VIDEO_OGG, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_MKV, MIME_VIDEO_X_MATROSKA, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_WAV, MIME_AUDIO_X_WAV, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_GIF, MIME_IMAGE_GIF, IMAGE, Supported.YES, Binary.YES),
            new MimeData(EXT_PNG, MIME_IMAGE_PNG, IMAGE, Supported.YES, Binary.YES),
            new MimeData(EXT_JPG, MIME_IMAGE_JPEG, IMAGE, Supported.YES, Binary.YES),
            new MimeData(EXT_JPEG, MIME_IMAGE_JPEG, IMAGE, Supported.YES, Binary.YES),
            new MimeData(EXT_BMP, MIME_IMAGE_BMP, IMAGE, Supported.YES, Binary.YES),
            new MimeData(EXT_ICO, MIME_IMAGE_ICON, IMAGE, Supported.YES, Binary.YES),
            new MimeData(EXT_MP4, MIME_VIDEO_QUICKTIME, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_MOV, MIME_VIDEO_QUICKTIME, VIDEO, Supported.NO, Binary.YES),
            new MimeData(EXT_AAC, MIME_AUDIO_AAC, AUDIO, Supported.NO, Binary.YES),
            new MimeData(EXT_FLAC, MIME_AUDIO_FLAC, AUDIO, Supported.NO, Binary.YES),
            new MimeData(EXT_ZIP, MIME_APPLICATION_ZIP, OTHER, Supported.NO, Binary.YES),
            new MimeData(EXT_GZIP, MIME_APPLICATION_X_GZIP, OTHER, Supported.NO, Binary.YES),
            new MimeData(EXT_GZ, MIME_APPLICATION_X_GZIP, OTHER, Supported.NO, Binary.YES),
            new MimeData(EXT_SWF, MIME_APPLICATION_X_SHOCKWAVE_FLASH, FLASH, Supported.NO, Binary.YES),
            new MimeData(EXT_MP4, MIME_VIDEO_VND_OBJECTVIDEO, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_MP4, MIME_AUDIO_VND_OBJECTAUDIO, AUDIO, Supported.YES, Binary.YES),
            new MimeData(EXT_MP4, MIME_VIDEO_MPEG2, VIDEO, Supported.YES, Binary.YES),
            new MimeData(EXT_HTML, MIME_TEXT_HTML, WEB, Supported.YES, Binary.NO),
            new MimeData("", MIME_TEXT_HTML, WEB, Supported.YES, Binary.NO),
            new MimeData(EXT_TXT, MIME_TEXT_PLAIN, DOC, Supported.YES, Binary.NO),
            new MimeData(EXT_CSS, MIME_TEXT_CSS, WEB, Supported.YES, Binary.NO),
            new MimeData(EXT_JS, MIME_APPLICATION_JAVASCRIPT, WEB, Supported.YES, Binary.NO),
            new MimeData(EXT_PDF, MIME_APPLICATION_PDF, DOC, Supported.YES, Binary.YES),
            new MimeData(EXT_PDF, MIME_APPLICATION_X_PDF, DOC, Supported.YES, Binary.YES)
    );

    static HashMap<String,MimeData> extToMime = new HashMap<>();

    static {
        initExtensionMap();
    }

    public static void initExtensionMap() {
        extToMime.clear();
        for (final MimeData md : mimeTypes) {
            extToMime.put(md.ext,md);
        }
    }

    public static MimeData get(final String fileNameOrUrl) {
        return extToMime.get(getExt(fileNameOrUrl));
    }

    public static String getExt(final String url) {
        String ext = null;
        final int qpos = url.indexOf("?");
        final int hpos = url.indexOf("#");
        if (qpos > -1) {
            final int i = url.lastIndexOf(".", qpos) + 1;
            if (qpos - i < 5) {
                ext = url.substring(i, qpos);
            } else {
                return null;
            }
        } else if (hpos > -1) {
            final int i = url.lastIndexOf(".", hpos) + 1;
            if (hpos - i < 5) {
                ext = url.substring(i, hpos);
            } else {
                return null;
            }
        } else {
            final int i = url.lastIndexOf(".") + 1;
            if (url.length() - i < 5) {
                ext = url.substring(i);
            } else {
                return null;
            }
        }
        return ext.toLowerCase();
    }
}
