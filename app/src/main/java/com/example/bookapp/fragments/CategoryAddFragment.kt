package com.example.bookapp.fragments

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddFragment : Fragment() {
    private lateinit var binding: FragmentCategoryAddBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentCategoryAddBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()

        progressDialog= ProgressDialog(this.context)
        progressDialog.setTitle("Խնդրում ենք սպասել")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.btnBack2.setOnClickListener {
            view.findNavController().navigate(R.id.action_categoryAddFragment_to_dashboardAdminFragment)
        }
        binding.btnAddCategorie.setOnClickListener {
            validation()
        }
    }
    private var category=""
    private fun validation() {
        category=binding.edTitle.text.toString().trim()
        if(category.isEmpty())
            Toast.makeText(this.context,"Մուտքագրեք Ժանր", Toast.LENGTH_LONG).show()
        else
            addCategorieToFirebase()
    }

    private fun addCategorieToFirebase() {
        progressDialog.show()
        val timestamp=System.currentTimeMillis()
        val map=HashMap<String,Any>()
        map["id"]=timestamp.toString()
        map["category"]=category
        map["timestamp"]=timestamp
        map["uid"]=auth.uid.toString()
        //reference of root
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(timestamp.toString())
            .setValue(map)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.edTitle.text.clear()
                Toast.makeText(this.context,"Հաջողությամբ ավելացվեց",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"Չհաջողվեց ավելացնել, քանի որ ${e.message}",Toast.LENGTH_LONG).show()
            }
    }
}