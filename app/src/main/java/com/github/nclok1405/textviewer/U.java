package com.github.nclok1405.textviewer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * Static Utilities
 */
public final class U {
    /** This is a Utility Class */
    private U() {}

    /**
    * UTF-8 Charset
    */
    @SuppressWarnings("CharsetObjectCanBeUsed")
    static public final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Close a {@link Closeable}.
     * @param c Closeable to close. If <code>null</code>, this method exits without any action.
     */
    static public void closeQuietly(Closeable c) {
        try {
            if(c != null) c.close();
        } catch (IOException ignore) {}
    }

    /**
     * Get a stack trace as a {@link String}.
     * @param t Any {@link Throwable}
     * @return Stack Trace as a <code>String</code>, returns <code>"null"</code> as a <code>String</code> if <code>t</code> is <code>null</code>
     */
    static public String getStackTrace(Throwable t) {
        if(t == null) {
            return "null";
        } else {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        }
    }

    /**
     * Create a byte[] from an {@link InputStream}.
     * @param in An InputStream
     * @param bufSize Buffer Size
     * @return byte[]
     * @throws IOException on any failure
     */
    static public byte[] readBytes(InputStream in, int bufSize) throws IOException {
        final byte[] buf = new byte[bufSize];
        final ByteArrayOutputStream ous = new ByteArrayOutputStream();
        int read;
        while((read = in.read(buf)) != -1) {
            ous.write(buf, 0, read);
        }
        return ous.toByteArray();
    }

    /**
     * Create a byte[] from an {@link InputStream}. Uses 8192 bytes buffer.
     * @param in An InputStream
     * @return byte[]
     * @throws IOException on any failure
     */
    static public byte[] readBytes(InputStream in) throws IOException {
        return readBytes(in, 8192);
    }

    /**
     * Get the filename portion of a Uri
     * @param uri Uri
     * @param cr ContentResolver
     * @return Filename (Uri as a String if unknown)
     */
    static public String getUriFilename(Uri uri, ContentResolver cr) {
        if (uri == null) {
            return "null";
        }
        final String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            final String path = uri.getPath();
            if (path != null) {
                return new File(path).getName();
            }
        }
        else if ("content".equals(scheme) && (cr != null)) {
            try {
                final String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
                final Cursor cursor = cr.query(uri, projection, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            return cursor.getString(0);
                        }
                    } finally {
                        cursor.close();
                    }
                }
            } catch (Exception ignore) {}
        }
        return uri.toString();
    }

    /**
     * Get the PackageInfo of this app
     * @param c Context
     * @return PackageInfo on success, null on failure
     */
    static public PackageInfo getSelfPackageInfo(Context c) {
        if (c == null) return null;
        try {
            return c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
