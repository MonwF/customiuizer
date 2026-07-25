package tv.withaibuild.customiuizer

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import java.io.FileNotFoundException

class PrefsProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.sharedprefs"
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "test/*", 5)
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    @Throws(FileNotFoundException::class)
    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val context = context ?: return null

        val parts = uri.pathSegments ?: return null
        if (uriMatcher.match(uri) == 5 && parts.size >= 2) {
            val fileType = parts[1]
            val filename = when (fileType) {
                "0" -> "test0.png"
                "1" -> "test1.mp3"
                "2" -> "test2.mp4"
                "3", "5" -> "test3.txt"
                "4" -> "test4.zip"
                else -> null
            }

            return filename?.let {
                try {
                    context.assets.openFd(it)
                } catch (t: Throwable) {
                    t.printStackTrace()
                    null
                }
            }
        }

        return null
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0
}
