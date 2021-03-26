package com.example.blebridge.BLEFacade;

public class Constants {
    static final String BLE_UUID_PREFIX = "0000";
    static final String BLE_UUID_POSTFIX = "-0000-1000-8000-00805f9b34fb";
    static final String BLE_UUID_SERVICE_ENVIRONMENTAL_SENSING = "181a";
    static final String BLE_UUID_CHAR_TEMPERATURE = "2a6e";
    static final String BLE_UUID_CHAR_HUMIDITY = "2a6f";
    static final String BLE_UUID_SERVICE_DEVICE_INFORMATION = "180a";
    static final String BLE_UUID_CHAR_MANUFACTURER_NAME = "2a29";
    static final String BLE_UUID_CHAR_MODEL_NUMBER = "2a24";
    static final String BLE_UUID_CHAR_SOFTWARE_REVISION = "2a28";

    static final UuidCharacteristic[] characteristicsPreferred = {
            new UuidCharacteristic(BLE_UUID(BLE_UUID_SERVICE_DEVICE_INFORMATION), BLE_UUID(BLE_UUID_CHAR_MANUFACTURER_NAME)),
            new UuidCharacteristic(BLE_UUID(BLE_UUID_SERVICE_DEVICE_INFORMATION), BLE_UUID(BLE_UUID_CHAR_MODEL_NUMBER)),
            new UuidCharacteristic(BLE_UUID(BLE_UUID_SERVICE_DEVICE_INFORMATION), BLE_UUID(BLE_UUID_CHAR_SOFTWARE_REVISION)),
            new UuidCharacteristic(BLE_UUID(BLE_UUID_SERVICE_ENVIRONMENTAL_SENSING), BLE_UUID(BLE_UUID_CHAR_TEMPERATURE)),
            new UuidCharacteristic(BLE_UUID(BLE_UUID_SERVICE_ENVIRONMENTAL_SENSING), BLE_UUID(BLE_UUID_CHAR_HUMIDITY)),
    };

    static String BLE_UUID(String uuid) {
        return BLE_UUID_PREFIX + uuid + BLE_UUID_POSTFIX;
    }
}
