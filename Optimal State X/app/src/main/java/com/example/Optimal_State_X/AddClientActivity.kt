package com.example.Optimal_State_X

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivityAddClientBinding
//import com.example.Optimal_State_X.databinding.ActivityAddClientBinding
//import com.example.loginapplication.databinding.ActivityAddClientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddClientActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddClientBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "AddClientActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.addClientButton.setOnClickListener {
            binding.addClientButton.isEnabled = false
            val clientEmail = binding.clientEmailInput.text.toString().trim()
            if (clientEmail.isNotEmpty()) {
                findClientByEmail(clientEmail)
            } else {
                Toast.makeText(this, "Please enter client email", Toast.LENGTH_SHORT).show()
                binding.addClientButton.isEnabled = true
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun findClientByEmail(clientEmail: String) {
        val realtimeDb = Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com").reference
        val patientsRef = realtimeDb.child("Users").child("Patients")

        Log.d(TAG, "Searching for client with email: $clientEmail")

        patientsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var clientId: String? = null

                for (patientSnapshot in snapshot.children) {
                    val email = patientSnapshot.child("email").getValue(String::class.java)
                    if (email == clientEmail) {
                        clientId = patientSnapshot.key
                        break
                    }
                }

                if (clientId != null) {
                    Log.d(TAG, "Found client with ID: $clientId")
                    checkExistingLink(clientId)
                } else {
                    Log.d(TAG, "No client found with email: $clientEmail")
                    runOnUiThread {
                        Toast.makeText(baseContext, "No client found with this email", Toast.LENGTH_SHORT).show()
                        binding.addClientButton.isEnabled = true
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                runOnUiThread {
                    Toast.makeText(baseContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    binding.addClientButton.isEnabled = true
                }
            }
        })
    }

    private fun checkExistingLink(clientId: String) {
        val firestoreDb = Firebase.firestore

        firestoreDb.collection("proLink")
            .get()
            .addOnSuccessListener { documents ->
                var isLinked = false
                for (document in documents) {
                    val clients = document.get("clients") as? List<*>
                    if (clients?.contains(clientId) == true) {
                        isLinked = true
                        break
                    }
                }

                if (isLinked) {
                    runOnUiThread {
                        Toast.makeText(baseContext, "This client is already linked", Toast.LENGTH_LONG).show()
                        binding.addClientButton.isEnabled = true
                    }
                } else {
                    linkClientWithProvider(clientId)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking existing links", e)
                runOnUiThread {
                    Toast.makeText(baseContext, "Error checking existing links: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.addClientButton.isEnabled = true
                }
            }
    }

    private fun linkClientWithProvider(clientId: String) {
        val currentProviderId = auth.currentUser?.uid
        if (currentProviderId == null) {
            runOnUiThread {
                Toast.makeText(this, "Provider not logged in", Toast.LENGTH_SHORT).show()
                binding.addClientButton.isEnabled = true
            }
            return
        }

        val firestoreDb = Firebase.firestore
        val proLinkRef = firestoreDb.collection("proLink").document(currentProviderId)

        proLinkRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    proLinkRef.update("clients", FieldValue.arrayUnion(clientId))
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully added client to existing proLink")
                            Toast.makeText(baseContext, "Client successfully linked!", Toast.LENGTH_SHORT).show()
                            onBackPressed()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating proLink", e)
                            runOnUiThread {
                                Toast.makeText(baseContext, "Failed to link client: ${e.message}", Toast.LENGTH_SHORT).show()
                                binding.addClientButton.isEnabled = true
                            }
                        }
                } else {
                    val data = hashMapOf(
                        "clients" to listOf(clientId)
                    )
                    proLinkRef.set(data)
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully created new proLink document")
                            runOnUiThread {
                                Toast.makeText(baseContext, "Client successfully linked!", Toast.LENGTH_SHORT).show()
                                onBackPressed()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error creating proLink document", e)
                            runOnUiThread {
                                Toast.makeText(baseContext, "Failed to link client: ${e.message}", Toast.LENGTH_SHORT).show()
                                binding.addClientButton.isEnabled = true
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error accessing proLink document", e)
                runOnUiThread {
                    Toast.makeText(baseContext, "Error accessing provider document: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.addClientButton.isEnabled = true
                }
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent1 = Intent(this, MainActivityProvider::class.java)
        startActivity(intent1)
        finish()
    }
}