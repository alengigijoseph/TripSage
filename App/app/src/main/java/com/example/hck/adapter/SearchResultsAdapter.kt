package com.example.hck.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hck.R
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion

class SearchResultsAdapter(private val listener: OnItemClickListener) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {

    private var searchResults: List<SearchSuggestion> = ArrayList()

    interface OnItemClickListener {
        fun onItemClick(searchResult: SearchSuggestion)
    }

    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        //val addressTextView: TextView = itemView.findViewById(R.id.address_text_view)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val result = searchResults[position]
                listener.onItemClick(result)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val result = searchResults[position]
        holder.titleTextView.text = result.name
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    fun setResults(results: List<SearchSuggestion>) {
        searchResults = results
        notifyDataSetChanged()
    }
}
