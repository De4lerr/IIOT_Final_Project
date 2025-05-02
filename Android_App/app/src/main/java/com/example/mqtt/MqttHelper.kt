package com.example.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttHelper(private val context: Context,
                 private val brokerUri : String) {

    private lateinit var mqttClient: MqttAndroidClient

    fun connect(
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        try {
            mqttClient = MqttAndroidClient(
                context,
                brokerUri,
                "${MqttConfig.CLIENT_ID_PREFIX}${System.currentTimeMillis()}"
            )

            val options = MqttConnectOptions().apply {
                isCleanSession = true       // Limpa sessões anteriores
                setAutomaticReconnect(true) // Reconecta automaticamente
                connectionTimeout = 30      // Tempo limite de conexão (segundos)
                keepAliveInterval = 60      // Intervalo de keep-alive (segundos)
            }

            mqttClient.setCallback(object : MqttCallback {
                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.d("MQTT", "Mensagem recebida: ${String(message.payload)}")
                }

                override fun connectionLost(cause: Throwable) {
                    Log.e("MQTT", "Conexão perdida: ${cause.message}")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    // Mensagem entregue
                }
            })

            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Conectado ao broker!")
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    val error = exception ?: Throwable("Erro desconhecido")
                    Log.e("MQTT", "Falha na conexão: ${error.message}")
                    onFailure(error)
                }
            })

        } catch (e: Exception) {
            Log.e("MQTT", "Erro ao configurar cliente: ${e.message}")
            onFailure(e)
        }
    }

    fun publish(message: String) {
        try {
            if (::mqttClient.isInitialized && mqttClient.isConnected) {
                mqttClient.publish(
                    MqttConfig.TOPIC_PUBLISH,
                    message.toByteArray(),
                    1,
                    false
                )
                Log.d("MQTT", "Mensagem enviada: $message")
            } else {
                Log.e("MQTT", "Cliente não conectado")
            }
        } catch (e: Exception) {
            Log.e("MQTT", "Erro ao publicar: ${e.message}")
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        try {
            if (::mqttClient.isInitialized && mqttClient.isConnected) {
                mqttClient.subscribe(topic, qos)
                Log.d("MQTT", "Assinado ao tópico: $topic")
            }
        } catch (e: Exception) {
            Log.e("MQTT", "Erro ao assinar tópico: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            if (::mqttClient.isInitialized && mqttClient.isConnected) {
                mqttClient.disconnect()
                Log.d("MQTT", "Desconectado")
            }
        } catch (e: Exception) {
            Log.e("MQTT", "Erro ao desconectar: ${e.message}")
        }
    }
}