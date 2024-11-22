package com.librarysdk.sdk.Contents

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityWcdma
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrengthGsm
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.CellSignalStrengthWcdma
import android.telephony.NetworkRegistrationInfo
import android.telephony.ServiceState
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.librarysdk.sdk.Contents.ContextInfo.getContext
import com.librarysdk.sdk.Data.Model.CrowdData.CrowdData
import com.librarysdk.sdk.Data.Model.Device.Device
import com.librarysdk.sdk.helper.PermissionHelper
import org.json.JSONArray
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.Locale
import java.util.TimeZone


object DeviceInfo {
    private var crowdData: CrowdData = CrowdData()

    class cellData {
        var arfcn: Int? = null
        var earfcn: Int? = null
        var uarfcn: Int? = null
        var nrarfcn: Int? = null
        var tac: Int? = null
        var lac: Int? = null
        var cellBandwidth: Int? = null
        var cellId: Int? = null
        var cellIdentifier: Number? = null
        var cqi: Int? = null
        var additionalPLMNId: MutableList<String> = mutableListOf()
        var bands: ArrayList<Int> = ArrayList()
        var enodeb: Int? = null
        var cellType: String = ""
        var asuLevel: Int? = null
        var csiRsrp: Number? = null
        var csiRsrq: Number? = null
        var csiSinr: Number? = null
        var frequencyRange: Int? = null
        var level: Int? = null
        var mcc: String? = ""
        var mnc: String? = ""
        var nci: Number? = null
        var pci: Int? = null
        var ssRsrp: Number? = null
        var ssRsrq: Number? = null
        var ssSinr: Number? = null
        var psc: Int? = null
        var rncId: Number? = null
        var rsrp: Int? = null
        var rsrq: Int? = null
        var rssi: Number? = null
        var rssnr: Int? = null
        var technologyFamily: String = "";
        var timingAdvance: Number? = null
        var ecno: Number? = null
        var dbm: Number? = null
        var bsic: Number? = null
        var cid: Number? = null
        var bandwidth: Number? = null
        var ci: Number? = null
        var nwOperator: String = ""
        var csiCqi: ArrayList<Int> = ArrayList()
        var csiCqiTableIndex: Number? = null
    }

    fun getDeviceId(): String {
        val context = getContext()
        if (context != null) {
            val contentResolver: ContentResolver =
                context.applicationContext.contentResolver
            return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        }
        return ""
    }

