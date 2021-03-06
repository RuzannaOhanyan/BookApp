package com.example.bookapp

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.github.barteksc.pdfviewer.PDFView

class PdfAdminAdapter(var dataList:ArrayList<PdfModelData>): RecyclerView.Adapter<PdfAdminAdapter.myPdfViewHolder>(){
    class myPdfViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var pdfView=itemView.findViewById<PDFView>(R.id.pdfView)
        var progressBar=itemView.findViewById<ProgressBar>(R.id.progressBar)
        var title_tv=itemView.findViewById<TextView>(R.id.titleTv)
        var description_tv=itemView.findViewById<TextView>(R.id.description)
        var category_tv=itemView.findViewById<TextView>(R.id.category_tv)
        var size_tv=itemView.findViewById<TextView>(R.id.size_tv)
        var date_tv=itemView.findViewById<TextView>(R.id.date_tv)
        var btn_more=itemView.findViewById<ImageButton>(R.id.more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myPdfViewHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.book_layout,parent,false)
        return myPdfViewHolder(view)
    }

    override fun onBindViewHolder(holder: myPdfViewHolder, position: Int) {

        var p=dataList.get(position)
        val pdfId=p.id
        val categoryId=p.categoryId
        val title=p.title
        val description=p.description
        val pdfUrl=p.url
        val timestamp=p.timestamp
        val formatDate=MyApplication.formatTimeStamp(timestamp)


        holder.title_tv.text=title
        holder.description_tv.text=description
        holder.date_tv.text=formatDate

        MyApplication.loadCategory(categoryId,holder.category_tv)
        MyApplication.loadPdfSinglePage(pdfUrl,title , holder.pdfView,holder.progressBar,null)
        MyApplication.loadPdfSize(pdfUrl,title , holder.size_tv)

        holder.btn_more.setOnClickListener {
            optionsDialog(p,holder)
        }

        holder.itemView.setOnClickListener {
            val bundle = bundleOf(
                "bookId" to pdfId,
                "bookTitle" to title,
                "bookDescription" to description,
                "bookdate" to formatDate,
                "bookUrl" to pdfUrl,
                "bookCategoryId" to categoryId,
                )
           holder.itemView.findNavController().navigate(R.id.action_pdfListAdminFragment_to_moreDetailsPdfFragment,bundle)
        }
    }

    private fun optionsDialog(p: PdfModelData, holder: PdfAdminAdapter.myPdfViewHolder) {
        val bookId=p.id
        val bookTitle=p.title
        val bookDescription=p.description
        val bookCategoryId=p.categoryId

        val bookUrl=p.url

        val options= arrayOf("??????????????","??????????")

        val builder= AlertDialog.Builder(holder.itemView.context)
        builder.setTitle("????????")
            .setItems(options){dialog,position->
                if(position==0){

                    val bundle = bundleOf( "bookId" to bookId,
                                                "bookTitle" to bookTitle,
                                                "bookDescription" to bookDescription,
                                                "bookCategoryId" to bookCategoryId)
                    holder.itemView.findNavController().navigate(R.id.action_pdfListAdminFragment_to_pdfEditFragment,bundle)
                }
                else if(position==1){
                     MyApplication.deleteBook(holder.itemView.context,bookId,bookUrl,bookTitle)
                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}