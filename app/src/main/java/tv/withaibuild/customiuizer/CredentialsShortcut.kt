package tv.withaibuild.customiuizer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CredentialsShortcut : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launchIntent = Intent(this, Credentials::class.java)
        val intent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.credentials_unlock))
            putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this@CredentialsShortcut, R.drawable.ic_credentials))
        }

        setResult(RESULT_OK, intent)
        finish()
    }
}
