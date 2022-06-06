package com.example.bookapp.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentBookAddBinding
import com.example.bookapp.models.CategoryData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class BookAddFragment : Fragment() {
    private lateinit var binding: FragmentBookAddBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var arrayCategotyList:ArrayList<CategoryData>
    private var pdfUri: Uri?= null
    private val TAG="PDF_ADD"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBookAddBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()
        loadPdfCategories()

        progressDialog= ProgressDialog(this.context)
        progressDialog.setTitle("Խնդրում ենք սպասել")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.edCategorie.setOnClickListener {
            categoryDialog()
        }
        binding.attPdf.setOnClickListener {
            pdfPickIntent()
        }
        binding.btnAddBook.setOnClickListener {
            validation()
        }
        binding.btnBack1.setOnClickListener {
            view.findNavController().navigate(R.id.action_bookAddFragment_to_dashboardAdminFragment)
        }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories:Բեռնվում են pdf Ժանրերը")

        arrayCategotyList= ArrayList()

        val ref=FirebaseDatabase.getInstance().getReference("Categories")
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

    private var title=""
    private var description=""
    private var category=""

    private fun validation() {
        title=binding.edTitle.text.toString().trim()
        description=binding.edDescription.text.toString().trim()
        category=binding.edCategorie.text.toString().trim()

        if(title.isEmpty())
            Toast.makeText(this.context,"Մուտքագրեք վերնագիրը", Toast.LENGTH_LONG).show()
        else if(description.isEmpty())
            Toast.makeText(this.context,"Մուտքագրեք նկարագրությունը", Toast.LENGTH_LONG).show()
        else if(category.isEmpty())
            Toast.makeText(this.context,"Մուտքագրեք ժանրը", Toast.LENGTH_LONG).show()
        else if(pdfUri==null)
            Toast.makeText(this.context,"Ընտրել Pdf ..", Toast.LENGTH_LONG).show()
        else
            uploadPdfToStorage()
    }

    private fun uploadPdfToStorage() {
        Log.d(TAG,"Բեռնում է պահեստում")

        progressDialog.setMessage("Pdf-ի բեռնում")
        progressDialog.show()


        val timestamp=System.currentTimeMillis()


        val filePathAndName="Books/${timestamp}"


        val storageReference= FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapeShot->

                val uriTask=taskSnapeShot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploaedPdfUrl=uriTask.result.toString()
                uploadedPdfInfoToDatabase(uploaedPdfUrl,timestamp)
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"Չհաջողվեց վերբեռնել, քանի որ ${e.message}",Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadedPdfInfoToDatabase(uploaedPdfUrl: String, timestamp: Long) {
        progressDialog.setMessage("Pdf տեղեկատվության բեռնում")
        progressDialog.show()
        val map=HashMap<String,Any>()
        map["id"]=timestamp.toString()
        map["categoryId"]=selectedCategoryId
        map["description"]=description
        map["title"]=title
        map["url"]=uploaedPdfUrl
        map["timestamp"]=timestamp
        map["uid"]=auth.uid.toString()
        map["viewCount"]=0
        map["downloadCount"]=0


        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(timestamp.toString())
            .setValue(map)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.edTitle.text.clear()
                Toast.makeText(this.context,"Հաջողությամբ վերբեռնվեց",Toast.LENGTH_LONG).show()
                pdfUri=null
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"Չհաջողվեց ավելացնել, քանի որ ${e.message}",Toast.LENGTH_LONG).show()
            }
    }

    private var selectedCategoryId=""
    private var selectedCategoryTitle=""
    private fun categoryDialog(){
        Log.d(TAG,"categoryDialog:Ցուցադրվում է pdf ժանրը նկարով")

        val arrayCategories= arrayOfNulls<String>(arrayCategotyList.size)
        var i=0
        arrayCategotyList.forEach {
            arrayCategories[i]=it.category
            i++
        }
        val builder= AlertDialog.Builder(this.context)
        builder.setTitle("Ժանր")
            .setItems(arrayCategories){ dialog, which->
                selectedCategoryId=arrayCategotyList.get(which).id
                selectedCategoryTitle=arrayCategotyList.get(which).category
                binding.edCategorie.setText(selectedCategoryTitle)
            }
            .show()
    }
    private fun pdfPickIntent(){
        Log.d(TAG,"pdfPickIntent: Մեկնարկային pdf նկարը")
        val intent= Intent()
        intent.type="application/pdf"
        intent.action= Intent.ACTION_GET_CONTENT
        pdfResultLauncher.launch(intent)
    }
    val pdfResultLauncher=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result->
            if(result.resultCode== AppCompatActivity.RESULT_OK){
                Log.d(TAG,"PDF ընտրված է")
                pdfUri=result.data?.data
            }
            else{
                Log.d(TAG,"PDF-ի ընտրությունը չեղարկված է")
                Toast.makeText(this.context,"Չեղյալ է հայտարարվել",Toast.LENGTH_LONG).show()
            }
        }
    )
}