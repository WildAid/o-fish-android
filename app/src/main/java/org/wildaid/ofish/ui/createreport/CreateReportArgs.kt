package org.wildaid.ofish.ui.createreport

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CreateReportBundle(val prefillVessel: PrefillVessel, val prefillCrew: PrefillCrew?) :
    Parcelable

@Parcelize
data class PrefillVessel(
    val vesselName: String,
    val vesselNumber: String,
    val flagState: String,
    val homePort: String,
    val attachmentsPhotosId: List<String>
) : Parcelable

@Parcelize
data class PrefillCrew(
    // first - license
    // second - name
    val captain: Pair<String, String>,
    val crew: List<Pair<String, String>>
) : Parcelable