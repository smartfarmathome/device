package com.example.blebridge.model;

import android.util.Log;

import com.example.blebridge.DeviceListAdapter;

import java.util.UUID;

public class Device {
    private static final String TAG = DeviceListAdapter.class.getSimpleName();
    private UUID uuid;
    private String macAddress;

    public Device() {
        uuid = UUID.randomUUID();
        macAddress = "00:03:78:11:22:33";
        Log.d(TAG, "BLE device is created: UUID " + uuid.toString());
    }

    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public String toString() {
        return "Device{" +
                "uuid=" + uuid +
                '}';
    }
}
