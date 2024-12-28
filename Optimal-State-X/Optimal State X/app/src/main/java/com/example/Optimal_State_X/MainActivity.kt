package com.example.Optimal_State_X

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivityMainBinding
//import com.example.loginapplication.databinding.ActivityMainBinding
//import com.example.Optimal_State_X.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var uid: String = ""

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        if (currentUser != null) {
            uid = currentUser.uid
            determineUserRole(uid)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            redirectToSignIn()
        }

        binding.outBtn.setOnClickListener {
            Firebase.auth.signOut()
            redirectToSignIn()
        }

        binding.Settings.setOnClickListener {
            val intent1 = Intent(this, settng::class.java)
            startActivity(intent1)
            finish()
        }

        binding.takeAssessment.setOnClickListener {
            val intent1 = Intent(this, DisplayImageActivity::class.java)
            startActivity(intent1)
            finish()
        }

        binding.viewHistory.setOnClickListener {
            val intent1 = Intent(this, ColorStatisticsActivity::class.java)
            startActivity(intent1)
            finish()
        }

        binding.viewExcersise.setOnClickListener {
            val db = Firebase.firestore
            db.collection("state").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        Log.d("FirestoreDebug", "Document successfully retrieved: $document")

                        val timestamps = try {
                            document.get("timestamps") as? Map<String, String>
                        } catch (e: Exception) {
                            Log.e("FirestoreDebug", "Error parsing timestamps: ${e.message}")
                            null
                        }

                        if (timestamps != null) {
                            val lastStatus: String? = timestamps.maxByOrNull { entry ->
                                LocalDateTime.parse(entry.key, DateTimeFormatter.ISO_DATE_TIME)
                            }?.value

                            if (lastStatus != null) {
                                when (lastStatus) {
                                    "White" -> {
                                        val intent1 = Intent(this, whiteEx::class.java)
                                        startActivity(intent1)
                                        finish()
                                    }
                                    "Red" -> {
                                        val intent1 = Intent(this, RecommendationsActivity::class.java)
                                        startActivity(intent1)
                                        finish()
                                    }
                                    "Blue" -> {
                                        val intent1 = Intent(this, blueEx::class.java)
                                        startActivity(intent1)
                                        finish()
                                    }
                                    "Gold" -> {
                                        val intent1 = Intent(this, goldEx::class.java)
                                        startActivity(intent1)
                                        finish()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "You don't have assigned exercises", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
    }

    private fun determineUserRole(uid: String) {
        val database = Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com").reference

        database.child("Users").child("Providers").child(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Log.d("MainActivity", "User is a Provider")
                    val provider = snapshot.getValue(Provider::class.java)
                    if (provider != null) {
                        displayProviderData(provider)
                    }
                } else {
                    database.child("Users").child("Patients").child(uid).get()
                        .addOnSuccessListener { patientSnapshot ->
                            if (patientSnapshot.exists()) {
                                Log.d("MainActivity", "User is a Patient")
                                val patient = patientSnapshot.getValue(Patient::class.java)
                                if (patient != null) {
                                    displayPatientData(patient)
                                }
                            } else {
                                Log.w("MainActivity", "User role not found")
                                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("MainActivity", "Error retrieving patient data", exception)
                            Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error retrieving provider data", exception)
                Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayProviderData(provider: Provider) {
        binding.welcomeText.text = "Welcome Provider, ${provider.firstname} ${provider.lastname}!"
        //binding.userDetails.text = "Provider Info:\nUsername: ${provider.username}\nEmail: ${provider.email}\nAge: ${provider.age}"
    }

    private fun displayPatientData(patient: Patient) {
        binding.welcomeText.text = "Hello, ${patient.firstname} ${patient.lastname}!"
        //binding.userDetails.text = "Patient Info:\nUsername: ${patient.username}\nEmail: ${patient.email}\nAge: ${patient.age}"
    }

    private fun redirectToSignIn() {
        val intent = Intent(this, signinActivity::class.java)
        startActivity(intent)
        finish()
    }
}