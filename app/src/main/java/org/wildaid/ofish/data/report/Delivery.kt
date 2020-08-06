// Please note : @LinkingObjects and default values are not represented in the schema and thus will not be part of the generated models
package org.wildaid.ofish.data.report

import io.realm.RealmObject
import java.util.Date
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Delivery : RealmObject() {
    var date: Date = Date()
    var location: String = ""
    var business: String = ""
    var attachments: Attachments? = Attachments()
}