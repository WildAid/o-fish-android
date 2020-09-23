package org.wildaid.ofish.ui.notes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_notes.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentNotesBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.ui.base.CARDS_OFFSET_SIZE
import org.wildaid.ofish.ui.crew.VerticalSpaceItemDecoration
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard

class NotesFragment : BaseReportFragment(R.layout.fragment_notes) {
    private val fragmentViewModel: NotesViewModel by viewModels { getViewModelFactory() }

    private lateinit var viewDataBinding: FragmentNotesBinding
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.initNotes(currentReport, currentReportPhotos)
    }

    override fun isAllRequiredFieldsNotEmpty(): Boolean {
        return true
    }

    override fun validateForms(): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentNotesBinding.bind(view)
            .apply {
                this.viewmodel = fragmentViewModel
                this.lifecycleOwner = this@NotesFragment.viewLifecycleOwner
            }

        initUI()

        fragmentViewModel.notesLiveData.observe(viewLifecycleOwner, Observer(::displayNotes))
        fragmentViewModel.notesUserEventLiveData.observe(viewLifecycleOwner, EventObserver(::handleUserEvent))
    }

    private fun initUI() {
        notesAdapter = NotesAdapter(
            noteRemoveListener = {
                requireActivity().currentFocus?.clearFocus()
                fragmentViewModel.removeNote(it)
            },
            editNoteListener = {
                fragmentViewModel.editNote(it)
            },
            addPhotoAttachmentListener = { note ->
                peekImage { imageUri ->
                    fragmentViewModel.addPhotoAttachmentForNote(imageUri, note)
                }
            },
            noteOnPhotoClickListener = ::showFullImage,
            removePhotoAttachmentListener = { photo, note ->
                fragmentViewModel.removePhotoAttachment(photo, note)
            }
        )

        notes_recycler.apply {
            adapter = notesAdapter
            addItemDecoration(VerticalSpaceItemDecoration(CARDS_OFFSET_SIZE))
        }

        notes_add_footer.setOnClickListener {
            requireActivity().currentFocus?.clearFocus()
            fragmentViewModel.addNote()
        }
    }

    private fun displayNotes(list: List<NoteItem>) {
        val newList = list.map { item -> item.copy() }
        notesAdapter.setItems(newList)
    }

    private fun handleUserEvent(buttonId: NotesViewModel.NotesUserEvent) {
        hideKeyboard()
        when (buttonId) {
            NotesViewModel.NotesUserEvent.SaveEvent -> {
                onNextListener.onNextClicked(true)
            }
        }
    }
}