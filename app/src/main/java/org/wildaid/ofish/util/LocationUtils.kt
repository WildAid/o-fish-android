package org.wildaid.ofish.util

import android.location.Location

const val LATITUDE = 0
const val LONGITUDE = 1

fun convert(coordinate: Double?, type: Int): String {
    if (coordinate == null) return ""
    return format(coordinate, type)
}

private fun format(coordinate: Double, type: Int): String {
    val snwe = extractDirection(type, coordinate)
    val strRepresentation = Location
        .convert(coordinate, Location.FORMAT_SECONDS)
        .substringAfter('-')
        .substringBefore('.')

    val coordinateList = strRepresentation
        .split(":")
        .map {
            if (it.length == 1) "0$it" else it
        }
    if (coordinateList.size != 3) return ""

    return "$snwe${coordinateList[0]}°${coordinateList[1]}'${coordinateList[2]}\""
}

private fun extractDirection(type: Int, coordinate: Double): String {
    return when {
        type == LATITUDE && coordinate < 0 -> "S"
        type == LATITUDE && coordinate >= 0 -> "N"
        type == LONGITUDE && coordinate < 0 -> "W"
        type == LONGITUDE && coordinate >= 0 -> "E"
        else -> ""
    }
}