package com.vrem.wifianalyzer.wifi.model

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class WiFiInformationElement(val id: Int, val idExt: Int, val bytes: ByteBuffer) {

    @OptIn(ExperimentalStdlibApi::class)
    companion object {
        private val OUI_MICROSOFT = "0050F2".hexToByteArray()
        private val OUI_ARUBA     = "000B86".hexToByteArray()
        private val OUI_APPLE     = "0017F2".hexToByteArray()
        private val OUI_MERAKI    = "00180A".hexToByteArray()

        fun parse(elements: List<WiFiInformationElement>): WiFiIEDetail {
            val wiFiIEDetail = WiFiIEDetail()

            elements.forEach { element ->
                try {
                    parseSingleElement(wiFiIEDetail, element)
                } catch (e: BufferUnderflowException) { // Malformed Information Element
                    wiFiIEDetail.messages += "FAIL: IE(${element.id})"
                }
            }

            return wiFiIEDetail
        }

        private fun parseSingleElement(wiFiIEDetail: WiFiIEDetail, element: WiFiInformationElement) {
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
                    } else if (oui.contentEquals(OUI_ARUBA)) {
                        val vsOuiVersion = bytes.get().toInt()
                        val vsOuiType = bytes.get().toInt()
                        val vsOuiSubType = bytes.get().toInt()

                        // Instant On AP ?
                        if (vsOuiVersion == 1 && vsOuiType == 7 && vsOuiSubType == 8) {
                            bytes.position(bytes.position() + 13) // macaddr, unknown

                            val siteIdLen = bytes.short.toInt()
                            val siteId = ByteArray(siteIdLen)
                            bytes.get(siteId)
                            wiFiIEDetail.arubaInstantOnSiteID = siteId.decodeToString()

                            bytes.position(bytes.position() + 1) // unknown

                            val deviceNameLen = bytes.short.toInt()
                            val deviceName = ByteArray(deviceNameLen)
                            bytes.get(deviceName)
                            wiFiIEDetail.arubaInstantOnDeviceName = deviceName.decodeToString()
                        }
                    } else if (oui.contentEquals(OUI_APPLE)) {
                        val vsUnknown1 = bytes.get().toInt() // 0x06
                        val vsUnknown2 = bytes.get().toInt() // 0x03
                        val vsUnknown3 = bytes.get().toInt() // 0x01
                        if (vsUnknown1 == 0x06 && vsUnknown2 == 0x03 && vsUnknown3 == 0x01) {
                            val ssidLen = bytes.get().toInt()
                            val ssid = ByteArray(ssidLen)
                            bytes.get(ssid)
                            wiFiIEDetail.appleHotspotConnectedSSID = ssid.decodeToString().trimStart { it <= '\u0000' }
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
    }

}