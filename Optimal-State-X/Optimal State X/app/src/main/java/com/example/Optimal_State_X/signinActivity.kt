package com.example.Optimal_State_X

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivitySigninBinding
//import com.example.loginapplication.databinding.ActivitySigninBinding
//import com.example.Optimal_State_X.databinding.ActivitySigninBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class signinActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Forgot password listener
        binding.forgotPassword.setOnClickListener {
            val email = binding.email.text.toString().trim()

            if (email.isBlank()) {
                binding.email.error = "Please enter your email first"
                binding.email.requestFocus()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Password reset link sent to your email",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to send reset email: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Sign-in button listener
        binding.continueBtn.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading indicator
            binding.progressBar.visibility = View.VISIBLE

            // Attempt to sign in
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            // Fetch user data from database
                            determineUserRole(currentUser.uid)
                        } else {
                            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("AuthError", task.exception?.message.toString())
                        Toast.makeText(
                            this,
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.progressBar.visibility = View.GONE
                }
        }

        // Move to signupActivity
        binding.move.setOnClickListener {
            val intent = Intent(this, singupActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.progressBar.visibility = View.VISIBLE
            determineUserRole(currentUser.uid)
        }
    }

    // Determine if the user is a Provider or a Patient
    private fun determineUserRole(uid: String) {
        val currentUser = auth.currentUser
        if (currentUser == null || !currentUser.isEmailVerified) {
            binding.progressBar.visibility = View.GONE
            auth.signOut()
            Toast.makeText(
                this,
                "Please verify your email before logging in",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val database = Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com").reference
        val providerRef = database.child("Users").child("Providers").child(uid)

        providerRef.get()
            .addOnSuccessListener { snapshot ->
                binding.progressBar.visibility = View.GONE
                if (snapshot.exists()) {
                    navigateToMainActivityProvider()
                } else {
                    navigateToMainActivity()
                }
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                Log.e("UserRole", "Error retrieving provider data", exception)
                Toast.makeText(this, "Error checking provider role", Toast.LENGTH_SHORT).show()
            }
    }

    // Navigate to MainActivity and pass the user role
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivityProvider() {
        val intent = Intent(this, MainActivityProvider::class.java)
        startActivity(intent)
        finish()
    }
}