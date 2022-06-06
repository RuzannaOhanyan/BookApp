package com.example.myproject
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.models.CategoryData
import com.example.bookapp.R
import com.example.bookapp.databinding.CategoryLayoutBinding
import com.google.firebase.database.FirebaseDatabase

class CategoryAdapter(var dataList:ArrayList<CategoryData>): RecyclerView.Adapter<CategoryAdapter.myViewHolder>(){
    private lateinit var binding: CategoryLayoutBinding
    class myViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var category=itemView.findViewById<TextView>(R.id.tv_category)
        var delete=itemView.findViewById<ImageButton>(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        var view:View=LayoutInflater.from(parent.context).inflate(R.layout.category_layout,parent,false)
        return myViewHolder(view)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        var p=dataList.get(position)
        holder.category.text=p.category
        holder.itemView.setOnClickListener {
            val bundle = bundleOf( "categoryId" to p.id,
                                          "category" to p.category)
            holder.itemView.findNavController().navigate(R.id.action_dashboardAdminFragment_to_pdfListAdminFragment,bundle)
        }
        holder.delete.setOnClickListener {

            val builder=AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Ջնջել")
                .setMessage("Դուք ջնջման գործընթացում եք")
                .setPositiveButton("Հաստատել"){_,_->

                        Toast.makeText(holder.itemView.context,"Ջնջվում է...",Toast.LENGTH_LONG).show()
                        deleteCategory(p.id,holder)
                   }
                   .setNegativeButton("ՉԵՂԱՐԿԵԼ"){a,d->
                        a.dismiss()
                   }
                .show()
        }
    }

    private fun deleteCategory(id: String,holder: myViewHolder) {
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
             ref.child(id)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(holder.itemView.context,"Հաջողությամբ ջնջվեց",Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {e->
                    Toast.makeText(holder.itemView.context,"Ջնջվելը ընդհատվել ${e.message} պատճառով",Toast.LENGTH_LONG).show()
                }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}