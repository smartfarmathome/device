package com.example.blebridge.BLEFacade;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEManager {
    private static final String TAG = BLEManager.class.getSimpleName();
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private boolean mScanning;
    private Handler handler;
    private BluetoothLeScanner bleScanner;
    private static final long SCAN_PERIOD = 10000; // Stops scanning after 10 seconds.
    private static final String BLE_UUID_PREFIX = "0000";
    private static final String BLE_UUID_POSTFIX = "-0000-1000-8000-00805f9b34fb";
    private static final String BLE_UUID_SERVICE_ENVIRONMENTAL_SENSING = "181a";
    private static final String BLE_UUID_CHAR_TEMPERATURE = "2a6e";
    private static final String BLE_UUID_CHAR_HUMIDITY = "2a6f";
    private static final String BLE_UUID_SERVICE_DEVICE_INFORMATION = "180a";
    private static final String BLE_UUID_CHAR_MANUFACTURER_NAME = "2a29";
    private static final String BLE_UUID_CHAR_MODEL_NUMBER = "2a24";
    private static final String BLE_UUID_CHAR_SOFTWARE_REVISION = "2a28";
    private static final ParcelUuid parcelUuidServiceDeviceInformation = ParcelUuid.fromString(BLE_UUID(BLE_UUID_SERVICE_DEVICE_INFORMATION));
    private static final int GATT_MAX_MTU_SIZE = 517;

    private BleCallbacks bleCallbacks;
    private List<SFAHDevice> devicesAll;
    private List<SFAHDevice> devicesListed;
    private List<SFAHDevice> devicesScanned;
    private SFAHDevice deviceToConnect;
    private SFAHDevice deviceConnected;
    private int deviceConnectedIndex;
    private BluetoothGatt gattConnected;

    class UuidCharacteristic {
        String uuidServ;
        String uuidChar;
        public UuidCharacteristic(String uuidServ, String uuidChar) {
            this.uuidServ = uuidServ;
            this.uuidChar = uuidChar;
        }
    }

    List<UuidCharacteristic> uuidCharacteristics;

    private static String BLE_UUID(String uuid) {
        return BLE_UUID_PREFIX + uuid + BLE_UUID_POSTFIX;
    }

    private BLEManager() {
    }

    private static class Singleton {
        public static final BLEManager instance = new BLEManager();
    }

    public static BLEManager getInstance() {
        return Singleton.instance;
    }

    public void init(Context context) {
        this.context = context;
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return;
        }
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        handler = new Handler(context.getMainLooper());
    }

    public boolean isAvailable() {
        return bluetoothAdapter != null;
    }

    public boolean isEnabled() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return true;
        }
        return false;
    }

    public void startScanLeDevice() {
        if (mScanning == false) {
            Log.d(TAG, "start BLE scan");
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "stop BLE scan by timer");
                    mScanning = false;
                    BLEManager.this.bleCallbacks = null;
                    bleScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            /*
            ScanFilter scan_filter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString(BLE_UUID(BLE_UUID_SERVICE_ENVIRONMENTAL_SENSING)),
                            ParcelUuid.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
                    .build();
             */
            ScanFilter scan_filter = new ScanFilter.Builder().setDeviceName("SFAH").build();
            List<ScanFilter> filters = new ArrayList<>();
            filters.add(scan_filter);

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .build();

            bleScanner.startScan(filters, settings, leScanCallback);
            devicesScanned = new ArrayList<>();
            mScanning = true;
        } else {
            Log.d(TAG, "??? WARNING: startScanLeDevice() while scanning in progress");
        }
    }

    public void startScanLeDevice(BleCallbacks bleCallbacks) {
        this.startScanLeDevice();
        this.bleCallbacks = bleCallbacks;
    }

    public void stopScanLeDevice() {
        if (mScanning) {
            Log.d(TAG, "stop BLE scan");
            mScanning = false;
            this.bleCallbacks = null;
            bleScanner.stopScan(leScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult() callbackType = " + callbackType);
            processScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResult() len = " + results.size());
            for (ScanResult result : results) {
                processScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed() errorCode = " + errorCode);
        }

        private void processScanResult(final ScanResult result) {
            int rssi = result.getRssi();
            Log.d(TAG, "processScanResult() " + result.toString() + ", rssi = " + rssi);
            SFAHDevice device = new SFAHDevice(result.getDevice(), rssi);
            devicesScanned.add(device);
            if (BLEManager.this.bleCallbacks != null) {
                BLEManager.this.bleCallbacks.onScanFound(devicesScanned.size() - 1);
            }
        }
    };

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            BluetoothDevice device = gatt.getDevice();
            Log.d(TAG, "onConnectionStateChange(" + gatt.toString() + ", " + status + ", " + newState + ")");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Successfully connected to " + device.getAddress());
                    deviceConnected = deviceToConnect;
                    deviceToConnect = null;
                    gattConnected = gatt;
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "Successfully disconnected from " + device.getAddress());
                }
            } else {
                Log.d(TAG, "Error for " + device.getAddress() + ", status = " + status);
                gatt.close();
                gattConnected = null;
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.d(TAG, "onMtuChanged(" + mtu + ", " + status + ")");
            readNextCharacteristic(gatt);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "onServicesDiscovered(" + gatt + ", " + status + ")");
            getAllCharacteristics(gatt);
            gatt.requestMtu(GATT_MAX_MTU_SIZE);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead(" + gatt + ", " + characteristic + ", " + status + ")");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String charUuid = characteristic.getUuid().toString();
                byte[] value = characteristic.getValue();
                deviceConnected.setCharacteristic(charUuid, value);
            } else {
                Log.d(TAG, "status error " + status);
            }
        }
    };

    public void reset() {
        if (gattConnected != null) {
            gattConnected.close();
            gattConnected = null;
        }
    }

    private void readNextCharacteristic(BluetoothGatt gatt) {
        if (uuidCharacteristics == null) {
            Log.d(TAG, "!!! WARNING: uuidCharacteristics is null");
            return;
        } else if (uuidCharacteristics.size() == 0) {
            Log.d(TAG, "no characteristics to read");
            return;
        }
        UuidCharacteristic uuidCharacteristic = uuidCharacteristics.get(0);
        BluetoothGattService service = gatt.getService(UUID.fromString(uuidCharacteristic.uuidServ));
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuidCharacteristic.uuidChar));
        Log.d(TAG, "try to read characteristic " + uuidCharacteristic.uuidChar);
        gatt.readCharacteristic(characteristic);
    }

    private void getAllCharacteristics(BluetoothGatt gatt) {
        uuidCharacteristics = new ArrayList<>();
        for (BluetoothGattService service: gatt.getServices()) {
            String uuidServ = service.getUuid().toString();
            for (BluetoothGattCharacteristic charac: service.getCharacteristics()) {
                String uuidChar = charac.getUuid().toString();
                Log.d(TAG, " * " + uuidServ + ", " + uuidChar);
                UuidCharacteristic uuidCharacteristic = new UuidCharacteristic(uuidServ, uuidChar);
                uuidCharacteristics.add(uuidCharacteristic);
            }
        }
        Log.d(TAG, "getAllCharacteristics() total " + uuidCharacteristics.size());
    }

    public void connectScanned(int i) {
        try {
            SFAHDevice device = devicesScanned.get(i);
            stopScanLeDevice();
            connectBleDevice(device);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }
    }

    public int getDeviceCountScanned() {
        return devicesScanned.size();
    }

    public SFAHDevice getSFAHDevice(int i) {
        try {
            return devicesScanned.get(i);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void connectBleDevice(SFAHDevice device) {
        uuidCharacteristics = null;
        deviceToConnect = device;
        device.bleDevice.connectGatt(context, false, gattCallback);
    }
}
