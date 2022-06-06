package com.example.bookapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.RowReservedBooksBinding
import com.example.bookapp.fragments.MoreDetailsPdfFragment

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterBookReserved : RecyclerView.Adapter<AdapterBookReserved.HolderPdfReserved> {

    private val context: Context
    private  var booksArrayList: ArrayList<PdfModelData>
    private lateinit var binding: RowReservedBooksBinding
    constructor(context: Context, booksArrayList: ArrayList<PdfModelData>)
    {
        this.context = context
        this.booksArrayList = booksArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfReserved {
        binding = RowReservedBooksBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfReserved(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfReserved, position: Int) {
        val model = booksArrayList[position]
        showBooksDetails(model, holder)
        val timestamp = model.timestamp

        val date = MyApplication.formatTimeStamp(timestamp)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MoreDetailsPdfFragment::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }
        holder.unReserveBtn.setOnClickListener {
            MyApplication.removeFromReserved(context, model.id)
        }
    }

    private fun showBooksDetails(model: PdfModelData, holder: HolderPdfReserved) {

        val bookId = model.id
        val ref = FirebaseDatabase.getInstance().getReference( "Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewCount").value}"

                    model.isReserved = true
                    model.title = title
                    model.categoryId = categoryId
                    model.downloadsCount = downloadsCount.toLong()
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()



                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    MyApplication.loadCategory("$categoryId", holder.categoryTv)
                    MyApplication.loadPdfSinglePage(
                        "$url",
                        "$title",
                        holder.pdfView,
                        holder.progressBar,
                        null
                    )
                    MyApplication.loadPdfSize("$url", "$title", holder.sizeTv)
                    holder.descriptionTv.text = description
                    holder.dateTv.text = date
                    holder.titleTv.text = title
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

    override fun getItemCount(): Int {
        return booksArrayList.size
    }
    inner class HolderPdfReserved(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var unReserveBtn = binding.unReserveBtn
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }
}