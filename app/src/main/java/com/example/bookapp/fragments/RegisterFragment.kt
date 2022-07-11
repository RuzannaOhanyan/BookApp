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
import com.example.bookapp.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog:ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentRegisterBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()

        progressDialog= ProgressDialog(this.context)
        progressDialog.setTitle("Խնդրում ենք սպասել")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.btnBack.setOnClickListener {
            view.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        binding.btnRegister.setOnClickListener {
            validation()
        }
    }

    var name:String=""
    var email:String=""
    var password:String=""
    var confPassword:String=""
    private fun validation() {
        name=binding.edName.text.toString().trim()
        email=binding.edEmail.text.toString().trim()
        password=binding.edPass.text.toString().trim()
        confPassword=binding.edCpass.text.toString().trim()
        if(name.isEmpty())
            Toast.makeText(this.context,"Մուտքագրեք համապատասխան անունը", Toast.LENGTH_LONG).show()
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            Toast.makeText(this.context,"Էլ․փոստի ձևաչափը անվավեր է", Toast.LENGTH_LONG).show()
        else if(password.isEmpty())
            Toast.makeText(this.context,"Մուտքագրեք համապատասխան գաղտնաբառը", Toast.LENGTH_LONG).show()
        else if(confPassword.isEmpty())
            Toast.makeText(this.context,"Կրկնեք գաղտնաբառը", Toast.LENGTH_LONG).show()
        else if(!password.equals(confPassword))
            Toast.makeText(this.context,"Սխալ գաղտնաբառ", Toast.LENGTH_LONG).show()
        else{
            createAccount()
        }
    }

    private fun createAccount() {
        progressDialog.setTitle("Հաշվի ստեղծում")
        progressDialog.show()
        auth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                updateUserInfo()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"Չհաջողվեց ստեղծել հաշիվ, քանի որ ${e.message}",Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUserInfo() {
        progressDialog.setTitle("Օգտատիրոջ տեղեկությունները պահվում են...")
        val timestamp=System.currentTimeMillis()

        val uid=auth.uid
        val map:HashMap<String,Any?> = HashMap()
        map["uid"]=uid
        map["name"]=name
        map["email"]=email
        map["image"]=""
        map["userType"]="user"
        map["timestamp"]=timestamp

        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(map)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this.context,"Ստեղծվել է հաշիվ",Toast.LENGTH_LONG).show()
                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Նամակն ուղարկված է", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Նամակը չի ուղարկվել", Toast.LENGTH_SHORT).show()
                    }
                }
                view?.findNavController()?.navigate(R.id.action_registerFragment_to_loginFragment)
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"Չհաջողվեց պահել օգտատիրոջ տվյալները ${e.message} պատճառով",Toast.LENGTH_LONG).show()
            }
    }
}