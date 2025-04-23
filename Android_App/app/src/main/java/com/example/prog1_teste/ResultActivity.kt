package com.example.prog1_teste

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var txtResult: TextView
    private lateinit var tvSetpointValue: TextView
    private lateinit var tvStatus: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var switchAntiWindup: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val controllerType = intent.getStringExtra("controller_type")
        val kcr = intent.getDoubleExtra("kcr", 0.0)
        val tcr = intent.getDoubleExtra("tcr", 0.0)

        txtResult = findViewById(R.id.txtResult)
        tvSetpointValue = findViewById(R.id.tv_setpoint_value)
        tvStatus = findViewById(R.id.tv_status)
        seekBar = findViewById(R.id.sb_setpoint)
        switchAntiWindup = findViewById(R.id.sw_anti_windup)

        // Cálculo dos parâmetros PID
        val (kp, Ti, Td) = calcularParametrosPID(controllerType ?: "", kcr, tcr)
        txtResult.text = "Kp = $kp\nTi = $Ti\nTd = $Td"

        // Configurações da SeekBar (-3.14 a 3.14)
        seekBar.min = -3.14
        seekBar.max = 3.14
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSetpointValue.text = "Ref. Posição (Potenciômetro): $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Monitorar estado do Anti-Windup
        switchAntiWindup.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvStatus.text = "Anti-Windup: Ligado"
            } else {
                tvStatus.text = "Anti-Windup: Desligado"
            }
        }
    }

    private fun calcularParametrosPID(tipo: String, kcr: Double, tcr: Double): Triple<Double, Double, Double> {
        return when (tipo) {
            "P" -> Triple(0.5 * kcr, 0.0, 0.0)
            "PID" -> Triple(0.6 * kcr, 1.2 * kcr / tcr, 0.075 * kcr * tcr)
            else -> Triple(0.0, 0.0, 0.0)
        }
    }
}
