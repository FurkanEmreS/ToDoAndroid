package com.xsoftware.todoapppractice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xsoftware.todoapppractice.databinding.FragmentNewTaskSheetBinding

class NewTaskSheet : BottomSheetDialogFragment() {




    private var _binding: FragmentNewTaskSheetBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentNewTaskSheetBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var name = binding.name.text.toString()
        var desc = binding.desc.text.toString()
        binding.saveButton.setOnClickListener{
            Toast.makeText(requireContext(), "Button clicked!", Toast.LENGTH_SHORT).show()

        }
        binding.deleteButton.setOnClickListener {

        }

        binding.datePickerButton.setOnClickListener{

        }
        binding.timePickerButton.setOnClickListener {


        }




    }


}