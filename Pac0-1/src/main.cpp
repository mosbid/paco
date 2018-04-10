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

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

}

void loop() {
  // put your main code here, to run repeatedly:
  char key = keypad.getKey();

  if (key != NO_KEY){
    Serial.println(key);
  }
}
