package org.wildaid.ofish.data.report

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.RealmField
import io.realm.annotations.Required
import org.bson.types.ObjectId

@RealmClass
open class MPA : RealmModel {
    @PrimaryKey
    var _id: ObjectId = ObjectId.get()
    @Required
    var name: String = ""
    @Required
    var agency: String = ""
    @Required
    var country: String = ""
    var description: String? = ""
    @Required
    var info: String = ""
    var text: String = ""
    @Required
    var hexColor: String = ""
    @RealmField(name = "coordinates")
    var coordinates: RealmList<Location> = RealmList()
}
