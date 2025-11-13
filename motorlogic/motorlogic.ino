#include <Servo.h>

Servo myServo1;
Servo myServo2;

// servo pin
const int servoPin1 = 6;
const int servoPin2 = 5;

// joystick pins
const int joystickPinX = A2;
const int joystickPinY = A3;


void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  myServo1.attach(servoPin1);
  myServo2.attach(servoPin2);

}

void loop() {
  // put your main code here, to run repeatedly:
  int xValue = analogRead(joystickPinX);
  int yValue = analogRead(joystickPinY);

  int servo1Pos = map(xValue, 0, 1023, 0, 180);
  int servo2Pos = map(yValue, 0, 1023, 0, 180);

  Serial.print("X value: ");
  Serial.println(xValue);
  Serial.print("Y Value: ");
  Serial.println(yValue);

  myServo1.write(servo1Pos);
  myServo2.write(servo2Pos);

  delay(10);
}