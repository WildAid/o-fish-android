package org.wildaid.ofish.ui.violation

import org.wildaid.ofish.data.report.Seizures
import org.wildaid.ofish.data.report.Violation
import org.wildaid.ofish.ui.base.AttachmentItem

data class ViolationItem(
    var violation: Violation,
    var title: String,
    val attachments: AttachmentItem,
    var inEditMode: Boolean = true
)

data class SeizureItem(
    var seizure: Seizures,
    val attachments: AttachmentItem
)