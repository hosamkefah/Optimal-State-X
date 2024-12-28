package com.example.Optimal_State_X

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivityMainProviderBinding
//import com.example.loginapplication.databinding.ActivityMainProviderBinding
//import com.example.Optimal_State_X.databinding.ActivityMainProviderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivityProvider : AppCompatActivity() {
    private lateinit var binding: ActivityMainProviderBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main_provider)
        binding = ActivityMainProviderBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            determineUserRole(uid)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            redirectToSignIn()
        }
        binding.outBtn1.setOnClickListener {
            Firebase.auth.signOut()
            redirectToSignIn()
        }

        binding.Settings1.setOnClickListener{
            val intent1 = Intent(this, settengP::class.java)
            startActivity(intent1)
            finish()
        }

        binding.addClient.setOnClickListener{
            val intent1 = Intent(this, AddClientActivity::class.java)
            startActivity(intent1)
            finish()
        }

        binding.removeClient.setOnClickListener{
            val intent1 = Intent(this, ManageClientsActivity::class.java)
            startActivity(intent1)
            finish()
        }

        binding.viewClientsHistory.setOnClickListener{
            val intent1 = Intent(this, PatientStatesActivity::class.java)
            startActivity(intent1)
            finish()
        }
    }

    private fun determineUserRole(uid: String) {
        val database = Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com").reference

        // Check if user is a provider
        database.child("Users").child("Providers").child(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Log.d("MainActivity", "User is a Provider")
                    val provider = snapshot.getValue(Provider::class.java)
                    if (provider != null) {
                        displayProviderData(provider)
                    }
                } else {
                    // Check if user is a patient
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
        binding.welcomeText1.text = "Welcome, ${provider.firstname} ${provider.lastname}!"
    }

    private fun displayPatientData(patient: Patient) {
        binding.welcomeText1.text = "Hello, ${patient.firstname} ${patient.lastname}!"
        //binding.userDetails1.text = "Patient Info:\nUsername: ${patient.username}\nEmail: ${patient.email}\nAge: ${patient.age}"
    }
    private fun redirectToSignIn() {
        val intent = Intent(this, signinActivity::class.java)
        startActivity(intent)
        finish()
    }
}