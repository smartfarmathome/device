package com.example.blebridge.BLEFacade;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;

import com.example.blebridge.DeviceListAdapter;

import java.util.UUID;

public class SFAHDevice {
    private static final String TAG = DeviceListAdapter.class.getSimpleName();
    private UUID uuid;
    private String macAddress;
    private String name;
    private String modelName;
    private int signalStrength;
    public BluetoothDevice bleDevice;
    Bundle characteristics;

    public SFAHDevice() {
        uuid = UUID.randomUUID();
        name = "name";
        modelName = "ABC-1234";
        macAddress = "00:03:78:11:22:33";
        signalStrength = 50;
        Log.d(TAG, "BLE device is created: UUID " + uuid.toString());
    }

    public SFAHDevice(BluetoothDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public SFAHDevice(BluetoothDevice bleDevice, int rssi) {
        this.bleDevice = bleDevice;
        this.macAddress = bleDevice.getAddress();
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

    void setCharacteristic(String uuid, byte[] value) {
        Log.d(TAG, "setCharacteristic(" + uuid + ", " + value.toString());
        characteristics.putByteArray(uuid, value);
    }
}