package com.manish.demoofimagecompereason

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.datatransport.runtime.dagger.Binds
import com.manish.demoofimagecompereason.databinding.ActivityNextBinding

class NextActivity : AppCompatActivity() {
    private lateinit var binding : ActivityNextBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.retry.setOnClickListener {
            val intent = Intent(this@NextActivity, MainActivity :: class.java)
            startActivity(intent)
            finishAffinity()
        }


    }
}