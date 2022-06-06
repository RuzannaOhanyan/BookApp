package com.example.bookapp

import android.widget.Filter

class FilterPdfUser : Filter {

    var filterList : ArrayList<PdfModelData>
    var adapterPdfUser : AdapterPdfUser


    constructor(filterList: ArrayList<PdfModelData>, adapterPdfUser: AdapterPdfUser) : super() {
       this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {

            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<PdfModelData>()
            for (i in filterList.indices) {

                if (filterList[i].title.uppercase().contains(constraint)) {

                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels
        }
                            else {

                    results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {

        adapterPdfUser.pdfArrayList = results.values as ArrayList<PdfModelData>

                adapterPdfUser.notifyDataSetChanged()
    }
}