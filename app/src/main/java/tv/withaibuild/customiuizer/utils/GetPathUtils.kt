package tv.withaibuild.customiuizer.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File

object GetPathUtils {

    private const val PATH_TREE = "tree"
    private const val PRIMARY_TYPE = "primary"
    private const val RAW_TYPE = "raw"

    fun getDirectoryPathFromUri(context: Context, uri: Uri?): String? {
        if (uri == null) return null

        if ("file" == uri.scheme) return uri.path

        if (!isTreeUri(uri)) return null

        val treeId = getTreeDocumentId(uri) ?: return null
        val paths = treeId.split(":")
        val type = paths[0]
        val subPath = if (paths.size == 2) paths[1] else ""

        return when {
            type.equals(RAW_TYPE, ignoreCase = true) -> treeId.substring(treeId.indexOf(File.separator))
            type.equals(PRIMARY_TYPE, ignoreCase = true) -> Environment.getExternalStorageDirectory().path + File.separator + subPath
            else -> {
                val pathSegment = treeId.split(":")
                val rootPath = getRemovableStorageRootPath(context, paths[0])
                if (pathSegment.size == 1) rootPath else "$rootPath${File.separator}${pathSegment[1]}"
            }
        }
    }

    private fun getRemovableStorageRootPath(context: Context, storageId: String): String {
        val rootPath = StringBuilder()
        val externalFilesDirs = context.getExternalFilesDirs(null)
        for (fileDir in externalFilesDirs) {
            if (fileDir?.path?.contains(storageId) == true) {
                val pathSegment = fileDir.path.split(File.separator)
                for (segment in pathSegment) {
                    if (segment == storageId) {
                        rootPath.append(storageId)
                        break
                    }
                    rootPath.append(segment).append(File.separator)
                }
                break
            }
        }
        return rootPath.toString()
    }

    private fun getTreeDocumentId(uri: Uri): String? {
        val paths = uri.pathSegments
        return if (paths.size >= 2 && paths[0] == PATH_TREE) paths[1] else null
    }

    private fun isTreeUri(uri: Uri): Boolean {
        val paths = uri.pathSegments
        return paths.size == 2 && paths[0] == PATH_TREE
    }
}
