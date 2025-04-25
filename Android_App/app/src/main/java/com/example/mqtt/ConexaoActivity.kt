package com.example.mqtt


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConexaoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conexao)

        val edtIp = findViewById<EditText>(R.id.edtIp)
        val edtPorta = findViewById<EditText>(R.id.edtPorta)
        val btnAvancar = findViewById<Button>(R.id.btnAvancar)
        val controllerType = intent.getStringExtra("controller_type")

        btnAvancar.setOnClickListener {
            val ip = edtIp.text.toString()
            val porta = edtPorta.text.toString()

            if (ip.isNotEmpty() && porta.isNotEmpty()) {
                // Monta a URI do broker
                val brokerUri = "tcp://$ip:$porta"
                val intent = Intent(this, InputParametersActivity::class.java).apply {
                    putExtra("BROKER_URI",brokerUri )
                    putExtra("controller_type", controllerType)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Preencha IP e porta!", Toast.LENGTH_SHORT).show()
            }
           // val intent = Intent(this, InputParametersActivity::class.java)
            //intent.putExtra("broker_ip", ip)
            //intent.putExtra("mqtt_topic", porta)
            //intent.putExtra("controller_type", controllerType)
            //startActivity(intent)
        }
    }
}