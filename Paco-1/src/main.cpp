#include <Arduino.h>
#include <Keypad.h>
#include <SoftwareSerial.h>
#include <String.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

//Config ESP
const char wifiName = {'E'};
SoftwareSerial wifi(13, 12);
char responseBuffer[50];
///////////////

//Config KEYPAD
const byte rows = 4; //four rows
const byte cols = 4; //three columns
char keys[rows][cols] = {
  {'1','2','3', 'A'},
  {'4','5','6', 'B'},
  {'7','8','9', 'C'},
  {'*','0','#', 'D'},
};

byte rowPins[rows] = {4, 5, 6, 7}; //connect to the row pinouts of the keypad
byte colPins[cols] = {A3, A2, A1, A0}; //connect to the column pinouts of the keypad

Keypad keypad = Keypad( makeKeymap(keys), colPins, rowPins ,cols, rows );
char men[5];
char pass[5] = {'C', 'A', 'C', 'A', '\0'};

int i = 0;
int changepass;
int doorState = 0;
String data;
String datalength;
String peticion;
String envio;
String test;


LiquidCrystal_I2C lcd(0x3F,16,2);

///////////////////////////////////////////////////////////////////////////////





///////////////////////////////////////////////////////////////////////////////

void setup() {
  String SSID = "Wifi Casa";
  String PASS = "pilarluisivanruben1492";


  Serial.begin(115200);
  Serial.setTimeout(500);
  wifi.begin(115200);
  wifi.setTimeout(500);

  //restartEsp();

  lcd.init(A5,A4);
  lcd.backlight();

  //Reinicio
  for(int i = 0; i<20;i++){
      wifi.println("AT+RST");
    if(wifi.find("OK")){
      Serial.println("Restart OK");
      break;
    }else{
      Serial.println("Bad restart");
      delay(200);
    }
  }

  //CWMODE
  for(int i = 0; i<20;i++){
    wifi.println("AT+CWMODE=1");
    if(wifi.find("OK")){
      Serial.println("CWMODE OK");
      break;
    }else{
      Serial.println("CWMODE bad");
      delay(200);
    }
  }


  //CIPMUX


  /*wifi.println("AT+CWLAP");
  for(int j = 0; j<6000;j++);
  String mensaje = wifi.;
  Serial.println(mensaje);*/
  //Conexion al wifi

  /*for(int i = 0; i<10;i++){
    wifi.println('AT+CWJAP="S8 Manu", "27121996"');
    long int time = millis();
    int ndx = 0;
   char rc = (char)0;
   memset(responseBuffer, 0, sizeof(responseBuffer));
    while(millis() - time < 10000)
       {

         while(wifi.available() > 0)
         {

           rc = wifi.read();
           responseBuffer[ndx] = rc;
           ndx++;


           /*Serial.println(wifi.read());
           if(wifi.find("WIFI CONNECTED/r/nOK")){
             Serial.println("Conexion wifi OK");
             break;
           }else{
             Serial.println("Conexion wifi bad");
             delay(200);
             if(i == 9){
               Serial.println("IMPOSSIBLE TO CONNECT");
             }
           }
          Serial.println(responseBuffer);

         }

       }*/


    //for(int j = 0; j<6000;j++);
    for(int i = 0; i<10;i++){
      wifi.println('AT+CWJAP="S8 Manu", "27121996"');
      delay(2000);
      if(wifi.find("WIFI GOT IP")){
        Serial.println("Conexion wifi OK");
        break;
      }else{
        Serial.println("Conexion wifi bad");
        delay(200);
        if(i == 9){
          Serial.println("IMPOSSIBLE TO CONNECT");
        }
      }
    }

    for(int i = 0; i<20;i++){
      wifi.println("AT+CIPMUX=0");
        if(wifi.find("OK")){
          Serial.println("CIPMUX OK");
          break;
        }else{
          Serial.println("CIPMUX bad");
          delay(200);
        }
    }


  /*for(int i = 0; i<20;i++){
    wifi.println('AT+CIPSTART="TCP","192.164.0.155",8083');
    delay(1000);
    if(wifi.find("OK")){
      Serial.println("TCP OK");
      break;
    }else{
      Serial.println("TCP bad");
      delay(200);
    }
  }*/


  wifi.println('AT+CIPSTART="TCP","192.168.43.170",8083');
  delay(2000);
  //
  /*data = 'PUT /api/door HTTP/1.1\r\n\r\n {"id":5,"doorState":0,"doorAddress":"Arduino","doorPass":"9999","doorAdmin":"AAAA"}';
  datalength = data.length();
  peticion = 'AT+CIPSEND='+ datalength;*/

  data = 'GET /api/door/1 HTTP/1.1\r\n\r\n';
  datalength = data.length();
  peticion = 'AT+CIPSEND='+ datalength;

  wifi.println(peticion);
  delay(2000);
  wifi.println(data);

  Serial.println(peticion);
  Serial.println(data);
/*

*/


}


