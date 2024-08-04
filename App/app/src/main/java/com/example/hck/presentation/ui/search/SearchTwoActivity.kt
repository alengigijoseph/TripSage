package com.example.hck.presentation.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.hck.R
import com.example.hck.adapter.SearchResultsAdapter
import com.example.hck.databinding.ActivitySearchTwoBinding
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion

class SearchTwoActivity :  AppCompatActivity() , SearchResultsAdapter.OnItemClickListener{

    private lateinit var searchEngine: SearchEngine
    private var searchRequestTask: AsyncOperationTask? = null
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    lateinit var searchRv: RecyclerView
    lateinit var binding: ActivitySearchTwoBinding

    private var selectedPlace: String = ""
    private var lat: String = ""
    private var lng: String = ""

    private val searchCallback = object : SearchSuggestionsCallback {

        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
            if (suggestions.isEmpty()) {
            } else {
                searchResultsAdapter.setResults(suggestions)
            }
        }


        override fun onError(e: Exception) {
        }
    }

    val selectCallback = object : SearchSelectionCallback {
        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            lat = result.coordinate.latitude().toString()
            lng = result.coordinate.longitude().toString()
            selectedPlace = result.name
            onBackPressed()

        }

        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
        }

        override fun onResults(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
        }

        override fun onError(e: Exception) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings(getString(R.string.mapbox_access_token))
        )

        searchResultsAdapter = SearchResultsAdapter(this)
        searchRv = binding.searchResultsVieww
        searchRv.adapter = searchResultsAdapter

        // Listen for text changes in the EditText
        binding.queryEditTextt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                performSearch(query)
            }
        })

    }

    override fun onDestroy() {
        searchRequestTask?.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        val intent = Intent()
        if (selectedPlace.isEmpty()){
            setResult(RESULT_CANCELED, intent);
        }
        else{
            intent.putExtra("to", selectedPlace);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            setResult(RESULT_OK, intent);
        }
        super.onBackPressed()
    }

    private fun performSearch(query: String) {
        searchRequestTask?.cancel() // Cancel previous search request if any

        searchRequestTask = searchEngine.search(
            query,
            SearchOptions(limit = 5),
            searchCallback
        )
    }

    override fun onItemClick(searchResult: SearchSuggestion) {
        searchRequestTask = searchEngine.select(searchResult, selectCallback)

    }
}