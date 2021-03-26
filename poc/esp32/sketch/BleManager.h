#ifndef __BLE_H__
#define __BLE_H__


/* operation mode */
enum Mode {
	MODE_NOT_CONNECTED,
	MODE_PAIRING,
	MODE_CONNECTED,
	MODE_MAX,
};


/* BLE */
// See the following for generating UUIDs:
// https://www.uuidgenerator.net/
// See the following for generating UUIDs: https://www.uuidgenerator.net/
#define BEACON_UUID           "8ec76ea3-6668-48da-9866-75be8bc86f4d" // UUID 1 128-Bit (may use linux tool uuidgen or random numbers via https://www.uuidgenerator.net/)
#define BLE_DEVICE_NAME "SFatHome"
#define BLE_MANUFACTURER_ID	(0x00 | (0x4c << 8))	// fake Apple 0x004C LSB (ENDIAN_CHANGE_U16!)


void ble_setup();
void ble_loop(unsigned long cur_sec, unsigned long cur_msec);
extern Mode mode;


#endif
