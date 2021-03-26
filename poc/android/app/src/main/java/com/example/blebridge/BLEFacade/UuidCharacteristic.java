package com.example.blebridge.BLEFacade;

class UuidCharacteristic {
    String uuidServ;
    String uuidChar;

    public UuidCharacteristic(String uuidServ, String uuidChar) {
        this.uuidServ = uuidServ;
        this.uuidChar = uuidChar;
    }

    public boolean equals(UuidCharacteristic other) {
        return this.uuidChar.equals(other.uuidChar) && this.uuidServ.equals(other.uuidServ);
    }
}
