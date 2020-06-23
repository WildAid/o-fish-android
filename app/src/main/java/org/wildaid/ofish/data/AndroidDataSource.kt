package org.wildaid.ofish.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream


private const val MAX_BYTES_SIZE = 3 * 1024 * 1024  // 3mb
private const val THUMBNAIL_IMAGE_PIXEL_SIZE = 100  // 100 PIXELS
private const val COMPRESS_STEP = 10
private const val PREFERENCE_NAME = "preferences"
private const val ON_DUTY_KEY = "on_duty"

class AndroidDataSource(
    val context: Context
) {
    fun readBytes(uri: Uri): ByteArray? {
        return openStream(uri)?.use { it.readBytes() }
    }

    fun readCompressedBytes(uri: Uri): ByteArray? {
        var bytes = openStream(uri)?.use { it.readBytes() }
        var size = bytes?.size ?: 0

        if (size > MAX_BYTES_SIZE) {
            var compressQuality = 100
            val stream = ByteArrayOutputStream()
            while (size >= MAX_BYTES_SIZE && compressQuality > 10) {
                stream.use {
                    it.flush()
                    it.reset()
                }

                compressQuality -= COMPRESS_STEP
                val bitmap = BitmapFactory.decodeStream(openStream(uri))
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, stream)
                bytes = stream.toByteArray()
                size = bytes.size
                bitmap.recycle()
            }
        }
        return bytes
    }

    private fun openStream(uri: Uri): InputStream? {
        return context.contentResolver.openInputStream(uri)
    }

    fun generateImagePreview(imageUri: Uri): ByteArray? {
        val originalBitmap = BitmapFactory.decodeStream(openStream(imageUri))
        val thumbnailBitmap = Bitmap.createScaledBitmap(
            originalBitmap,
            THUMBNAIL_IMAGE_PIXEL_SIZE,
            THUMBNAIL_IMAGE_PIXEL_SIZE,
            false
        )

        originalBitmap.recycle()

        val baos = ByteArrayOutputStream()
        thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    fun getOnDutyStatus() =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getBoolean(ON_DUTY_KEY, false)

    fun saveOnDutyStatus(onDuty: Boolean) {
        val pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        with(pref.edit()) {
            putBoolean(ON_DUTY_KEY, onDuty)
            commit()
        }
    }
}