package com.example.bookapp.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentForgotBinding
import com.google.firebase.auth.FirebaseAuth


class ForgotFragment : Fragment() {
    private lateinit var binding : FragmentForgotBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding  = FragmentForgotBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this.context)
        progressDialog.setTitle("Սպասեք")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.submitBtn.setOnClickListener {
            validateemail()
        }
        binding.backBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_forgotFragment_to_loginFragment)
        }

    }
    private var email = ""
    private fun validateemail() {
        email = binding.emailEt.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this.context, "Ձեր էլ.փոստը", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher (email).matches()) {
            Toast.makeText(this.context, "Էլ․փոստը սխալ է", Toast.LENGTH_SHORT).show()
        }
        else {
            recoverPassword()
        }

    }

    private fun recoverPassword() {
        progressDialog.setMessage("Գտնել այն, ինչ դուք պետք է անեք$email")
        progressDialog.show()
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this.context, "Ուղարկված հրահանգներ \t$email", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this.context, "Չուղարկելու պատճառով ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    }


