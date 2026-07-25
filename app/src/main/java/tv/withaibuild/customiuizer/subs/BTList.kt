package tv.withaibuild.customiuizer.subs

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.mods.GlobalActions
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers

class BTList : SubFragment() {

    private val fetchInterval = 15 * 1000
    private var key: String? = null
    private var listView1: ListView? = null
    private var listView2: ListView? = null
    private var btAdapter1: BTAdapter? = null
    private var btAdapter2: BTAdapter? = null
    private val btList = mutableListOf<Pair<String, String>>()
    private var addresses = LinkedHashSet<String>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var fetchJob: Job? = null

    private val devicesReceiver = object : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val deviceList: ArrayList<BluetoothDevice>? =
                intent.getParcelableArrayListExtra("device_list")
            btList.clear()
            if (deviceList != null) {
                for (device in deviceList) {
                    btList.add(Pair(device.address, device.name))
                }
            }
            btAdapter1?.notifyDataSetChanged()
            btAdapter2?.notifyDataSetChanged()
            updateProgressBar()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        padded = false
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        key = requireArguments().getString("key")
        addresses = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))

        val ctx = requireContext()
        btAdapter1 = BTAdapter(ctx, true)
        btAdapter2 = BTAdapter(ctx, false)

        val view = requireView()
        listView1 = view.findViewById(android.R.id.text1)
        listView2 = view.findViewById(android.R.id.text2)

        @SuppressLint("CutPasteId")
        val locationStub: ViewStub? = view.findViewById(R.id.fetch_devices)
        locationStub?.layoutResource = R.layout.pref_item
        locationStub?.inflate()

        @SuppressLint("CutPasteId")
        val location: View? = view.findViewById(R.id.fetch_devices)
        location?.findViewById<TextView>(android.R.id.title)?.text = getString(R.string.bt_fetch_devices_title)
        location?.findViewById<TextView>(android.R.id.summary)?.text = getString(R.string.bt_fetch_devices_summ)
        location?.setOnClickListener {
            btList.clear()
            btAdapter1?.notifyDataSetChanged()
            btAdapter2?.notifyDataSetChanged()
            updateProgressBar()
            fetchCachedDevices()
        }

        listView1?.adapter = btAdapter1
        listView1?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val sr = btAdapter1?.getItem(position) ?: return@OnItemClickListener
            addresses = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))
            AppHelper.removeStringPair(addresses, sr.first)
            AppHelper.appPrefs!!.edit().putStringSet(key, if (addresses.isEmpty()) null else addresses).apply()
            btAdapter1?.notifyDataSetChanged()
            btAdapter2?.notifyDataSetChanged()
        }

        listView2?.adapter = btAdapter2
        listView2?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val sr = btAdapter2?.getItem(position) ?: return@OnItemClickListener
            addresses = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))
            AppHelper.addStringPair(addresses, sr.first, sr.second)
            AppHelper.appPrefs!!.edit().putStringSet(key, addresses).apply()
            btAdapter1?.notifyDataSetChanged()
            btAdapter2?.notifyDataSetChanged()
        }

        if (BluetoothAdapter.getDefaultAdapter()?.isEnabled == false) {
            Toast.makeText(requireActivity(), R.string.request_bt, Toast.LENGTH_SHORT).show()
        }
        updateProgressBar()
    }

    private fun fetchCachedDevices() {
        val intent = Intent(GlobalActions.ACTION_PREFIX + "FetchCachedDevices")
        intent.setPackage("com.android.systemui")
        getValidContext().sendBroadcast(intent)
    }

    fun updateProgressBar() {
        view?.findViewById<View>(R.id.progress_bar)?.visibility =
            if (BluetoothAdapter.getDefaultAdapter()?.isEnabled == true && btList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun registerReceivers() {
        unregisterReceivers()
        getValidContext().registerReceiver(
            devicesReceiver,
            IntentFilter(GlobalActions.EVENT_PREFIX + "CACHEDDEVICESUPDATE"),
            Context.RECEIVER_EXPORTED
        )
        fetchJob?.cancel()
        fetchJob = scope.launch {
            delay(1000L)
            while (isActive) {
                fetchCachedDevices()
                delay(fetchInterval.toLong())
            }
        }
    }

    private fun unregisterReceivers() {
        try {
            fetchJob?.cancel()
            fetchJob = null
            getValidContext().unregisterReceiver(devicesReceiver)
        } catch (ignored: Throwable) {
        }
    }

    override fun onDestroy() {
        unregisterReceivers()
        scope.cancel()
        super.onDestroy()
    }

    override fun onPause() {
        unregisterReceivers()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        registerReceivers()
    }

    private inner class BTAdapter(context: Context, private val isSelected: Boolean) : BaseAdapter() {
        private val mInflater: LayoutInflater = LayoutInflater.from(context)

        override fun getCount(): Int = if (isSelected) addresses.size else btList.size

        override fun getItem(position: Int): Pair<String, String>? {
            return if (isSelected) {
                if (addresses.isEmpty()) return null
                val network = addresses.elementAt(position).split("|", limit = 2)
                Pair(network[0], if (network.size > 1) network[1] else "")
            } else {
                btList[position]
            }
        }

        override fun getItemId(position: Int): Long = position.toLong()

        @SuppressLint("MissingPermission")
        override fun isEnabled(position: Int): Boolean {
            return isSelected || !Helpers.containsStringPair(addresses, getItem(position)?.first ?: "")
        }

        @SuppressLint("MissingPermission")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val row: View = if (convertView != null) {
                convertView
            } else {
                mInflater.inflate(R.layout.pref_item, parent, false)
            }

            val itemTitle: TextView = row.findViewById(android.R.id.title)
            val itemSumm: TextView = row.findViewById(android.R.id.summary)
            val sr = getItem(position) ?: return row
            itemTitle.text = sr.second
            itemSumm.text = sr.first

            if (isEnabled(position)) {
                row.isEnabled = true

                var isBonded = false
                val bonded = BluetoothAdapter.getDefaultAdapter()?.bondedDevices
                for (device in bonded ?: emptySet()) {
                    if (device.address == sr.first) isBonded = true
                }

                itemTitle.setTextColor(
                    resources.getColor(
                        if (isBonded) R.color.highlight_normal_light else R.color.preference_primary_text_color,
                        activity?.theme
                    )
                )
                itemTitle.alpha = 1.0f
                itemSumm.alpha = 1.0f
            } else {
                row.isEnabled = false
                itemTitle.setTextColor(resources.getColor(R.color.preference_secondary_text_color, activity?.theme))
                itemTitle.alpha = 0.5f
                itemSumm.alpha = 0.5f
            }
            return row
        }
    }
}
