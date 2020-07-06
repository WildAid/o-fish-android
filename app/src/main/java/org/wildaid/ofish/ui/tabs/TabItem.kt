package org.wildaid.ofish.ui.tabs

data class TabItem(
    val position: Int,
    val title: String,
    var status: TabStatus = TabStatus.NOT_VISITED
)

enum class TabStatus {
    VISITED,
    SKIPPED,
    NOT_VISITED
}