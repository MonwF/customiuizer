package name.monwf.customiuizer.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import java.io.File;
import java.util.List;

public class GetPathUtils {

    private static final String PATH_TREE = "tree";
    private static final String PRIMARY_TYPE = "primary";
    private static final String RAW_TYPE = "raw";

//    public static String getFilePathFromUri(final Context context, final Uri uri) {
//
//        // DocumentProvider
//        if (DocumentsContract.isDocumentUri(context, uri)) {
//            // ExternalStorageProvider
//            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                //Timber.d("docId -> %s", docId);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                if (PRIMARY_TYPE.equalsIgnoreCase(type)) {
//                    return Environment.getExternalStorageDirectory() + "/" + split[1];
//                } else {
//                    // TODO handle non-primary volumes
//                    StringBuilder path = new StringBuilder();
//                    String[] pathSegment = docId.split(":");
//                    return path.append(getRemovableStorageRootPath(context, pathSegment[0])).append(File.separator).append(pathSegment[1]).toString();
//                }
//            } else if (isDownloadsDocument(uri)) {  // DownloadsProvider
//
//                final String id = DocumentsContract.getDocumentId(uri);
//
//                if (id.contains("raw:")) {
//                    return id.substring(id.indexOf(File.separator));
//                } else {
//                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
//                    return getDataColumn(context, contentUri, null, null);
//                }
//            } else if (isMediaDocument(uri)) {  // MediaProvider
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                Uri contentUri = null;
//                if ("image".equals(type)) {
//                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//
//                final String selection = "_id=?";
//                final String[] selectionArgs = new String[]{
//                        split[1]
//                };
//
//                return getDataColumn(context, contentUri, selection, selectionArgs);
//            }
//        } else if ("content".equalsIgnoreCase(uri.getScheme())) { // MediaStore (and general)
//            return getDataColumn(context, uri, null, null);
//        } else if ("file".equalsIgnoreCase(uri.getScheme())) { // File
//            return uri.getPath();
//        }
//
//        return null;
//    }
//
//    /**
//     * Get the value of the data column for this Uri. This is useful for
//     * MediaStore Uris, and other file-based ContentProviders.
//     *
//     * @param context       The context.
//     * @param uri           The Uri to query.
//     * @param selection     (Optional) Filter used in the query.
//     * @param selectionArgs (Optional) Selection arguments used in the query.
//     * @return The value of the _data column, which is typically a file path.
//     */
//    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
//
//        Cursor cursor = null;
//        final String column = MediaStore.MediaColumns.DATA;
//        final String[] projection = {column};
//        try {
//            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
//                    null);
//            if (cursor != null && cursor.moveToFirst()) {
//                final int column_index = cursor.getColumnIndexOrThrow(column);
//                return cursor.getString(column_index);
//            }
//        } finally {
//            if (cursor != null)
//                cursor.close();
//        }
//
//        return null;
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is ExternalStorageProvider.
//     */
//    private static boolean isExternalStorageDocument(Uri uri) {
//        return "com.android.externalstorage.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is DownloadsProvider.
//     */
//    private static boolean isDownloadsDocument(Uri uri) {
//        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is MediaProvider.
//     */
//    private static boolean isMediaDocument(Uri uri) {
//        return "com.android.providers.media.documents".equals(uri.getAuthority());
//    }

    /**
     * @param uri DocumentsUI URI
     * @return file path of Uri
     */
    public static String getDirectoryPathFromUri(Context context, Uri uri) {
    	if (uri == null) return null;

        if ("file".equals(uri.getScheme())) return uri.getPath();

        if (isTreeUri(uri)) {
            String treeId = getTreeDocumentId(uri);
            if (treeId == null) return null;

			String[] paths = treeId.split(":");
			String type = paths[0];
			String subPath = paths.length == 2 ? paths[1] : "";

			if (RAW_TYPE.equalsIgnoreCase(type)) {
				return treeId.substring(treeId.indexOf(File.separator));
			} else if (PRIMARY_TYPE.equalsIgnoreCase(type)) {
				return Environment.getExternalStorageDirectory() + File.separator + subPath;
			} else {
				StringBuilder path = new StringBuilder();
				String[] pathSegment = treeId.split(":");
				if (pathSegment.length == 1) {
					path.append(getRemovableStorageRootPath(context, paths[0]));
				} else {
					String rootPath = getRemovableStorageRootPath(context, paths[0]);
					path.append(rootPath).append(File.separator).append(pathSegment[1]);
				}
				return path.toString();
			}
        }
        return null;
    }

    private static String getRemovableStorageRootPath(Context context, String storageId) {
        StringBuilder rootPath = new StringBuilder();
        File[] externalFilesDirs = context.getExternalFilesDirs(null);
        for (File fileDir : externalFilesDirs) {
            if (fileDir.getPath().contains(storageId)) {
                String[] pathSegment = fileDir.getPath().split(File.separator);
                for (String segment : pathSegment) {
                    if (segment.equals(storageId)) {
                        rootPath.append(storageId);
                        break;
                    }
                    rootPath.append(segment).append(File.separator);
                }
                //rootPath.append(fileDir.getPath().split("/Android")[0]); // faster
                break;
            }
        }
        return rootPath.toString();
    }

    //https://github.com/rcketscientist/DocumentActivity/blob/master/library/src/main/java/com/anthonymandra/framework/DocumentUtil.java#L56
    /**
     * Extract the via {@link DocumentsContract.Document#COLUMN_DOCUMENT_ID} from the given URI.
     * From {@link DocumentsContract} but return null instead of throw
     */
    private static String getTreeDocumentId(Uri uri) {
        final List<String> paths = uri.getPathSegments();
        if (paths.size() >= 2 && PATH_TREE.equals(paths.get(0))) {
            return paths.get(1);
        }
        return null;
    }

    private static boolean isTreeUri(Uri uri) {
        final List<String> paths = uri.getPathSegments();
        return (paths.size() == 2 && PATH_TREE.equals(paths.get(0)));
    }
}