object MqttConfig {
    // Broker (servidor MQTT)
    const val BROKER_URI = "tcp://200.19.144.16:1883" // ou "ssl://..." para TLS
    const val BROKER_USERNAME = ""
    const val BROKER_PASSWORD = ""

    // Tópicos
    const val TOPIC_PUBLISH = "/lab_pid" 

    // Configurações do cliente
    const val CLIENT_ID_PREFIX = "android-client-"
}
