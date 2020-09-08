package org.wildaid.ofish.ui.risk

import org.wildaid.ofish.data.report.SafetyLevel
import org.wildaid.ofish.ui.base.AttachmentItem

data class RiskItem (
    val safetyLevel: SafetyLevel,
    val attachments: AttachmentItem
) {
    fun setLevel(level: String) {
        safetyLevel.level = level
    }
}