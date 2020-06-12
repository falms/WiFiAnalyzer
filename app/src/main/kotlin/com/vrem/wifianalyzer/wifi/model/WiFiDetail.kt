/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2020 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.vrem.wifianalyzer.wifi.model

import com.vrem.util.EMPTY

// FIXME remove @JvmOverloads after full conversion to Kotlin
data class WiFiDetail @JvmOverloads constructor(
        val wiFiIdentifier: WiFiIdentifier = WiFiIdentifier.EMPTY,
        val capabilities: String = String.EMPTY,
        val wiFiSignal: WiFiSignal = WiFiSignal.EMPTY,
        val wiFiAdditional: WiFiAdditional = WiFiAdditional.EMPTY,
        val children: List<WiFiDetail> = emptyList()) : Comparable<WiFiDetail> {

    constructor(wiFiDetail: WiFiDetail, wiFiAdditional: WiFiAdditional) :
            this(wiFiDetail.wiFiIdentifier, wiFiDetail.capabilities, wiFiDetail.wiFiSignal, wiFiAdditional)

    constructor(wiFiDetail: WiFiDetail, children: List<WiFiDetail>) :
            this(wiFiDetail.wiFiIdentifier, wiFiDetail.capabilities, wiFiDetail.wiFiSignal, wiFiDetail.wiFiAdditional, children)

    fun security(): Security = Security.findOne(capabilities)

    fun securities(): Set<Security> = Security.findAll(capabilities)

    fun noChildren(): Boolean = children.isNotEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WiFiDetail

        return wiFiIdentifier == other.wiFiIdentifier
    }

    override fun hashCode(): Int = wiFiIdentifier.hashCode()

    override fun compareTo(other: WiFiDetail): Int = wiFiIdentifier.compareTo(other.wiFiIdentifier)

    companion object {
        @JvmField
        val EMPTY = WiFiDetail()
    }
}