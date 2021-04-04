/*
 * Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleServer.cpp
 * Ported to Arduino ESP32 by Evandro Copercini
 */
#include <Arduino.h>
#include "sys/time.h"

#include <BLEUtils.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEBeacon.h>
#include <BLECharacteristic.h>
#include <BLE2902.h>

#include "board.h"
#include "BleManager.h"


void startAdvertising();
void stopAdvertising();
void TransferBLE();


static unsigned long last_notify_sec;
static unsigned long last_sec;

Mode mode;


BLEServer *bleServer = nullptr;

BLEService *serviceDeviceInformation = nullptr;
BLECharacteristic *charManufacturerName = nullptr;
BLECharacteristic *charModelNumber = nullptr;
BLECharacteristic *charSoftwareRevision = nullptr;

BLEService *bleEsService = nullptr;
BLECharacteristic *bleTemperatureChar = nullptr;
BLECharacteristic *bleHumidityChar = nullptr;
BLECharacteristic *bleIlluminanceChar = nullptr;

//BLEDescriptor outdoorTemperatureDescriptor(BLEUUID((uint16_t)0x2901));
//BLEDescriptor outdoorHumidityDescriptor(BLEUUID((uint16_t)0x2901));
//BLECharacteristic outHumidityCharacteristic(BLEUUID((uint16_t)0x2A6F), BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);
//BLECharacteristic outTemperatureCharacteristic(BLEUUID((uint16_t)0x2A6E), BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);

/**
 * Characteristic event callback
 */
class ServerEventCallback: public BLEServerCallbacks {
	void onConnect(BLEServer* pServer) {
		Serial.println("BLE Connected!");
		mode = MODE_CONNECTED;
		last_notify_sec = 0;
	}
	void onDisconnect(BLEServer* pServer) {
		Serial.println("BLE Disconnected!");
		mode = MODE_NOT_CONNECTED;
		last_notify_sec = 0;
	}
}; // ServerEventCallback

class CharacteristicCallbacks : public BLECharacteristicCallbacks {
	void onRead(BLECharacteristic *pCharacteristic) {
		Serial.println("onRead");
	}
	void onWrite(BLECharacteristic *pCharacteristic) {
		Serial.println("onWrite");
	}
};

void startAdvertising() {
	// Start advertising
	bleServer->getAdvertising()->start();
}

void stopAdvertising() {
	// Stop advertising
	bleServer->getAdvertising()->stop();
}

