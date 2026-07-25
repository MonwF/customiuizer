package tv.withaibuild.customiuizer.tasker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import tv.withaibuild.customiuizer.R

class UnlockSettings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.tasker_unlock)

        val ok = findViewById<Button>(R.id.force_ok)
        ok.setOnClickListener {
            val opt = findViewById<RadioGroup>(R.id.force_option).checkedRadioButtonId
            val lockState = when (opt) {
                R.id.force_locked -> 0
                R.id.force_unlocked -> 1
                else -> -1
            }

            val stringRes = when (lockState) {
                1 -> R.string.system_noscreenlock_force_unlocked
                0 -> R.string.system_noscreenlock_force_locked
                else -> R.string.system_noscreenlock_force_off
            }

            val resultIntent = Intent().apply {
                putExtra(Constants.EXTRA_STRING_BLURB, getString(stringRes))
                val bundle = Bundle().apply {
                    putInt("system_noscreenlock_force", lockState)
                }
                putExtra(Constants.EXTRA_BUNDLE, bundle)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        val bundle = intent.getBundleExtra(Constants.EXTRA_BUNDLE)
        if (bundle != null) {
            val opt = bundle.getInt("system_noscreenlock_force", -1)
            val checkedId = when (opt) {
                0 -> R.id.force_locked
                1 -> R.id.force_unlocked
                else -> R.id.force_off
            }
            findViewById<RadioGroup>(R.id.force_option).check(checkedId)
        }
    }
}
