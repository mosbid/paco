#include <Arduino.h>
#include <Keypad.h>
#include <SoftwareSerial.h>
//Config ESP
SoftwareSerial wifi(13, 12);
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

byte rowPins[rows] = {A2, A1, A0, 4}; //connect to the row pinouts of the keypad
byte colPins[cols] = {A5, A4, A3, 7}; //connect to the column pinouts of the keypad

Keypad keypad = Keypad( makeKeymap(keys), colPins, rowPins ,cols, rows );
char men[20];
int i = 0;
////////////////////////

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(4000);
  wifi.begin(9600);

  wifi.println("AT+RST");
  wifi.println("AT+CWMODE=1");
  wifi.println("AT+CIPMUX=0");
  wifi.println('AT+CWJAP="S8 Manu","27121996"');
}

void loop() {
  char key = keypad.getKey();

if (key != NO_KEY) {
  if (key != '*' && key != '#'){
    analogWrite(9, 0);
    analogWrite(10, 255);
    analogWrite(11, 0);
    delay(100);
    analogWrite(9, 0);
    analogWrite(10, 0);
    analogWrite(11, 00);
    men[i] = key;
    i++;
  }else if (key == '*'){
    i = 0;
    Serial.println(men);
    memset(men, 0, sizeof(men));

    analogWrite(9, 255);
    analogWrite(10, 100);
    analogWrite(11, 00);
  }else if (key == '#'){
    i = 0;
    memset(men, 0, sizeof(men));
    analogWrite(9, 255);
    analogWrite(10, 0);
    analogWrite(11, 0);
  }
}
/*  if (key != NO_KEY){
    Serial.println(key);

    if(key == 'A'){
      analogWrite(11, 255);
      analogWrite(10, 0);
      analogWrite(9, 0);
      }
    if(key == 'B'){
      analogWrite(11, 0);
      analogWrite(10, 255);
      analogWrite(9, 0);
      }
    if(key == 'C'){
      analogWrite(11, 0);
      analogWrite(10, 0);
      analogWrite(9, 255);
      }

    if(key == 'D'){
      analogWrite(11, 0);
      analogWrite(10, 0);
      analogWrite(9, 0);
      }
  }*/
}
