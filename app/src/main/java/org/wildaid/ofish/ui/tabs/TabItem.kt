package org.wildaid.ofish.ui.tabs

data class TabItem(
    val position: Int,
    val title: String,
    var wasVisited: Boolean = false,
    var isFormValid: Boolean = false
)