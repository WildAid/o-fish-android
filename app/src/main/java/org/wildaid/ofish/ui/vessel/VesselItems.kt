package org.wildaid.ofish.ui.vessel

import org.wildaid.ofish.data.report.Boat
import org.wildaid.ofish.data.report.Delivery
import org.wildaid.ofish.data.report.EMS
import org.wildaid.ofish.ui.base.AttachmentItem

data class VesselItem(
    val vessel: Boat,
    var inEditMode: Boolean = true,
    val attachments: AttachmentItem
)

data class DeliveryItem(
    val lastDelivery: Delivery,
    var inEditMode: Boolean,
    val attachments: AttachmentItem
)

data class EMSItem(
    val ems: EMS,
    var inEditMode: Boolean,
    val attachments: AttachmentItem
)