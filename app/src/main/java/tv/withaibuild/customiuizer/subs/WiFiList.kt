package tv.withaibuild.customiuizer.subs

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
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
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers

class WiFiList : SubFragment() {

    private val scanInterval = 15 * 1000
    private var key: String? = null
    private var listView1: ListView? = null
    private var listView2: ListView? = null
    private var wifiAdapter1: WiFiAdapter? = null
    private var wifiAdapter2: WiFiAdapter? = null
    private lateinit var wifiManager: WifiManager
    private var wifiList: List<ScanResult> = emptyList()
    private var bssids = LinkedHashSet<String>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var scanJob: Job? = null

    private val wifiReceiver = object : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            if (action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                wifiList = try {
                    wifiManager.scanResults
                } catch (ignored: SecurityException) {
                    emptyList()
                }
                wifiAdapter1?.notifyDataSetChanged()
                wifiAdapter2?.notifyDataSetChanged()
                updateProgressBar()
            } else if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                val netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO) as? NetworkInfo
                if (netInfo == null) return
                if (netInfo.detailedState == NetworkInfo.DetailedState.CONNECTED) isWiFiReady()
                if (netInfo.detailedState == NetworkInfo.DetailedState.CONNECTED ||
                    netInfo.detailedState == NetworkInfo.DetailedState.DISCONNECTED
                ) {
                    scheduleScan(1000L)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        padded = false
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        key = requireArguments().getString("key")
        bssids = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))

        wifiManager = requireActivity().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ctx = requireContext()
        wifiAdapter1 = WiFiAdapter(ctx, true)
        wifiAdapter2 = WiFiAdapter(ctx, false)

        val view = requireView()
        listView1 = view.findViewById(android.R.id.text1)
        listView2 = view.findViewById(android.R.id.text2)

        @SuppressLint("CutPasteId")
        val locationStub: ViewStub? = view.findViewById(R.id.location_settings)
        locationStub?.layoutResource = R.layout.pref_item
        locationStub?.inflate()

        @SuppressLint("CutPasteId")
        val location: View? = view.findViewById(R.id.location_settings)
        location?.findViewById<TextView>(android.R.id.title)?.text = getString(R.string.wifi_location_title)
        location?.findViewById<TextView>(android.R.id.summary)?.text = getString(R.string.wifi_location_summ)
        location?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        listView1?.adapter = wifiAdapter1
        listView1?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val sr = wifiAdapter1?.getItem(position) ?: return@OnItemClickListener
            bssids = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))
            AppHelper.removeStringPair(bssids, sr.first)
            AppHelper.appPrefs.edit().putStringSet(key, if (bssids.isEmpty()) null else bssids).apply()
            wifiAdapter1?.notifyDataSetChanged()
            wifiAdapter2?.notifyDataSetChanged()
        }

        listView2?.adapter = wifiAdapter2
        listView2?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val sr = wifiAdapter2?.getItem(position) ?: return@OnItemClickListener
            bssids = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))
            AppHelper.addStringPair(bssids, sr.first, sr.second)
            AppHelper.appPrefs.edit().putStringSet(key, bssids).apply()
            wifiAdapter1?.notifyDataSetChanged()
            wifiAdapter2?.notifyDataSetChanged()
        }

        isWiFiReady()
        updateProgressBar()
    }

    fun updateProgressBar() {
        view?.findViewById<View>(R.id.progress_bar)?.visibility =
            if (wifiManager.isWifiEnabled && isLocationServicesEnabled() && wifiList.isEmpty()) View.VISIBLE else View.GONE
    }

    fun isLocationServicesEnabled(): Boolean {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return try {
            locationManager?.run {
                isProviderEnabled(LocationManager.GPS_PROVIDER) || isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } ?: false
        } catch (t: Throwable) {
            false
        }
    }

    fun isWiFiReady() {
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(requireActivity(), R.string.request_wifi, Toast.LENGTH_SHORT).show()
            return
        }
        if (!isLocationServicesEnabled()) {
            Toast.makeText(requireActivity(), R.string.request_location, Toast.LENGTH_LONG).show()
        }
    }

    private fun scheduleScan(initialDelayMs: Long = 0L) {
        scanJob?.cancel()
        scanJob = scope.launch {
            if (initialDelayMs > 0) delay(initialDelayMs)
            while (isActive) {
                try { wifiManager.startScan() } catch (ignored: Throwable) {}
                updateProgressBar()
                delay(scanInterval.toLong())
            }
        }
    }

    private fun registerReceivers() {
        unregisterReceivers()
        isWiFiReady()
        val intentFilter = IntentFilter().apply {
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        }
        requireActivity().registerReceiver(wifiReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        scheduleScan(1000L)
    }

    private fun unregisterReceivers() {
        try {
            scanJob?.cancel()
            scanJob = null
            requireActivity().unregisterReceiver(wifiReceiver)
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

    private inner class WiFiAdapter(context: Context, private val isSelected: Boolean) : BaseAdapter() {
        private val mInflater: LayoutInflater = LayoutInflater.from(context)

        override fun getCount(): Int = if (isSelected) bssids.size else wifiList.size

        override fun getItem(position: Int): Pair<String, String>? {
            return if (isSelected) {
                if (bssids.isEmpty()) return null
                val network = bssids.elementAt(position).split("|", limit = 2)
                Pair(network[0], if (network.size > 1) network[1] else "")
            } else {
                val result = wifiList[position]
                Pair(result.BSSID, result.SSID)
            }
        }

        override fun getItemId(position: Int): Long = position.toLong()

        override fun isEnabled(position: Int): Boolean {
            return isSelected || !Helpers.containsStringPair(bssids, getItem(position)?.first ?: "")
        }

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
                if (sr.first == wifiManager.connectionInfo?.bssid)
                    itemTitle.setTextColor(resources.getColor(R.color.highlight_normal_light, activity?.theme))
                else
                    itemTitle.setTextColor(resources.getColor(R.color.preference_primary_text_color, activity?.theme))
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
