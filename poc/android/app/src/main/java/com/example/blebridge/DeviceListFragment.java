package com.example.blebridge;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blebridge.BLEFacade.BLEManager;
import com.example.blebridge.BLEFacade.SfDevice;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DeviceListFragment extends Fragment {
    private static final String TAG = DeviceListFragment.class.getSimpleName();

    static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    protected RecyclerView mRecyclerView;
    protected DeviceListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.device_list_fragment, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.device_list_recycler_view);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new DeviceListAdapter();
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // END_INCLUDE(initializeRecyclerView)

        for (int i = 0; i < 10; i++) {
            SfDevice device = new SfDevice();
            mAdapter.addDevice(device);
        }

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // check to determine whether BLE is supported on the device.
                    if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                        Log.d(TAG, "Cannot start BLE because this device does not support Bluetooth LE.");
                        Toast.makeText(getContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (checkBluetoothEnabled() == false) {
                        return;
                    }

                    if (checkLocationPermission() == false) {
                        return;
                    }

                    NavHostFragment.findNavController(DeviceListFragment.this)
                            .navigate(R.id.action_DeviceListFragment_to_ScanFragment);
                }
            });
            fab.setImageResource(android.R.drawable.ic_input_add);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() " + requestCode);
        if (requestCode == ENABLE_BLUETOOTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Bluetooth is enabled by the user.");
            } else {
                Log.d(TAG, "Bluetooth is not enabled.");
            }
        }
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "ACCESS_FINE_LOCATION is granted by the user.");
                NavHostFragment.findNavController(DeviceListFragment.this)
                        .navigate(R.id.action_DeviceListFragment_to_ScanFragment);
            } else {
                Log.d(TAG, "ACCESS_FINE_LOCATION is denied by the user.");
                Toast.makeText(getContext(), R.string.fine_location_should_be_permitted, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean checkBluetoothEnabled() {
        if (!BLEManager.getInstance().isEnabled()) {
            Log.d(TAG, "Cannot start BLE because bluetooth is not enabled.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            getActivity().startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private boolean checkLocationPermission() {
        int permission = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "ACCESS_FINE_LOCATION is not permitted for this app.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == true) {
                Log.d(TAG, "Show the user why ACCESS_FINE_LOCATION permission is required.");
                Toast.makeText(getContext(), R.string.fine_location_should_be_permitted, Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Request ACCESS_FINE_LOCATION permission to the user.");
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
            return false;
        }
        return true;
    }
}