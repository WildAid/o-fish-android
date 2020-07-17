package org.wildaid.ofish.data

import android.net.Uri
import io.realm.Sort
import io.realm.mongodb.AppException
import io.realm.mongodb.User
import org.bson.types.ObjectId
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report
import java.util.*

class RepositoryImpl(
    private val realmDataSource: RealmDataSource,
    private val localDataSource: LocalDataSource,
    private val androidDataSource: AndroidDataSource
) : Repository {

    override fun login(
        userName: String, password: String,
        loginSuccess: (User) -> Unit, loginError: (AppException?) -> Unit
    ) =
        realmDataSource.login(userName, password, loginSuccess, loginError)

    override fun logOut(logoutSuccess: () -> Unit, logoutError: (Throwable?) -> Unit) {
        realmDataSource.logOut(logoutSuccess, logoutError)
    }

    override fun restoreLoggedUser() = realmDataSource.restoreLoggedUser()

    override fun saveOnDutyChange(onDuty: Boolean) {
        realmDataSource.saveOnDutyChange(onDuty)
        androidDataSource.saveOnDutyStatus(onDuty)
        if (onDuty) {
            androidDataSource.setOnDutyStartTime(Date().time)
            androidDataSource.setOnDutyEndTime(0)
        } else {
            androidDataSource.setOnDutyEndTime(Date().time)
        }
    }

    override fun saveReport(
        report: Report,
        reportPhotos: List<Pair<Photo, Uri?>>,
        listener: OnSaveListener
    ) {
        realmDataSource.saveReportWithTransaction(report, listener, object : Iterator<Photo> {
            val originalIterator = reportPhotos.iterator()
            override fun hasNext() = originalIterator.hasNext()

            override fun next(): Photo {
                val pair = originalIterator.next()
                val imageUri = pair.second

                return pair.first.also {
                    if (imageUri != null) {
                        it.picture = androidDataSource.readCompressedBytes(imageUri)
                        it.thumbNail = androidDataSource.generateImagePreview(imageUri)
                    }
                }
            }
        })
    }

    override fun getCurrentOfficer() = realmDataSource.getCurrentOfficer()

    override fun isLoggedIn() = realmDataSource.isLoggedIn()

    override fun findReportsGroupedByVessel(sort: Sort) =
        realmDataSource.findReportsGroupedByVessel(sort)

    override fun findAllReports(sort: Sort) = realmDataSource.findAllReports(sort)

    override fun findReport(reportId: ObjectId) = realmDataSource.findReport(reportId)

    override fun findReportsForBoat(boatPermitNumber: String) =
        realmDataSource.findReportsForBoat(boatPermitNumber)

    override fun findReportsForCurrentDuty() : List<Report>{
        val currentDuty = getOnDutyStatus()
        return realmDataSource.findReportsFromDate(currentDuty.dutyStartTime)
    }

    override fun findAllBoats() = realmDataSource.findAllBoats()

    override fun findBoat(boatPermitNumber: String) = realmDataSource.findBoat(boatPermitNumber)

    override fun getPhotosWithIds(ids: List<String>) = realmDataSource.getPhotosWithIds(ids)

    override fun getPhotoById(id: String) = realmDataSource.getPhotoById(id)

    override fun getOffences() = localDataSource.getOffences()

    override fun getBusinessAndLocation() : List<Pair<String, String>>{
        return realmDataSource.getAllDeliveryBusiness().map {
            Pair(it.business, it.location)
        }
    }

    override fun getFlagStates(agency: String?): List<String> {
        val states = localDataSource.getFlagStates().toMutableList()
        val prior = listOf("Ecuador", "Panama") //todo extract from agency
        states.removeAll(prior)
        states.addAll(0, prior)
        return states
    }

    override fun getOnDutyStatus(): DutyStatus {
        return DutyStatus(
            androidDataSource.getOnDutyStatus(),
            androidDataSource.getOnDutyStartTime(),
            androidDataSource.getOnDtyEndTime()
        )
    }
}