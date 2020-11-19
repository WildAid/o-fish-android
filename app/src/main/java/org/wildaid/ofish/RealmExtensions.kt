package org.wildaid.ofish

import io.realm.*
import io.realm.internal.RealmObjectProxy
import io.realm.kotlin.isLoaded
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive

// FIXME: remove this file once we update to Realm 10.1.0

//------------------------------------------------------------------------------------------
// REALM OBJECT
//------------------------------------------------------------------------------------------

fun <T : RealmModel> T?.toFlow(): Flow<T?> {
    // Return flow with object or null flow if this function is called on null
    return this?.let { obj ->
        if (obj is RealmObjectProxy) {
            val proxy = obj as RealmObjectProxy

            @Suppress("INACCESSIBLE_TYPE")
            when (val realm = proxy.`realmGet$proxyState`().`realm$realm`) {
                is Realm -> from<T>(realm, obj)
                else -> throw UnsupportedOperationException("${realm.javaClass} is not supported as a candidate for 'toFlow'. Only subclasses of RealmModel/RealmObject can be used.")
            }
        } else {
            // Return a one-time emission in case the object is unmanaged
            return flowOf(this)
        }
    } ?: flowOf(null)
}

private fun <T : RealmModel> from(realm: Realm, realmObject: T): Flow<T> {
    // Return "as is" if frozen, there will be no listening for changes
    if (realm.isFrozen) {
        return flowOf(realmObject)
    }

    val config = realm.configuration

    return callbackFlow<T> {
        // Check if the Realm is closed (instead of using isValid - findFirstAsync always return "invalid object" right away, which would render this logic useless
        if (realm.isClosed) {
            awaitClose {}

            return@callbackFlow
        }

        // Get instance to ensure the Realm is open for as long as we are listening
        val flowRealm = Realm.getInstance(config)
        val listener = RealmChangeListener<T> { listenerObj ->
            if (isActive) {
                offer(RealmObject.freeze(listenerObj) as T)
            }
        }

        RealmObject.addChangeListener(realmObject, listener)

        // Emit current value
        if (realmObject.isLoaded()) {
            offer(RealmObject.freeze(realmObject))
        }

        awaitClose {
            // Remove listener and cleanup
            if (!flowRealm.isClosed) {
                RealmObject.removeChangeListener(realmObject, listener)
                flowRealm.close()
            }
        }
    }
}

//------------------------------------------------------------------------------------------
// REALM LIST
//------------------------------------------------------------------------------------------

fun <T> RealmList<T>.toFlow(): Flow<RealmList<T>> {
    @Suppress("INACCESSIBLE_TYPE")
    return when (val realmInstance = realm) {
        is Realm -> from(realmInstance, this)
        else -> throw IllegalStateException("Wrong type of Realm.")
    }
}

private fun <T> from(realm: Realm, realmList: RealmList<T>): Flow<RealmList<T>> {
    // Return "as is" if frozen, there will be no listening for changes
    if (realm.isFrozen) {
        return flowOf(realmList)
    }

    val config = realm.configuration

    return callbackFlow {
        // Do nothing if the results are invalid
        if (!realmList.isValid) {
            awaitClose {}

            return@callbackFlow
        }

        // Get instance to ensure the Realm is open for as long as we are listening
        val flowRealm = Realm.getInstance(config)
        val listener = RealmChangeListener<RealmList<T>> { listenerResults ->
            if (isActive) {
                offer(listenerResults.freeze())
            }
        }

        realmList.addChangeListener(listener)

        // Emit current value
        offer(realmList.freeze())

        awaitClose {
            // Remove listener and cleanup
            if (!flowRealm.isClosed) {
                realmList.removeChangeListener(listener)
                flowRealm.close()
            }
        }
    }
}

//------------------------------------------------------------------------------------------
// REALM RESULTS
//------------------------------------------------------------------------------------------

fun <T : RealmModel> RealmResults<T>.toFlow(): Flow<RealmResults<T>> {
    @Suppress("INACCESSIBLE_TYPE")
    return when (val realmInstance = realm) {
        is Realm -> from(realmInstance, this)
        else -> throw IllegalStateException("Wrong type of Realm.")
    }
}

fun <T> from(realm: Realm, results: RealmResults<T>): Flow<RealmResults<T>> {
    // Return "as is" if frozen, there will be no listening for changes
    if (realm.isFrozen) {
        return flowOf(results)
    }

    val config = realm.configuration

    return callbackFlow {
        // Do nothing if the results are invalid
        if (!results.isValid) {
            awaitClose {}

            return@callbackFlow
        }

        // Get instance to ensure the Realm is open for as long as we are listening
        val flowRealm = Realm.getInstance(config)
        val listener = RealmChangeListener<RealmResults<T>> { listenerResults ->
            if (isActive) {
                offer(listenerResults.freeze())
            }
        }

        results.addChangeListener(listener)

        // Emit current value
        offer(results.freeze())

        awaitClose {
            // Remove listener and cleanup
            if (!flowRealm.isClosed) {
                results.removeChangeListener(listener)
                flowRealm.close()
            }
        }
    }
}
