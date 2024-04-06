package com.vrem.wifianalyzer.wifi.model

data class WiFiIEDetail(
    var stationCount: Int = -1,
) {
    companion object {
        val EMPTY = WiFiIEDetail()
    }
}