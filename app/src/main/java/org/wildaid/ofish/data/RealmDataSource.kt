package org.wildaid.ofish.data

import android.content.Context
import android.util.Log
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.AppException
import io.realm.mongodb.Credentials
import io.realm.mongodb.sync.SyncConfiguration
import org.bson.Document
import org.bson.types.ObjectId
import org.wildaid.ofish.BuildConfig
import org.wildaid.ofish.data.report.*
import java.util.*

const val ON_DUTY = "On Duty"
const val OFF_DUTY = "Off Duty"
const val FIELD_ID = "_id"
const val FIELD_PERMIT_NUMBER = "permitNumber"
const val FIELD_NAME = "name"
const val FIELD_STATUS = "status"
const val DATE = "date"
const val BUSINESS = "business"
const val LOCATION = "location"
const val DRAFT = "draft"
const val LAST_DELIVERY_DATE = "lastDelivery.date"
const val VESSEL_PERMIT_NUMBER = "vessel.permitNumber"
const val VESSEL_NAME = "vessel.name"
const val USER_EMAIL = "user.email"

private const val TAG = "Realm Setup"

class RealmDataSource(context: Context) {
    private val realmApp: App

    private lateinit var realm: Realm
    private lateinit var currentRealmUser: io.realm.mongodb.User
    private lateinit var currentOfficer: OfficerData

    init {
        Log.w("DataSource", "Created new instance of Realm data source")
        Realm.init(context)
        if (BuildConfig.DEBUG) {
            RealmLog.setLevel(LogLevel.ALL)
        }
        if (BuildConfig.REALM_APP_ID.isBlank()) {
            Log.e(
                TAG, "You need to specify properties realm_app_id in local.properties"
            )
        }

        val appConfigBuilder = AppConfiguration.Builder(BuildConfig.REALM_APP_ID)
            .appName(BuildConfig.VERSION_NAME)
            .appVersion(BuildConfig.VERSION_CODE.toString())

        if (!BuildConfig.REALM_URL.isBlank()) {
            appConfigBuilder.baseUrl(BuildConfig.REALM_URL)
        }

        realmApp = App(appConfigBuilder.build())
    }

    fun login(
        userName: String,
        password: String,
        loginSuccess: (io.realm.mongodb.User) -> Unit,
        loginError: (AppException?) -> Unit
    ) {

        val appCredentials = Credentials.emailPassword(userName, password)
        realmApp.loginAsync(appCredentials) {
            if (it.error != null) {
                loginError.invoke(it.error)
            } else {
                val user = it.get()
                instantiateRealm(user)
                loginSuccess.invoke(user)
            }
        }
    }

    fun logOut(
        logoutSuccess: () -> Unit,
        logoutError: (Throwable?) -> Unit
    ) {
        realmApp.currentUser()?.logOutAsync {
            if (it.error != null) {
                logoutError.invoke(it.error.exception)
            } else {
                logoutSuccess.invoke()
            }
        }
    }

    fun restoreLoggedUser(): io.realm.mongodb.User? {
        return realmApp.currentUser()?.also {
            instantiateRealm(it)
        }
    }

    fun saveOnDutyChange(onDuty: Boolean, date: Date) {
        realm.executeTransaction {
            realm.copyToRealm(DutyChange().apply {
                this.user = User().apply {
                    name = Name().apply {
                        first = currentOfficer.firstName
                        last = currentOfficer.lastName
                    }
                    email = currentOfficer.email
                }
                this.date = date
                status = if (onDuty) ON_DUTY else OFF_DUTY
            })
        }
    }

    fun getRecentOnDutyChange(): DutyChange? {
        return realm.where<DutyChange>()
            .equalTo(USER_EMAIL, currentOfficer.email)
            .sort(DATE, Sort.DESCENDING)
            .findFirst()
    }

    fun getRecentStartCurrentDuty(): DutyChange? {
        return realm.where<DutyChange>()
            .equalTo(USER_EMAIL, currentOfficer.email)
            .equalTo(FIELD_STATUS, ON_DUTY)
            .sort(DATE, Sort.DESCENDING)
            .findFirst()
    }

    fun updateStartDateForCurrentDuty(date: Date) {
        val onDuty = getRecentStartCurrentDuty()!!
        realm.executeTransaction {
            onDuty.date = date
        }
    }

