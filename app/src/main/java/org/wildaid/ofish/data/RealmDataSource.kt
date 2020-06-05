package org.wildaid.ofish.data

import android.util.Log
import io.realm.*
import io.realm.kotlin.where
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.ObjectServerError
import io.realm.mongodb.sync.SyncConfiguration
import org.bson.types.ObjectId
import org.wildaid.ofish.BuildConfig
import org.wildaid.ofish.data.report.*

const val ON_DUTY = "On Duty"
const val OFF_DUTY = "Off Duty"
const val FIELD_ID = "_id"
const val FIELD_PERMIT_NUMBER = "permitNumber"
const val DATE = "date"
const val LAST_DELIVERY_DATE = "lastDelivery.date"
const val VESSEL_PERMIT_NUMBER = "vessel.permitNumber"

private const val TAG = "Realm Setup"

class RealmDataSource {
    private lateinit var realm: Realm
    private val realmApp: App

    init {
        Log.w("DataSource", "Created new instance of Realm data source")
        if (BuildConfig.DEBUG) {
            RealmLog.setLevel(LogLevel.ALL)
        }
        if (BuildConfig.REALM_APP_ID.isBlank()) {
            Log.e(
                TAG, "You need to specify properties realm_app_id in realm.properties"
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

    fun registerUser(
        userName: String,
        password: String,
        loginSuccess: () -> Unit,
        loginError: (Throwable?) -> Unit
    ) {
        realmApp.emailPasswordAuth.registerUserAsync(userName, password) {
            if (it.isSuccess) {
                loginSuccess.invoke()
            } else {
                loginError.invoke(it.error)
            }
        }
    }

    fun login(
        userName: String,
        password: String,
        loginSuccess: (io.realm.mongodb.User) -> Unit,
        loginError: (ObjectServerError?) -> Unit
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

    fun saveOnDutyChange(user: io.realm.mongodb.User, onDuty: Boolean) {
        realm.executeTransaction {
            realm.copyToRealm(DutyChange().apply {
                this.user = User().apply {
                    name = Name().apply {
                        first = user.firstName.orEmpty()
                        last = user.lastName.orEmpty()
                    }
                    email = user.email.orEmpty()
                }
                status = if (onDuty) ON_DUTY else OFF_DUTY
            })
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

    fun getCurrentUser(): io.realm.mongodb.User? {
        return realmApp.currentUser()
    }

    fun findAllReports(sort: Sort): List<Report> {
        return realm.where<Report>().sort(DATE, sort).findAll()
    }

    fun findReport(reportId: ObjectId): Report? {
        return realm.where<Report>().equalTo(FIELD_ID, reportId).findFirst()
    }

    fun findReportsForBoat(boatPermitNumber: String): List<Report> {
        return realm.where<Report>()
            .equalTo(VESSEL_PERMIT_NUMBER, boatPermitNumber)
            .sort(DATE, Sort.DESCENDING)
            .findAll()
            .toList()
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

    fun findBoat(boatPermitNumber: String): Boat? {
        return realm.where<Boat>()
            .equalTo(FIELD_PERMIT_NUMBER, boatPermitNumber)
            .sort(LAST_DELIVERY_DATE, Sort.DESCENDING)
            .findFirst()
    }

    private fun instantiateRealm(user: io.realm.mongodb.User) {
        if (BuildConfig.REALM_PARTITION.isBlank()) {
            Log.e(
                TAG,
                "You need to specify property realm_partition in realm.properties for setting up Sync Configuration"
            )
        }
        val configuration = SyncConfiguration
            .Builder(user, BuildConfig.REALM_PARTITION)
            .build()
        realm = Realm.getInstance(configuration)
    }
}