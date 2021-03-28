package com.example.blebridge.BLEFacade.Actions;

import android.bluetooth.BluetoothGatt;

public class BleActionClose extends BleAction {
    public BleActionClose(BluetoothGatt gatt) {
        super(gatt);
    }

    @Override
    public void doAction() {
        gatt.close();
    }
}
