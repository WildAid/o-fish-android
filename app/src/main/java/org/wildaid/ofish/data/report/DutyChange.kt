// Please note : @LinkingObjects and default values are not represented in the schema and thus will not be part of the generated models
package org.wildaid.ofish.data.report

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.bson.types.ObjectId
import java.util.Date

@RealmClass
open class DutyChange : RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId.get()
    var user: User? = User()
    var date: Date = Date()
    var status: String = ""
    var agency = "WildAid"
}
