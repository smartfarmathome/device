package com.example.blebridge.BLEFacade.Actions;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

public class BleActionReadCharacteristic extends BleAction {
    String uuidServ, uuidChar;

    public BleActionReadCharacteristic(BluetoothGatt gatt, String uuidServ, String uuidChar) {
        super(gatt);
        this.uuidServ = uuidServ;
        this.uuidChar = uuidChar;
    }

    @Override
    public void doAction() {
        BluetoothGattService service = gatt.getService(UUID.fromString(uuidServ));
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuidChar));
        gatt.readCharacteristic(characteristic);
    }
}
