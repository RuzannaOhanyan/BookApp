package com.example.bookapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.bookapp.MyApplication
import com.example.bookapp.PdfModelData
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentProfileBinding
import com.example.bookapp.AdapterBookReserved

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ProfileFragment : Fragment() {
    private lateinit var binding : FragmentProfileBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var booksReservedArrayList : ArrayList<PdfModelData>
    private lateinit var adapterBookReserved : AdapterBookReserved


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        loadReservedBooks()

        binding.backBtn.setOnClickListener{
            view.findNavController().navigate(R.id.action_profileFragment_to_dashboardUserFragment)
        }
        binding.profileEditBtn.setOnClickListener{
            view.findNavController().navigate(R.id.action_profileFragment_to_profileEditFragment)

        }


    }

    private fun onBackPressed() {
        onBackPressed()
    }

    private fun loadReservedBooks() {

        booksReservedArrayList =  ArrayList();
        val ref = FirebaseDatabase.getInstance().getReference( "Users")
        ref.child(firebaseAuth.uid!!).child( "Reserved")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksReservedArrayList.clear()
                    for (ds in snapshot.children) {
                        val bookId = "${ds.child("bookId").value}"
                        val modelPdf = PdfModelData()
                        modelPdf.id = bookId
                        booksReservedArrayList.add(modelPdf)
                    }

                    adapterBookReserved =
                        AdapterBookReserved(context!!, booksReservedArrayList)
                    binding.reservedBooksRv.adapter = adapterBookReserved


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference( "Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType


                    try{
                        Glide.with(this@ProfileFragment).load(profileImage).placeholder(R.drawable.ic_person_gray).into(binding.profileIv)
                    }
                    catch (e: Exception){


                    }                        }

                override fun onCancelled(error: DatabaseError) {

                }


            })

    }


}