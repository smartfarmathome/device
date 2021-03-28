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

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
