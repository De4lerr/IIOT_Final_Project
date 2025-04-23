import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class mqttHelper(private val context: Context) {

    private lateinit var mqttClient: MqttAndroidClient

    // Conecta ao broker MQTT
    fun connect(
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        try {
            mqttClient = MqttAndroidClient(
                context,
                MqttConfig.BROKER_URI, // Usa a constante do MqttConfig.kt
                MqttConfig.CLIENT_ID_PREFIX + System.currentTimeMillis()
            )

            val options = MqttConnectOptions().apply {
                userName = MqttConfig.BROKER_USERNAME
                password = MqttConfig.BROKER_PASSWORD.toCharArray()
                isCleanSession = true
                automaticReconnect = true
            }

            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Conectado com sucesso!")
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    val errorMsg = "Erro na conexão: ${exception?.message ?: "Sem detalhes"}"
                    Log.e("MQTT", errorMsg)
                    onFailure(exception ?: Throwable(errorMsg))
                }
            })

        } catch (e: MqttException) {
            Log.e("MQTT", "Exceção ao conectar: ${e.message}")
            onFailure(e)
        }
    }

    // Publica uma mensagem no tópico definido em MqttConfig.kt
    fun publish(message: String) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                qos = 1 // Garante entrega (pode ter duplicatas)
                retained = false
            }
            mqttClient.publish(MqttConfig.TOPIC_PUBLISH, mqttMessage) // Usa a constante do MqttConfig.kt
            Log.d("MQTT", "Mensagem enviada: [${MqttConfig.TOPIC_PUBLISH}] $message")
        } catch (e: Exception) {
            Log.e("MQTT", "Erro ao publicar: ${e.message}")
        }
    }

    // Desconecta do broker
    fun disconnect() {
        try {
            mqttClient.disconnect()
            Log.d("MQTT", "Desconectado")
        } catch (e: Exception) {
            Log.e("MQTT", "Erro ao desconectar: ${e.message}")
        }
    }
}
