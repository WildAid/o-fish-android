// Please note : @LinkingObjects and default values are not represented in the schema and thus will not be part of the generated models
package org.wildaid.ofish.data.report

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class SafetyLevel : RealmObject() {
    var level: String = ""
    var amberReason:String = ""
    var attachments: Attachments? = Attachments()
}
