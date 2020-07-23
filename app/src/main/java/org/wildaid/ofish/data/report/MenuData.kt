package org.wildaid.ofish.data.report

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import org.bson.types.ObjectId

@RealmClass
open class MenuData : RealmModel {
    @PrimaryKey
    var _id: ObjectId = ObjectId.get()
    @Required
    var countryPickerPriorityList: RealmList<String> = RealmList()
    @Required
    var ports: RealmList<String> = RealmList()
    @Required
    var fisheries: RealmList<String> = RealmList()
    @Required
    var species: RealmList<String> = RealmList()
    @Required
    var emsTypes: RealmList<String> = RealmList()
    @Required
    var activities: RealmList<String> = RealmList()
    @Required
    var gear: RealmList<String> = RealmList()
    @Required
    var violationCodes: RealmList<String> = RealmList()
    @Required
    var violationDescriptions: RealmList<String> = RealmList()
}