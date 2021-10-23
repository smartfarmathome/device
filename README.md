# device

This is a part of Smart Farm at Home project and implements the device part in the picture.

```
+----------+               +--------------+                    +----------+
|  device  |  <---BLE--->  |  smartphone  |  <---Internet--->  |  server  |
+----------+               +--------------+                    +----------+
```

The device periodically collects environmental data such as temperature, humidity, and light intensity and delivers it to a smartphone via Bluetooth Low Energy or BLE. 

# The Current Status

It's in the middle of PoC (Proof of Concept) implementation. ESP32, which has BLE, is used for the device.

* Support the BLE Environmental Sensing profile, to transfer the temperature and humidity data.
* The BLE Environmental Sensing profile is verified with the public application such as "nRF Connect for Mobile" and "BLE Scanner 4.0" running on iPhone.
* No sensors on the board for now, so the data is fake.
* The test app on Android smartphone, to verify the scanning and collecting data.
