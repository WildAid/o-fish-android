package org.wildaid.ofish.ui.activity

import org.wildaid.ofish.data.report.Activity
import org.wildaid.ofish.data.report.Fishery
import org.wildaid.ofish.data.report.GearType
import org.wildaid.ofish.ui.base.AttachmentItem

data class ActivityItem(
    val activity: Activity,
    val attachments: AttachmentItem
)

data class FisheryItem(
    val fishery: Fishery,
    val attachments: AttachmentItem
)

data class GearItem(
    val gear: GearType,
    val attachments: AttachmentItem
)