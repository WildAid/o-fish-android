package org.wildaid.ofish.data.description

import org.wildaid.ofish.ui.catches.CatchItem

class ReportDescriptionFields {
    var activityDescription = ""
    var fisheryDescription = ""
    var gearDescription = ""
    var catches = mutableListOf<CatchItem>()
}