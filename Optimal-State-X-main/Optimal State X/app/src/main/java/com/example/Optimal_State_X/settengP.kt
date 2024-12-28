package com.example.Optimal_State_X

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.Optimal_State_X.databinding.ActivitySettengPBinding
//import com.example.loginapplication.databinding.ActivitySettengPBinding

//import com.example.Optimal_State_X.databinding.ActivitySettengPBinding

class settengP : AppCompatActivity() {
    private lateinit var binding: ActivitySettengPBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettengPBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.returnBtn.setOnClickListener{
            val intent = Intent(this, MainActivityProvider::class.java)
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
        val intent = Intent(this, MainActivityProvider::class.java)
        startActivity(intent)
        finish()
    }
}
