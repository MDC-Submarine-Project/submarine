# Motor Logic Module

## Overview
The Motor Logic module is responsible for controlling servo motors using an analog joystick via an Arduino Mega. This module is part of the larger 'submarine' project and provides the hardware interface for motor control.

## Hardware Components
- Arduino Mega
- 2 Servo Motors
- Analog Joystick
- Breadboard and connecting wires

## Setup Instructions

### Prerequisites
- Arduino CLI: [Installation Guide](https://arduino.github.io/arduino-cli/0.20/installation/)
- Gradle: This project uses Gradle for build automation

### Configuration
The hardware configuration is defined in the `diagram.json` file, which specifies the connections between:
- Arduino Mega
- Servo motors
- Analog joystick
- Breadboard

## Build and Upload

### Gradle Tasks
The following Gradle tasks are available:

1. **Install Arduino Core**
   ```
   ./gradlew installArduinoCore
   ```
   This task installs the Arduino AVR core required for compiling sketches for Arduino Mega.
   The core installation is automatically handled when running the compile task.

2. **Install Servo Library**
   ```
   ./gradlew installServoLibrary
   ```
   This task installs the Servo library required for controlling servo motors.
   The library installation is automatically handled when running the compile task.

3. **Compile Task**
   ```
   ./gradlew compileArduino
   ```
   This task uses the Arduino CLI to compile the sketch for the Arduino Mega.
   It automatically installs the required Arduino AVR core and Servo library if not already installed.
   Reference: [arduino-cli compile](https://arduino.github.io/arduino-cli/0.20/commands/arduino-cli_compile/)

4. **Upload Task**
   ```
   ./gradlew uploadArduino
   ```
   This task uploads the compiled sketch to the Arduino Mega.
   Reference: [arduino-cli upload](https://arduino.github.io/arduino-cli/0.19/commands/arduino-cli_upload/)

   Note: This task depends on the compile task and will only be needed when uploading to an actual board.

### Build Output
- Compiled files (hex and elf) will be stored in the `build` folder within the motorlogic module
- The `build` folder is included in the `.gitignore` file

## Wokwi Simulation
This project can be simulated using Wokwi. The configuration is defined in `wokwi.toml`.

To run the simulation:
1. Compile the Arduino sketch using the Gradle task
2. The `wokwi.toml` file will be automatically updated with the correct paths to the compiled firmware files
3. Open the project in Wokwi
