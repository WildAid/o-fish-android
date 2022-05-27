package org.wildaid.ofish.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import org.wildaid.ofish.data.MpaData
import org.wildaid.ofish.data.report.MPA

open class MpaViewModel : ViewModel() {

    fun fetchMpaData(mpaDataList: Flow<List<MPA?>>): MutableList<MpaData> {
        val mpaList: MutableList<MpaData> = mutableListOf()
        mpaDataList.map {
            it.map { singleMpa ->
                val locationList: MutableList<LatLng> = mutableListOf()
                singleMpa?.coordinates?.map { coordinates ->
                    locationList.add(
                        LatLng(
                            coordinates.lat,
                            coordinates.lon
                        )
                    )
                }
                mpaList.add(
                    MpaData(
                        id = singleMpa?._id.toString(),
                        name = singleMpa?.name ?: "",
                        agency = singleMpa?.agency ?: "",
                        country = singleMpa?.country ?: "",
                        description = singleMpa?.description ?: "",
                        info = singleMpa?.info ?: "",
                        text = singleMpa?.text ?: "",
                        hexColor = singleMpa?.hexColor ?: "",
                        coordinates = locationList
                    )
                )
            }
        }
            .launchIn(viewModelScope)
        return mpaList
    }
}