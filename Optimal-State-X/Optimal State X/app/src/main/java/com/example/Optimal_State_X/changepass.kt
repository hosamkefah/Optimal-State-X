package com.example.Optimal_State_X

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivityChangepassBinding
//import com.example.Optimal_State_X.databinding.ActivityChangepassBinding
//import com.example.loginapplication.databinding.ActivityChangepassBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class changepass : AppCompatActivity() {
    private lateinit var binding: ActivityChangepassBinding
    private lateinit var auth: FirebaseAuth
    var bool1 :Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding and set the content view
        binding = ActivityChangepassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Save new password on button click
        binding.save1.setOnClickListener {
            changePassword()
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

        // Go back to the previous activity
        binding.goback2.setOnClickListener {
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

    private fun changePassword() {
        val oldPassword = binding.oldpass.text.toString().trim()
        val newPassword = binding.newpass.text.toString().trim()
        val confirmPassword = binding.confpass.text.toString().trim()

        // Check if fields are empty
        if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if new password and confirmation match
        if (newPassword != confirmPassword) {
            Toast.makeText(this, "New password and confirmation do not match", Toast.LENGTH_SHORT).show()
            return
        }


        // Get the current user
        val user = auth.currentUser
        if (user != null && user.email != null) {
            //check type



                    // Reauthenticate the user with their old password
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Update the user's password
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()

                                        val database = Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com").reference
                                        val providerRef = database.child("Users").child("Providers").child(user.uid)
                                        // Check if the user is a provider
                                        providerRef.get()
                                            .addOnSuccessListener { snapshot ->
                                                if (snapshot.exists()) {
                                                    val intent1 = Intent(this, settengP::class.java)
                                                    startActivity(intent1)
                                                    finish()
                                                } else {
                                                    val intent1 = Intent(this, settng::class.java)
                                                    startActivity(intent1)
                                                    finish()
                                                }
                                            }

                                } else {
                                    Toast.makeText(this, "Failed to change password: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show()
                    }
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
