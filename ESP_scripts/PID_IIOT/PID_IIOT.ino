//Incluindo bibliot//Incluindo biblioteca para leitura do encoder
#include <Encoder.h>

/*-------------------------------COMUNICAÇÃO------------------------------------*/
                                                                                //
// Incluindo bibliotecas para comuicação com wifi e broker                      //
#include <EWiFi.h>                                                              //
#include <PubSubClient.h>                                                       //
#include <WiFiPassword.h>                                                       //
                                                                                //
// Definindo objetos wifi e mqqt                                                //
WiFiClient espClient;                                                           //
PubSubClient MQTT(espClient);                                                   //
                                                                                //
// Definindo variáveis para controle de wifi e mqtt                             //
String status = "";                                                             //
int count = 0;                                                                  //
                                                                                //
/*-------------------------------COMUNICAÇÃO------------------------------------*/

//Definindo objeto meuEncoder
Encoder meuEncoder(2, 3);

//Definindo outras variaveis uteis
double theta = 0.0, tempo1 = 0.0, tempo2 = 0.0, dt = 0.0, erro = 0.0, u = 0.0;
double erroint = 0.0, erroAnt = 0.0,  erroderiv = 0.0;
long contEnc = 0.0;

// Escolhendo referencia
double thetaRef = 0.0; //em rad

double ThetaPot = 0.0;   //_________________________________________________________BRENO

//Definindo ganho do controlador proporcional
double Kp = 0.0;         //_________________________________________________________BRENO
double Ti = 0.0;         //_________________________________________________________BRENO
double Td = 0.0;         //_________________________________________________________BRENO

//Definindo variaveis booleanas
bool PID = false;        //_________________________________________________________BRENO
bool ANTI_WINDUP = false;//_________________________________________________________BRENO
bool THETA_POT = false;  //_________________________________________________________BRENO

void setup() {
  //Inicializando comunicacao serial  
  Serial.begin(115200);

/*-------------------------------COMUNICAÇÃO WIFI-------------------------------*/
                                                                                //
  do {                                                                          //
    if(count) Serial.println("\n\nConnection error, trying again.\n");          //
                                                                                //
    count %= 3;                                                                 //
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
    }                                                                           //
                                                                                //
    ewifi.connect();                                                            //
  } while(ewifi.status() != WL_CONNECTED);                                      //
                                                                                //
/*-------------------------------COMUNICAÇÃO WIFI-------------------------------*/


/*-------------------------------COMUNICAÇÃO MQTT-------------------------------*/
                                                                                //
  MQTT.setServer(MQTTServer, MQTTPort);                                         //
  MQTT.setCallback(callback);                                                   //
                                                                                //
/*-------------------------------COMUNICAÇÃO MQTT-------------------------------*/

   //Definindo Entradas da ponte H
  pinMode(5,OUTPUT); //velocidade de giro
  pinMode(7,OUTPUT); //sentido de giro
  pinMode(8,OUTPUT); //sentido de giro
}

// Loop principal
void loop() {
  if (!MQTT.connected()) conectarMQTT();

  MQTT.loop();

  run_PID();
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
    msg.toLowerCase();

    // Parse the received message for control values
    if (msg.indexOf("pid") != -1) {
      // Extracting PID parameters (Kp, Ti, Td) and boolean flags (PID, ANTI_WINDUP)
      int kpIndex = msg.indexOf("Kp");
      int tiIndex = msg.indexOf("Ti");
      int tdIndex = msg.indexOf("Td");
      int thetapotIndex = msg.indexOf("ThetaPot");
      int pidIndex = msg.indexOf("PID");
      int awIndex = msg.indexOf("ANTI_WINDUP");
      int thetaIndex = msg.indexOf("THETA_POT");

      // Example: msg = "Kp=2.0, Ti=1.5, Td=0.5, PID=true, ANTI_WINDUP=false"
      if (kpIndex != -1) {
        Kp = extractFloatValue(msg, kpIndex);
        Serial.print("Novo valor de Kp: ");
        Serial.println(Kp);
      }

      if (tiIndex != -1) {
        Ti = extractFloatValue(msg, tiIndex);
        Serial.print("Novo valor de Ti: ");
        Serial.println(Ti);
      }

      if (tdIndex != -1) {
        Td = extractFloatValue(msg, tdIndex);
        Serial.print("Novo valor de Td: ");
        Serial.println(Td);
      }

      if (thetapotIndex != -1) {
        ThetaPot = extractFloatValue(msg, thetapotIndex);
        Serial.print("Novo valor de ThetaPot: ");
        Serial.println(ThetaPot);
      }

      if (pidIndex != -1) {
        PID = extractBoolValue(msg, pidIndex);
        Serial.print("Novo valor de PID: ");
        Serial.println(PID ? "true" : "false");
      }

      if (awIndex != -1) {
        ANTI_WINDUP = extractBoolValue(msg, awIndex);
        Serial.print("Novo valor de ANTI_WINDUP: ");
        Serial.println(ANTI_WINDUP ? "true" : "false");
      }

      if (thetaIndex != -1) {
        THETA_POT = extractBoolValue(msg, thetaIndex);
        Serial.print("Novo valor de THETA_POT: ");
        Serial.println(THETA_POT ? "true" : "false");
      }
    }
  }
}

/* FUNCTION TO EXTRACT FLOAT VALUES FROM MESSAGE */
float extractFloatValue(String msg, int index) {
  String valueStr = "";
  int valueStart = msg.indexOf('=', index) + 1;
  int valueEnd = msg.indexOf(',', index);
  if (valueEnd == -1) valueEnd = msg.length();
  valueStr = msg.substring(valueStart, valueEnd);
  return valueStr.toFloat();
}

/* FUNCTION TO EXTRACT BOOLEAN VALUES FROM MESSAGE */
bool extractBoolValue(String msg, int index) {
  String valueStr = "";
  int valueStart = msg.indexOf('=', index) + 1;
  int valueEnd = msg.indexOf(',', index);
  if (valueEnd == -1) valueEnd = msg.length();
  valueStr = msg.substring(valueStart, valueEnd);
  return (valueStr == "true");
}

/* MQTT FUNCTION FOR CONECT TO SERVER */
void conectarMQTT() {
  int num = 20;
  char esp_id[num];

  snprintf(esp_id, num, "ESP32-%s", ewifi.getmacAddress());
  // printf("%s\n", esp_id);
  while (!MQTT.connected())
    if (MQTT.connect(esp_id)) MQTT.subscribe(topic);
}

/*----------------------------------------FUNÇÃO PID--------------------------------------*/

void run_PID() {
  //Salvando valores anteriores de tempo e posicao
  tempo1 = tempo2;
  erroAnt = erro; 

  //Determinando leitura autal do encoder
  contEnc = meuEncoder.read();
  
  //Calculando theta a partir da leitura do encoder
  theta = contEnc*2.0*3.1415/(334.0*4.0); //em rad

  if (tempo2/1000000.0 > 3.0){
    if(THETA_POT){
      thetaRef = ThetaPot;
    }else{
      thetaRef = 120.0*3.1415/180.0;
    }
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

  u = PID * (Kp * (erro+1 / Ti*erroint + Td*erroderiv)) + !PID * (Kp*erro);

  //Alterando sentido de giro de acordo com o sinal do controle
  if (u >= 0){
        //sentido horario
        digitalWrite(8,LOW);
        digitalWrite(7,HIGH);
  }
  else{
      //sentido anti-horario
        digitalWrite(8,HIGH);
        digitalWrite(7,LOW);
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
