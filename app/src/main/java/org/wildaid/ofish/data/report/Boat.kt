// Please note : @LinkingObjects and default values are not represented in the schema and thus will not be part of the generated models
package org.wildaid.ofish.data.report

import io.realm.RealmObject
import io.realm.RealmList
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass(embedded = true)
open class Boat : RealmObject() {
    var name: String = ""
    var homePort: String = ""
    var nationality: String = ""
    var permitNumber: String = ""
    var ems: RealmList<EMS> = RealmList()
    var lastDelivery: Delivery? = Delivery()
    var attachments: Attachments? = Attachments()

    private fun getCountryName(countryCode: String): String {
        val locale = Locale("", countryCode)
        return locale.displayCountry
    }

    fun nationality(): String {
        return if (nationality.isBlank()) "" else getCountryName(nationality)
    }
}