package com.example.prog1_teste

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

class InputParametersActivity : AppCompatActivity() {

    private lateinit var edtKcr: EditText
    private lateinit var edtTcr: EditText
    private lateinit var btnCalculate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_parameters)

        val controllerType = intent.getStringExtra("controller_type")

        edtKcr = findViewById(R.id.edtKcr)
        edtTcr = findViewById(R.id.edtTcr)
        btnCalculate = findViewById(R.id.btnCalculate)

        btnCalculate.setOnClickListener {
            val kcr = edtKcr.text.toString().toDouble()
            val tcr = edtTcr.text.toString().toDouble()

            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("controller_type", controllerType)
            intent.putExtra("kcr", kcr)
            intent.putExtra("tcr", tcr)
            startActivity(intent)
        }
    }
}