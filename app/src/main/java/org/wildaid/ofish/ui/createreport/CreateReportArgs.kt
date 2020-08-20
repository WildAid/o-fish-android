package org.wildaid.ofish.ui.createreport

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CreateReportBundle(val prefillVessel: PrefillVessel, val prefillCrew: PrefillCrew) :
    Parcelable

@Parcelize
data class PrefillVessel(
    val prefillVesselName: String,
    val prefillVesselNumber: String,
    val prefillFlagState: String,
    val prefillPort: String
) : Parcelable

@Parcelize
data class PrefillCrew(
    val prefillCaptain: Pair<String, String>?,
    val listOfCrewMembers: List<Pair<String, String>>
) : Parcelable