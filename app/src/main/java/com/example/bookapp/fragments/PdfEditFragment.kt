package com.example.bookapp.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bookapp.databinding.FragmentPdfEditBinding
import com.example.bookapp.models.CategoryData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditFragment : Fragment() {
    private lateinit var binding: FragmentPdfEditBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var arrayCategotyList:ArrayList<CategoryData>

    private val TAG="PDF_UPDATE"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPdfEditBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth= FirebaseAuth.getInstance()
        loadPdfCategories()
        progressDialog= ProgressDialog(this.context)
        progressDialog.setTitle("Խնդրում ենք սպասել")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.edTitleUp.setText(arguments?.getString("bookTitle"))
        binding.edDescriptionUp.setText(arguments?.getString("bookDescription"))
        loadOldCategory()

        binding.edCategorieUp.setOnClickListener {
            categoryDialog()
        }
        binding.btnUpBook.setOnClickListener {
            validation()
        }
    }

    private var title=""
    private var description=""
    private var category=""

    private fun validation() {
        title=binding.edTitleUp.text.toString().trim()
        description=binding.edDescriptionUp.text.toString().trim()
        category=binding.edCategorieUp.text.toString().trim()

        if(title.isEmpty())
            Toast.makeText(this.context,"Մուտքագրեք վերնագիրը", Toast.LENGTH_LONG).show()
        else if(description.isEmpty())
            Toast.makeText(this.context,"Մուտքագրեք նկարագրությունը", Toast.LENGTH_LONG).show()
        else if(category.isEmpty())
            Toast.makeText(this.context,"Մուտքագրեք ժանրը", Toast.LENGTH_LONG).show()
        else
            updatePdf()
    }

    private fun updatePdf() {
        //show progress dialog
        progressDialog.setMessage("Գրքի տեղեկությունները թարմացվում են")
        progressDialog.show()
        val map=HashMap<String,Any>()
        if(selectedCategoryId=="")
            map["categoryId"]=arguments?.getString("bookCategoryId").toString()
        else
            map["categoryId"]=selectedCategoryId
        map["description"]=description
        map["title"]=title
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(arguments?.getString("bookId").toString())
            .updateChildren(map)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this.context,"Հաջողությամբ թարմացվում է", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this.context,"Թարմացումը Չհաջողվեց ${it.message} պատճառով ", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadOldCategory() {
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(arguments?.getString("bookCategoryId").toString())
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val category=snapshot.child("ժանր").value

                    binding.edCategorieUp.setText(category.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private var selectedCategoryId=""
    private var selectedCategoryTitle=""
    private fun categoryDialog(){
        Log.d(TAG,"categoryDialog:Ցուցադրվում է pdf ժանրի նկարը")

        val arrayCategories= arrayOfNulls<String>(arrayCategotyList.size)
        var i=0
        arrayCategotyList.forEach {
            arrayCategories[i]=it.category
            i++
        }

        val builder= AlertDialog.Builder(this.context)
        builder.setTitle("Category")
            .setItems(arrayCategories){ dialog, which->

                selectedCategoryId=arrayCategotyList.get(which).id
                selectedCategoryTitle=arrayCategotyList.get(which).category
                binding.edCategorieUp.setText(selectedCategoryTitle)
            }
            .show()
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Բեռնվում են pdf ժանրերը")
        arrayCategotyList= ArrayList()

        val ref= FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                arrayCategotyList.clear()
                snapshot.children.forEach {
                    val id=it.child("id").getValue(String::class.java)!!
                    val category=it.child("category").getValue(String::class.java)!!
                    val timestamp=it.child("timestamp").getValue(Long::class.java)!!
                    val uid=it.child("uid").getValue(String::class.java)!!
                    val categoryData= CategoryData(id,category,timestamp,uid)
                    arrayCategotyList.add(categoryData)
                    Log.d(TAG,"onDataChange:${categoryData.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }
}