package org.wildaid.ofish.ui.reportdetail

import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.PhotoItem

data class ReportItem(
    val report: Report,
    val lastDeliveryPhotos: List<PhotoItem>,
    val activityPhotos: List<PhotoItem>
)