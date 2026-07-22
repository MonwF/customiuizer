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


    /**
     * @param uri DocumentsUI URI
     * @return file path of Uri
     */
    public static String getDirectoryPathFromUri(Context context, Uri uri) {
		if (uri == null) {
			return null;
		}

		if ("file".equals(uri.getScheme())) {
			return uri.getPath();
		}

        if (isTreeUri(uri)) {
            String treeId = getTreeDocumentId(uri);
            if (treeId == null) {
                return null;
            }

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
