package org.wildaid.ofish.data.mpa

import android.content.res.Resources
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import org.wildaid.ofish.R

fun addTestMpa(googleMap: GoogleMap, resources: Resources) {
    for (mpa in Mpa.values()) {
        val polygon = googleMap.addPolygon(
            PolygonOptions()
                .clickable(false)
                .addAll(mpa.polygon)
        )
        polygon.fillColor = resources.getColor(R.color.amber, null)
        polygon.tag = mpa.mpaName
    }
}

enum class Mpa(val mpaName: String, val polygon: List<LatLng>) {
    GALAPAGOS(
        "Galapagos",
        listOf(
            LatLng(-0.534202465, -88.03310394),
            LatLng(-2.811371193, -88.28613281),
            LatLng(-2.635788574, -93.12011719),
            LatLng(-0.790990498, -95.14160156),
            LatLng(2.372368709, -96.28417969),
            LatLng(4.302591077, -94.26269531),
            LatLng(3.250208562, -91.01074219),
            LatLng(-0.534202465, -88.03310394)
        )
    ),
    TEST(
        "Test",
        listOf(
            LatLng(10.0, -90.0),
            LatLng(12.811371193, -90.28613281),
            LatLng(12.635788574, -93.12011719),
            LatLng(10.790990498, -93.14160156)
        )
    )
}