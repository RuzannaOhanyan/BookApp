package com.example.bookapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.bookapp.PdfModelData
import com.example.bookapp.databinding.FragmentBooksUserBinding
import com.example.bookapp.AdapterPdfUser

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception


class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding
    public companion object{
        private const val TAG =  "B0OKS_USER_TAG"
        public fun newInstance(categoryId: String, category: String, uid: String): BooksUserFragment{
            val fragment = BooksUserFragment ()

            val args =  Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }}
    private var categoryId =""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<PdfModelData>
    private lateinit var adapterPdfUser: AdapterPdfUser
    var arrayData=ArrayList<PdfModelData>()
    var tempArrayData=ArrayList<PdfModelData>()
    lateinit var adapter: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args != null)
        {
            categoryId = args.getString("categoryId")!!
            category  = args.getString ("category")!!
            uid = args.getString("uid")!!
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)
        Log.d(TAG, "onCreateView: Ժանր: $category")
        if (category == "Բոլորը") {

            loadAllBooks()
        }
        else if (category ==  "Ամենադիտված") {

            loadMostViewedDownloadedBooks("viewsCount")
        }
        else if (category == "Ամենաներբեռնված") {

            loadMostViewedDownloadedBooks("downloadsCount")
        }
        else {

            loadCategorizedBooks()
        }

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{
                    tempArrayData.clear()
                    val searchText=s.toString().uppercase()
                    if(searchText!=null && searchText.isNotEmpty()){
                        arrayData.forEach {
                            if(it.title.uppercase().contains(searchText)){
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


        return binding.root
    }
    private fun loadAllBooks() {

        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                pdfArrayList.clear()
                for (ds in snapshot.children) {

                    val model = ds.getValue(PdfModelData::class.java)
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)

                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun loadMostViewedDownloadedBooks(orderBy: String) {

        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    pdfArrayList.clear()
                    for (ds in snapshot.children) {

                        val model = ds.getValue(PdfModelData::class.java)

                        pdfArrayList.add(model!!)
                    }

                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)

                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }


    private fun loadCategorizedBooks() {

        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    pdfArrayList.clear()
                    for (ds in snapshot.children) {

                        val model = ds.getValue(PdfModelData::class.java)

                        pdfArrayList.add(model!!)
                    }

                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}

