package com.survivalcoding.noteapp.presentation.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.survivalcoding.noteapp.App
import com.survivalcoding.noteapp.R
import com.survivalcoding.noteapp.databinding.FragmentNotesBinding
import com.survivalcoding.noteapp.domain.model.Note
import com.survivalcoding.noteapp.domain.usecase.GetNotesByOrderUseCase
import com.survivalcoding.noteapp.presentation.add_edit_note.AddEditNoteFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {

    private val viewModel: NotesViewModel by viewModels {
        NotesViewModelFactory(
            GetNotesByOrderUseCase((requireActivity().application as App).noteRepository)
        )
    }

    private val binding: FragmentNotesBinding by lazy {
        FragmentNotesBinding.inflate(layoutInflater)
    }

    private val noteListAdapter: NoteListAdapter by lazy {
        NoteListAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notesRecyclerView.adapter = noteListAdapter
        binding.addButton.setOnClickListener { viewModel.navigateToAddNote() }

        observe()
    }

    private fun observe() {
        viewModel.notesUiState.observe(this) {
            noteListAdapter.submitList(it.noteList)
        }

        repeatOnStart {
            viewModel.eventFlow.collect { handleEvent(it) }
        }
    }

    private fun repeatOnStart(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }

    private fun handleEvent(event: NotesViewModel.Event) {
        when (event) {
            is NotesViewModel.Event.NavigateToEditNote -> navigateToEditNote(event.note)
            is NotesViewModel.Event.NavigateToAddNote -> navigateToAddNote()
        }
    }

    private fun navigateToAddNote() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, AddEditNoteFragment.newInstance(null))
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToEditNote(note: Note) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, AddEditNoteFragment.newInstance(null))
            .addToBackStack(null)
            .commit()
    }

    override fun onStart() {
        super.onStart()

        viewModel.loadList()
    }

    companion object {
        @JvmStatic
        fun newInstance() = NotesFragment().apply {
            arguments = Bundle().apply {
            }
        }
    }
}