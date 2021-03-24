#ifndef __BOARD_H__
#define __BOARD_H__


#include <Arduino.h>


#define TOUCH_BUTTON_PIN	T0


/* status LED */
#define STATUS_LED_PIN			13


/* loop interval (msec) */
#define LOOP_USEC     10000
extern unsigned long cur_sec;
extern unsigned long cur_msec;
extern unsigned long cur_usec;


#define BLE_NOTIFY_INTERVAL_SEC		10


#endif
