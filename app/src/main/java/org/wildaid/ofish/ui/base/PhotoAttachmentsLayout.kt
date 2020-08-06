package org.wildaid.ofish.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.item_photos_container.view.*
import org.wildaid.ofish.R
import org.wildaid.ofish.util.setVisible

class PhotoAttachmentsLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var photoList: List<PhotoItem>
    private var photosAdapter: PhotosAdapter? = null
    private var inEditMode: Boolean = false

    var onPhotoClickListener: ((view: View, photoItem: PhotoItem) -> Unit)? = null
    var onPhotoRemoveListener: ((PhotoItem) -> Unit)? = null

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflate(context, R.layout.item_photos_container, this)
    }

    fun getEditMode() = inEditMode
    fun getPhotos() = photoList

    fun setEditMode(inEditMode: Boolean) {
        this.inEditMode = inEditMode
        this.photosAdapter?.editMode = inEditMode
        this.photosAdapter?.notifyDataSetChanged()
    }

    fun setPhotos(photos: List<PhotoItem>?) {
        if (photos.isNullOrEmpty()) {
            setVisible(false)
            photos_title.setVisible(false)
            photos_recycler.adapter = null
        } else {
            setVisible(true)
            photos_title.setVisible(true)
            photos_recycler.adapter = PhotosAdapter(photos.toMutableList(), inEditMode,
                { view, item ->
                    onPhotoClickListener?.invoke(view, item)
                },
                {
                    onPhotoRemoveListener?.invoke(it)
                })
        }
    }
}