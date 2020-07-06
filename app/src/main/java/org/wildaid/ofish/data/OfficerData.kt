package org.wildaid.ofish.data

data class OfficerData(
    val email: String,
    val firstName: String,
    val lastName: String,
    val agency: String,
    var pictureUrl: String? = null
)

data class DutyStatus(
    var dutyStatus: Boolean,
    var dutyStartTime: Long,
    var onDutyEndTime: Long
)