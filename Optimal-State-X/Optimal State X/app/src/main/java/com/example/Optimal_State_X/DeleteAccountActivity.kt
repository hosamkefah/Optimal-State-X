package com.example.Optimal_State_X

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivityDeleteAccountBinding
//import com.example.loginapplication.databinding.ActivityDeleteAccountBinding
//import com.example.Optimal_State_X.databinding.ActivityDeleteAccountBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DeleteAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteAccountBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        binding.deleteAccountBtn.setOnClickListener {
            val password = binding.passwordInput.text.toString().trim()

            if (password.isEmpty()) {
                binding.passwordInput.error = "Password is required"
                return@setOnClickListener
            }

            // Show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Confirm Account Deletion")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes, Delete") { _, _ ->
                    deleteAccount(password)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun deleteAccount(password: String) {
        binding.progressBar.visibility = View.VISIBLE

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            return
        }

        // Re-authenticate user before deleting account
        val credential = EmailAuthProvider.getCredential(user.email ?: "", password)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Delete user data from database
                val database = Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com")
                    .reference
                    .child("Users")

                // Check and delete from both Providers and Patients nodes
                database.child("Providers").child(user.uid).removeValue()
                database.child("Patients").child(user.uid).removeValue()
                    .addOnSuccessListener {
                        // After database deletion, delete the auth account
                        user.delete()
                            .addOnSuccessListener {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show()

                                // Navigate to sign in screen
                                val intent = Intent(this, signinActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, "Failed to delete account: ${exception.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Failed to delete user data: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Authentication failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}