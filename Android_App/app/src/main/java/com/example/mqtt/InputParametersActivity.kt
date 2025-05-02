package com.example.mqtt

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

class InputParametersActivity : AppCompatActivity() {

    private lateinit var edtKcr: EditText
    private lateinit var edtTcr: EditText
    private lateinit var btnCalculate: Button
    private lateinit var mqttHelper: MqttHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_parameters)

        val controllerType = intent.getStringExtra("controller_type")


        edtKcr = findViewById(R.id.edtKcr)
        edtTcr = findViewById(R.id.edtTcr)
        btnCalculate = findViewById(R.id.btnCalculate)

        btnCalculate.setOnClickListener {
            val brokerUri = intent.getStringExtra("BROKER_URI") ?: ""
            val kcr = edtKcr.text.toString().toDouble()
            val tcr = edtTcr.text.toString().toDouble()

            mqttHelper = MqttHelper(this, brokerUri)

            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("BROKER_URI", brokerUri)
            intent.putExtra("controller_type", controllerType)
            intent.putExtra("kcr", kcr)
            intent.putExtra("tcr", tcr)
            startActivity(intent)

            if (controllerType != null) {
                sendControllerType(controllerType)
            }


        }
    }
    private fun sendControllerType(controllerType: String) {
        val message = when (controllerType) {
            "P" -> "P=true"
            "PID" -> "PID=true"
            else -> return
        }

        mqttHelper.connect(
            onSuccess = {
                runOnUiThread {
                    mqttHelper.publish(message)
                }
            },
            onFailure = {
                }

        )
    }
}