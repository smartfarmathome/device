package com.example.blebridge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.util.List;

public class BLEManager {
    private static final String TAG = BLEManager.class.getSimpleName();
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private boolean mScanning;
    private Handler handler;
    private BluetoothLeScanner bleScanner;
    private static final long SCAN_PERIOD = 10000; // Stops scanning after 10 seconds.
    private static final String BLE_UUID_ENVIRONMENTAL_SENSING = "0000181a-0000-1000-8000-00805f9b34fb";

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

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.d(TAG, "start BLE scan");
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "stop BLE scan by timer");
                    mScanning = false;
                    bleScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bleScanner.startScan(leScanCallback);
        } else {
            Log.d(TAG, "stop BLE scan");
            mScanning = false;
            bleScanner.stopScan(leScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult() callbackType = " + callbackType);
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResult() len = " + results.size());
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed() errorCode = " + errorCode);
        }

        private void processResult(final ScanResult result) {
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            Log.d(TAG, "onLeScan() " + device.toString() + ", rssi = " + rssi);
        }
    };
}
