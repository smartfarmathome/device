package com.example.blebridge.BLEFacade;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;

import com.example.blebridge.DeviceListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.blebridge.BLEFacade.Constants.BLE_UUID;
import static com.example.blebridge.BLEFacade.Constants.BLE_UUID_CHAR_MANUFACTURER_NAME;
import static com.example.blebridge.BLEFacade.Constants.BLE_UUID_CHAR_MODEL_NUMBER;
import static com.example.blebridge.BLEFacade.Constants.BLE_UUID_CHAR_SOFTWARE_REVISION;
import static com.example.blebridge.BLEFacade.Constants.characteristicsPreferred;

public class SfDevice {
    private static final String TAG = DeviceListAdapter.class.getSimpleName();
    private UUID uuid;
    private String macAddress;
    private String name;
    private String modelName;
    private String manufacturerName;
    private String softwareVersion;
    private int signalStrength;
    public BluetoothDevice bleDevice;
    List<UuidCharacteristic> characteristicsToRead;
    int indexToRead;
    Bundle characteristicsRead;

    public SfDevice() {
        uuid = UUID.randomUUID();
        name = "name";
        modelName = "ABC-1234";
        macAddress = "00:03:78:11:22:33";
        signalStrength = 50;
        Log.d(TAG, "BLE device is created: UUID " + uuid.toString());
    }

    public SfDevice(BluetoothDevice bleDevice) {
        this.bleDevice = bleDevice;
        this.macAddress = bleDevice.getAddress();
    }

    public SfDevice(BluetoothDevice bleDevice, int rssi) {
        this(bleDevice);
        this.signalStrength = rssi;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getName() {
        return name;
    }

    public String getModelName() {
        return modelName;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setRssi(int rssi) {
        this.signalStrength = rssi;
    }

    @Override
    public String toString() {
        return "Device{" +
                "uuid=" + uuid +
                ", macAddress='" + macAddress + '\'' +
                ", name='" + name + '\'' +
                ", modelName='" + modelName + '\'' +
                ", signalStrength=" + signalStrength +
                '}';
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

    void setCharacteristic(String uuid, byte[] value) {
        Log.d(TAG, "setCharacteristic() " + uuid + ", " + bytesToHex(value));
        if (uuid.equals(BLE_UUID(BLE_UUID_CHAR_MANUFACTURER_NAME))) {
            manufacturerName = value.toString();
        } else if (uuid.equals(BLE_UUID(BLE_UUID_CHAR_MODEL_NUMBER))) {
            modelName = value.toString();
        } else if (uuid.equals(BLE_UUID(BLE_UUID_CHAR_SOFTWARE_REVISION))) {
            softwareVersion = value.toString();
        } else {
            characteristicsRead.putByteArray(uuid, value);
        }
    }

    void setCharacteristicsToRead(List<UuidCharacteristic> uuidCharacteristicsInDevice) {
        characteristicsToRead = new ArrayList<>();
        indexToRead = 0;
        characteristicsRead = new Bundle();
        for (UuidCharacteristic uuidCharacteristicPreferred : characteristicsPreferred) {
            for (UuidCharacteristic uuidCharacteristicInDevice : uuidCharacteristicsInDevice) {
                if (uuidCharacteristicPreferred.equals(uuidCharacteristicInDevice)) {
                    characteristicsToRead.add(uuidCharacteristicInDevice);
                    Log.d(TAG, "setCharacteristicsToRead() add " + uuidCharacteristicInDevice.uuidChar);
                }
            }
        }
        Log.d(TAG, "setCharacteristicsToRead() total " + characteristicsToRead.size());
    }

    UuidCharacteristic getNextCharacteristicToRead() {
        if (indexToRead < characteristicsToRead.size()) {
            UuidCharacteristic characteristicToRead = characteristicsToRead.get(indexToRead);
            indexToRead++;
            return characteristicToRead;
        }
        return null;
    }

    public boolean equals(SfDevice other) {
        return this.bleDevice.getAddress().equals(other.bleDevice.getAddress());
    }
}
