package com.example.blebridge.BLEFacade.Actions;

import android.bluetooth.BluetoothGatt;

public abstract class BleAction {
    BluetoothGatt gatt;

    public BleAction(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public abstract void doAction();
}
