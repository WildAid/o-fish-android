package org.wildaid.ofish.data.mpa

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.PolygonData
import org.wildaid.ofish.ui.polygon_info.PolygonBottomSheetFragment

const val POLYGON_CAPACITY = "#99"
const val POLYGON_STROKE_CAPACITY = "#40"
const val MAIN_COLOR_CAPACITY = 3
const val LOCATION_ICON_SIZE = 150

class MarineProtectionArea {
    var polygonBottomSheetFragment: PolygonBottomSheetFragment = PolygonBottomSheetFragment()

    private var galapagosPolyline: Polygon? = null
    private var testPolyline: Polygon? = null
    private lateinit var marker: Marker


    private var _onPolygonClick = MutableLiveData<Event<PolygonData>>()
    val onPolygonClick: LiveData<Event<PolygonData>>
        get() = _onPolygonClick


    private fun setColorWithCapacity(color: String, capacity: String): Int {
        val rgbColor = color.substring(MAIN_COLOR_CAPACITY)
        val colorWithCapacity = capacity.plus(rgbColor)
        return Color.parseColor(colorWithCapacity)
    }

    fun removeMarker(){
        marker.remove()
    }

    @SuppressLint("ResourceType")
    fun addMpa(context: Context?, googleMap: GoogleMap) {
        galapagosPolyline = googleMap.addPolygon(
            PolygonOptions()
                .clickable(true)
                .addAll(Mpa.GALAPAGOS.polygon)
        )
        galapagosPolyline?.tag = Mpa.GALAPAGOS
        galapagosPolyline?.fillColor =
            context?.resources?.getString(R.color.amber)
                ?.let { setColorWithCapacity(it, POLYGON_CAPACITY) }!!
        galapagosPolyline?.strokeColor = context.resources?.getString(R.color.amber)
            ?.let { setColorWithCapacity(it, POLYGON_STROKE_CAPACITY) }!!


        testPolyline = googleMap.addPolygon(
            PolygonOptions()
                .clickable(true)
                .addAll(Mpa.TEST.polygon)
        )
        testPolyline?.tag = Mpa.TEST
        testPolyline?.fillColor =
            context.resources?.getString(R.color.red)
                ?.let { setColorWithCapacity(it, POLYGON_CAPACITY) }!!

        testPolyline?.strokeColor = context.resources?.getString(R.color.red)
            ?.let { setColorWithCapacity(it, POLYGON_STROKE_CAPACITY) }!!

        googleMap.setOnPolygonClickListener {
            if (checkVisibilityOfPolygon()) {
                _onPolygonClick.value =
                    Event(getSelectedPolygonInformation(it.tag.toString()))
            }

        }

    }

    fun disablePolygons() {
        galapagosPolyline?.isVisible = false
        testPolyline?.isVisible = false
    }

    fun enablePolygons() {
        galapagosPolyline?.isVisible = true
        testPolyline?.isVisible = true
    }

    private fun checkVisibilityOfPolygon(): Boolean {
        return testPolyline?.isVisible == true && galapagosPolyline?.isVisible == true
    }

    private fun getSelectedPolygonInformation(polygonTag: String): PolygonData {
        val data = PolygonData()
        when (polygonTag) {
            Mpa.GALAPAGOS.toString() -> {
                data.polygonName = Mpa.GALAPAGOS.mpaName
                data.color = R.color.amber
            }
            Mpa.TEST.toString() -> {
                data.polygonName = Mpa.TEST.mpaName
                data.color = R.color.red
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

    private fun getSelectedPolygon(polygonName: String): List<LatLng> {
        lateinit var selectedPolygon: List<LatLng>
        when (polygonName) {
            Mpa.TEST.mpaName -> {
                selectedPolygon = Mpa.TEST.polygon
            }
            Mpa.GALAPAGOS.mpaName -> {
                selectedPolygon = Mpa.GALAPAGOS.polygon
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

    private fun Int.toBitmap(context: Context, @ColorRes tintColor: Int? = null): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, this) ?: return null
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bm = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        tintColor?.let { DrawableCompat.setTint(drawable, ContextCompat.getColor(context, it)) }
        val canvas = Canvas(bm)
        drawable.draw(canvas)
        return bm
    }

    enum class Mpa(val mpaName: String, var polygon: List<LatLng>) {
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
}