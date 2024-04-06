package com.vrem.wifianalyzer.wifi.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class WiFiInformationElement(val id: Int, val idExt: Int, val bytes: ByteBuffer) {

    @OptIn(ExperimentalStdlibApi::class)
    companion object {
        private val OUI_MICROSOFT = "0050F2".hexToByteArray()

        fun parse(elements: List<WiFiInformationElement>): WiFiIEDetail {
            val wiFiIEDetail = WiFiIEDetail()

            elements.forEach { element ->
                val bytes = element.bytes.duplicate()
                when (element.id) {
                    // BSS Load
                    11 -> {
                        wiFiIEDetail.stationCount = bytes.order(ByteOrder.LITTLE_ENDIAN).short.toInt()
                    }

                    // Vendor Specific
                    221 -> {
                        val oui = ByteArray(3)
                        bytes.get(oui)
                        if (oui.contentEquals(OUI_MICROSOFT)) {
                            val vsOuiType = bytes.get().toInt()

                            // WPS
                            if (vsOuiType == 0x04) {
                                while (bytes.hasRemaining()) {
                                    val elemType = bytes.short.toInt()
                                    val elemLength = bytes.short.toInt()
                                    val elemData = ByteArray(elemLength)
                                    bytes.get(elemData)
                                    when (elemType) {
                                        0x1011 -> { // Device Name
                                            wiFiIEDetail.wpsDeviceName = elemData.decodeToString()
                                        }
                                        0x1021 -> { // Manufacturer
                                            wiFiIEDetail.wpsManufacturer = elemData.decodeToString()
                                        }
                                        0x1023 -> { // Model Name
                                            wiFiIEDetail.wpsModelName = elemData.decodeToString()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return wiFiIEDetail
        }
    }

}