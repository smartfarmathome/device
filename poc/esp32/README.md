# ESP32

Implement BLE pairing and Environmental Sensing profile on ESP32.

## Install Arduino-cli

https://ukayzm.github.io/arduino-cli/

## compile and upload

arduino-cli compile --fqbn esp32:esp32:esp32 sketch/
arduino-cli upload -p /dev/ttyUSB0 --fqbn esp32:esp32:esp32 sketch/
arduino-cli compile --fqbn esp32:esp32:esp32 -u -p /dev/ttyUSB0 sketch/
stty -F /dev/ttyUSB0 cs8 115200 ignbrk -brkint -icrnl -imaxbel -opost -onlcr -isig -icanon -iexten -echo -echoe -echok -echoctl -echoke noflsh -ixon -crtscts
cat /dev/ttyUSB0