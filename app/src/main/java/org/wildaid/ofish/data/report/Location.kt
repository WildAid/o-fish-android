package org.wildaid.ofish.data.report

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Location:RealmObject() {
    var lat: Double = 0.0
    var lon: Double = 0.0
}