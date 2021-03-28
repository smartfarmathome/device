package com.example.blebridge.BLEFacade.Actions;

import android.bluetooth.BluetoothGatt;

public class BleActionDiscoverServices extends BleAction {
    public BleActionDiscoverServices(BluetoothGatt gatt) {
        super(gatt);
    }

    @Override
    public void doAction() {
        gatt.discoverServices();
    }
}
