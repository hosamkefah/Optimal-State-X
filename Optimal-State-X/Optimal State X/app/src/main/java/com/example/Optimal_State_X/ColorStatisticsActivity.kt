package com.example.Optimal_State_X

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Optimal_State_X.databinding.ActivityColorStatisticsBinding
import com.example.Optimal_State_X.databinding.ItemTimestampBinding
//import com.example.loginapplication.databinding.ActivityColorStatisticsBinding
//import com.example.loginapplication.databinding.ItemTimestampBinding
//import com.example.Optimal_State_X.databinding.ActivityColorStatisticsBinding
//import com.example.Optimal_State_X.databinding.ItemTimestampBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ColorStatisticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityColorStatisticsBinding
    private lateinit var pieChart: PieChart
    private var isProvider: Boolean = false
    private var patientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColorStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the patient ID either from intent (if provider) or from current user (if patient)
        isProvider = intent.getBooleanExtra("IS_PROVIDER", false)
        patientId = if (isProvider) {
            // Get the patient ID that was passed from PatientStatesActivity
            intent.getStringExtra("PATIENT_ID")
        } else {
            // If it's the patient themselves, use their own ID
            FirebaseAuth.getInstance().currentUser?.uid
        }

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (isProvider) {
            val patientName = intent.getStringExtra("PATIENT_NAME")
            supportActionBar?.title = "$patientName's Statistics"
        }

        pieChart = binding.pieChart
        setupPieChart()

        // Handle navigation icon click (back button)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (patientId.isNullOrEmpty()) {
            Toast.makeText(this, "Patient ID not found!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("ColorStatisticsActivity", "Fetching data for patient ID: $patientId")

        // Initialize RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data from Firestore using the patient ID
        val db = Firebase.firestore
        db.collection("state").document(patientId!!).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d("FirestoreDebug", "Document successfully retrieved: $document")

                    // Safely retrieve percentages and timestamps
                    val percentages = try {
                        document.get("percentages") as? Map<String, Double>
                    } catch (e: Exception) {
                        Log.e("FirestoreDebug", "Error parsing percentages: ${e.message}")
                        null
                    }

                    val timestamps = try {
                        document.get("timestamps") as? Map<String, String>
                    } catch (e: Exception) {
                        Log.e("FirestoreDebug", "Error parsing timestamps: ${e.message}")
                        null
                    }

                    // Display percentages and update pie chart
                    if (percentages != null) {
                        binding.goldPercentage.text = "Gold: ${"%.2f".format(percentages["Gold"] ?: 0.0)}%"
                        binding.redPercentage.text = "Red: ${"%.2f".format(percentages["Red"] ?: 0.0)}%"
                        binding.bluePercentage.text = "Blue: ${"%.2f".format(percentages["Blue"] ?: 0.0)}%"
                        binding.whitePercentage.text = "White: ${"%.2f".format(percentages["White"] ?: 0.0)}%"

                        // Update pie chart with actual data
                        updatePieChartData(percentages)
                    } else {
                        Log.e("FirestoreDebug", "Percentages map is null or invalid")
                        Toast.makeText(this, "Failed to load percentages", Toast.LENGTH_SHORT).show()
                    }

                    // Process timestamps
                    if (timestamps != null) {
                        val timestampList = timestamps.map { Pair(it.key, it.value) }.sortedBy { it.first }
                        val adapter = TimestampsAdapter(timestampList)
                        binding.recyclerView.adapter = adapter
                    } else {
                        Log.e("FirestoreDebug", "Timestamps map is null or invalid")
                        Toast.makeText(this, "Failed to load timestamps", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("FirestoreDebug", "Document does not exist for patient ID: $patientId")
                    Toast.makeText(this, "No data found for this patient", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreDebug", "Failed to retrieve data: ${exception.message}")
                Toast.makeText(this, "Failed to retrieve data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePieChartData(percentages: Map<String, Double>) {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(percentages["Gold"]?.toFloat() ?: 0f, "Gold"))
        entries.add(PieEntry(percentages["Red"]?.toFloat() ?: 0f, "Red"))
        entries.add(PieEntry(percentages["Blue"]?.toFloat() ?: 0f, "Blue"))
        entries.add(PieEntry(percentages["White"]?.toFloat() ?: 0f, "White"))

        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#FFD700")) // Gold
        colors.add(Color.parseColor("#FF4444")) // Red
        colors.add(Color.parseColor("#2196F3")) // Blue
        colors.add(Color.parseColor("#757575")) // Gray for White

        val dataSet = PieDataSet(entries, "Colors")
        dataSet.apply {
            this.colors = colors
            sliceSpace = 3f
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(pieChart)
        }

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawEntryLabels(false)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            legend.isEnabled = false
            setTouchEnabled(true)
            isRotationEnabled = true
        }
    }

    // RecyclerView Adapter as an inner class
    inner class TimestampsAdapter(private val items: List<Pair<String, String>>) :
        RecyclerView.Adapter<TimestampsAdapter.ViewHolder>() {

        @SuppressLint("NewApi")
        private val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
        @SuppressLint("NewApi")
        private val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd - HH:mm")

        inner class ViewHolder(private val binding: ItemTimestampBinding) :
            RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("NewApi")
            fun bind(timestamp: String, region: String) {
            // Parse the ISO timestamp and format it to the desired pattern
            val formattedTimestamp = try {
                val dateTime = LocalDateTime.parse(timestamp, inputFormatter)
                dateTime.format(outputFormatter)
            } catch (e: Exception) {
                Log.e("TimestampFormat", "Error formatting timestamp: ${e.message}")
                timestamp // Fallback to original timestamp if parsing fails
            }

            binding.timestampText.text = formattedTimestamp
            binding.regionText.text = region
            binding.regionText.setTextColor(getColorForRegion(region))
        }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemTimestampBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (timestamp, region) = items[position]
            holder.bind(timestamp, region)
        }

        override fun getItemCount(): Int = items.size

        private fun getColorForRegion(region: String): Int {
            return when (region) {
                "Gold" -> Color.parseColor("#FFD700")
                "Red" -> Color.RED
                "Blue" -> Color.BLUE
                "White" -> Color.GRAY  // Changed from WHITE to GRAY for visibility
                else -> Color.BLACK
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (isProvider) {
            finish() // This will return to PatientStatesActivity
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}