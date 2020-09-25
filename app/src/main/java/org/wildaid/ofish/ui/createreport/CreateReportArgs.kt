package org.wildaid.ofish.ui.createreport

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.bson.types.ObjectId

@Parcelize
data class CreateReportBundle(
    val reportDraftId: ObjectId? = null,
    val prefillVessel: PrefillVessel? = null,
    val prefillCrew: PrefillCrew? = null
) : Parcelable

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
    val captain: PrefillCrewMember,
    val crew: List<PrefillCrewMember>
) : Parcelable

@Parcelize
data class PrefillCrewMember(
    val name: String,
    val license: String,
    val photosIds: List<String>
):Parcelable