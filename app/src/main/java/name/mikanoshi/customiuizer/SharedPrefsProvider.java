package name.mikanoshi.customiuizer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import name.mikanoshi.customiuizer.utils.Helpers;

public class SharedPrefsProvider extends ContentProvider {

	public static final String AUTHORITY = "name.mikanoshi.customiuizer.provider.sharedprefs";
	private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	SharedPreferences prefs;

	static {
		uriMatcher.addURI(AUTHORITY, "string/*/", 0);
		uriMatcher.addURI(AUTHORITY, "string/*/*", 1);
		uriMatcher.addURI(AUTHORITY, "integer/*/*", 2);
		uriMatcher.addURI(AUTHORITY, "boolean/*/*", 3);
		uriMatcher.addURI(AUTHORITY, "stringset/*", 4);
	}

	@Override
	public boolean onCreate() {
		try {
			prefs = Helpers.getProtectedContext(getContext()).getSharedPreferences(Helpers.prefsName, Context.MODE_PRIVATE);
			return true;
		} catch (Throwable throwable) {
			return false;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		List<String> parts = uri.getPathSegments();
		//Log.e("parts", String.valueOf(parts));
		MatrixCursor cursor = new MatrixCursor(new String[]{"data"});

		switch (uriMatcher.match(uri)) {
			case 0: {
				cursor.newRow().add("data", prefs.getString(parts.get(1), ""));
				return cursor;
			}
			case 1: {
				cursor.newRow().add("data", prefs.getString(parts.get(1), parts.get(2)));
				return cursor;
			}
			case 2: {
				cursor.newRow().add("data", prefs.getInt(parts.get(1), Integer.parseInt(parts.get(2))));
				return cursor;
			}
			case 3: {
				cursor.newRow().add("data", prefs.getBoolean(parts.get(1), Integer.parseInt(parts.get(2)) == 1) ? 1 : 0);
				return cursor;
			}
			case 4: {
				Set<String> strings = prefs.getStringSet(parts.get(1), new LinkedHashSet<String>());
				if (strings != null)
				for (String str: strings) cursor.newRow().add("data", str);
				return cursor;
			}
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