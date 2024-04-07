package com.vrem.wifianalyzer.wifi.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class WiFiInformationElement(val id: Int, val idExt: Int, val bytes: ByteBuffer) {

    @OptIn(ExperimentalStdlibApi::class)
    companion object {
        private val OUI_MICROSOFT = "0050F2".hexToByteArray()
        private val OUI_MERAKI    = "00180A".hexToByteArray()

        fun parse(elements: List<WiFiInformationElement>): WiFiIEDetail {
            val wiFiIEDetail = WiFiIEDetail()

            elements.forEach { element ->
                val bytes = element.bytes.duplicate()
                when (element.id) {
                    // BSS Load
                    11 -> {
                        wiFiIEDetail.stationCount = bytes.order(ByteOrder.LITTLE_ENDIAN).short.toInt()
                    }

                    // Roaming Consortium
                    111 -> {
                        // Number of ANQP OIs
                        wiFiIEDetail.roamingConsortiumNumANQPOIs = bytes.get().toInt()

                        val oiLengths = bytes.get().toInt()
                        val oi1Length = oiLengths and 0b00001111
                        val oi2Length = (oiLengths and 0b11110000) shr 4

                        val ois = mutableListOf<String>()
                        if (oi1Length > 0) {
                            val oi1 = ByteArray(oi1Length)
                            bytes.get(oi1)
                            ois.add(oi1.toHexString(HexFormat.UpperCase))
                        }
                        if (oi2Length > 0) {
                            val oi2 = ByteArray(oi2Length)
                            bytes.get(oi2)
                            ois.add(oi2.toHexString(HexFormat.UpperCase))
                        }
                        if (bytes.hasRemaining()) {
                            val oi3 = ByteArray(bytes.remaining())
                            bytes.get(oi3)
                            ois.add(oi3.toHexString(HexFormat.UpperCase))
                        }

                        wiFiIEDetail.roamingConsortiumOIs = ois
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
                        } else if (oui.contentEquals(OUI_MERAKI)) {
                            val vsUnknown1 = bytes.get().toInt() // 0x07
                            if (vsUnknown1 == 0x07) {
                                bytes.position(bytes.position() + 5)
                                val networkId = ByteArray(bytes.remaining())
                                bytes.get(networkId)
                                wiFiIEDetail.merakiNetworkID = networkId.toHexString(HexFormat.UpperCase).trimStart('0')
                            }
                        }
                    }
                }
            }

            return wiFiIEDetail
        }
    }

}