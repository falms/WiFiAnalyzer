package com.vrem.wifianalyzer.wifi.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class WiFiInformationElement(val id: Int, val idExt: Int, val bytes: ByteBuffer) {

    companion object {
        fun parse(elements: List<WiFiInformationElement>): WiFiIEDetail {
            val wiFiIEDetail = WiFiIEDetail()

            elements.forEach { element ->
                val bytes = element.bytes.duplicate()
                when (element.id) {
                    // BSS Load
                    11 -> {
                        wiFiIEDetail.stationCount = bytes.order(ByteOrder.LITTLE_ENDIAN).short.toInt()
                    }
                }
            }

            return wiFiIEDetail
        }
    }

}