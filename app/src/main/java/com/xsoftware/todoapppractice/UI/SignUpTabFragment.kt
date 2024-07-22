package com.xsoftware.todoapppractice.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.xsoftware.todoapppractice.R
import com.xsoftware.todoapppractice.databinding.FragmentSignUpTabBinding

class SignUpTabFragment : Fragment() {
    private var _binding: FragmentSignUpTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth

        val signupclick = binding.signupButton

        signupclick.setOnClickListener {
            val email = binding.signupEmail.text.toString()
            val pass = binding.signupPassword.text.toString()
            val passagain = binding.signupConfirm.text.toString()

            if (pass == passagain) {
                if (email.isNotEmpty() && pass.isNotEmpty() && passagain.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener {
                        Toast.makeText(context, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                        navigateToNewTaskFragment()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Account creation failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToNewTaskFragment() {
        val fragmentContainerId = (view?.parent as? ViewGroup)?.id ?: R.id.fragment_container

        if (parentFragmentManager.findFragmentByTag("newTaskFragmentTag") == null) {
            val fragment = HomeFragment()
            parentFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
                .replace(fragmentContainerId, fragment, "newTaskFragmentTag")
                .addToBackStack(null)
                .commit()
        } else {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}