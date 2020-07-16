package org.wildaid.ofish.data.report

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.bson.types.ObjectId

@RealmClass
open class MenuData : RealmModel {
    @PrimaryKey
    var _id: ObjectId = ObjectId.get()
    var countryPickerPriorityList: RealmList<String> = RealmList()
    var ports: RealmList<String> = RealmList()
    var fisheries: RealmList<String> = RealmList()
    var species: RealmList<String> = RealmList()
    var emsTypes: RealmList<String> = RealmList()
    var activities: RealmList<String> = RealmList()
    var gear: RealmList<String> = RealmList()
    var violationCodes: RealmList<String> = RealmList()
    var violationDescriptions: RealmList<String> = RealmList()
}