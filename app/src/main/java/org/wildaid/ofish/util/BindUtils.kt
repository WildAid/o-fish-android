package org.wildaid.ofish.util

import android.net.Uri
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import java.util.*


@BindingAdapter("app:dateToString")
fun setDate(view: TextView, date: Date?) {
    view.text = if (date == null || date == Date(0)) "" else dateFormat.format(date)
}

@BindingAdapter("app:dateToString")
fun setDate(view: TextView, date: Long) {
    view.text = dateFormat.format(Date(date))
}

@BindingAdapter("app:timeToString")
fun setTime(view: TextView, date: Date) {
    view.text = timeFormat.format(date)
}

@BindingAdapter("app:timeToString")
fun setTime(view: TextView, date: Long) {
    view.text = timeFormat.format(Date(date))
}

@BindingAdapter("app:toCount")
fun setCount(view: EditText, newValue: Long) {
    val text = view.text.toString()
    val newText = if (newValue == 0L) "" else newValue.toString()
    // Important to break potential infinite loops.
    if (text != newText) {
        view.setText(newText)
    }
}

@InverseBindingAdapter(attribute = "app:toCount", event = "android:textAttrChanged")
fun getCount(view: EditText): Long {
    return try {
        view.text.toString().toLong()
    } catch (e: NumberFormatException) {
        0L
    }
}

@BindingAdapter("toWeight")
fun setWeight(view: EditText, newValue: Double) {
    val text = view.text.toString()
    val newText = if (newValue == .0) "" else newValue.toString()
    // Important to break potential infinite loops.
    if (text != newText) {
        view.setText(newText)
    }
}

@InverseBindingAdapter(attribute = "toWeight", event = "android:textAttrChanged")
fun getWeight(view: EditText): Double {
    return try {
        view.text.toString().toDouble()
    } catch (e: NumberFormatException) {
        .0
    }
}

@BindingAdapter("android:src")
fun setImageUri(view: ImageView, imageUri: String?) {
    if (imageUri == null) {
        view.setImageURI(null)
    } else {
        view.setImageURI(Uri.parse(imageUri))
    }
}

@BindingAdapter("app:srcId")
fun setImageResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}