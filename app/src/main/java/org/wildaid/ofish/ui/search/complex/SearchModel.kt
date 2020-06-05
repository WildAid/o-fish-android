package org.wildaid.ofish.ui.search.complex

import androidx.annotation.StringRes
import org.wildaid.ofish.data.report.Boat
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Report
import java.io.Serializable

open class SearchModel: Serializable
class BusinessSearchModel(val value: Pair<String, String>) : SearchModel()
class ViolationSearchModel(val value: String) : SearchModel()
class RecordSearchModel(val vessel: Boat, val reports: List<Report>) : SearchModel()
class CrewSearchModel(val value: CrewMember, val isCaptain: Boolean) : SearchModel()
class AddSearchModel(@StringRes val titleId: Int) : SearchModel()
class TextViewSearchModel(@StringRes val titleId: Int) : SearchModel()