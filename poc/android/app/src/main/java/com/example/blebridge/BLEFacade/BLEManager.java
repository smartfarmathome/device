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

import com.example.blebridge.BLEFacade.Actions.BleAction;
import com.example.blebridge.BLEFacade.Actions.BleActionClose;
import com.example.blebridge.BLEFacade.Actions.BleActionDiscoverServices;
import com.example.blebridge.BLEFacade.Actions.BleActionReadCharacteristic;
import com.example.blebridge.BLEFacade.Actions.BleActionRequestMtu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.example.blebridge.BLEFacade.Constants.BLE_UUID;
import static com.example.blebridge.BLEFacade.Constants.BLE_UUID_SERVICE_ENVIRONMENTAL_SENSING;

public class BLEManager {
    private static final String TAG = BLEManager.class.getSimpleName();
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private boolean mScanning;
    private Handler handler;
    private BluetoothLeScanner bleScanner;
    private static final long SCAN_PERIOD = 10000; // Stops scanning after 10 seconds.
    private static final int GATT_MAX_MTU_SIZE = 517;

    private BleCallbacks bleCallbacks;
    private List<SfDevice> devicesAll;
    private List<SfDevice> devicesListed;
    private List<SfDevice> devicesScanned;
    private SfDevice deviceToConnect;
    private SfDevice deviceConnected;
    private BluetoothGatt gattConnected;
    private int indexToRead;

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
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }

    public void setCallbacks(BleCallbacks bleCallbacks) {
        this.bleCallbacks = bleCallbacks;
    }

    private Runnable stopScanByTimer = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "stop BLE scan by timer");
            mScanning = false;
            bleScanner.stopScan(leScanCallback);
        }
    };

    public void startScanLeDevice() {
        if (mScanning == false) {
            Log.d(TAG, "start BLE scan");
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(stopScanByTimer, SCAN_PERIOD);

            List<ScanFilter> filters = new ArrayList<>();
            //filters.add(new ScanFilter.Builder().setDeviceName("SFatHome").build());
            filters.add(new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString(BLE_UUID(BLE_UUID_SERVICE_ENVIRONMENTAL_SENSING)))
                    .build());

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

    public void stopScanLeDevice() {
        if (mScanning) {
            Log.d(TAG, "stop BLE scan");
            mScanning = false;
            handler.removeCallbacks(stopScanByTimer);
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
            BluetoothDevice bleDeviceToAdd = result.getDevice();
            int i = 0;
            for (SfDevice device : devicesScanned) {
                if (device.bleDevice.getAddress().equals(bleDeviceToAdd.getAddress())) {
                    Log.d(TAG, "update RSSI " + rssi + " of the device scanned already: " + bleDeviceToAdd.getAddress());
                    device.setRssi(rssi);
                    if (BLEManager.this.bleCallbacks != null) {
                        BLEManager.this.bleCallbacks.onItemChanged(i);
                    }
                    return;
                }
                i++;
            }
            SfDevice deviceToAdd = new SfDevice(bleDeviceToAdd, rssi);
            devicesScanned.add(deviceToAdd);
            if (BLEManager.this.bleCallbacks != null) {
                BLEManager.this.bleCallbacks.onItemInserted(devicesScanned.size() - 1);
            }
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            pendingAction = null;
            BluetoothDevice device = gatt.getDevice();
            Log.d(TAG, "onConnectionStateChange(" + gatt.toString() + ", " + status + ", " + newState + ")");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Successfully connected to " + device.getAddress());
                    deviceConnected = deviceToConnect;
                    deviceToConnect = null;
                    gattConnected = gatt;
                    enqueueAction(new BleActionDiscoverServices(gatt));
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "Successfully disconnected from " + device.getAddress());
                }
            } else {
                Log.d(TAG, "Error for " + device.getAddress() + ", status = " + status);
                gatt.close();
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            pendingAction = null;
            Log.d(TAG, "onMtuChanged(" + mtu + ", " + status + ")");
            doNextAction();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            pendingAction = null;
            Log.d(TAG, "onServicesDiscovered(" + status + ")");
            enqueueAction(new BleActionRequestMtu(gatt, GATT_MAX_MTU_SIZE));
            deviceConnected.setCharacteristicsToRead(getAllCharacteristics(gatt));
            for (UuidCharacteristic uuidCharacteristic: deviceConnected.characteristicsToRead) {
                enqueueAction(new BleActionReadCharacteristic(gatt, uuidCharacteristic.uuidServ, uuidCharacteristic.uuidChar));
            }
            enqueueAction(new BleActionClose(gatt));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            pendingAction = null;
            String charUuid = characteristic.getUuid().toString();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] value = characteristic.getValue();
                //Log.d(TAG, "onCharacteristicRead(" + gatt + ", " + charUuid + ", " + value + ")");
                deviceConnected.setCharacteristic(charUuid, value);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (BLEManager.this.bleCallbacks != null) {
                            BLEManager.this.bleCallbacks.onItemChanged(indexToRead);
                        }
                    }
                });
            } else {
                Log.d(TAG, "onCharacteristicRead() " + charUuid + ", status error = " + status);
            }
            doNextAction();
        }
    };

    private ConcurrentLinkedQueue<BleAction> actionQueue = new ConcurrentLinkedQueue<>();
    private BleAction pendingAction;

    synchronized void enqueueAction(BleAction action) {
        actionQueue.add(action);
        if (pendingAction == null) {
            doNextAction();
        }
    }

    synchronized private void doNextAction() {
        if (pendingAction != null) {
            Log.d(TAG, "doNextAction() called when an operation is pending! Aborting.");
            return;
        }
        BleAction action = actionQueue.poll();
        if (action == null) {
            Log.d(TAG, "Operation queue empty, returning");
            return;
        }
        pendingAction = action;
        action.doAction();
    }

    public synchronized void reset() {
        actionQueue.clear();
        if (gattConnected != null) {
            actionQueue.add(new BleActionClose(gattConnected));
            gattConnected = null;
        }
        deviceConnected = null;
        stopScanLeDevice();
    }

    private List<UuidCharacteristic> getAllCharacteristics(BluetoothGatt gatt) {
        List<UuidCharacteristic> uuidCharacteristics = new ArrayList<>();
        for (BluetoothGattService service : gatt.getServices()) {
            String uuidServ = service.getUuid().toString();
            for (BluetoothGattCharacteristic charac : service.getCharacteristics()) {
                String uuidChar = charac.getUuid().toString();
                Log.d(TAG, " * " + uuidServ + ", " + uuidChar);
                UuidCharacteristic uuidCharacteristic = new UuidCharacteristic(uuidServ, uuidChar);
                uuidCharacteristics.add(uuidCharacteristic);
            }
        }
        Log.d(TAG, "getAllCharacteristics() total " + uuidCharacteristics.size());
        return uuidCharacteristics;
    }

    public void connectScanned(int i) {
        try {
            stopScanLeDevice();
            SfDevice device = devicesScanned.get(i);
            connectBleDevice(device);
            indexToRead = i;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public int getDeviceCountScanned() {
        return devicesScanned.size();
    }

    public SfDevice getSFAHDevice(int i) {
        try {
            return devicesScanned.get(i);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void connectBleDevice(SfDevice device) {
        deviceToConnect = device;
        device.bleDevice.connectGatt(context, false, gattCallback);
    }
}
