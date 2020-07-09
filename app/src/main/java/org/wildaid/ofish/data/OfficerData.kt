package org.wildaid.ofish.data

data class OfficerData(
    val email: String,
    val firstName: String,
    val lastName: String,
    val agency: String,
    val pictureId: String
)

data class DutyStatus(
    var dutyStatus: Boolean,
    var dutyStartTime: Long,
    var onDutyEndTime: Long
)