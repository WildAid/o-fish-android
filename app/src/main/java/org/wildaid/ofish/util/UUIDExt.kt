package org.wildaid.ofish.util

import java.util.*

fun UUID.upperCase(): String {
    return this.toString().toUpperCase(Locale.getDefault())
}