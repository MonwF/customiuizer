package name.monwf.customiuizer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.util.List;


public class PrefsProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.sharedprefs";
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    SharedPreferences prefs;

    static {
        uriMatcher.addURI(AUTHORITY, "test/*", 1);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        if (getContext() == null) return null;

        List<String> parts = uri.getPathSegments();
        if (uriMatcher.match(uri) == 5) {
            String filename = null;
            String fileType = parts.get(1);
            if ("0".equals(fileType)) filename = "test0.png";
            else if ("1".equals(fileType)) filename = "test1.mp3";
            else if ("2".equals(fileType)) filename = "test2.mp4";
            else if ("3".equals(fileType) || "5".equals(fileType)) filename = "test3.txt";
            else if ("4".equals(fileType)) filename = "test4.zip";

            AssetFileDescriptor afd = null;
            if (filename != null) try {
                afd = getContext().getAssets().openFd(filename);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return afd;
        }

        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

}