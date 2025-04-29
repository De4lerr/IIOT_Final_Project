#define ENC_A 22
#define ENC_B 23
#define PWM   18
#define H_IN1 19
#define H_IN2 21

#include <Arduino.h>

//Incluindo bibliot//Incluindo biblioteca para leitura do encoder
#include <ESP32Encoder.h>

/*-------------------------------COMUNICAÇÃO------------------------------------*/
                                                                                //
// Incluindo bibliotecas para comuicação com wifi e broker                      //
#include <EWiFi.h>                                                              //
#include <PubSubClient.h>                                                       //
#include <WiFiPassword.h>                                                       //
                                                                                //
// Definindo objetos wifi e mqqt                                                //
WiFiClient espClient;                                                           //
PubSubClient client(espClient);                                                 //
                                                                                //
// Definindo variáveis para controle de wifi e mqtt                             //
String status = "";                                                             //
int count = 8;                                                                  //
                                                                                //
/*-------------------------------COMUNICAÇÃO------------------------------------*/

//Definindo objeto meuEncoder
ESP32Encoder meuEncoder;

//Definindo outras variaveis uteis
double theta = 0.0, tempo1 = 0.0, tempo2 = 0.0, dt = 0.0, erro = 0.0, u = 0.0;
double erroint = 0.0, erroAnt = 0.0,  erroderiv = 0.0;
long contEnc = 0.0;

// Escolhendo referencia
double thetaRef = 0.0; //em rad

double ThetaPot = 120.0*3.1415/180.0;   //__________________________________________TOPICO

//Definindo ganho do controlador proporcional
double Kp = 0.0;         //_________________________________________________________TOPICO
double Ti = 0.0;         //_________________________________________________________TOPICO
double Td = 0.0;         //_________________________________________________________TOPICO

//Definindo variaveis booleanas
bool PID = false;        //_________________________________________________________TOPICO
bool ANTI_WINDUP = false;//_________________________________________________________TOPICO
bool THETA_POT = false;  //_________________________________________________________TOPICO

void setup() {
  //Inicializando comunicacao serial  
  Serial.begin(115200);
  meuEncoder.attachHalfQuad(ENC_A, ENC_B);

/*-------------------------------COMUNICAÇÃO WIFI-------------------------------*/
                                                                                //
  do {                                                                          //
    if(count) Serial.println("\n\nConnection error, trying again.\n");          //
                                                                                //
    count %= 3;                                                               //
                                                                                //
    switch(count) {                                                             //
      case 0:                                                                   //
        ewifi.setWiFi(SSID1, WPA2_AUTH_PEAP, anonymous, username, userpassword);//
        count++;                                                                //
        break;                                                                  //
      case 1:                                                                   //
        ewifi.setWiFi(SSID2, WPA2_AUTH_PEAP, anonymous, username, userpassword);//
        count++;                                                                //
        break;                                                                  //
      case 2:                                                                   //
        ewifi.setWiFi(SSID3,password);                                          //
        count++;                                                                //
        break;                                                                  //     
    }                                                                           //
    ewifi.connect();                                                            //
  } while(ewifi.status() != WL_CONNECTED);                                      //
                                                                                //
/*-------------------------------COMUNICAÇÃO WIFI-------------------------------*/


/*-------------------------------COMUNICAÇÃO MQTT-------------------------------*/
                                                                                //
  client.setServer(MQTTServer, MQTTPort);                                       // 
  client.setCallback(callback);                                                 // 
                                                                                //
/*-------------------------------COMUNICAÇÃO MQTT-------------------------------*/

  //Definindo Entradas da ponte H
  pinMode(PWM,OUTPUT); //velocidade de giro
  pinMode(H_IN1,OUTPUT); //sentido de giro
  pinMode(H_IN2,OUTPUT); //sentido de giro
}

// Loop principal
void loop() {  
  if (!client.connected()) conectarMQTT();

  client.loop();

  // Publicar feedback do controle
  //String feedback = "Setpoint: " + String(setpoint) + ", PV: " + String(process_variable) + ", Output: " + String(output);
  //client.publish(topic_feedback, feedback.c_str());

  //Serial.println("."); delay(2000);

  run_PID();//_______________________________________________________________________FUNÇÃO PRINCIPAL
}

/*----------------------------------------FUNÇÕES MQTT--------------------------------------*/

