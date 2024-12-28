package com.example.Optimal_State_X

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivityChangeInfoBinding
//import com.example.Optimal_State_X.databinding.ActivityChangeInfoBinding
//import com.example.loginapplication.databinding.ActivityChangeInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class change_info : AppCompatActivity() {
    private lateinit var binding: ActivityChangeInfoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    var bool1 :Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding and set the content view
        binding = ActivityChangeInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication and Database
        auth = FirebaseAuth.getInstance()
        database =
            Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com").reference

        // Fetch current user data
        loadUserInfo()

        // Save changes on button click
        binding.save1.setOnClickListener {
            saveUserInfo()
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
        binding.goback.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadUserInfo() {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            // Check if the user is a Provider or Patient
            database.child("Users").child("Providers").child(uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // User is a Provider
                    val firstName = snapshot.child("firstname").value.toString()
                    val lastName = snapshot.child("lastname").value.toString()
                    val username = snapshot.child("username").value.toString()

                    // Populate the fields
                    binding.firstName1.setText(firstName)
                    binding.lastName1.setText(lastName)
                    binding.username1.setText(username)
                    binding.firstName1.tag = "Providers" // Save the user type for later
                } else {
                    // Check if the user is a Patient
                    database.child("Users").child("Patients").child(uid).get().addOnSuccessListener { patientSnapshot ->
                        if (patientSnapshot.exists()) {
                            val firstName = patientSnapshot.child("firstname").value.toString()
                            val lastName = patientSnapshot.child("lastname").value.toString()
                            val username = patientSnapshot.child("username").value.toString()

                            // Populate the fields
                            binding.firstName1.setText(firstName)
                            binding.lastName1.setText(lastName)
                            binding.username1.setText(username)
                            binding.firstName1.tag = "Patients" // Save the user type for later
                        } else {
                            Toast.makeText(this, "User type not found in database", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to check patient data", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to check provider data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserInfo() {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            val updatedFirstName = binding.firstName1.text.toString().trim()
            val updatedLastName = binding.lastName1.text.toString().trim()
            val updatedUsername = binding.username1.text.toString().trim()

            if (updatedFirstName.isBlank() || updatedLastName.isBlank() || updatedUsername.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }

            // Get the user type from the tag set in loadUserInfo
            val userType = binding.firstName1.tag?.toString() ?: return

            // Create a map for the updated data
            val updatedData = mapOf(
                "firstname" to updatedFirstName,
                "lastname" to updatedLastName,
                "username" to updatedUsername
            )

            // Update the user data in the correct node
            database.child("Users").child(userType).child(uid).updateChildren(updatedData).addOnSuccessListener {
                Toast.makeText(this, "Information updated successfully", Toast.LENGTH_SHORT).show()
                if(userType!="Patients"){
                val intent1 = Intent(this, settengP::class.java)
                startActivity(intent1)
                finish()
                } else{
                    val intent1 = Intent(this, settng::class.java)
                    startActivity(intent1)
                    finish()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to update information", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
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
