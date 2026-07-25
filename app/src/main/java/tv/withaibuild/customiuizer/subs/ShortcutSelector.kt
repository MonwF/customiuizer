package tv.withaibuild.customiuizer.subs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.AdapterView

import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList

import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragmentWithSearch
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.ResolveInfoAdapter

@SuppressLint("ClickableViewAccessibility")
class ShortcutSelector : SubFragmentWithSearch() {

    private var key: String? = null
    private var keyContents: String? = null
    private val shortcuts = ArrayList<ResolveInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        this.padded = false
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val args = arguments
        key = args?.getString("key")
        keyContents = AppHelper.getStringOfAppPrefs(key, null)

        val shortcutIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
        val pm = activity?.packageManager ?: return
        shortcuts.addAll(pm.queryIntentActivities(shortcutIntent, 0))

        listView?.adapter = ResolveInfoAdapter(context, shortcuts)
        listView?.setOnItemClickListener { parent: AdapterView<*>, _, position: Int, _ ->
            val app = parent.adapter?.getItem(position) as? ResolveInfo ?: return@setOnItemClickListener
            val cn = ComponentName(app.activityInfo.packageName, app.activityInfo.name)
            createShortcutIntent.action = Intent.ACTION_CREATE_SHORTCUT
            createShortcutIntent.component = cn
            keyContents = app.activityInfo.packageName + "|" + app.activityInfo.name
            startActivityForResult(createShortcutIntent, 7350)
        }

        view?.findViewById<View>(R.id.am_progressBar)?.visibility = View.GONE
    }

    private val createShortcutIntent = Intent()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != 7350) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        if (resultCode == Activity.RESULT_OK && data != null) {
            var icon: Bitmap? = null
            val iconResId = data.getParcelableExtra<Intent.ShortcutIconResource>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)

            if (iconResId != null) try {
                val mContext = context?.createPackageContext(iconResId.packageName, Context.CONTEXT_IGNORE_SECURITY)
                if (mContext != null) {
                    icon = BitmapFactory.decodeResource(mContext.resources, mContext.resources.getIdentifier(iconResId.resourceName, "drawable", iconResId.packageName))
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            if (icon == null) icon = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)

            val intent = Intent(context, this.javaClass)

            if (icon != null && key != null) try {
                val dir = context?.filesDir?.path + "/shortcuts"
                val fileName = "$dir/tmp.png"

                File(dir).mkdirs()
                val shortcutFileName = File(fileName)
                FileOutputStream(shortcutFileName, false).use { shortcutOutStream ->
                    if (icon.compress(Bitmap.CompressFormat.PNG, 100, shortcutOutStream)) {
                        intent.putExtra("shortcut_icon", fileName)
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }

            intent.putExtra("shortcut_contents", keyContents)
            intent.putExtra("shortcut_name", data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME))
            intent.putExtra("shortcut_intent", data.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT))
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