void whiteFlash(){
  analogWrite(9, 255);
  analogWrite(10, 255);
  analogWrite(11, 255);
  delay(100);
  analogWrite(9, 0);
  analogWrite(10, 0);
  analogWrite(11, 0);
}

void blueFlash(){
  analogWrite(9, 0);
  analogWrite(10, 0);
  analogWrite(11, 255);
  delay(100);
  analogWrite(9, 0);
  analogWrite(10, 0);
  analogWrite(11, 00);
}

void redFlash(){
  analogWrite(9, 255);
  analogWrite(10, 0);
  analogWrite(11, 0);
  delay(100);
  analogWrite(9, 0);
  analogWrite(10, 0);
  analogWrite(11, 00);
}

void red(){
  analogWrite(9, 255);
  analogWrite(10, 0);
  analogWrite(11, 0);
}

void green(){
  analogWrite(9, 0);
  analogWrite(10, 255);
  analogWrite(11, 0);
}

void blue(){
  analogWrite(9, 0);
  analogWrite(10, 0);
  analogWrite(11, 255);
}


void loop() {

  char key = keypad.getKey();


if (key != NO_KEY) {
  if(doorState == 1 && key == 'C' && i == 0){ // CERRAR PUERTA SI ESTÁ ABIERTA, Y SE PULSA 'C' POR PRIMERA VEZ
    digitalWrite(12, LOW);
    doorState = 0;
    Serial.println("Door closed.");
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print("Door closed.");
    whiteFlash();
  }else if(doorState == 1 && key == 'A' && i == 0 && changepass == 0){ //INSERTAR NUEVA PASS SI ESTÁ ABIERTO, SE PULSA A Y ES EL PRIMER CARACTER
    Serial.println("Insert new pass:");
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print("Insert new pass:");
    changepass = 1;
  }else if (key != '*' && key != '#'){
    blueFlash();
    men[i] = key;
    if(i==0){
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("Password:");
    }
    lcd.setCursor(i,1);
    lcd.print('*');

    i++;
  }else if (key == '*' && changepass == 1){ //NUEVA PASS
    i = 0;
    memset(pass, 0, sizeof(pass));
    strcpy(pass, men);
    memset(men, 0, sizeof(men));
    Serial.print("New pass: ");
    Serial.println(pass);

    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print("Password changed");

    changepass = 0;
    whiteFlash();
    delay(100);
    whiteFlash();
    delay(100);
    whiteFlash();
  }else if (key == '*' && changepass == 0){
    i = 0;
    if(strcmp(men, pass) == 0){ //Equals zero means that both strings are equals
      green();
      Serial.print("Password OK, door open: ");
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("Password OK");

      doorState = 1;
      digitalWrite(12, HIGH);

    }else{
      redFlash();
      Serial.print("Password error: ");
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("Password error.");
    }
    Serial.println(men);
    memset(men, 0, sizeof(men));

  }else if (key == '#'){
    i = 0;
    Serial.println("Password reset.");
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print("Password reset.");
    memset(men, 0, sizeof(men));
    redFlash();


  }
}
}
