package org.wildaid.ofish.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.MpaData
import org.wildaid.ofish.data.PolygonData
import org.wildaid.ofish.ui.polygon_info.PolygonBottomSheetFragment

const val POLYGON_CAPACITY = "#99"
const val POLYGON_STROKE_CAPACITY = "#80"
const val MAIN_COLOR_CAPACITY = 3
const val LOCATION_ICON_SIZE = 150

class MarineProtectionArea {
    var polygonBottomSheetFragment: PolygonBottomSheetFragment = PolygonBottomSheetFragment()

    private var _onPolygonClick = MutableLiveData<Event<PolygonData>>()
    val onPolygonClick: LiveData<Event<PolygonData>>
        get() = _onPolygonClick

    private lateinit var marker: Marker
    private lateinit var mpaData: List<MpaData>

    private fun setColorWithCapacity(color: String, capacity: String): Int {
        val rgbColor = color.substring(MAIN_COLOR_CAPACITY)
        val colorWithCapacity = capacity.plus(rgbColor)
        println("110 color = ${Color.parseColor(colorWithCapacity)}")
        return Color.parseColor(colorWithCapacity)
    }

    fun removeMarker() {
        marker.remove()
    }

    fun addMpa(mpaList: List<MpaData>, googleMap: GoogleMap) {
        mpaData = mpaList
        mpaList.forEach { mpa ->
            mpa.polygon = googleMap.addPolygon(
                PolygonOptions()
                    .clickable(true)
                    .addAll(mpa.coordinates)
            )
            mpa.polygon.also {
                it?.tag = mpa.name
                it?.fillColor = setColorWithCapacity("#".plus(mpa.hexColor), POLYGON_CAPACITY)
                it?.strokeColor = setColorWithCapacity(
                    "#".plus(mpa.hexColor),
                    POLYGON_STROKE_CAPACITY
                )
            }
        }

        googleMap.setOnPolygonClickListener {
            if (checkVisibilityOfPolygon()) {
                _onPolygonClick.value =
                    Event(getSelectedPolygonInformation(it.tag.toString()))
            }
        }

    }

    fun disablePolygons() {
        mpaData.forEach {
            it.polygon?.isVisible=false
        }
    }

    fun enablePolygons() {
        mpaData.forEach {
            it.polygon?.isVisible = true
        }
    }

    private fun checkVisibilityOfPolygon(): Boolean {
        return mpaData[0].polygon?.isVisible == true
    }

    private fun getSelectedPolygonInformation(polygonTag: String): PolygonData {
        val data = PolygonData()
        mpaData.forEach {
            if (polygonTag == it.polygon?.tag){
                data.polygonName = it.name
                data.agency = it.agency
                data.country = it.country
                data.description = it.text
                data.info = it.info
                data.color = Color.parseColor("#".plus(it.hexColor))
            }
        }
        return data
    }

    fun onPolygonReact(
        value: PolygonData,
        fragmentManager: FragmentManager,
        googleMap: GoogleMap,
        context: Context?,
    ) {
        polygonBottomSheetFragment.setPolygonData(value)
        polygonBottomSheetFragment.show(fragmentManager, "TAG")
        addMarker(
            context,
            googleMap,
            getLatLongOfPolygon(getSelectedPolygon(value.polygonName)),
            value.color
        )
    }

    private fun getSelectedPolygon(polygonName:String):List<LatLng>{
        lateinit var selectedPolygon:List<LatLng>
        mpaData.forEach {
            if (polygonName == it.name){
                selectedPolygon = it.coordinates
            }
        }
        return selectedPolygon
    }


    private fun addMarker(
        context: Context?,
        googleMap: GoogleMap,
        centerOfPolygon: LatLng,
        color: Int
    ) {
        val bitmap = context?.let { R.drawable.bottom_sheet_location_asset.toBitmap(it, color) }
        val scaledBitmap = bitmap?.let {
            Bitmap.createScaledBitmap(
                it,
                LOCATION_ICON_SIZE,
                LOCATION_ICON_SIZE,
                false
            )
        }
        marker = googleMap.addMarker(
            MarkerOptions()
                .position(centerOfPolygon)
                .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
        )
    }

    private fun getLatLongOfPolygon(points: List<LatLng>): LatLng {
        val center = doubleArrayOf(0.0, 0.0)
        for (i in points.indices) {
            center[0] += points[i].latitude
            center[1] += points[i].longitude
        }
        val totalPoints = points.size
        center[0] = center[0] / totalPoints
        center[1] = center[1] / totalPoints
        return LatLng(center[0], center[1])
    }

    private fun Int.toBitmap(context: Context, color:Int?=null): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, this) ?: return null
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bm = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        color?.let { DrawableCompat.setTint(drawable, color) }
        val canvas = Canvas(bm)
        drawable.draw(canvas)
        return bm
    }



}