package org.wildaid.ofish.data

import android.net.Uri
import io.realm.Sort
import io.realm.mongodb.AppException
import io.realm.mongodb.User
import org.bson.types.ObjectId
import org.wildaid.ofish.data.report.DutyChange
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report
import java.util.*

class RepositoryImpl(
    private val realmDataSource: RealmDataSource,
    private val localDataSource: LocalDataSource,
    private val androidDataSource: AndroidDataSource
) : Repository {

    override fun login(
        userName: String,
        password: String,
        loginSuccess: (User) -> Unit,
        loginError: (AppException?) -> Unit
    ) =
        realmDataSource.login(userName, password, loginSuccess, loginError)

    override fun logOut(logoutSuccess: () -> Unit, logoutError: (Throwable?) -> Unit) {
        realmDataSource.logOut(logoutSuccess, logoutError)
    }

    override fun restoreLoggedUser() = realmDataSource.restoreLoggedUser()

    override fun saveOnDutyChange(onDuty: Boolean, date: Date) {
        realmDataSource.saveOnDutyChange(onDuty, date)
    }

    override fun saveReport(
        report: Report,
        reportPhotos: List<Pair<Photo, Uri?>>,
        listener: OnSaveListener
    ) {
        // Remove empty items before save
        report.vessel?.ems?.removeAll {
            it.emsType.isBlank() && it.registryNumber.isBlank()
        }
        report.crew.removeAll {
            it.name.isBlank()
        }

        report.inspection?.actualCatch?.removeAll {
            it.fish.isBlank()
        }
        report.inspection?.summary?.violations?.removeAll {
            it.offence == null || it.offence?.code?.isBlank() == true
        }
        report.notes.removeAll {
            it.note.isBlank()
        }

        val delivery = report.vessel?.lastDelivery
        val deliveryIsEmpty =
            delivery?.business.isNullOrBlank() && report.vessel?.lastDelivery?.location.isNullOrBlank()
        if (deliveryIsEmpty) {
            report.vessel?.lastDelivery = null
        }

        realmDataSource.saveReportWithTransaction(
            report, listener,
            object : Iterator<Photo> {
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
            }
        )
    }


    override fun getCurrentOfficer() = realmDataSource.getCurrentOfficer()

    override fun isLoggedIn() = realmDataSource.isLoggedIn()

    override fun findReportsGroupedByVessel(sort: Sort) =
        realmDataSource.findReportsGroupedByVesselNameAndPermitNumber(sort)

    override fun findAllReports(sort: Sort) = realmDataSource.findAllReports(sort)

    override fun findReport(reportId: ObjectId) = realmDataSource.findReport(reportId)

    override fun findReportsForBoat(boatPermitNumber: String, vesselName: String) =
        realmDataSource.findReportsForBoat(boatPermitNumber, vesselName)

    override fun getAmountOfDrafts(): Int =
        realmDataSource.getAmountOfDrafts()

    override fun findReportsForCurrentDuty(): List<Report> {
        return realmDataSource.findReportsForCurrentDuty()
    }

    override fun findAllBoats() = realmDataSource.findAllBoats()

    override fun findBoat(boatPermitNumber: String, vesselName: String) =
        realmDataSource.findBoat(boatPermitNumber, vesselName)

    override fun getMenuData() = realmDataSource.getMenuData()

    override fun getPhotosWithIds(ids: List<String>) = realmDataSource.getPhotosWithIds(ids)

    override fun getPhotoById(id: String) = realmDataSource.getPhotoById(id)

    override fun getOffences(): List<OffenceData> {
        val offences = mutableListOf<OffenceData>()
        getMenuData()?.let {
            if (it.violationCodes.size == it.violationDescriptions.size) {
                it.violationCodes.forEachIndexed { index, code ->
                    offences.add(OffenceData(code, it.violationDescriptions[index] ?: ""))
                }
            }
        }

        return offences
    }

    override fun getBusinessAndLocation(): List<Pair<String, String>> {
        return realmDataSource.getAllDeliveryBusiness().map {
            Pair(it.business, it.location)
        }
    }

    override fun getFlagStates(): List<String> {
        val states = localDataSource.getFlagStates().toMutableList()
        val prior = getMenuData()?.countryPickerPriorityList?.map {
            val locale = Locale("", it)
            locale.displayCountry
        }?.toList()
        if (prior != null) {
            states.removeAll(prior)
            states.addAll(0, prior)
        }
        return states
    }

    override fun getRecentOnDutyChange(): DutyChange? = realmDataSource.getRecentOnDutyChange()

    override fun getRecentStartCurrentDuty(): DutyChange? =
        realmDataSource.getRecentStartCurrentDuty()

    override fun updateStartDateForCurrentDuty(date: Date) =
        realmDataSource.updateStartDateForCurrentDuty(date)

    override fun updateCurrentOfficerPhoto(uri: Uri) {
        val pictureId = getCurrentOfficer().pictureId
        val photo = Photo().apply {
            _id = ObjectId(pictureId)
            picture = androidDataSource.readCompressedBytes(uri)
            thumbNail = androidDataSource.generateImagePreview(uri)
        }
        realmDataSource.savePhoto(photo)
    }

    override fun getCurrentOfficerPhoto(): Photo? {
        val pictureId = getCurrentOfficer().pictureId
        return getPhotoById(pictureId)
    }
}