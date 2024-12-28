package com.example.Optimal_State_X

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivityViewInfoBinding
//import com.example.loginapplication.R
//import com.example.loginapplication.databinding.ActivityViewInfoBinding
//import com.example.Optimal_State_X.databinding.ActivityViewInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class viewInfo : AppCompatActivity() {
    private lateinit var binding: ActivityViewInfoBinding
    private lateinit var auth: FirebaseAuth
    private var uid: String = ""
    var bool1 :Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_info)
        binding = ActivityViewInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        if (currentUser != null) {
            uid = currentUser.uid
            determineUserRole(uid)
        }
        val user1 = auth.currentUser
        if (user1 != null) {
            val database =
                Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com").reference
            val providerRef = database.child("Users").child("Providers").child(user1.uid)
            // Check if the user is a provider
            providerRef.get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        bool1 = true
                    } else {
                        bool1 = false
                    }
                }
        }
        binding.returnBtn.setOnClickListener {
            onBackPressed()
            finish()
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
        binding.welcomeText.text = "First Name: ${provider.firstname} \nLast Name: ${provider.lastname}"
        binding.userDetails.text = "Username: ${provider.username}\nEmail: ${provider.email}\nAge: ${provider.age}"
    }

    private fun displayPatientData(patient: Patient) {
        binding.welcomeText.text = "First Name: ${patient.firstname}\nLast Name: ${patient.lastname}"
        binding.userDetails.text = "Username: ${patient.username}\nEmail: ${patient.email}\nAge: ${patient.age}"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(bool1){
            val intent1 = Intent(this, settengP::class.java)
            startActivity(intent1)
            finish()
        } else {
            val intent1 = Intent(this, settng::class.java)
            startActivity(intent1)
            finish()
        }
    }
}