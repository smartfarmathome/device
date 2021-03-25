package com.example.blebridge;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blebridge.BLEFacade.BLEManager;
import com.example.blebridge.BLEFacade.SFAHDevice;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DeviceListFragment extends Fragment {
    private static final String TAG = DeviceListFragment.class.getSimpleName();
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
            SFAHDevice device = new SFAHDevice();
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
                        Toast.makeText(getContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int permission = ContextCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION );
                    if (permission == PackageManager.PERMISSION_DENIED ) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == true) {
                            Toast.makeText(getContext(), R.string.should_be_permitted, Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (permission != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions( getActivity(), new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, 1);
                        return;
                    }
                    if (!BLEManager.getInstance().isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        getActivity().startActivityForResult(enableBtIntent, 1);
                    }
                    NavHostFragment.findNavController(DeviceListFragment.this)
                            .navigate(R.id.action_DeviceListFragment_to_ScanFragment);
                }
            });
            fab.setImageResource(android.R.drawable.ic_input_add);
        }
    }
}