package org.wildaid.ofish.ui.catches

import org.wildaid.ofish.data.report.Catch
import org.wildaid.ofish.ui.base.AttachmentItem

data class CatchItem(
    var catch: Catch,
    var title: String,
    var inEditMode: Boolean,
    var attachmentItem: AttachmentItem
)