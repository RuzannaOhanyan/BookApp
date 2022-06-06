package com.example.bookapp.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.example.bookapp.`object`.Constants
import com.example.bookapp.MyApplication
import com.example.bookapp.R
import com.example.bookapp.databinding.DialogCommentAddBinding
import com.example.bookapp.databinding.FragmentMoreDetailsPdfBinding
import com.example.bookapp.AdapterComment
import com.example.myproject.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class MoreDetailsPdfFragment : Fragment() {
    private companion object{
        const val TAG = "BOOK_DETAILS_TAG"
    }
    lateinit var BYTES:ByteArray
    val CREATE_FILE=0
    private lateinit var binding: FragmentMoreDetailsPdfBinding

    private lateinit var progressDialog: ProgressDialog
    private lateinit var commentArrayList : ArrayList<ModelComment>
    private lateinit var adapterComment : AdapterComment
    private lateinit var auth: FirebaseAuth

    private var url=""
    private var bookId=""
    private var title = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentMoreDetailsPdfBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth =FirebaseAuth.getInstance()
        if (auth.currentUser != null)
        {
            checkIfReserved()
        }

        progressDialog= ProgressDialog(this.context)
        progressDialog.setTitle("Խնդրում ենք սպասել")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.descriptionDet.text=arguments?.getString("bookDescription")
        binding.titleDet.text=arguments?.getString("bookTitle")
        binding.dateDet.text=arguments?.getString("bookdate")
        url=arguments?.getString("bookUrl")!!
        bookId=arguments?.getString("bookId")!!
        title = arguments?.getString("bookTitle")!!
        val categoryId=arguments?.getString("bookCategoryId")
        MyApplication.loadPdfSize(url, title , binding.sizeDet)
        MyApplication.loadPdfSinglePage(url ,title ,  binding.pdfViewer,binding.progressBar2,binding.pagesDet)
        MyApplication.loadCategory(categoryId!!,binding.categoryDet)

        MyApplication.incrementBookView(bookId)

        loadRestBookInfo(bookId)
        showComments()
        binding.btnReadBook.setOnClickListener {
            val bundle = bundleOf(
                "bookUrl" to url,
            )
            view.findNavController().navigate(R.id.action_moreDetailsPdfFragment_to_pdfViewerFragment,bundle)
        }

        binding.btnDownload.setOnClickListener {

            if(ContextCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                downloadBook()
            else
                requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        binding.addCommentBtn.setOnClickListener {
            if (auth.currentUser == null) {
                Toast.makeText(this.context, "Խնդրում ենք նախ մուտք գործել", Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }
        binding.btnReserve.setOnClickListener {
            if (auth.currentUser == null)
            {
                Toast.makeText(this.context, "Մուտք գործեք նախ", Toast.LENGTH_SHORT).show()
            }
            else{
                if (checkIfReserved() == true)
                {

                    this.context?.let { it1 -> MyApplication.removeFromReserved(it1, bookId) }
                }
                else{
                    addToReservered()
                }
            }


        }

    }



    private fun showComments() {
        commentArrayList =  ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference(  "Books")
        ref.child(bookId).child( "Comments")
            .addValueEventListener (object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    commentArrayList.clear()
                    for (ds in snapshot.children) {

                        val model = ds.getValue(ModelComment::class.java)

                        commentArrayList.add(model!!)
                    }

                    adapterComment = AdapterComment(context!!,commentArrayList)
                    binding.commentsRv.adapter = adapterComment
                }

                override fun onCancelled(errer: DatabaseError) {

                }
            })

    }

    private var comment = ""
    private fun addCommentDialog() {
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this.context))
        val builder = AlertDialog.Builder(this.context, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)
        val alertDialog = builder.create()
        alertDialog.show()
        commentAddBinding.backBtn.setOnClickListener { alertDialog.dismiss() }
        commentAddBinding.submitBtn.setOnClickListener {
            comment = commentAddBinding.commentET.text.toString().trim()
            if (comment.isEmpty()) {
                Toast.makeText(this.context, "Գրեք ձեր մեկնաբանությունը", Toast.LENGTH_SHORT)
                    .show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGaranted->
        if(isGaranted)
            downloadBook()
        else
            Toast.makeText(this.context,"Թույլտվությունը մերժվել է", Toast.LENGTH_LONG).show()
    }

    private fun downloadBook() {
        progressDialog.setMessage("Մուտքագրվում է")
        progressDialog.show()
        val storageReference= FirebaseStorage.getInstance().getReferenceFromUrl(url)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                BYTES=bytes
                progressDialog.dismiss()
                val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type="application/pdf"
                    putExtra(Intent.EXTRA_TITLE,arguments?.getString("bookTitle")+".pdf")
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI,"")
                }
                startActivityForResult(intent,CREATE_FILE)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this.context,"Չհաջողվեց վերցնել գիրքը պահեստից, քանի որ ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==CREATE_FILE && resultCode == AppCompatActivity.RESULT_OK){
            val uri=data!!.data
            try {
                val outputStream=this.requireActivity().contentResolver.openOutputStream(uri!!)
                outputStream?.write(BYTES)
                outputStream?.close()
                MyApplication.incrementDownloadCount(bookId)
                Toast.makeText(this.context,"Գիրքը հաջողությամբ ներբեռնվեց",Toast.LENGTH_LONG).show()
            }
            catch(e:Exception){

            }
        }
    }
    private fun loadRestBookInfo(bookId: String) {
        val ref= FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val viewsCount=snapshot.child("viewCount").value.toString()
                    val downloadsCount=snapshot.child("downloadCount").value.toString()
                    binding.viewsDet.text=viewsCount
                    binding.downloadsDet.text=downloadsCount
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addComment() {
        progressDialog.setMessage("Մեկնաբանության ավելացում ...")
        progressDialog.show()
        val timestamp = "${System.currentTimeMillis()}"
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${auth.uid}"


        val ref = FirebaseDatabase.getInstance().getReference("Books")

        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this.context, "Ձեր մեկնաբանությունը ավելացված է !", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this.context,
                    "Մեկնաբանության ավելացումը ձախողվեց, քանի որ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private var status = false

    private fun checkIfReserved() : Boolean {



        Log.d(TAG, "Ստուգում, արդյոք այս գիրքը նախկինում ամրագրված է")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(auth.uid!!).child( "Reserved").child(bookId)
            .addValueEventListener (object : ValueEventListener{
                override fun onDataChange (snapshot: DataSnapshot) {
                    var isInReserved = snapshot.exists()
                    if (isInReserved ) {
                        Log.d(TAG, "Հասանելի է հավանածներում")
                        binding.btnReserve.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_white,
                            0,
                            0
                        )
                        binding.btnReserve.text = "Չհավանել"



                    } else {

                        Log.d(TAG, "Պահպանվածը գրքերում չկա")
                        binding.btnReserve.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_border_white,
                            0,
                            0
                        )
                        binding.btnReserve.text = "Հավանել"


                    }
                    status = isInReserved

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        return status




    }
    private fun addToReservered(){

        Log.d(TAG, "Reserve: Ավելացվում է Հավանածներում:")
        val timestamp = "${System.currentTimeMillis()}"
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        val ref = FirebaseDatabase.getInstance().getReference( "Users")
        ref.child(auth.uid!!).child("Reserved" ).child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "Հավանված է")
                Toast.makeText(
                    this.context,
                    "Ավելացված է հավանածներում",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->

                Log.d(
                    TAG,
                    "addToFavorite: Չհաջողվեց ավելացնել հավանածներին ${e.message} պատճառով"
                )

                Toast.makeText(
                    this.context,
                    "Չհաջողվեց ավելացնել հավանածներին ${e.message} պատճառով",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }


}