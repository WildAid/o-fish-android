package org.wildaid.ofish.data

import android.net.Uri
import io.realm.Sort
import io.realm.mongodb.AppException
import io.realm.mongodb.User
import org.bson.types.ObjectId
import org.wildaid.ofish.data.report.Boat
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report

interface Repository {
    fun login(
        userName: String, password: String,
        loginSuccess: (User) -> Unit, loginError: (AppException?) -> Unit
    )

    fun logOut(logoutSuccess: () -> Unit, logoutError: (Throwable?) -> Unit)

    fun restoreLoggedUser(): User?

    fun saveOnDutyChange(onDuty: Boolean)

    fun saveReport(report: Report, reportPhotos: List<Pair<Photo, Uri?>>, listener: OnSaveListener)

    fun getCurrentOfficer(): OfficerData

    fun isLoggedIn(): Boolean

    fun findReportsGroupedByVessel(sort: Sort = Sort.DESCENDING): List<Report>

    fun findAllReports(sort: Sort = Sort.DESCENDING): List<Report>

    fun findReportsForCurrentDuty(): List<Report>

    fun findReport(reportId: ObjectId): Report?

    fun findReportsForBoat(boatPermitNumber: String): List<Report>

    fun findAllBoats(): List<Boat>

    fun findBoat(boatPermitNumber: String): Boat?

    fun getPhotosWithIds(ids: List<String>): List<Photo>

    fun getPhotoById(id: String): Photo?

    fun getOffences(): List<OffenceData>

    fun getBusinessAndLocation(): List<Pair<String, String>>

    fun getFlagStates(agency: String?): List<String>

    fun getOnDutyStatus() : DutyStatus
}