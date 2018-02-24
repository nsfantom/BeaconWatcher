package tm.nsfantom.beaconwatcher.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.databinding.FragmentMainBinding;
import tm.nsfantom.beaconwatcher.ui.LeDevicesAdapter;
import tm.nsfantom.beaconwatcher.util.Constants;
import tm.nsfantom.beaconwatcher.util.SampleGattAttributes;
import tm.nsfantom.beaconwatcher.util.UuidUtil;

public class MainFragment extends Fragment {

    private Listener listener;
    private FragmentMainBinding layout;
    private LeDevicesAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isScanning;
    private Handler mHandler;
    private UUID[] scanUUIDs = new UUID[]{
            UUID.fromString(SampleGattAttributes.GEAR_S2_LE),
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT),
            UUID.fromString(SampleGattAttributes.INFORMU_MU_TAG)
    };

    public static MainFragment newInstance(BluetoothAdapter bluetoothAdapter) {
        MainFragment mainFragment = new MainFragment();
        mainFragment.bluetoothAdapter = bluetoothAdapter;
        return mainFragment;
    }

    public interface Listener {
        void onDeviceClicked(BluetoothDevice device);
    }

    @Override
    public void onAttach(Context context) {
        if (!(getActivity() instanceof Listener)) {
            throw new IllegalStateException("Activity must implement fragment Listener.");
        }
        super.onAttach(context);
        adapter = new LeDevicesAdapter();
        listener = (Listener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        return layout.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new Handler();
        adapter.setItemClickedListener(device -> {
            if (device == null) return;
            listener.onDeviceClicked(device);
            Toast.makeText(getContext(), "device: " + device.getName(), Toast.LENGTH_SHORT).show();
        });
        layout.toggleRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                adapter.clear();
            }
            scanLeDevice(isChecked);
        });


    }

    private void scanLeDevice(final boolean enable) {
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
        layout.progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initializes list view adapter.
        layout.rvDevice.setLayoutManager(new LinearLayoutManager(getContext()));
        layout.rvDevice.setAdapter(adapter);
        scanLeDevice(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
        adapter.clear();
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
