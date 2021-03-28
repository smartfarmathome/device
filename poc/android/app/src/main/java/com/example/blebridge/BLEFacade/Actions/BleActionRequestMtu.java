package com.example.blebridge.BLEFacade.Actions;

import android.bluetooth.BluetoothGatt;

public class BleActionRequestMtu extends BleAction {
    int mtu;

    public BleActionRequestMtu(BluetoothGatt gatt, int mtu) {
        super(gatt);
        this.mtu = mtu;
    }

    @Override
    public void doAction() {
        gatt.requestMtu(mtu);
    }
}
