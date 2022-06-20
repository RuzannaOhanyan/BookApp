package com.example.bookapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bookapp.`object`.Constants
import com.example.bookapp.databinding.FragmentPdfViewerBinding
import com.google.firebase.storage.FirebaseStorage

class PdfViewerFragment : Fragment() {
    private lateinit var binding: FragmentPdfViewerBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPdfViewerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var url=arguments?.getString("bookUrl").toString()
        loadBookFromStorage(url)
    }

    private fun loadBookFromStorage(bookUrl: String) {
        val TAG="LOAD_PDF"
        val ref= FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        ref.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                binding.pdfViewer.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange{page,pageCount->
                        val currentPage=page+1
                        binding.Pages.text="$currentPage/$pageCount"
                        Log.d(TAG,"Հաջողությամբ բեռնված էջը $currentPage/$pageCount :)")
                    }
                    .onError {
                        Log.d(TAG,"Սխալ՝ պայմանավորված ${it.message} :(")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG,"Սխալ բեռնելիս ${page} պատճառով ${t.message} :(")
                    }
                    .load()
                binding.progressBarViewer.visibility= View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this.context,"Կա սխալ", Toast.LENGTH_LONG).show()
                Log.d(TAG,"Գիրք ստանալու սխալի ${it.message} պատճառով :(")
                binding.progressBarViewer.visibility= View.GONE
            }
    }
}