void ble_setup() {
	// Create the BLE Device
	BLEDevice::init(BLE_DEVICE_NAME);

	// Create the BLE Server
	bleServer = BLEDevice::createServer();
	bleServer->setCallbacks(new ServerEventCallback());

	// Device Information service
	serviceDeviceInformation = bleServer->createService(BLEUUID((uint16_t)0x180a));
	charManufacturerName = serviceDeviceInformation->createCharacteristic(BLEUUID((uint16_t)0x2a29), BLECharacteristic::PROPERTY_READ);
	charManufacturerName->setValue("SFAF Co.");
	charModelNumber = serviceDeviceInformation->createCharacteristic(BLEUUID((uint16_t)0x2a24), BLECharacteristic::PROPERTY_READ);
	charModelNumber->setValue("ABC-1234");
	charSoftwareRevision = serviceDeviceInformation->createCharacteristic(BLEUUID((uint16_t)0x2a28), BLECharacteristic::PROPERTY_READ);
	charSoftwareRevision->setValue("0.1");
	serviceDeviceInformation->start();

	// Environmental Sensing service
	bleEsService = bleServer->createService(BLEUUID((uint16_t)0x181a));
	BLEDescriptor *desc;

	/*
	 * temperature characteristic
	 */
	bleTemperatureChar = bleEsService->createCharacteristic(BLEUUID((uint16_t)0x2a6e), BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);

	/* ES Measurement Descriptor value */
	desc = new BLEDescriptor(BLEUUID((uint16_t)0x290c));
	uint8_t value11[] = {
		0x00, 0x00,	// flag
		0x02,		// sampling function
		0x0a, 0x00, 0x00,	// measurement period
		0x84, 0x03, 0x00,	// update interval
		0x01,		// application
		0x02,		// measurement uncertaintity
	};
	desc->setValue(value11, sizeof(value11));
	bleTemperatureChar->addDescriptor(desc);

	/* Valid Range Descriptor */
	desc = new BLEDescriptor(BLEUUID((uint16_t)0x2906));
	int16_t value12[] = {
		-4000, 6000,
	};
	desc->setValue((uint8_t *)value12, sizeof(value12));
	bleTemperatureChar->addDescriptor(desc);

	/*
	 * humidity characteristic
	 */
	bleHumidityChar = bleEsService->createCharacteristic(BLEUUID((uint16_t)0x2a6f), BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);

	/* ES Measurement Descriptor */
	desc = new BLEDescriptor(BLEUUID((uint16_t)0x290c));
	uint8_t value21[] = {
		0x00, 0x00,	// flag
		0x02,		// sampling function
		0x0a, 0x00, 0x00,	// measurement period
		0x84, 0x03, 0x00,	// update interval
		0x12,		// application
		0x02,		// measurement uncertaintity
	};
	desc->setValue(value21, sizeof(value21));
	bleHumidityChar->addDescriptor(desc);

	/* Valid Range Descriptor */
	desc = new BLEDescriptor(BLEUUID((uint16_t)0x2906));
	int16_t value22[] = {
		0, 10000,
	};
	desc->setValue((uint8_t *)value22, sizeof(value22));
	bleHumidityChar->addDescriptor(desc);

	bleEsService->start();

#if 0
	/*
	 * illuminance characteristic
	 */
	bleHumidityChar = bleEsService->createCharacteristic(BLEUUID((uint16_t)0x2afb), BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);

	/* ES Measurement Descriptor */
	desc = new BLEDescriptor(BLEUUID((uint16_t)0x290c));
	uint8_t value31[] = {
		0x00, 0x00,	// flag
		0x02,		// sampling function
		0x0a, 0x00, 0x00,	// measurement period
		0x84, 0x03, 0x00,	// update interval
		0x21,		// application
		0x02,		// measurement uncertaintity
	};
	desc->setValue(value31, sizeof(value31));
	bleHumidityChar->addDescriptor(desc);

	/* Valid Range Descriptor */
	desc = new BLEDescriptor(BLEUUID((uint16_t)0x2906));
	int16_t value32[] = {
		0, 10000,
	};
	desc->setValue((uint8_t *)value32, sizeof(value32));
	bleHumidityChar->addDescriptor(desc);
#endif

	bleEsService->start();

#if 0
	// Set beacon data
	BLEBeacon oBeacon = BLEBeacon();
	oBeacon.setProximityUUID(BLEUUID(BEACON_UUID));
	oBeacon.setMajor(1);
	oBeacon.setMinor(1);
	oBeacon.setManufacturerId(BLE_MANUFACTURER_ID);

	BLEAdvertisementData oAdvertisementData = BLEAdvertisementData();
	oAdvertisementData.setFlags(0x04); // BR_EDR_NOT_SUPPORTED 0x04
	std::string strServiceData = "";
	strServiceData += (char)26;     // Len
	strServiceData += (char)0xFF;   // Type
	strServiceData += oBeacon.getData(); 
	oAdvertisementData.addData(strServiceData);
	bleServer->getAdvertising()->setAdvertisementData(oAdvertisementData);
#endif

	BLEAdvertisementData oScanResponseData = BLEAdvertisementData();
	//oScanResponseData.setName("bedroom");		// nickname
	oScanResponseData.setPartialServices(BLEUUID((uint16_t)0x181a));
	//oScanResponseData.setAppearance();
	bleServer->getAdvertising()->setScanResponseData(oScanResponseData);

	startAdvertising();
}

void TransferBLE() {
	int16_t temperature = random(-40, 60) * 100;
	bleTemperatureChar->setValue((uint8_t *)&temperature, sizeof(temperature));
	bleTemperatureChar->notify();
	uint16_t humidity = random(0, 100) * 100;
	bleHumidityChar->setValue((uint8_t *)&humidity, sizeof(humidity));
	bleHumidityChar->notify();
#if 0
	uint16_t illuminance = random(0, 100) * 100;
	bleHumidityChar->setValue((uint8_t *)&illuminance, sizeof(illuminance));
	bleHumidityChar->notify();
#endif
	//vTaskDelay(1);  // one tick delay (15ms) for stability
	Serial.println("BLE notify");
}

void ble_loop(unsigned long cur_sec, unsigned long cur_msec) {
	if (cur_sec != last_sec) {
		Serial.print(cur_sec);
		Serial.print(" mode = ");
		Serial.print(mode);
		Serial.println();
		last_sec = cur_sec;
	}
	if (mode == MODE_CONNECTED) {
		if (cur_sec - last_notify_sec >= BLE_NOTIFY_INTERVAL_SEC) {
			TransferBLE();
			last_notify_sec = cur_sec;
		}
	}
}
