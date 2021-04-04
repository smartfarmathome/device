#include <Arduino.h>
#include "board.h"
#include "button.h"
#include "BleManager.h"


unsigned long cur_sec;
unsigned long cur_msec;
unsigned long cur_usec;
unsigned long loop_timer;
//Mode mode, prev_mode;


//unsigned long start_advertising_msec = 0;


void setup_board()
{
	Serial.begin(115200);
    while (!Serial); // wait for Leonardo enumeration, others continue immediately
	Serial.println("Start POC.ESP32");

	pinMode(STATUS_LED_PIN, OUTPUT);

	//mode = MODE_NOT_CONNECTED;
	//prev_mode = MODE_NOT_CONNECTED;
	cur_usec = micros();

	randomSeed(analogRead(0));

	ble_setup();
}

void loop_board()
{
	static unsigned long last_msec = 0;

	/* wait until the starting the next loop */
	while (micros() - cur_usec < LOOP_USEC);
	cur_usec = micros();
	cur_msec = millis();
	if (cur_msec - last_msec >= 1000) {
		cur_sec++;
		last_msec = cur_msec;
	}

	/*
	if (mode == MODE_NOT_CONNECTED || mode == MODE_CONNECTED) {
		if (is_changed() && is_pressed()) {
			digitalWrite(STATUS_LED_PIN, HIGH);
			Serial.println("Start BLE advertising");
			startAdvertising();
			start_advertising_msec = cur_msec;
			prev_mode = mode;
			mode = MODE_PAIRING;
		}
	}
	if (isConnected) {
		mode = MODE_CONNECTED;
	}
	if (mode == MODE_PAIRING) {
		if (cur_msec - start_advertising_msec > (30 * 1000)) {
			digitalWrite(STATUS_LED_PIN, LOW);
			Serial.println("Stop BLE advertising");
			stopAdvertising();
			mode = prev_mode;
		}
	}
	*/
	ble_loop(cur_sec, cur_msec);
}

