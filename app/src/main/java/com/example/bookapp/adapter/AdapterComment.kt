package com.example.bookapp

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapp.databinding.RowCommentBinding
import com.example.myproject.ModelComment

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment : RecyclerView.Adapter<AdapterComment.HolderComment>{

    val context: Context
    val comnentArrayList: ArrayList<ModelComment>
    private lateinit var binding: RowCommentBinding
    private lateinit var auth: FirebaseAuth
    constructor(context: Context, comnentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.comnentArrayList = comnentArrayList

        auth= FirebaseAuth.getInstance()
    }

        inner class HolderComment (itemView: View): RecyclerView.ViewHolder(itemView) {

            val profileIv = binding.profileIv
            val nameTv =  binding.nameTv
            val dateTv = binding.dateTv
            val commentTv =  binding.commentTv


        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context),parent, false)
        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {

        val model = comnentArrayList[position]
        val  id = model.id
        val bookId = model.bookId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        val date =  MyApplication.formatTimeStamp(timestamp.toLong())

                holder.dateTv.text =  date
        holder.commentTv.text =  comment
                loadUserDetails (model, holder)
        holder.itemView.setOnClickListener {
            if (auth.currentUser != null && auth.uid==uid ) {
                deleteCommentDialog(model, holder)
            }}


    }

    private fun deleteCommentDialog(model: ModelComment, holder: HolderComment) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Ջնջել մեկնաբանությունը")
            .setMessage("Իսկապե՞ս ուզում եք ջնջել այս մեկնաբանությունը:")
            .setPositiveButton( "ՋՆՋԵԼ") { d, e ->
                val bookId = model.bookId
                val commentId = model.id

                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Ջնջված է!!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Չհաջողվեց ջնջել, քանի որ ${e.message}", Toast.LENGTH_SHORT).show()


                    }
            }
                .setNegativeButton( "ՉԵՂԱՐԿԵԼ") { d, e ->
                    d.dismiss()
                }

            .show()

    }

    private fun loadUserDetails(model: ModelComment, holder: HolderComment) {

        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference( "Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange (snapshot: DataSnapshot) {

                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    holder.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileIv)
                    } catch (e: Exception) {

                        holder.profileIv.setImageResource(R.drawable.ic_person_gray)
                    }
                }
                        override fun onCancelled(error: DatabaseError) {

                        }
                        })

    }

    override fun getItemCount(): Int {
        return comnentArrayList.size
    }

}