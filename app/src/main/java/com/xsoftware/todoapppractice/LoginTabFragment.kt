package com.xsoftware.todoapppractice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.xsoftware.todoapppractice.databinding.FragmentLoginTabBinding
import com.xsoftware.todoapppractice.databinding.FragmentSignUpTabBinding


class LoginTabFragment : Fragment() {
    private var _binding: FragmentLoginTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth

        val signupButton = binding.signupButton
        val loginButton = binding.loginButton

        signupButton.setOnClickListener {
            navigateSignUpFragment()
        }

        loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val pass = binding.loginPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener {
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                    navigateToNewTaskFragment()
                }.addOnFailureListener {
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToNewTaskFragment() {
        val fragmentContainerId = (view?.parent as? ViewGroup)?.id ?: R.id.fragment_container

        if (parentFragmentManager.findFragmentByTag("newTaskFragmentTag") == null) {
            val fragment = NewTaskFragment()
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

    private fun navigateSignUpFragment() {
        val fragmentContainerId = (view?.parent as? ViewGroup)?.id ?: R.id.fragment_container

        if (parentFragmentManager.findFragmentByTag("newSignupFragmentTag") == null) {
            val fragment = SignUpTabFragment()
            parentFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
                .replace(fragmentContainerId, fragment, "newSignUpFragmentTag")
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
        _binding = FragmentLoginTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}