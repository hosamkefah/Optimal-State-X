package com.example.Optimal_State_X

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Optimal_State_X.databinding.ActivityBlueExBinding
import com.example.Optimal_State_X.databinding.ItemRecommendationBinding
//import com.example.Optimal_State_X.databinding.ActivityBlueExBinding
//import com.example.Optimal_State_X.databinding.ItemRecommendationBinding
//import com.example.loginapplication.databinding.ActivityBlueExBinding
//import com.example.loginapplication.databinding.ItemRecommendationBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class blueEx : AppCompatActivity() {
    private lateinit var binding: ActivityBlueExBinding
    private val db = Firebase.firestore
    private val TAG = "RecommendationsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting activity")
        binding = ActivityBlueExBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        setupRecyclerViews()
        fetchAllData()
    }

    private fun setupRecyclerViews() {
        Log.d(TAG, "setupRecyclerViews: Setting up adapters")

        // Initialize each RecyclerView with an empty adapter
        binding.meditationRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = RecommendationsAdapter1(isVideo = true)
        }

        binding.breathingRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = RecommendationsAdapter1(isVideo = true)
        }

        binding.musicRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = RecommendationsAdapter1(isVideo = true)
        }

        binding.booksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = RecommendationsAdapter1(isVideo = false)
        }

        binding.foodsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = RecommendationsAdapter1(isVideo = false)
        }
    }

    private fun fetchAllData() {
        Log.d(TAG, "fetchAllData: Starting to fetch all data")
        fetchVideoData("meditation")
        fetchVideoData("breathing")
        fetchVideoData("music")
        fetchDescriptionData("books")
        fetchDescriptionData("foods")
    }

    private fun fetchVideoData(collectionName: String) {
        Log.d(TAG, "fetchVideoData: Fetching data for $collectionName")

        db.collection("blue").document(collectionName)
            .get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "fetchVideoData $collectionName response: ${document.data}")

                if (document != null && document.exists()) {
                    val videoItems = document.data?.map { (name, url) ->
                        Log.d(TAG, "$collectionName item: name=$name, url=$url")
                        Pair(name, url.toString())
                    } ?: listOf()

                    Log.d(TAG, "$collectionName items count: ${videoItems.size}")

                    val recyclerView = when (collectionName) {
                        "meditation" -> binding.meditationRecyclerView
                        "breathing" -> binding.breathingRecyclerView
                        "music" -> binding.musicRecyclerView
                        else -> null
                    }

                    recyclerView?.adapter?.let { adapter ->
                        if (adapter is RecommendationsAdapter1) {
                            adapter.updateItems(videoItems)
                            Log.d(TAG, "Updated adapter for $collectionName with ${videoItems.size} items")
                        }
                    }
                } else {
                    Log.e(TAG, "Document for $collectionName does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching $collectionName: ${exception.message}", exception)
                Toast.makeText(this, "Error loading $collectionName", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchDescriptionData(collectionName: String) {
        Log.d(TAG, "fetchDescriptionData: Fetching data for $collectionName")

        db.collection("blue").document(collectionName)
            .get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "fetchDescriptionData $collectionName response: ${document.data}")

                if (document != null && document.exists()) {
                    val items = document.data?.map { (name, description) ->
                        Log.d(TAG, "$collectionName item: name=$name, description=$description")
                        Pair(name, description.toString())
                    } ?: listOf()

                    Log.d(TAG, "$collectionName items count: ${items.size}")

                    val recyclerView = when (collectionName) {
                        "books" -> binding.booksRecyclerView
                        "foods" -> binding.foodsRecyclerView
                        else -> null
                    }

                    recyclerView?.adapter?.let { adapter ->
                        if (adapter is RecommendationsAdapter1) {
                            adapter.updateItems(items)
                            Log.d(TAG, "Updated adapter for $collectionName with ${items.size} items")
                        }
                    }
                } else {
                    Log.e(TAG, "Document for $collectionName does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching $collectionName: ${exception.message}", exception)
                Toast.makeText(this, "Error loading $collectionName", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

class RecommendationsAdapter1(
    private var items: List<Pair<String, String>> = listOf(),
    private val isVideo: Boolean = false
) : RecyclerView.Adapter<RecommendationsAdapter1.ViewHolder>() {

    private val TAG = "RecommendationsAdapter1"

    inner class ViewHolder(private val binding: ItemRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<String, String>) {
            Log.d(TAG, "Binding item: ${item.first}")
            binding.titleText.text = item.first
            binding.descriptionText.text = if (isVideo) "Click to watch video" else item.second

            if (isVideo) {
                binding.root.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.second))
                    binding.root.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, "Creating new ViewHolder")
        val binding = ItemRecommendationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "Binding ViewHolder at position $position")
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Pair<String, String>>) {
        Log.d(TAG, "Updating adapter with ${newItems.size} items")
        items = newItems
        notifyDataSetChanged()
    }
}