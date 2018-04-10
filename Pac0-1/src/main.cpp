#include <Arduino.h>
#include <Keypad.h>

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

void setup() {
  Serial.begin(9600);

}

void loop() {
  char key = keypad.getKey();
//palabr

if (key != NO_KEY) {
  if (key != '*' && key != '#'){
    men[i] = key;
    i++;
  }else if (key == '*'){
    i = 0;
    Serial.println(men);
    memset(men, 0, sizeof(men));
  }else if (key == '#'){
    i = 0;
    memset(men, 0, sizeof(men));
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
