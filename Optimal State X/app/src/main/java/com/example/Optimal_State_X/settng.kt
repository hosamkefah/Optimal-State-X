package com.example.Optimal_State_X

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivitySettngBinding
//import com.example.loginapplication.databinding.ActivitySettngBinding

//import com.example.Optimal_State_X.databinding.ActivitySettngBinding

class settng : AppCompatActivity() {
    private lateinit var binding: ActivitySettngBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettngBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.returnBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.changeNameBtn.setOnClickListener{
            val intent = Intent(this, change_info::class.java)
            startActivity(intent)
            finish()
        }

        binding.chnagePassBtn.setOnClickListener{
            val intent = Intent(this, changepass::class.java)
            startActivity(intent)
            finish()
        }

        binding.setNotificationsBtn.setOnClickListener{
            val intent1 = Intent(this, NotificationSchedulerActivity::class.java)
            startActivity(intent1)
            finish()
        }

        binding.viewInfor.setOnClickListener{
            val intent = Intent(this, viewInfo::class.java)
            startActivity(intent)
            finish()
        }

        binding.deleteAcc.setOnClickListener{
            val intent = Intent(this, DeleteAccountActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}