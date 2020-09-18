// Please note : @LinkingObjects and default values are not represented in the schema and thus will not be part of the generated models
package org.wildaid.ofish.data.report

import io.realm.RealmObject
import java.util.Date
import io.realm.RealmList
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import org.bson.types.ObjectId

@RealmClass
open class Report : RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId.get()
    var reportingOfficer: User? = User()
    var timestamp: Date = Date()
    @Required
    var location: RealmList<Double> = RealmList() // In order longitude, latitude
    var date: Date? = Date()
    var vessel: Boat? = Boat()
    var captain: CrewMember? = CrewMember()
    var crew: RealmList<CrewMember> = RealmList()
    var notes: RealmList<AnnotatedNote> = RealmList()
    var draft: Boolean? = false
    var inspection: Inspection? = Inspection()
}