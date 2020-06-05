// Please note : @LinkingObjects and default values are not represented in the schema and thus will not be part of the generated models
package org.wildaid.ofish.data.report

import io.realm.RealmObject
import io.realm.RealmList
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass(embedded = true)
open class AnnotatedNote : RealmObject() {
    var note: String = ""

    @Required
    var photoIDs: RealmList<String> = RealmList()
}
