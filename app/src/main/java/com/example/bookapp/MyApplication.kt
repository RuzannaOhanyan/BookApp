package com.example.bookapp

import android.app.Application
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.bookapp.`object`.Constants
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MyApplication: Application(){
    override fun onCreate() {
        super.onCreate()
    }
    companion object{
        fun formatTimeStamp(timestamp: Long):String{
            val calendar= Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis=timestamp
            return DateFormat.format("dd/mm/yyyy",calendar).toString()
        }
        fun loadPdfSize(pdfUrl:String, pdfTitle: String, sizeTv: TextView){
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {
                    val bytes= it.sizeBytes.toDouble()
                    val kb=bytes/1024
                    val mb=kb/1024
                    if(mb>1)
                        sizeTv.text= String.format("%.2f MB",mb)
                    else if(kb>1)
                        sizeTv.text= String.format("%.2f KB",kb)
                    else
                        sizeTv.text= String.format("%.2f bytes",bytes)
                }
                .addOnFailureListener {

                }
        }
        fun loadPdfSinglePage(pdfUrl:String, pdfTitle:String,  pdfView: PDFView, progressBar: ProgressBar, pagesTv: TextView?){
            val TAG="PDF_Sigle_Page"
            val ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener {bytes->
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { e->
                            progressBar.visibility= View.INVISIBLE
                            Log.d(TAG,"Error:Բեռնել ${e.message} պատճառով")
                            Toast.makeText(null, "Սխալ", Toast.LENGTH_LONG).show()
                        }
                        .onPageError { page, t ->
                            progressBar.visibility= View.INVISIBLE
                            Log.d(TAG,"Error: էջը բեռնելու պատճառով ${t.message}")
                            Toast.makeText(null, "Սխալ էջ", Toast.LENGTH_LONG).show()
                        }
                        .onLoad{nbPages->
                            progressBar.visibility= View.INVISIBLE
                            if(pagesTv!=null)
                                pagesTv.text=nbPages.toString()
                        }
                        .load()
                }
                .addOnFailureListener {
                    Log.d(TAG,"Չհաջողվեց բայթ ստանալ, քանի որ ${it.message}")
                }
        }

        fun loadCategory(categoryId:String,categoryTv: TextView){
            val ref= FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category=snapshot.child("category").value.toString()
                        categoryTv.text=category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        fun deleteBook(context: Context, bookId:String, bookUrl:String, bookTitle:String){
            val progressDialog= ProgressDialog(context)
            progressDialog.setTitle("Խնդրում ենք սպասել")
            progressDialog.setMessage("Ջնջվում է $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            val storageRef= FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageRef.delete()
                .addOnSuccessListener {

                    val ref= FirebaseDatabase.getInstance().getReference("Books")
                        .child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context,"Գիրքը հաջողությամբ ջնջվեց", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(context,"Չհաջողվեց ջնջել գիրքը տվյալների բազայից, քանի որ ${it.message}",
                                Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(context,"Չհաջողվեց ջնջել գիրքը պահեստից՝${it.message} պատճառով",
                        Toast.LENGTH_LONG).show()
                }
        }
        fun incrementBookView(bookId:String){
            val ref= FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        var viewsCount=snapshot.child("viewCount").value.toString()
                        if(viewsCount=="")
                            viewsCount="0"

                        var newViewCount=viewsCount.toLong()+1
                        val map= HashMap<String,Any>()
                        map["viewCount"]=newViewCount
                        val dbRef= FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(map)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        fun incrementDownloadCount(bookId:String){
            val ref= FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        var downloadsCount=snapshot.child("downloadCount").value.toString()
                        //incremented val
                        var newDownloadsCount=downloadsCount.toLong()+1
                        val map= HashMap<String,Any>()
                        map["downloadCount"]=newDownloadsCount
                        val dbRef= FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(map)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        public fun removeFromReserved (context: Context, bookId: String){
            val TAG = "UnReserved"
            val auth = FirebaseAuth.getInstance()



            Log.d(TAG, "Ջնջել Reserved")
            val ref = FirebaseDatabase.getInstance().getReference(  "Users")
            ref.child(auth.uid!!).child("Reserved").child(bookId)
                .removeValue()
                .addOnSuccessListener {

                    Log.d(ContentValues.TAG, "Հանված է հավանածներից")
                    Toast.makeText(context, "Հանված է հավանածներից", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.d(ContentValues.TAG, "Չհաջողվեց հեռացնել վերապահված գրքերից՝ ${e.message} պատճառով")
                    Toast.makeText(
                        context,
                        "Չհաջողվեց հեռացնել վերապահված գրքերից՝ ${e.message} պատտճառով",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}