/*
 * Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleServer.cpp
 * Ported to Arduino ESP32 by Evandro Copercini
 */
#include <Arduino.h>
#include "sys/time.h"

#include "board.h"
#include "ble.h"
#include <BLEUtils.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEBeacon.h>
#include <BLECharacteristic.h>
#include <BLE2902.h>


BLEServer *pServer;
BLEService *pService;
BLECharacteristic *pCharIn;
BLECharacteristic *pCharOut;

bool isConnected = false;

BLEDescriptor outdoorHumidityDescriptor(BLEUUID((uint16_t)0x2901));
BLEDescriptor outdoorTemperatureDescriptor(BLEUUID((uint16_t)0x2901));

BLECharacteristic outHumidityCharacteristic(BLEUUID((uint16_t)0x2A6F), BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);
BLECharacteristic outTemperatureCharacteristic(BLEUUID((uint16_t)0x2A6E), BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);


/**
 * Characteristic event callback
 */
class ServerEventCallback: public BLEServerCallbacks {
	void onConnect(BLEServer* pServer) {
		Serial.println("BLE Connected!");
		isConnected = true;
	}
	void onDisconnect(BLEServer* pServer) {
		Serial.println("BLE Disconnected!");
		isConnected = false;
	}
}; // ServerEventCallback


void startAdvertising() {
	// Start advertising
	pServer->getAdvertising()->start();
}

void stopAdvertising() {
	// Stop advertising
	pServer->getAdvertising()->stop();
}

void initBle() {
	// Create the BLE Device
	BLEDevice::init(BLE_DEVICE_NAME);
	// Create the BLE Server
	pServer = BLEDevice::createServer();
	pServer->setCallbacks(new ServerEventCallback());

	// create the BLE service
	pService = pServer->createService(BLEUUID((uint16_t)0x181A));

	// https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
	// Create a BLE Descriptor
	outHumidityCharacteristic.addDescriptor(new BLE2902());
	outTemperatureCharacteristic.addDescriptor(new BLE2902());

	pService->addCharacteristic(&outHumidityCharacteristic);
	pService->addCharacteristic(&outTemperatureCharacteristic);
	pService->start();

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
	pServer->getAdvertising()->setAdvertisementData(oAdvertisementData);

	BLEAdvertisementData oScanResponseData = BLEAdvertisementData();
	oScanResponseData.setName(BLE_DEVICE_NAME);
	pServer->getAdvertising()->setScanResponseData(oScanResponseData);

}

