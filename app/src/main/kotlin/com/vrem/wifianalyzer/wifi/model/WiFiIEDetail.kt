package com.vrem.wifianalyzer.wifi.model

import com.vrem.util.EMPTY

data class WiFiIEDetail(
    var stationCount: Int = -1,
    var wpsManufacturer: String = String.EMPTY,
    var wpsDeviceName: String = String.EMPTY,
    var wpsModelName: String = String.EMPTY,
    var arubaInstantOnSiteID: String = String.EMPTY,
    var arubaInstantOnDeviceName: String = String.EMPTY,
    var merakiNetworkID: String = String.EMPTY,
    var roamingConsortiumNumANQPOIs: Int = -1,
    var roamingConsortiumOIs: List<String> = listOf(),
    var messages: List<String> = listOf(),
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
        if (arubaInstantOnDeviceName.isNotEmpty()) {
            details.add(arubaInstantOnDeviceName)
        }
        if (arubaInstantOnSiteID.isNotEmpty()) {
            details.add("ION: $arubaInstantOnSiteID")
        }
        if (merakiNetworkID.isNotEmpty()) {
            details.add("Meraki: $merakiNetworkID")
        }
        if (roamingConsortiumNumANQPOIs >= 0 || roamingConsortiumOIs.isNotEmpty()) {
            var text = "RCOI"
            if (roamingConsortiumNumANQPOIs >= 0) { text += "(${roamingConsortiumNumANQPOIs})" }
            if (roamingConsortiumOIs.isNotEmpty()) { text += ": " + roamingConsortiumOIs.joinToString(separator = " ") }
            details.add(text)
        }
        if (messages.isNotEmpty()) {
            details.add(messages.joinToString(separator = " "))
        }
        return details.distinct().joinToString(separator = " ")
    }

    companion object {
        val EMPTY = WiFiIEDetail()
    }
}