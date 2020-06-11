package org.wildaid.ofish.data

import android.net.Uri
import io.realm.Sort
import io.realm.mongodb.AppException
import io.realm.mongodb.User
import org.bson.types.ObjectId
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report

class RepositoryImpl(
    private val realmDataSource: RealmDataSource,
    private val localDataSource: LocalDataSource,
    private val androidDataSource: AndroidDataSource
) : Repository {

    override fun registerUser(userName: String, password: String,
                       loginSuccess: () -> Unit, loginError: (Throwable?) -> Unit) =
        realmDataSource.registerUser(userName, password, loginSuccess, loginError)

    override fun login(userName: String, password: String,
                       loginSuccess: (User) -> Unit, loginError: (AppException?) -> Unit) =
        realmDataSource.login(userName, password, loginSuccess, loginError)

    override fun logOut(logoutSuccess: () -> Unit, logoutError: (Throwable?) -> Unit) {
        realmDataSource.logOut(logoutSuccess, logoutError)
    }

    override fun restoreLoggedUser() = realmDataSource.restoreLoggedUser()

    override fun saveOnDutyChange(user: User, onDuty: Boolean) =
        realmDataSource.saveOnDutyChange(user, onDuty)

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

    override fun getCurrentUser() = realmDataSource.getCurrentUser()

    override fun findAllReports(sort: Sort) = realmDataSource.findAllReports(sort)

    override fun findReport(reportId: ObjectId) = realmDataSource.findReport(reportId)

    override fun findReportsForBoat(boatPermitNumber: String) = realmDataSource.findReportsForBoat(boatPermitNumber)

    override fun findAllBoats() = realmDataSource.findAllBoats()

    override fun findBoat(boatPermitNumber: String) = realmDataSource.findBoat(boatPermitNumber)

    override fun getPhotosWithIds(ids: List<String>) = realmDataSource.getPhotosWithIds(ids)

    override fun getViolations() = localDataSource.getViolations()

    override fun getBusinessAndLocation() = localDataSource.getBusiness()

    override fun getFlagStates(agency: String?): List<String> {
        val states = localDataSource.getFlagStates().toMutableList()
        val prior = listOf("Ecuador", "Panama") //todo extract from agency
        states.removeAll(prior)
        states.addAll(0, prior)
        return states
    }
}