package org.wildaid.ofish.data

import android.net.Uri
import io.realm.Sort
import io.realm.mongodb.AppException
import io.realm.mongodb.User
import org.bson.types.ObjectId
import org.wildaid.ofish.data.report.*
import java.util.*

interface Repository {

    fun login(
        userName: String,
        password: String,
        loginSuccess: (User) -> Unit,
        loginError: (AppException?) -> Unit
    )

    fun logOut(logoutSuccess: () -> Unit, logoutError: (Throwable?) -> Unit)

    fun restoreLoggedUser(): User?

    fun saveOnDutyChange(onDuty: Boolean, date: Date)

    fun saveReport(
        report: Report,
        isDraft: Boolean?,
        reportPhotos: List<Pair<Photo, Uri?>>,
        listener: OnSaveListener
    )

    fun deleteDraft(draft: Report)

    fun getCurrentOfficer(): OfficerData

    fun isLoggedIn(): Boolean

    fun findReportsGroupedByVessel(sort: Sort = Sort.DESCENDING): List<Report>

    fun findDraftsGroupedByOfficerNameAndEmail(sort: Sort = Sort.DESCENDING): List<Report>

    fun findAllReports(sort: Sort = Sort.DESCENDING): List<Report>

    fun findReportsForCurrentDuty(): List<Report>

    fun findReport(reportId: ObjectId): Report?

    fun findDraft(draftId: ObjectId): Report?

    fun findReportsForBoat(boatPermitNumber: String, vesselName: String): List<Report>

    fun getAmountOfDraftsByEmail(): Int

    fun getAmountOfDraftsForCurrentDuty(): Int

    fun findAllBoats(): List<Boat>

    fun getMenuData(): MenuData?

    fun findBoat(boatPermitNumber: String, vesselName: String): Boat?

    fun getPhotosWithIds(ids: List<String>): List<Photo>

    fun getPhotoById(id: String): Photo?

    fun getOffences(): List<OffenceData>

    fun getBusinessAndLocation(): List<Pair<String, String>>

    fun getFlagStates(): List<String>

    fun getRecentOnDutyChange(): DutyChange?

    fun getRecentStartCurrentDuty(): DutyChange?

    fun updateStartDateForCurrentDuty(date: Date)

    fun updateCurrentOfficerPhoto(uri: Uri)

    fun getCurrentOfficerPhoto(): Photo?

    fun saveDarkModeState(darkModeEnabled: Boolean)

    fun getDarkModeState(): DarkMode?

    fun initDarkModeState()
}