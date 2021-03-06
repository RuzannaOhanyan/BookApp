package com.example.bookapp.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentDashboardAdminBinding
import com.example.myproject.CategoryAdapter
import com.example.bookapp.models.CategoryData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminFragment : Fragment() {

    var arrayData=ArrayList<CategoryData>()
    var tempArrayData=ArrayList<CategoryData>()
    private lateinit var binding: FragmentDashboardAdminBinding
    private lateinit var auth: FirebaseAuth
    lateinit var adapter:CategoryAdapter

     override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         binding= FragmentDashboardAdminBinding.inflate(inflater,container,false)
         return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()
        checkUser()
        loadCategories(this.context)
        binding.btnLogoutAdmin.setOnClickListener {
            auth.signOut()
            checkUser()
        }
        binding.btnMoveToAddCategory.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_dashboardAdminFragment_to_categoryAddFragment)
        }
        binding.pdfAddFrag.setOnClickListener {
            view.findNavController().navigate(R.id.action_dashboardAdminFragment_to_bookAddFragment)
        }

        binding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{
                    tempArrayData.clear()
                    val searchText=s.toString().uppercase()
                    if(searchText!=null && searchText.isNotEmpty()){
                        arrayData.forEach {
                            if(it.category.uppercase().contains(searchText)){
                                tempArrayData.add(it)
                            }
                        }
                    }
                    else{
                        tempArrayData.clear()
                        tempArrayData.addAll(arrayData)
                    }
                    adapter.notifyDataSetChanged()
                }catch(e:Exception){

                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun loadCategories(context: Context?) {
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                arrayData.clear()
                snapshot.children.forEach {
                    val id=it.child("id").getValue(String::class.java)!!
                    val category=it.child("category").getValue(String::class.java)!!
                    val timestamp=it.child("timestamp").getValue(Long::class.java)!!
                    val uid=it.child("uid").getValue(String::class.java)!!
                    val categoryData= CategoryData(id,category,timestamp,uid)
                    arrayData.add(categoryData)
                }
                tempArrayData.clear()
                tempArrayData.addAll(arrayData)
                binding.rv.layoutManager= LinearLayoutManager(context)
                adapter= CategoryAdapter(tempArrayData)
                binding.rv.adapter=adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun checkUser() {
        val user=auth.currentUser
        if(user==null){
            view?.findNavController()?.navigate(R.id.action_dashboardAdminFragment_to_betweenFragment)
        }
        else{
            val email=user.email
            binding.adminEmail.text=email
        }
    }
}