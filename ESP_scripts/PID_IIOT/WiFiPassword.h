/** Input WiFi data
 * Definition of macros SSID, username and password
*/
#define SSID1 "UFU-Institucional"
#define SSID2 "eduroam"
#define SSID3 ""
#define password ""
#define username "dilermandoa1@ufu.br"    // Email ufu
#define userpassword "Atheon-7"           // Senha do email ufu
#define anonymous "dilermandoa1@ufu.br"   // Email ufu

/** Input WiFi data
 * Definition of macros SSID, username and password
*/
#define MQTTServer "10.14.108.205"//"192.168.100.37" //"200.19.144.16"
#define MQTTPort 1883

/* DECLARATION OF TOPICS VARIABLES */
const char* topic = "/lab_pid";
const char* topic_feedback = "lab_pid_feedback";

// mosquitto_pub -h 192.168.100.37 -t "lab_pid_config" -m "Kp=2.0, Ti=1.5, Td=0.5, ThetaPot=3.14, PID=true, ANTI_WINDUP=false, THETA_POT=true"
// mosquitto_sub -h localhost -t "lab_pid_config"