/* CALLBACK FUNCTION FOR SUBSCRIBE TOPICS */
void callback(char* topic, byte* payload, unsigned int length) {
  String msg = "";
  for (int i = 0; i < length; i++) {
    msg += (char)payload[i];
  }

  Serial.print("Tópico: ");
  Serial.print(topic);
  Serial.print("\tMensagem: ");
  Serial.println(msg);

  if (status != msg) {
    status = msg;

    // Parse the received message for control values
    Kp = extractFloatValue(msg, "KP", Kp);
    Ti = extractFloatValue(msg, "TI", Ti);
    Td = extractFloatValue(msg, "TD", Td);
    ThetaPot = extractFloatValue(msg, "ThetaPot", ThetaPot);
    PID = extractBoolValue(msg, "PID", PID);
    ANTI_WINDUP = extractBoolValue(msg, "ANTI_WINDUP", ANTI_WINDUP);
    THETA_POT = extractBoolValue(msg, "THETA_POT", THETA_POT);

    Serial.print("Novo valor de Kp: ");
    Serial.println(Kp);
    Serial.print("Novo valor de Ti: ");
    Serial.println(Ti);
    Serial.print("Novo valor de Td: ");
    Serial.println(Td);
    Serial.print("Novo valor de ThetaPot: ");
    Serial.println(ThetaPot);
    Serial.print("Novo valor de PID: ");
    Serial.println(PID ? "true" : "false");
    Serial.print("Novo valor de ANTI_WINDUP: ");
    Serial.println(ANTI_WINDUP ? "true" : "false");
    Serial.print("Novo valor de THETA_POT: ");
    Serial.println(THETA_POT ? "true" : "false");
  }
}

/* FUNCTION TO EXTRACT FLOAT VALUES FROM MESSAGE */
float extractFloatValue(const String& msg, const String& param, float origin) {
  int startIdx = msg.indexOf(param + "=");
  if (startIdx == -1) return origin;
  startIdx += param.length() + 1;
  int endIdx = msg.indexOf(",", startIdx);
  if (endIdx == -1) endIdx = msg.length();
  return msg.substring(startIdx, endIdx).toFloat();
}

bool extractBoolValue(const String& msg, const String& param, bool origin) {
  int startIdx = msg.indexOf(param + "=");
  if (startIdx == -1) return origin;
  startIdx += param.length() + 1;
  int endIdx = msg.indexOf(",", startIdx);
  if (endIdx == -1) endIdx = msg.length();
  String value = msg.substring(startIdx, endIdx);
  return (value == "true");
}


/* MQTT FUNCTION FOR CONECT TO SERVER */
void conectarMQTT() {
  int num = 20;
  char esp_id[num];

  snprintf(esp_id, num, "ESP32-%s", ewifi.getmacAddress());

  Serial.println("ID do ESP:"); Serial.print(esp_id);

  Serial.println("\nConectando ao Broker, aguarde...");

  while (!client.connected())

    if (client.connect(esp_id)) client.subscribe(topic);
  
  Serial.println("\nConexão com o Broker estabelecida!");
}

/*----------------------------------------FUNÇÃO PID--------------------------------------*/

void run_PID() {
  //Salvando valores anteriores de tempo e posicao
  tempo1 = tempo2;
  erroAnt = erro; 

  //Determinando leitura autal do encoder
  contEnc =  meuEncoder.getCount();

  
  //Calculando theta a partir da leitura do encoder
  theta = contEnc*3.1415/(334.0*4.0); //em rad

  if (tempo2/1000000.0 > 20.0){
    /*if(THETA_POT){
      thetaRef = ThetaPot;
    }else{
      thetaRef = 120.0*3.1415/180.0;
    }*/
    thetaRef = ThetaPot;
  }

  //Determinando tempo atual
  tempo2 = micros();

  //Calculando diferenca de tempo ente instante atual e instante da ultima leitura  
  dt = tempo2 - tempo1;//em micro s

  //Obtendo erro de rastreamento
  erro = thetaRef-theta; 
  erroint = erroint + erro*dt/1000000.0;
  erroderiv = (erro - erroAnt)/(dt/1000000.0);  
  
  // Anti-Windup
  if(ANTI_WINDUP){
      if(u>=65){
        erroint=0;
      }
      if(u<0){
        erroint=0;
      }
  }

  //Calculando controle
  if(!PID){
    u = Kp*erro;
  }else{
    u = Kp * (erro+1 / Ti*erroint + Td*erroderiv);
  }

  //Alterando sentido de giro de acordo com o sinal do controle
  if (u >= 0){
        //sentido horario
        digitalWrite(H_IN2,LOW);
        digitalWrite(H_IN1,HIGH);
  }
  else{
      //sentido anti-horario
        digitalWrite(H_IN2,HIGH);
        digitalWrite(H_IN1,LOW);
        u = -u;
  }
    
  //Saturando na faixa linear
  u = min(u,(double)65);//limitando superiormente
  u = max(u,(double)0);//limitando inferiormente

  //Aplicando controle a planta
  analogWrite(5,(u+35.0)*255.0/100.0);
    
  //Imprimindo os valores de velocidade e tempo na porta serial
  Serial.print(theta*180.0/3.1415);
  Serial.print(" ");
  Serial.print(u);
  Serial.print(" ");
  Serial.println(tempo2/1000000);
} 
