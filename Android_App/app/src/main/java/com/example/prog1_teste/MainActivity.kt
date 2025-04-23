package com.example.prog1_teste

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.RadioGroup

class MainActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        radioGroup = findViewById(R.id.radioGroup)
        btnNext = findViewById(R.id.btnNext)

        btnNext.setOnClickListener {
            val selectedType = when (radioGroup.checkedRadioButtonId) {
                R.id.radioP -> "P"
                R.id.radioPID -> "PID"
                else -> ""
            }

            val intent = Intent(this, ConexaoActivity::class.java)
            intent.putExtra("controller_type", selectedType)
            startActivity(intent)
        }
    }
}