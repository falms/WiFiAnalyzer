package com.vrem.wifianalyzer.wifi.model

import com.vrem.util.EMPTY

data class WiFiIEDetail(
    var stationCount: Int = -1,
    var wpsManufacturer: String = String.EMPTY,
    var wpsDeviceName: String = String.EMPTY,
    var wpsModelName: String = String.EMPTY,
) {
    fun deviceDetail(): String {
        val details = mutableListOf<String>()
        if (wpsManufacturer.isNotEmpty()) {
            details.add(wpsManufacturer)
        }
        if (wpsDeviceName.isNotEmpty()) {
            details.add(wpsDeviceName)
        }
        if (wpsModelName.isNotEmpty()) {
            details.add(wpsModelName)
        }
        return details.distinct().joinToString(separator = " ")
    }

    companion object {
        val EMPTY = WiFiIEDetail()
    }
}