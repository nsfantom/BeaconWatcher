package tm.nsfantom.beaconwatcher.ui.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import java.util.UUID;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.ui.adapter.LeDevicesAdapter;
import tm.nsfantom.beaconwatcher.ui.fragment.base.BaseMonitorFragment;
import tm.nsfantom.beaconwatcher.util.Constants;
import tm.nsfantom.beaconwatcher.util.SampleGattAttributes;

public class MainFragment extends BaseMonitorFragment {


    private LeDevicesAdapter adapter;

    private UUID[] scanUUIDs = new UUID[]{
            UUID.fromString(SampleGattAttributes.GEAR_S2_LE),
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT),
            UUID.fromString(SampleGattAttributes.INFORMU_MU_TAG)
    };

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    protected void init() {
        adapter = new LeDevicesAdapter();
        getBTAdapter();
        // Initializes list view adapter.
        layout.rvDevice.setLayoutManager(new LinearLayoutManager(getContext()));
        layout.rvDevice.setAdapter(adapter);
        adapter.setItemClickedListener(device -> {
            if (device == null) return;
            listener.onDeviceClicked(device);
            Toast.makeText(getContext(), "device: " + device.getName(), Toast.LENGTH_SHORT).show();
        });

        layout.toggleRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(buttonView.getId() == layout.toggleRefresh.getId())
                Timber.e("toggle from button");
            if (isChecked) {
                adapter.clear();
            }
            startScan(isChecked);
        });
    }

    @Override
    protected void startScan(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                isScanning = false;
                bluetoothAdapter.stopLeScan(mLeScanCallback);
                layout.progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
                layout.toggleRefresh.setChecked(isScanning);
            }, Constants.SCAN_PERIOD);

            isScanning = true;

            if (layout.switchMuTag.isChecked()) {
                bluetoothAdapter.startLeScan(scanUUIDs, mLeScanCallback);
            } else {
                bluetoothAdapter.startLeScan(mLeScanCallback);
            }
        } else {
            isScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        layout.toggleRefresh.setChecked(isScanning);
        layout.progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    getActivity().runOnUiThread(() -> {
                        adapter.addDevice(device);
                        adapter.notifyDataSetChanged();
                    });
                }
            };

    public double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

}
