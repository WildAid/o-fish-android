package org.wildaid.ofish.util

import android.location.Location

const val LATITUDE = 0
const val LONGITUDE = 1

fun convert(coordinate: Double?, type: Int): String {
    if (coordinate == null) return ""
    return format(coordinate, type)
}

private fun format(coordinate: Double, type: Int): String {
    val snwe = when {
        type == LATITUDE && coordinate < 0 -> "S"
        type == LATITUDE && coordinate >= 0 -> "N"
        type == LONGITUDE && coordinate < 0 -> "W"
        type == LONGITUDE && coordinate >= 0 -> "E"
        else -> ""
    }
    val str = Location.convert(coordinate, Location.FORMAT_SECONDS)
    val list = str.split(":").toMutableList()
    if (list[0].startsWith("-")) list[0] = list[0].substring(1)
    return "$snwe${list[0]}°${list[1]}'${list[2]}\""
}