    @SuppressLint("MissingPermission")
    fun getPhoneNumber(context: Context): ArrayList<String> {
        val phoneNumbers: ArrayList<String> = ArrayList()
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        if (checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val subscriptionInfoList: List<SubscriptionInfo> =
                subscriptionManager.activeSubscriptionInfoList
            if (subscriptionInfoList != null) {
                for (subscriptionInfo in subscriptionInfoList) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val subscriptionId = subscriptionInfo.subscriptionId
                        val phoneNumber = subscriptionManager.getPhoneNumber(subscriptionId)
                        if (!phoneNumber.isNullOrEmpty()) {
                            phoneNumbers.add(phoneNumber)
                        }
                    } else {
                        val phoneNumber = subscriptionInfo.number
                        if (!phoneNumber.isNullOrEmpty()) {
                            phoneNumbers.add(phoneNumber)
                        }
                    }
                }
            }
        }
        return phoneNumbers
    }

    fun getDeviceDetails(context: Context): Device {
        var device: Device = Device()
        if (context != null) {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            if (accounts.size > 0) {
                device.androidOwnerEmail = accounts[0].name // This is the email address
                // Do something with the email
            }

            device.sdkVersion = "101"
            device.deviceId = getDeviceId()
            var phoneList = getPhoneNumber(context)
            device.androidOwnerMsisdn = if (phoneList.size > 0) phoneList[0] else ""
            device.model = Build.MODEL
            device.manufacturer = Build.MANUFACTURER
            device.deviceType =
                if (telephonyManager.phoneType == TelephonyManager.PHONE_TYPE_NONE) "tablet" else "phone"
            device.systemName = Build.VERSION.RELEASE
            device.systemVersion = Build.VERSION.SDK_INT.toString()
        }
        return device
    }

    @SuppressLint("MissingPermission")
    private fun getDataConnectionType(
        telephonyManager: TelephonyManager,
        connectivityManager: ConnectivityManager
    ): Int {
        var connectionType = 0
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                connectionType = 2 // Wifi
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                connectionType = telephonyManager.dataNetworkType
            }
        }

        return connectionType
    }

    @SuppressLint("MissingPermission")
    private fun getDownstreamBandwidthKbps(connectivityManager: ConnectivityManager): Int {
        val activeNetwork = connectivityManager.activeNetworkInfo
        var bandwidth = 0
        if (activeNetwork != null && activeNetwork.isConnected) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    // Estimate for WiFi
                    bandwidth = 20000 // Example estimate for WiFi (20 Mbps)
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    // Estimate for Cellular
                    val networkType = activeNetwork.type

                    if (networkType == ConnectivityManager.TYPE_MOBILE && activeNetwork.subtype == TelephonyManager.NETWORK_TYPE_LTE)
                        bandwidth = 10000
                    else if (networkType == ConnectivityManager.TYPE_MOBILE)
                        bandwidth = 1500
                    else
                        bandwidth = 1000

                }
            }
        }
        return bandwidth // Default to 0 if no network is connected
    }

    @SuppressLint("MissingPermission")
    private fun hasCellularConnection(connectivityManager: ConnectivityManager): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            return networkInfo.type == ConnectivityManager.TYPE_MOBILE
        }
        return false
    }

    @SuppressLint("MissingPermission")
    private fun isCellularDataAllowed(
        telephonyManager: TelephonyManager,
        connectivityManager: ConnectivityManager
    ): Boolean {
        // Check if data is enabled
        val isDataEnabled = telephonyManager.isDataEnabled
        // Check network capabilities
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isNetworkDataCapable =
            capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        // Return true if data is enabled and the network is capable
        return isDataEnabled && isNetworkDataCapable
    }

    @SuppressLint("MissingPermission")
    private fun isCarrierAggregationEnabled(connectivityManager: ConnectivityManager): Boolean {
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getLteBandwidth(bandWith: Number?): Int {
        // Implement your logic to derive the LTE bandwidth based on cell identity
        return when (bandWith) {
            1 -> 5000 // 5 MHz
            2 -> 10000 // 10 MHz
            3 -> 15000 // 15 MHz
            4 -> 20000 // 20 MHz
            5 -> 30000 // 30 MHz
            6 -> 40000 // 40 MHz
            7 -> 50000 // 50 MHz
            else -> 0 // Unknown bandwidth
        }
    }

    private fun getBandwidthFromARFCN(arfcn: Int): Int {
        // Her GSM kanalı için bant genişliği
        val bandwidthKHz = 200 // 200 kHz
        // GSM 900 ve GSM 1800 için aralığı kontrol et
        if (arfcn >= 0 && arfcn <= 124) {
            // GSM 900
            return bandwidthKHz // 200 kHz
        } else if (arfcn >= 128 && arfcn <= 251) {
            // GSM 1800
            return bandwidthKHz // 200 kHz
        }
        return 0 // Bilinmeyen ARFCN için
    }

    private fun getBandwidthFromUARFCN(uarfcn: Int): Int {
        // Bant genişlikleri aralığı

        if (uarfcn >= 0 && uarfcn <= 511) {
            return 5000 // 5 MHz
        } else if (uarfcn >= 512 && uarfcn <= 1023) {
            return 10000 // 10 MHz
        } else if (uarfcn >= 1024 && uarfcn <= 1619) {
            return 15000 // 15 MHz
        }
        return 0 // Bilinmeyen UARFCN için
    }

    private fun getBandwidthFromNARFCN(narfcn: Number?): Int {
        return when {
            narfcn in 0..255 -> 5  // NR Band n1
            narfcn in 256..511 -> 10 // NR Band n2
            narfcn in 512..885 -> 20 // NR Band n3
            narfcn in 886..1275 -> 40 // NR Band n78
            narfcn in 1276..2555 -> 100 // NR Band n79
            // Add more mappings based on the NR bands
            else -> -1 // Unknown bandwidth
        }
    }

    private fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0 // Default value
        ) == 1 // 1 means airplane mode is on
    }

    private fun hasValidDeviceId(context: Context): Boolean {
        // Get the Android ID
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        // Check if the Android ID is valid (not null and not an empty string)
        return androidId.isNotEmpty() && androidId != "9774d56d682e549c" // Default value indicating uninitialized
    }

    @SuppressLint("MissingPermission")
    private fun canSupportVoiceAndData(telephonyManager: TelephonyManager): Boolean {
        val networkType = telephonyManager.networkType
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA -> true

            else -> false
        }
    }

    private fun getServiceState(serviceState: Int?): String {
        if (serviceState != null) {
            val state = when (serviceState) {
                ServiceState.STATE_IN_SERVICE -> "In Service"
                ServiceState.STATE_OUT_OF_SERVICE -> "Out of Service"
                ServiceState.STATE_EMERGENCY_ONLY -> "Emergency Only"
                ServiceState.STATE_POWER_OFF -> "Power Off"
                else -> "Unknown State"
            }
            return state
        }
        return ""
    }

    private fun getSimCardState(simState: Int): String {
        if (simState != null) {
            val stateDescription = when (simState) {
                TelephonyManager.SIM_STATE_READY -> "Ready"
                TelephonyManager.SIM_STATE_NOT_READY -> "Not Ready"
                TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
                TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
                TelephonyManager.SIM_STATE_UNKNOWN -> "Unknown State"
                else -> "Unknown State"
            }
            return stateDescription
        }
        return ""
    }

    @SuppressLint("MissingPermission")
    private fun getUpstreamBandwidthKbps(connectivityManager: ConnectivityManager): Int {
        // Check if network is available
        val activeNetwork = connectivityManager.activeNetwork ?: return -1
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return -1

        // Get upstream bandwidth in Kbps
        return networkCapabilities.linkUpstreamBandwidthKbps
    }

    private fun frequencyToChannel(frequency: Int): Int {
        return when (frequency) {
            in 2400..2483 -> {
                // 2.4 GHz channels
                ((frequency - 2400) / 5) + 1
            }

            in 4915..4980 -> {
                // 5 GHz channels (UNII-1)
                ((frequency - 4915) / 5) + 36
            }

            in 5035..5080 -> {
                // 5 GHz channels (UNII-2)
                ((frequency - 5035) / 5) + 52
            }

            in 5170..5825 -> {
                // 5 GHz channels (UNII-3)
                ((frequency - 5170) / 5) + 36
            }

            else -> {
                // Frequency not in standard Wi-Fi bands
                -1 // Indicate invalid frequency
            }
        }
    }

    private fun getRncAndCid(combinedCid: Int): Pair<Int, Int> {
        // Extract RNC ID (12 bits) by shifting the combinedCid to the right by 16 bits
        val rncId = (combinedCid shr 16) and 0xFFFF

        // Extract Cell ID (16 bits) by masking with 0xFFFF
        val cellId = combinedCid and 0xFFFF

        return Pair(rncId, cellId)
    }

    private fun setCellData(cellInfo: CellInfo): cellData {
        var cell: cellData = cellData()
        when (cellInfo) {
            is CellInfoLte -> {
                cell = setCellLteData(cellInfo)
            }

            is CellInfoGsm -> {
                cell = setCellGsmData(cellInfo)
            }

            is CellInfoWcdma -> {
                cell = setCellWcdmaData(cellInfo)
            }

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (cellInfo is CellInfoNr) {
                cell = setCellNr(cellInfo)
            }
        }
        return cell
    }

    private fun setCellLteData(cellInfoLte: CellInfoLte): cellData {
        var cell: cellData = cellData()
        var cellIdentityLte = cellInfoLte.cellIdentity
        if (cellIdentityLte != null) {
            val signal = cellInfoLte.cellSignalStrength as CellSignalStrengthLte
            cell.earfcn = cellIdentityLte.earfcn
            cell.tac = cellIdentityLte.tac
            cell.cellBandwidth = getLteBandwidth(cellIdentityLte.bandwidth)
            cell.cellIdentifier = cellIdentityLte.ci
            cell.cqi = signal.cqi
            cell.rsrp = signal.rsrp
            cell.rsrq = signal.rsrq
            cell.rssnr = signal.rssnr
            cell.pci = cellIdentityLte.pci
            cell.mcc = cellIdentityLte.mccString
            cell.mnc = cellIdentityLte.mncString
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cell.additionalPLMNId.addAll(cellIdentityLte.additionalPlmns)
                for (band in cellIdentityLte.bands) {
                    cell.bands.add(band)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cell.rssi = signal.rssi
            }
            cell.dbm = signal.dbm
            cell.level = signal.level
            cell.bandwidth = cellIdentityLte.bandwidth
            cell.ci = cellIdentityLte.ci
            cell.timingAdvance = signal.timingAdvance
            cell.nwOperator = cellIdentityLte.mobileNetworkOperator ?: ""

            val nt = cellIdentityLte.ci
            val cellidHex = Integer.toHexString(nt)
            val eNBHex = cellidHex.substring(0, cellidHex.length - 2)
            cell.enodeb = eNBHex.toInt(16)
            cell.cellId = cellidHex.substring(cellidHex.length - 2).toInt(16)

            cell.technologyFamily = "LTE";

            cell.cellType = "lte"
        }
        return cell
    }

    private fun setCellGsmData(cellInfoGsm: CellInfoGsm): cellData {
        var cell: cellData = cellData()
        var cellIdentityGsm = cellInfoGsm.cellIdentity
        if (cellIdentityGsm != null) {
            val signal = cellInfoGsm.cellSignalStrength as CellSignalStrengthGsm
            cell.arfcn = cellIdentityGsm.arfcn
            cell.lac = cellIdentityGsm.lac
            cell.psc = cellIdentityGsm.psc
            cell.cellBandwidth = getBandwidthFromARFCN((cell.arfcn as Int).toInt())
            cell.cellIdentifier = cellIdentityGsm.cid
            cell.mcc = cellIdentityGsm.mccString ?: ""
            cell.mnc = cellIdentityGsm.mncString ?: ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cell.additionalPLMNId.addAll(cellIdentityGsm.additionalPlmns)
                cell.rssi = signal.rssi
            }
            cell.dbm = signal.dbm
            cell.level = signal.level
            cell.timingAdvance = signal.timingAdvance
            cell.bsic = cellIdentityGsm.bsic
            cell.cid = cellIdentityGsm.cid
            cell.technologyFamily = "GSM";
            cell.cellType = "gsm"
        }
        return cell
    }

    private fun setCellWcdmaData(cellInfoWcdma: CellInfoWcdma): cellData {
        var cell: cellData = cellData()
        var cellIdentityWcdma: CellIdentityWcdma = cellInfoWcdma.cellIdentity
        if (cellIdentityWcdma != null) {
            var (rncid, cellid) = getRncAndCid(cellIdentityWcdma.cid)
            cell.rncId = rncid
            cell.cellId = cellid
            cell.uarfcn = cellIdentityWcdma.uarfcn
            cell.lac = cellIdentityWcdma.lac
            cell.cellBandwidth = getBandwidthFromUARFCN((cell.uarfcn as Int).toInt())
            cell.cellIdentifier = cellIdentityWcdma.cid
            cell.mcc = cellIdentityWcdma.mccString ?: ""
            cell.mnc = cellIdentityWcdma.mncString ?: ""
            cell.psc = cellIdentityWcdma.psc
            val signal = cellInfoWcdma.cellSignalStrength as CellSignalStrengthWcdma
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cell.additionalPLMNId.addAll(cellIdentityWcdma.additionalPlmns)
                cell.ecno = signal.ecNo
            }
            cell.dbm = signal.dbm
            cell.level = signal.level
            cell.asuLevel = signal.asuLevel
            cell.cid = cellIdentityWcdma.cid
            cell.technologyFamily = "WCDMA";
            cell.cellType = "wcdma"
        }
        return cell
    }

    private fun setCellNr(cellInfoNr: CellInfoNr): cellData {
        var cell: cellData = cellData()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var cellIdentityNr = cellInfoNr.cellIdentity as CellIdentityNr
            if (cellIdentityNr != null) {
                cell.nrarfcn = cellIdentityNr.nrarfcn
                cell.tac = cellIdentityNr.tac
                cell.cellBandwidth = getBandwidthFromNARFCN((cell.nrarfcn as Int).toInt())
                cell.cellIdentifier = cellIdentityNr.nci
                cell.mcc = cellIdentityNr.mccString ?: ""
                cell.mnc = cellIdentityNr.mncString ?: ""
                cell.nci = cellIdentityNr.nci
                cell.pci = cellIdentityNr.pci
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cell.additionalPLMNId.addAll(cellIdentityNr.additionalPlmns)
                    for (band in cellIdentityNr.bands) {
                        cell.bands.add(band)
                    }
                }

                val signal = cellInfoNr.cellSignalStrength as CellSignalStrengthNr
                cell.asuLevel = signal.asuLevel
                cell.csiRsrp = signal.csiRsrp
                cell.csiRsrq = signal.csiRsrq
                cell.csiSinr = signal.csiSinr
                cell.level = signal.level
                cell.ssRsrp = signal.ssRsrp
                cell.ssRsrq = signal.ssRsrq
                cell.ssSinr = signal.ssSinr
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    cell.csiCqiTableIndex = signal.csiCqiTableIndex
                    for (report in signal.csiCqiReport) {
                        cell.csiCqi.add(report)
                    }
                }
                cell.dbm = signal.dbm
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    cell.timingAdvance = signal.timingAdvanceMicros
                }
                cell.technologyFamily = "5G NR";
                cell.cellType = "nr"
            }
        }
        return cell
    }

    @SuppressLint("MissingPermission")
    private fun getCellData(telephonyManager: TelephonyManager) {
        if (crowdData.csClickHouse.grantFineLocation == "true") {
            if (telephonyManager != null) {

                val cellInfoList = telephonyManager.allCellInfo
                if (cellInfoList.isNotEmpty()) {
                    var cellInfoData = cellInfoList.filter { info -> info.isRegistered == true }
                    //var cellInfoData = cellInfoList.find { info -> info.isRegistered == true  }
                    if (cellInfoData != null && cellInfoData.size > 0) {
                        var cell = setCellData(cellInfoData[0])
                        crowdData.csClickHouse.earFcn = cell.earfcn
                        crowdData.csClickHouse.arfcn = cell.arfcn
                        crowdData.csClickHouse.uarFcn = cell.uarfcn
                        crowdData.csClickHouse.nrArfcn = cell.nrarfcn
                        crowdData.csOracle.lac = cell.lac
                        crowdData.csClickHouse.tac = cell.tac
                        crowdData.csOracle.tac = cell.tac
                        crowdData.csClickHouse.cellBandwidth = cell.cellBandwidth
                        crowdData.csOracle.cellId = cell.cellId
                        crowdData.csClickHouse.cellIdentifier = cell.cellIdentifier
                        crowdData.csClickHouse.cqi = cell.cqi
                        crowdData.csClickHouse.rsrp = cell.rsrp
                        crowdData.csClickHouse.rsrq = cell.rsrq
                        crowdData.csClickHouse.rssi = cell.rssi
                        crowdData.csClickHouse.rssnr = cell.rssnr
                        crowdData.csClickHouse.psc = cell.psc
                        crowdData.csClickHouse.computedCellularGeneration = cell.technologyFamily
                        val bandArray = JSONArray()
                        if (cell.bands != null && cell.bands.size > 0) {
                            for (item in cell.bands) {
                                bandArray.put(item)
                            }
                        }

                        val plmnArray = JSONArray()
                        if (cell.additionalPLMNId != null && cell.additionalPLMNId.size > 0) {
                            for (item in cell.additionalPLMNId) {
                                plmnArray.put(item)
                            }
                        }
                        if (cell.cellType == "lte") {
                            crowdData.csClickHouse.lteAdditionalPlmns = plmnArray.toString()
                            crowdData.csClickHouse.lteBands =
                                if (cell.bands.size > 0) bandArray.toString() else ""
                            crowdData.csClickHouse.pci = cell.pci
                            crowdData.csClickHouse.timingAdvance = cell.timingAdvance
                            crowdData.csOracle.lteEnodeb = cell.enodeb
                            crowdData.csClickHouse.lteCqi = cell.cqi
                            crowdData.csClickHouse.lteDbm = cell.dbm
                            crowdData.csClickHouse.lteLevel = cell.level
                            crowdData.csClickHouse.lteRssi = cell.rssi
                            crowdData.csClickHouse.lteBandwidth = cell.bandwidth
                            crowdData.csClickHouse.lteCi = cell.ci
                            crowdData.csClickHouse.lteTac = cell.tac
                            crowdData.csClickHouse.lteMcc = cell.mcc ?: ""
                            crowdData.csClickHouse.lteMnc = cell.mnc ?: ""
                            crowdData.csClickHouse.lteNWOperator = cell.nwOperator

                        }
                        if (cell.cellType == "nr") {
                            crowdData.csClickHouse.nrAdditionalPlmns = plmnArray.toString()
                            crowdData.csClickHouse.nrBands =
                                if (cell.bands.size > 0) bandArray.toString() else ""
                            crowdData.csClickHouse.nrAsu = cell.asuLevel
                            crowdData.csClickHouse.nrCsiRsrp = cell.csiRsrp
                            crowdData.csClickHouse.nrCsiRsrq = cell.csiRsrq
                            crowdData.csClickHouse.nrCsiSinr = cell.csiSinr
                            crowdData.csClickHouse.nrLevel = cell.level
                            crowdData.csClickHouse.nrMcc = cell.mcc ?: ""
                            crowdData.csClickHouse.nrMnc = cell.mnc ?: ""
                            crowdData.csClickHouse.nrNci = cell.nci
                            crowdData.csClickHouse.nrPci = cell.pci
                            crowdData.csClickHouse.nrSsRsrp = cell.ssRsrp
                            crowdData.csClickHouse.nrSsRsrq = cell.ssRsrq
                            crowdData.csClickHouse.nrSsSinr = cell.ssSinr
                            crowdData.csClickHouse.nrTac = cell.tac
                            crowdData.csClickHouse.nrCsiCqi =
                                if (cell.csiCqi.size > 0) bandArray.toString() else ""
                            crowdData.csClickHouse.nrCsiCqiTableIndex = cell.csiCqiTableIndex
                            crowdData.csClickHouse.nrDbm = cell.dbm
                            crowdData.csClickHouse.nrTimingAdvance = cell.timingAdvance
                        }
                        if (cell.cellType == "wcdma") {
                            crowdData.csClickHouse.wcdmaAdditionalPlmns = plmnArray.toString()
                            crowdData.csClickHouse.wcdmaEcno = cell.ecno
                            crowdData.csClickHouse.wcdmaDbm = cell.dbm
                            crowdData.csClickHouse.wcdmaLevel = cell.level
                            crowdData.csClickHouse.wcdmaAsuLevel = cell.asuLevel
                            crowdData.csClickHouse.wcdmaCid = cell.cid
                            crowdData.csClickHouse.wcdmaLac = cell.lac
                            crowdData.csClickHouse.wcdmaPsc = cell.psc
                            crowdData.csClickHouse.wcdmaMcc = cell.mcc ?: ""
                            crowdData.csClickHouse.wcdmaMnc = cell.mnc ?: ""
                        }
                        if (cell.cellType == "gsm") {
                            crowdData.csClickHouse.gsmAdditionalPlmns = plmnArray.toString()
                            crowdData.csClickHouse.gsmRssi = cell.rssi
                            crowdData.csClickHouse.gsmDbm = cell.dbm
                            crowdData.csClickHouse.gsmLevel = cell.level
                            crowdData.csClickHouse.gsmTimingAdvance = cell.timingAdvance
                            crowdData.csClickHouse.gsmBsic = cell.bsic
                            crowdData.csClickHouse.gsmCid = cell.cid
                            crowdData.csClickHouse.gsmLac = cell.lac
                        }

                        for (cellInfo in cellInfoData) (
                                if (cellInfo.isRegistered) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        if (cellInfo is CellInfoNr) {
                                            val signal =
                                                cellInfo.cellSignalStrength as CellSignalStrengthNr
                                            if (signal.ssRsrp != Integer.MAX_VALUE || signal.ssRsrq != Integer.MAX_VALUE || signal.ssSinr != Integer.MAX_VALUE) {
                                                crowdData.csClickHouse.isNrTelephonySourced = "true"
                                            }

                                        }
                                    }
                                }
                                )
                        crowdData.csClickHouse.numberRegisteredNetworks = cellInfoData.size
                        crowdData.csClickHouse.numberUnregisteredNetworks =
                            cellInfoList.size - cellInfoData.size
                    }
                }
            }
        }

    }

    private fun getBatteryInfos(context: Context) {
        val batteryStatus: Intent? =
            IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
                context.registerReceiver(null, intentFilter)
            }

        batteryStatus?.let {
            // Batarya seviyesi
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            crowdData.csClickHouse.batteryLevelMax = scale
            crowdData.csClickHouse.batteryLevel = (level * 100 / scale).toString()
            crowdData.csClickHouse.batteryPlugged =
                when (it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "true"
                    BatteryManager.BATTERY_PLUGGED_USB -> "true"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "true"
                    else -> "false"
                }
            crowdData.csClickHouse.batteryPresent = BatteryManager.EXTRA_PRESENT
            crowdData.csClickHouse.batteryStatus = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            crowdData.csClickHouse.batteryTechnology =
                it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Bilinmiyor"
            crowdData.csClickHouse.batteryVoltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            crowdData.csClickHouse.batteryTemperature =
                it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0
        }
    }

    private fun getConenctivitiData(
        connectivityManager: ConnectivityManager,
        telephonyManager: TelephonyManager
    ) {
        if (crowdData.csClickHouse.grantNetworkState == "true") {
            if (connectivityManager != null) {
                crowdData.csClickHouse.connectionType =
                    getDataConnectionType(telephonyManager, connectivityManager)
                crowdData.csClickHouse.downstreamBandwidthKbps =
                    getDownstreamBandwidthKbps(connectivityManager)
                crowdData.csClickHouse.hasCellularService =
                    hasCellularConnection(connectivityManager).toString()
                crowdData.csClickHouse.upstreamBandwidthKbps =
                    getUpstreamBandwidthKbps(connectivityManager)
                crowdData.csClickHouse.isUsingCarrierAggregation =
                    isCarrierAggregationEnabled(connectivityManager).toString()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceData(
        context: Context, telephonyManager: TelephonyManager,
        powerManager: PowerManager, displayManager: DisplayManager,
        connectivityManager: ConnectivityManager
    ) {
        crowdData.csClickHouse.deviceBrandRaw = Build.BRAND
        crowdData.csClickHouse.deviceManufacturer = Build.MANUFACTURER
        crowdData.csClickHouse.deviceManufacturerRaw = Build.MANUFACTURER
        crowdData.csClickHouse.deviceModel = Build.MANUFACTURER + " " + Build.MODEL
        crowdData.csClickHouse.deviceModelRaw = Build.MODEL
        crowdData.csClickHouse.osVersion = Build.VERSION.SDK_INT.toString()
        crowdData.csClickHouse.radio = Build.getRadioVersion()
        val locale = Locale.getDefault()
        crowdData.csClickHouse.country = locale.displayCountry
        crowdData.csClickHouse.deviceLanguage = locale.language
        crowdData.deviceId = getDeviceId()
        crowdData.csClickHouse.isAirplaneMode = isAirplaneModeOn(context).toString()
        crowdData.csClickHouse.timeZoneName = TimeZone.getDefault().getID()
        crowdData.csClickHouse.validDeviceCheck = hasValidDeviceId(context).toString()
        if (telephonyManager != null) {
            crowdData.csClickHouse.dataActivity = telephonyManager.dataActivity
            crowdData.csClickHouse.dataState = telephonyManager.dataState
            crowdData.csClickHouse.hasIccCard =
                (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY).toString()
            crowdData.csClickHouse.isNetworkRoaming = telephonyManager.isNetworkRoaming.toString()
            crowdData.csClickHouse.isWorldPhone = telephonyManager.isWorldPhone.toString()
            crowdData.csClickHouse.serviceState = telephonyManager.serviceState.toString()
            //crowdData.phoneNumber = telephonyManager.line1Number ?: ""
            val serviceState: ServiceState? = telephonyManager.serviceState
            if (serviceState != null) {
                crowdData.csClickHouse.serviceState = getServiceState(serviceState.state)
                val bandwidthArray = JSONArray()
                for (bandwith in serviceState.cellBandwidths) {
                    bandwidthArray.put(bandwith)
                }
                crowdData.csClickHouse.cellBandwidths = bandwidthArray.toString()
            }
            crowdData.csClickHouse.simState = getSimCardState(telephonyManager.simState)
            val simOperator = telephonyManager.simOperator
            crowdData.csClickHouse.simOperatorMccCode =
                if (simOperator != null && simOperator.length >= 3)
                    simOperator.substring(0, 3) else ""
            crowdData.csClickHouse.simOperatorMncCode =
                if (simOperator != null && simOperator.length >= 3)
                    simOperator.substring(3) else ""
            crowdData.csClickHouse.networkOperatorName = telephonyManager.networkOperatorName
            if (telephonyManager.networkOperator.isNotEmpty()) {
                crowdData.csClickHouse.networkOperatorMccCode =
                    telephonyManager.networkOperator.substring(0, 3)
                crowdData.csClickHouse.networkOperatorMncCode =
                    telephonyManager.networkOperator.substring(3)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                crowdData.csClickHouse.activeModemCount = telephonyManager.activeModemCount
                crowdData.csClickHouse.supportedModemCount = telephonyManager.activeModemCount
            } else {
                crowdData.csClickHouse.activeModemCount = telephonyManager.phoneCount
                crowdData.csClickHouse.supportedModemCount = telephonyManager.phoneCount
            }
            if (crowdData.csClickHouse.grantPhoneState == "true") {
                crowdData.csClickHouse.isAccessNetworkTechnologyNr =
                    (telephonyManager.networkType == TelephonyManager.NETWORK_TYPE_NR).toString()
                crowdData.csClickHouse.isConcurrentVoiceDataSupported =
                    canSupportVoiceAndData(telephonyManager).toString()
                crowdData.csClickHouse.isDataCapable =
                    (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE &&
                            telephonyManager.isDataEnabled).toString()
                crowdData.csClickHouse.isDataEnabled = telephonyManager.isDataEnabled.toString()
                val networkOperator = telephonyManager.networkOperator
                val simOperator = telephonyManager.simOperator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    crowdData.csClickHouse.isDataRoamingEnabled =
                        telephonyManager.isDataRoamingEnabled().toString()
                    crowdData.csClickHouse.isMultiSimSupported =
                        telephonyManager.isMultiSimSupported
                    val networkType = telephonyManager.networkType
                    crowdData.csClickHouse.isNrAvailable =
                        (networkType == TelephonyManager.NETWORK_TYPE_NR).toString()
                    crowdData.csClickHouse.isDevice5gCapable =
                        (telephonyManager.isDataEnabled && telephonyManager.networkType == TelephonyManager.NETWORK_TYPE_NR).toString()
                } else {
                    crowdData.csClickHouse.isDataRoamingEnabled = (Settings.Secure.getInt(
                        context.contentResolver,
                        Settings.Secure.DATA_ROAMING
                    ) == 1).toString()
                }

                crowdData.csClickHouse.isInternationalRoaming =
                    (crowdData.csClickHouse.isDataRoamingEnabled == "true" && networkOperator != null && simOperator != null && !networkOperator.equals(
                        simOperator
                    )).toString()

                if (connectivityManager != null) {
                    crowdData.csClickHouse.isDataConnectionAllowed =
                        isCellularDataAllowed(telephonyManager, connectivityManager).toString()
                }
            }
        }
        if (powerManager != null) {
            crowdData.csClickHouse.deviceIdleMode = powerManager.isDeviceIdleMode.toString()
            crowdData.csClickHouse.powerInteractive = powerManager.isInteractive.toString()
            crowdData.csClickHouse.powerSaveMode = powerManager.isPowerSaveMode.toString()

        }
        if (displayManager != null) {
            for (display in displayManager.displays) {
                crowdData.csClickHouse.displayState =
                    display.state // Get the current state of the display
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationData(context: Context, locationManager: LocationManager) {
        if (crowdData.csClickHouse.grantCoarseLocation == "true" && crowdData.csClickHouse.grantFineLocation == "true") {

            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context);
            //val x = getCurrentLocation(fusedLocationClient)

            //TO DO provider kontrolü ekle
            var lastGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            var lastNetworkLocation =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            var fused = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            var xx = locationManager.getProviders(true)

            val location = lastNetworkLocation ?: lastGPSLocation ?: fused
            if (location != null) {
                crowdData.csClickHouse.locationType = 1
                crowdData.csOracle.altitude = location.altitude.toInt()
                crowdData.csClickHouse.locationAccuracy = location.accuracy
                crowdData.csClickHouse.locationCheck = isValidLocation(location).toString()
                crowdData.csClickHouse.locationSpeedMps = location.speed
                crowdData.csClickHouse.verticalAccuracy = location.verticalAccuracyMeters
                val currentTime = System.currentTimeMillis()
                crowdData.csClickHouse.locationAge = currentTime - location.time
                val adress = fetchLocalityFromLocation(context, location, 3)
                if (adress != null) {
                    crowdData.csClickHouse.locality = adress.locality ?: ""
                    crowdData.csClickHouse.postalCode = adress.postalCode ?: ""
                    crowdData.csClickHouse.region = adress.adminArea ?: ""
                    crowdData.csClickHouse.subregion = adress.subAdminArea
                    adress.subThoroughfare
                }

            }
        }

    }

    private fun isValidLocation(location: Location?): Boolean {
        // Check if location is not null
        if (location == null) {
            return false
        }
        // Check if latitude and longitude are within valid ranges
        val isLatitudeValid = location.latitude in -90.0..90.0
        val isLongitudeValid = location.longitude in -180.0..180.0

        // Check if accuracy is acceptable (e.g., less than 100 meters)
        val isAccuracyValid = location.accuracy < 100

        return isLatitudeValid && isLongitudeValid && isAccuracyValid
    }

    private fun fetchLocalityFromLocation(
        context: Context,
        location: Location,
        retryCount: Int
    ): Address? {

        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null && !addresses.isEmpty()) {
                return addresses[0]
            }
        } catch (e: IOException) {
            if (e is SocketTimeoutException) {
                fetchLocalityFromLocation(context, location, retryCount - 1)
            } else {
                e.printStackTrace()
            }
        }
        return null
    }

    @SuppressLint("MissingPermission")
    private fun getSubscriptionData(
        subscriptionManager: SubscriptionManager,
        telephonyManager: TelephonyManager
    ) {
        if (subscriptionManager != null && telephonyManager.simState != TelephonyManager.SIM_STATE_ABSENT) {
            crowdData.csClickHouse.simCount =
                subscriptionManager.activeSubscriptionInfoCount.toString()
            if (crowdData.csClickHouse.grantPhoneState == "true") {
                val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
                if (subscriptionInfoList != null) {
                    //if(subscriptionInfoList.size > 0) {
                    //if (crowdData.phoneNumber.isEmpty()) {
                    //val x = subscriptionInfoList[0].subscriptionId
                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    //crowdData.phoneNumber = subscriptionManager.getPhoneNumber(x)
                    //} else {
                    //crowdData.phoneNumber = subscriptionInfoList[0].number
                    //}
                    //}
                    crowdData.csClickHouse.rawSimOperatorName =
                        (subscriptionInfoList[0].carrierName ?: "").toString()
                    crowdData.csClickHouse.simOperatorName =
                        subscriptionInfoList[0].displayName.toString()
                }
                if (subscriptionInfoList.size > 1) {
                    crowdData.csClickHouse.altRawSimOperatorName =
                        subscriptionInfoList[1].carrierName.toString()
                    crowdData.csClickHouse.altSimOperatorMccCode = subscriptionInfoList[1].mcc.toString()
                    crowdData.csClickHouse.altSimOperatorMncCode = subscriptionInfoList[1].mnc.toString()
                    crowdData.csClickHouse.altSimOperatorName =
                        subscriptionInfoList[1].displayName.toString()
                }
            }
        }
    }

    private fun getGrandPermissions(context: Context) {
        if (PermissionHelper.hasPermission(context, Manifest.permission.READ_PHONE_STATE))
            crowdData.csClickHouse.grantPhoneState = "true"
        if (PermissionHelper.hasPermission(context, Manifest.permission.RECEIVE_BOOT_COMPLETED))
            crowdData.csClickHouse.grantBootCompleted = "true"
        if (PermissionHelper.hasPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            crowdData.csClickHouse.grantBackgroundLocation = "true"
            crowdData.csClickHouse.hasBgLocationPermission = "true"
        }
        if (PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION))
            crowdData.csClickHouse.grantCoarseLocation = "true"
        if (PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
            crowdData.csClickHouse.grantFineLocation = "true"
        if (PermissionHelper.hasPermission(context, Manifest.permission.INTERNET))
            crowdData.csClickHouse.grantInternet = "true"
        if (PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE))
            crowdData.csClickHouse.grantNetworkState = "true"
        if (PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_WIFI_STATE))
            crowdData.csClickHouse.grantWifiState = "true"
    }

    private fun getWifiData(wifiManager: WifiManager) {
        if (wifiManager != null) {
            crowdData.csClickHouse.wifiEnabled = wifiManager.isWifiEnabled.toString()
            crowdData.csClickHouse.wifiState = wifiManager.wifiState
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                crowdData.csClickHouse.wifiIs2_4GhzBandSupported =
                    wifiManager.is24GHzBandSupported.toString()
                crowdData.csClickHouse.wifiIs60GhzBandSupported =
                    wifiManager.is60GHzBandSupported.toString()
                crowdData.csClickHouse.wifiIs6GhzBandSupported =
                    wifiManager.is6GHzBandSupported.toString()

            }
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null && wifiManager.wifiState !in intArrayOf(0, 1)) {
                crowdData.csClickHouse.wifiCarrierName = wifiInfo.ssid
                crowdData.csClickHouse.wifiChannel = frequencyToChannel(wifiInfo.frequency)
                crowdData.csClickHouse.wifiChannelWidth = when {
                    wifiInfo.frequency in 2400..2483 -> 20 // 2.4 GHz networks typically use 20 MHz
                    wifiInfo.frequency in 4915..4980 -> 40 // 5 GHz networks can use both
                    wifiInfo.frequency in 5035..5080 -> 40
                    wifiInfo.frequency in 5170..5825 -> 160 // Wider channels are possible
                    else -> 0
                }
                crowdData.csClickHouse.wifiFrequency = wifiInfo.frequency
                crowdData.csClickHouse.wifiRssi = wifiInfo.rssi
                crowdData.csClickHouse.wifiBssId = wifiInfo.bssid
                crowdData.csClickHouse.wifiHiddenSSID = wifiInfo.hiddenSSID.toString()
                crowdData.csClickHouse.wifiLinkSpeed = wifiInfo.linkSpeed
                crowdData.csClickHouse.wifiNetworkId = wifiInfo.networkId

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    crowdData.csClickHouse.wifiCurrentSecurityType = wifiInfo.currentSecurityType
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    crowdData.csClickHouse.wifiPasspointFqdn = wifiInfo.passpointFqdn ?: ""
                    crowdData.csClickHouse.wifiPasspointProviderName =
                        wifiInfo.passpointProviderFriendlyName ?: ""
                    crowdData.csClickHouse.wifiRxLinkSpeed = wifiInfo.rxLinkSpeedMbps
                    crowdData.csClickHouse.wifiTxLinkSpeedMbps = wifiInfo.txLinkSpeedMbps
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    crowdData.csClickHouse.wifiMaxSupportedRxLinkSpeed =
                        wifiInfo.maxSupportedRxLinkSpeedMbps
                    crowdData.csClickHouse.wifiMaxSupportedTxLinkSpeed =
                        wifiInfo.maxSupportedTxLinkSpeedMbps
                    crowdData.csClickHouse.wifiStandard = wifiInfo.wifiStandard
                }
            }
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    // Check if it's not a loopback address and is an IPv4 address
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.getHostAddress()
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return "Unavailable"
    }

    @SuppressLint("MissingPermission")
    fun getDeviceInfo(triggerName: String): CrowdData {
        val context = getContext()
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val powerManeger = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        getGrandPermissions(context)

        getBatteryInfos(context)

        getDeviceData(context, telephonyManager, powerManeger, displayManager, connectivityManager)

        getSubscriptionData(subscriptionManager, telephonyManager)

        getCellData(telephonyManager)

        getLocationData(context, locationManager)

        getConenctivitiData(connectivityManager, telephonyManager)

        getWifiData(wifiManager)

        val localDate = LocalDate.now()

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE  // Formats to "yyyy-MM-dd"
        crowdData.csClickHouse.receivedDateLocal = localDate.format(formatter)
        crowdData.csClickHouse.triggerName = triggerName

        val gson = Gson()
        val json = gson.toJson(crowdData)
        println(json)
        return crowdData
    }
}