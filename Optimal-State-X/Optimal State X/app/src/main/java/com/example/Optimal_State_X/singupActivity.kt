package com.example.Optimal_State_X

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivitySingupBinding
//import com.example.loginapplication.databinding.ActivitySingupBinding
//import com.example.Optimal_State_X.databinding.ActivitySingupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class singupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySingupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database("https://optimal-state-x-8b723-default-rtdb.firebaseio.com")
            .reference
            .child("Users")

        // Check if user is already signed in and email is verified
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (!currentUser.isEmailVerified) {
                // If email is not verified, sign out the user and show message
                auth.signOut()
                Toast.makeText(
                    this,
                    "Please verify your email before logging in",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, signinActivity::class.java)
                startActivity(intent)
                finish()
                return
            }

            // Only proceed to check user role if email is verified
            val providerRef = database.child("Providers").child(currentUser.uid)
            providerRef.get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val intent = Intent(this, MainActivityProvider::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            return
        }

        setupBirthdayPicker()

        binding.continueBtn.setOnClickListener {
            val firstname = binding.firstName.text.toString().trim()
            val lastname = binding.lastName.text.toString().trim()
            val username = binding.username.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val isProvider = binding.isProviderCheckBox.isChecked
            val birthday = binding.birthdayInput.text.toString().trim()
            val age = calculateAge(selectedYear, selectedMonth, selectedDay)

            if (firstname.isBlank() || lastname.isBlank() || username.isBlank() ||
                email.isBlank() || password.isBlank() || birthday.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Verification email sent to $email",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to send verification email: ${verificationTask.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                        val uid = user?.uid
                        if (uid != null) {
                            if (isProvider) {
                                val provider = Provider(firstname, lastname, username, email, age)
                                database.child("Providers").child(uid).setValue(provider)
                                    .addOnSuccessListener {
                                        binding.progressBar.visibility = View.GONE
                                        showVerificationDialog()
                                    }
                                    .addOnFailureListener {
                                        binding.progressBar.visibility = View.GONE
                                        Toast.makeText(this, "Failed to save provider data", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                val patient = Patient(firstname, lastname, username, email, age)
                                database.child("Patients").child(uid).setValue(patient)
                                    .addOnSuccessListener {
                                        binding.progressBar.visibility = View.GONE
                                        showVerificationDialog()
                                    }
                                    .addOnFailureListener {
                                        binding.progressBar.visibility = View.GONE
                                        Toast.makeText(this, "Failed to save patient data", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        binding.move.setOnClickListener {
            val intent = Intent(this, signinActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showVerificationDialog() {
        val intent = Intent(this, signinActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(
            this,
            "Please verify your email before logging in",
            Toast.LENGTH_LONG
        ).show()
        finish()
    }

    private fun setupBirthdayPicker() {
        val calendar = Calendar.getInstance()

        binding.birthdayInput.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    this.selectedYear = selectedYear
                    this.selectedMonth = selectedMonth
                    this.selectedDay = selectedDay

                    // Format selected date and display it
                    val birthday = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                    binding.birthdayInput.setText(birthday)
                },
                year,
                month,
                day
            )

            datePicker.show()
        }
    }

    private fun calculateAge(year: Int, month: Int, day: Int): Int {
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - year

        if (today.get(Calendar.DAY_OF_YEAR) < Calendar.getInstance().apply {
                set(year, month, day)
            }.get(Calendar.DAY_OF_YEAR)
        ) {
            age--
        }

        return age
    }
}



