package com.example.blebridge;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.blebridge.BLEFacade.BLEManager;
import com.example.blebridge.BLEFacade.SfDevice;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class ScanListAdapter extends RecyclerView.Adapter<ScanListAdapter.ViewHolder> {
    private static final String TAG = ScanListAdapter.class.getSimpleName();

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView viewDeviceMacAddress;
        private final TextView viewDeviceName;
        private final TextView viewDeviceModelName;
        private final TextView viewDeviceSignalStrength;
        private final ImageView viewDeviceImage;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Log.d(TAG, "Element " + position + " clicked.");
                    BLEManager.getInstance().connectScanned(position);
                }
            });
            viewDeviceMacAddress = (TextView) v.findViewById(R.id.deviceMacAddress);
            viewDeviceName = (TextView) v.findViewById(R.id.deviceName);
            viewDeviceModelName = (TextView) v.findViewById(R.id.deviceModelName);
            viewDeviceSignalStrength = (TextView) v.findViewById(R.id.deviceSignalStrength);
            viewDeviceImage = (ImageView) v.findViewById(R.id.deviceImage);
        }

        public TextView getViewDeviceName() {
            return viewDeviceName;
        }

        public TextView getViewDeviceModelName() {
            return viewDeviceModelName;
        }

        public TextView getViewDeviceMacAddress() {
            return viewDeviceMacAddress;
        }

        public TextView getViewDeviceSignalStrength() {
            return viewDeviceSignalStrength;
        }

        public ImageView getViewDeviceImage() {
            return viewDeviceImage;
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.device_item, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        SfDevice SfDevice = BLEManager.getInstance().getSFAHDevice(position);
        viewHolder.getViewDeviceName().setText(SfDevice.getName());
        viewHolder.getViewDeviceModelName().setText(SfDevice.getModelName());
        viewHolder.getViewDeviceMacAddress().setText(SfDevice.getMacAddress());
        viewHolder.getViewDeviceSignalStrength().setText(Integer.toString(SfDevice.getSignalStrength()));
        viewHolder.getViewDeviceImage().setImageResource(R.drawable.dev1_sensor_only);
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return BLEManager.getInstance().getDeviceCountScanned();
    }
}
