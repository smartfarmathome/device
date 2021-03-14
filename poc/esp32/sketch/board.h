#ifndef __BOARD_H__
#define __BOARD_H__


#include <Arduino.h>


#define TOUCH_BUTTON_PIN	T0


/* status LED */
#define STATUS_LED_PIN			13


/* loop interval (msec) */
#define LOOP_USEC     10000
extern unsigned long cur_msec;
extern unsigned long cur_usec;


/* operation mode */
enum Mode {
	MODE_NOT_CONNECTED,
	MODE_PAIRING,
	MODE_CONNECTED,
	MODE_MAX,
};
extern Mode mode;


/* BLE */
// See the following for generating UUIDs:
// https://www.uuidgenerator.net/
// See the following for generating UUIDs: https://www.uuidgenerator.net/
#define BEACON_UUID           "8ec76ea3-6668-48da-9866-75be8bc86f4d" // UUID 1 128-Bit (may use linux tool uuidgen or random numbers via https://www.uuidgenerator.net/)
#define SERVICE_UUID          "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define IN_CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define OUT_CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26fa"
#define BLE_DEVICE_NAME "SFAH"
#define BLE_MANUFACTURER_ID	(0x00 | (0x4c << 8))	// fake Apple 0x004C LSB (ENDIAN_CHANGE_U16!)



#endif
