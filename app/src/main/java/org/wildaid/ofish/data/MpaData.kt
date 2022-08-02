package org.wildaid.ofish.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon

class MpaData(
    var id: String = "",
    var name: String = "",
    var agency: String = "",
    var country: String = "",
    var description: String? = "",
    var info: String = "",
    var text: String = "",
    var hexColor: String = "",
    var coordinates: List<LatLng> = ArrayList(),
    var polygon: Polygon?=null
)
