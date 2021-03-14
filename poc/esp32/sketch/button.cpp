#include <Arduino.h>
#include "board.h"
#include "button.h"


void setup_button()
{
}


static boolean prevState = false;

boolean is_changed()
{
	boolean curState = is_pressed();
	return prevState == curState;
}

boolean is_pressed()
{
#define MSEC_TO_CHANGE	80
#define NUM_BYTE		(((MSEC_TO_CHANGE * 1000 / LOOP_USEC) + 7) / 8)
	static unsigned char buf[NUM_BYTE];
	static boolean curState;
	int i, sum;

	for (i = 0, sum = 0; i < NUM_BYTE - 1; i++) {
		buf[i] = (buf[i] << 1) | (buf[i+1] >> 7);
		sum += buf[i];
	}
	int touchVal = touchRead(TOUCH_BUTTON_PIN);     // read touch switch state
	buf[i] = buf[i] << 1 | (touchVal < 80 ? 1 : 0);
	sum += buf[i];
	if (sum == 0xff * NUM_BYTE) {
		curState = true;
	} else if (sum == 0) {
		curState = false;
	}
	return curState;
}
