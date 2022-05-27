package org.wildaid.ofish.ui.polygon_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.wildaid.ofish.R
import org.wildaid.ofish.data.PolygonData
import org.wildaid.ofish.databinding.FragmentPolygonBottomSheetBinding
import org.wildaid.ofish.util.getViewModelFactory

private const val COLLAPSED_HEIGHT = 228

class PolygonBottomSheetFragment(private var polygonData: PolygonData? = null) :
    BottomSheetDialogFragment() {

    private var _isBottomSheetFragmentClosed = MutableLiveData<Boolean>()
    val isBottomSheetFragmentClosed: LiveData<Boolean>
        get() = _isBottomSheetFragmentClosed

    private val polygonViewModel: PolygonBottomSheetViewModel by viewModels { getViewModelFactory() }
    lateinit var binding: FragmentPolygonBottomSheetBinding

    fun setPolygonData(polygonData: PolygonData?) {
        this.polygonData = polygonData
    }

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPolygonBottomSheetBinding.bind(
            inflater.inflate(
                R.layout.fragment_polygon_bottom_sheet,
                container
            )
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel = this@PolygonBottomSheetFragment.polygonViewModel
        initUi()

        binding.closeBtn.setOnClickListener { dismiss() }
    }

    private fun initUi() {
        binding.locationImage.setColorFilter(
            polygonData?.color ?: 0, android.graphics.PorterDuff.Mode.SRC_IN
        )

        binding.polygonName.text = polygonData?.polygonName
        binding.polygonCountry.text = polygonData?.country
        binding.info.text = polygonData?.info
        binding.polygonText.text = polygonData?.description
    }

    override fun onStart() {
        super.onStart()
        val density = requireContext().resources.displayMetrics.density

        dialog?.let {
            val bottomSheet =
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.peekHeight = (COLLAPSED_HEIGHT * density).toInt()
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        }
    }


    override fun onPause() {
        _isBottomSheetFragmentClosed.value = true
        super.onPause()
    }
}