package com.example.mqtt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var mqttHelper: MqttHelper
    private lateinit var txtResult: TextView
    private lateinit var tvSetpointValue: TextView
    private lateinit var tvStatus: TextView
    private lateinit var seekBarTheta: SeekBar
    private lateinit var switchAntiWindup: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val brokerUri = intent.getStringExtra("BROKER_URI") ?: run {
            Toast.makeText(this, "Broker não configurado!", Toast.LENGTH_SHORT).show()
            finish() // Fecha a Activity se não houver URI
            return
        }
        mqttHelper = MqttHelper(this, brokerUri)
        val controllerType = intent.getStringExtra("controller_type")
        val kcr = intent.getDoubleExtra("kcr", 0.0)
        val tcr = intent.getDoubleExtra("tcr", 0.0)


        txtResult = findViewById(R.id.txtResult)
        tvSetpointValue = findViewById(R.id.tv_setpoint_value)
        tvStatus = findViewById(R.id.tv_status)
        seekBarTheta = findViewById(R.id.sb_setpoint)
        switchAntiWindup = findViewById(R.id.sw_anti_windup)

        // Cálculo dos parâmetros PID
        val (kp, Ti, Td) = calcularParametrosPID(controllerType ?: "", kcr, tcr)
        txtResult.text = "Kp = $kp\nTi = $Ti\nTd = $Td"


        setupSeekBar()

        // Monitorar estado do Anti-Windup
        switchAntiWindup.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvStatus.text = "Anti-Windup: Ligado"
            } else {
                tvStatus.text = "Anti-Windup: Desligado"
            }
        }

        //val intent = Intent(this, PublishActivity::class.java).apply {
        //  putExtra("KCR", 10.0)       // Exemplo: valor de Kcr
        //putExtra("TCR", 5.0)        // Exemplo: valor de Tcr
        //putExtra("TIPO", "PID")     // "P" ou "PID"
        //}
        //startActivity(intent)

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            val (kp, Ti, Td) = calcularParametrosPID(controllerType ?: "", kcr, tcr)
            val message = "KP=$kp,KI=$Ti,KD=$Td"
            mqttHelper.connect(
                onSuccess = {
                    runOnUiThread {
                        mqttHelper.publish(message)
                        Toast.makeText(this, "PID Enviado: $message", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { erro ->
                    runOnUiThread {
                        Toast.makeText(this, "Erro: ${erro.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        val swAntiWindup = findViewById<Switch>(R.id.sw_anti_windup)
        swAntiWindup.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "ANTI_WINDUP=TRUE" else "ANTI_WINDUP=FALSE"

            // Envia a mensagem via MQTT
            mqttHelper.connect(
                onSuccess = {
                    runOnUiThread {
                        mqttHelper.publish(message)
                        Toast.makeText(this, "Anti-Windup: $message", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { erro ->
                    runOnUiThread {
                        Toast.makeText(this, "Erro: ${erro.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

    }

    private fun setupSeekBar() {
        seekBarTheta.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Converte o progresso para o intervalo -3,14 a 3,14
                val mappedValue = mapProgressToRange(progress)

                // Atualiza a UI (ex: TextView)
                findViewById<TextView>(R.id.tv_setpoint_value).text =
                    "Valor: ${"%.2f".format(mappedValue)}"  // Formata com 2 casas decimais
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val mappedValue = mapProgressToRange(seekBar.progress)
                sendValueViaMQTT(mappedValue)
            }
        })
    }

    // Função para mapear 0-628 → -3,14 a 3,14
    private fun mapProgressToRange(progress: Int): Double {
        val minDesired = -3.14
        val maxDesired = 3.14
        val totalRange = maxDesired - minDesired
        return minDesired + (progress * totalRange / seekBarTheta.max)
    }

    private fun sendValueViaMQTT(value: Double) {
        val topic = "controle/setpoint"
        val message = "%.2f".format(value)  // Formata com 2 casas decimais
        mqttHelper.publish(message)
       // mqttHelper.connect(
            // onSuccess = { mqttHelper.publish(message) },
            //   onFailure = { erro ->
            //     Toast.makeText(this, "Erro: ${erro.message}", Toast.LENGTH_LONG).show()
            //}
        //)
    }


    private fun calcularParametrosPID(
        tipo: String,
        kcr: Double,
        tcr: Double
    ): Triple<Double, Double, Double> {
        return when (tipo) {
            "P" -> Triple(0.5 * kcr, 0.0, 0.0)
            "PID" -> Triple(0.6 * kcr, 1.2 * kcr / tcr, 0.075 * kcr * tcr)
            else -> Triple(0.0, 0.0, 0.0)
        }
    }
}

