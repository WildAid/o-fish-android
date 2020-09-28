package org.wildaid.ofish.ui.base

import android.app.Application
import android.net.Uri
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report

abstract class BaseReportViewModel(
    protected val repository: Repository,
    protected val app: Application
) : AndroidViewModel(app) {

    protected lateinit var currentReportPhotos: MutableList<PhotoItem>
    protected lateinit var currentReport: Report

    @CallSuper
    open fun initViewModel(report: Report, currentReportPhotos: MutableList<PhotoItem>) {
        this.currentReport = report
        this.currentReportPhotos = currentReportPhotos
    }

    protected fun createPhotoItem(imageUri: Uri): PhotoItem {
        return PhotoItem(
            Photo().apply {
                referencingReportID = currentReport._id.toString()
            },
            imageUri
        )
    }
}