    fun saveReportWithTransaction(
        report: Report,
        listener: OnSaveListener,
        photoIterator: Iterator<Photo>
    ) {
        realm.executeTransactionAsync(
            {
                while (photoIterator.hasNext()) {
                    it.insert(photoIterator.next())
                }
                it.insert(report)
            },
            { listener.onSuccess() },
            { listener.onError(it) }
        )
    }

    fun getCurrentOfficer() = currentOfficer

    fun isLoggedIn() = realmApp.currentUser() != null

    fun findReportsGroupedByVesselNameAndPermitNumber(sort: Sort): List<Report> {
        return realm.where<Report>()
            .isNotEmpty(VESSEL_NAME)
            .sort(DATE, sort)
            .findAll()
    }

    fun findAllReports(sort: Sort): List<Report> {
        return realm.where<Report>()
            .sort(DATE, sort)
            .findAll()
    }

    fun findReport(reportId: ObjectId): Report? {
        return realm.where<Report>().equalTo(FIELD_ID, reportId).findFirst()
    }

    fun findReportsForBoat(boatPermitNumber: String, vesselName: String): List<Report> {
        return realm.where<Report>()
            .equalTo(VESSEL_PERMIT_NUMBER, boatPermitNumber)
            .and()
            .equalTo(VESSEL_NAME, vesselName)
            .sort(DATE, Sort.DESCENDING)
            .findAll()
            .toList()
    }

    fun getAmountOfDrafts(): Int {
        return realm.where<Report>()
            .equalTo(DRAFT, true)
            .findAll()
            .count()
    }

    fun findReportsForCurrentDuty(): List<Report> {
        val dutyChange = getRecentStartCurrentDuty()
        dutyChange?.let {
            return realm.where<Report>()
                .sort(DATE, Sort.DESCENDING)
                .greaterThan(DATE, it.date)
                .findAll()
                .toList()
        }
        return emptyList()
    }

    fun findAllBoats(): List<Boat> {
        return realm.where<Boat>().findAll()
    }

    fun getPhotosWithIds(ids: List<String>): List<Photo> {
        if (ids.isNullOrEmpty()) return emptyList()

        val query = realm.where<Photo>().equalTo(FIELD_ID, ObjectId(ids.first()))
        ids.forEach { query.or().equalTo(FIELD_ID, ObjectId(it)) }
        return query.findAll()
    }

    fun getPhotoById(id: String): Photo? {
        if (id.isBlank()) return null
        return realm.where<Photo>().equalTo(FIELD_ID, ObjectId(id)).findFirst()
    }

    fun findBoat(boatPermitNumber: String, vesselName: String): Boat? {
        return realm.where<Boat>()
            .equalTo(FIELD_PERMIT_NUMBER, boatPermitNumber)
            .and()
            .equalTo(FIELD_NAME, vesselName)
            .sort(LAST_DELIVERY_DATE, Sort.DESCENDING)
            .findFirst()
    }

    fun getMenuData(): MenuData? {
        return realm.where<MenuData>().findFirst()
    }

    private fun instantiateRealm(user: io.realm.mongodb.User) {
        currentRealmUser = user

        val userData = user.customData
        val agencyName = (userData["agency"] as Document?)?.get("name") as String? ?: ""
        val officerEmail = userData["email"] as String? ?: ""
        val officerFirstName = (userData["name"] as Document?)?.get("first") as String? ?: ""
        val officerLastName = (userData["name"] as Document?)?.get("last") as String? ?: ""
        val profilePicId = userData["profilePic"] as String? ?: ""

        currentOfficer =
            OfficerData(officerEmail, officerFirstName, officerLastName, agencyName, profilePicId)

        val configuration = SyncConfiguration
            .Builder(user, agencyName)
            .build()
        realm = Realm.getInstance(configuration)
    }

    fun getAllDeliveryBusiness(): List<Delivery> {
        return realm.where<Delivery>()
            .isNotEmpty(BUSINESS)
            .sort(BUSINESS, Sort.ASCENDING)
            .distinct(BUSINESS, LOCATION)
            .findAll()
            .toList()
    }

    fun savePhoto(photo: Photo) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(photo)
        }
    }
}