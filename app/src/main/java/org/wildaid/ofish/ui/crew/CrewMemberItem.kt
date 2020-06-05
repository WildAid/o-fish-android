package org.wildaid.ofish.ui.crew

import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.ui.base.AttachmentItem

data class CrewMemberItem(
    var crewMember: CrewMember,
    var title: String,
    var attachments: AttachmentItem,
    var inEditMode: Boolean = true,
    var isRemovable: Boolean = true,
    var isCaptain: Boolean = false
) {
    fun nameOrTitle(): String {
        return crewMember.name.ifBlank { title }
    }
}