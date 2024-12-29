package com.example.Optimal_State_X

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Optimal_State_X.databinding.ActivityPatientStatesBinding
import com.example.Optimal_State_X.databinding.ItemPatientStateBinding
//import com.example.loginapplication.databinding.ActivityPatientStatesBinding
//import com.example.loginapplication.databinding.ItemPatientStateBinding
//import com.example.Optimal_State_X.databinding.ActivityPatientStatesBinding
//import com.example.Optimal_State_X.databinding.ItemPatientStateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PatientStatesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientStatesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var patientsAdapter: PatientStatesAdapter
    private val TAG = "PatientStatesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityPatientStatesBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "Layout inflated successfully")

            auth = FirebaseAuth.getInstance()

            // Set up toolbar
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            Log.d(TAG, "Toolbar setup complete")

            // Initialize RecyclerView
            binding.patientsRecyclerView.layoutManager = LinearLayoutManager(this)
            patientsAdapter = PatientStatesAdapter(mutableListOf()) { patient ->
                openPatientStatistics(patient)
            }
            binding.patientsRecyclerView.adapter = patientsAdapter
            Log.d(TAG, "RecyclerView setup complete")

            // Show progress
            binding.progressBar.visibility = View.VISIBLE

            // Load data
            loadPatientsWithStates()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_LONG).show()
            finish()
        }


    }
    private fun openPatientStatistics(patient: PatientStateData) {
        Log.d(TAG, "Opening statistics for patient ID: ${patient.id}")
        val intent = Intent(this, ColorStatisticsActivity::class.java).apply {
            putExtra("PATIENT_ID", patient.id)
            putExtra("PATIENT_NAME", patient.name)
            putExtra("IS_PROVIDER", true)
        }
        startActivity(intent)
    }

    private fun loadPatientsWithStates() {
        try {
            val currentProviderId = auth.currentUser?.uid
            if (currentProviderId == null) {
                Log.e(TAG, "No user logged in")
                Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            val firestoreDb = Firebase.firestore
            val realtimeDb = Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com").reference

            firestoreDb.collection("proLink").document(currentProviderId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val clients = document.get("clients") as? List<String> ?: listOf()
                        Log.d(TAG, "Found ${clients.size} clients")

                        if (clients.isEmpty()) {
                            binding.progressBar.visibility = View.GONE
                            // You might want to show a message that there are no patients
                            return@addOnSuccessListener
                        }

                        val patientsList = mutableListOf<PatientStateData>()

                        clients.forEach { clientId ->
                            realtimeDb.child("Users").child("Patients").child(clientId).get()
                                .addOnSuccessListener { snapshot ->
                                    val firstname = snapshot.child("firstname").getValue(String::class.java) ?: ""
                                    val lastname = snapshot.child("lastname").getValue(String::class.java) ?: ""
                                    val email = snapshot.child("email").getValue(String::class.java) ?: ""

                                    Log.d(TAG, "Retrieved patient data: $firstname $lastname")

                                    firestoreDb.collection("state").document(clientId).get()
                                        .addOnSuccessListener { stateDoc ->
                                            try {
                                                val timestamps = stateDoc.get("timestamps") as? Map<String, String>
                                                val lastState = timestamps?.entries?.maxByOrNull { it.key }

                                                patientsList.add(
                                                    PatientStateData(
                                                        id = clientId,
                                                        name = "$firstname $lastname",
                                                        email = email,
                                                        currentState = lastState?.value ?: "Unknown",
                                                        lastUpdate = lastState?.key ?: ""
                                                    )
                                                )

                                                Log.d(TAG, "Added patient to list: ${patientsList.size}")
                                                patientsAdapter.updatePatients(patientsList)
                                                binding.progressBar.visibility = View.GONE
                                            } catch (e: Exception) {
                                                Log.e(TAG, "Error processing state data: ${e.message}", e)
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error getting state data: ${e.message}", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error getting patient data: ${e.message}", e)
                                }
                        }
                    } else {
                        Log.d(TAG, "No proLink document exists")
                        binding.progressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error loading clients: ${e.message}", e)
                    binding.progressBar.visibility = View.GONE
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in loadPatientsWithStates: ${e.message}", e)
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivityProvider::class.java)
        startActivity(intent)
        finish()
    }
}


data class PatientStateData(
    val id: String,
    val name: String,
    val email: String,
    val currentState: String,
    val lastUpdate: String
)

class PatientStatesAdapter(
    private var patients: MutableList<PatientStateData>,
    private val onPatientClick: (PatientStateData) -> Unit
) : RecyclerView.Adapter<PatientStatesAdapter.PatientViewHolder>() {

    class PatientViewHolder(
        private val binding: ItemPatientStateBinding,
        private val onPatientClick: (PatientStateData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NewApi")
        fun bind(patient: PatientStateData) {
            binding.root.setOnClickListener { onPatientClick(patient) }
            binding.patientNameText.text = patient.name
            binding.patientEmailText.text = patient.email
            binding.currentStateText.text = patient.currentState
            binding.currentStateText.setTextColor(getColorForState(patient.currentState))

            // Format timestamp
            val formattedDate = try {
                val timestamp = LocalDateTime.parse(
                    patient.lastUpdate,
                    DateTimeFormatter.ISO_DATE_TIME
                )
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            } catch (e: Exception) {
                patient.lastUpdate
            }
            binding.lastUpdateText.text = "Last updated: $formattedDate"
        }

        private fun getColorForState(state: String): Int {
            return when (state) {
                "Gold" -> Color.parseColor("#FFD700")
                "Red" -> Color.RED
                "Blue" -> Color.BLUE
                "White" -> Color.GRAY
                else -> Color.BLACK
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientViewHolder(binding, onPatientClick)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patients[position])
    }

    override fun getItemCount() = patients.size

    fun updatePatients(newPatients: List<PatientStateData>) {
        patients.clear()
        patients.addAll(newPatients)
        notifyDataSetChanged()
    }
}