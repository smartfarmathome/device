package com.example.blebridge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blebridge.BLEFacade.BLEManager;
import com.example.blebridge.BLEFacade.BleCallbacks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ScanFragment extends Fragment {
    private static final String TAG = ScanFragment.class.getSimpleName();
    protected RecyclerView mRecyclerView;
    protected ScanListAdapter mAdapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.scan_list_fragment, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.scan_list_recycler_view);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new ScanListAdapter();
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // END_INCLUDE(initializeRecyclerView)

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton)getActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BLEManager.getInstance().stopScanLeDevice();
                    NavHostFragment.findNavController(ScanFragment.this)
                            .navigate(R.id.action_ScanFragment_to_DeviceListFragment);
                }
            });
            fab.setImageResource(android.R.drawable.ic_media_rew);
        }
        BLEManager.getInstance().startScanLeDevice(bleCallbacks);
    }

    BleCallbacks bleCallbacks = new BleCallbacks() {
        @Override
        public void onScanFound(int i) {
            mAdapter.notifyItemInserted(i);
        }
    };
}