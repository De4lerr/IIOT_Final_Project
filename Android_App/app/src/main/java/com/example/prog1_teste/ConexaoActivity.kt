package com.example.prog1_teste

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class ConexaoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conexao)

        val edtIp = findViewById<EditText>(R.id.edtIp)
        val edtTopico = findViewById<EditText>(R.id.edtTopico)
        val btnAvancar = findViewById<Button>(R.id.btnAvancar)
        val controllerType = intent.getStringExtra("controller_type")

        btnAvancar.setOnClickListener {
            val ip = edtIp.text.toString()
            val topico = edtTopico.text.toString()

            val intent = Intent(this, InputParametersActivity::class.java)
            intent.putExtra("broker_ip", ip)
            intent.putExtra("mqtt_topic", topico)
            intent.putExtra("controller_type", controllerType)
            startActivity(intent)
        }
    }
}