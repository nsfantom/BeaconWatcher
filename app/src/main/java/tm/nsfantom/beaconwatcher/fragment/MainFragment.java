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
    private ScanFilter scanFilter;
    private ScanSettings scanSettings;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setScanFilter();
            setScanSettings();
            Timber.e("AdvertisementSupported: %s", bluetoothAdapter.isMultipleAdvertisementSupported());
            Toast.makeText(getContext(),
                    "AdvertisementSupported: " + bluetoothAdapter.isMultipleAdvertisementSupported(),
                    Toast.LENGTH_SHORT).show();
        }
        adapter.setItemClickedListener(device -> {
            if (device == null) return;
            listener.onDeviceClicked(device);
            Toast.makeText(getContext(), "device: " + device.getName(), Toast.LENGTH_SHORT).show();
        });
        layout.toggleRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                adapter.clear();
            }

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                scannerLeDevice(isChecked);
//            else
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void scannerLeDevice(final boolean enable) {

        final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();


        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                isScanning = false;
                bluetoothLeScanner.stopScan(mLeScannerCallback);
                layout.progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
                layout.toggleRefresh.setChecked(isScanning);
            }, Constants.SCAN_PERIOD);

            isScanning = true;
            if (layout.switchMuTag.isChecked()) {
                bluetoothLeScanner.startScan(Collections.singletonList(scanFilter), scanSettings, mLeScannerCallback);
            } else {
                bluetoothLeScanner.startScan(mLeScannerCallback);
            }
        } else {
            isScanning = false;
            bluetoothLeScanner.stopScan(mLeScannerCallback);
        }
        layout.progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initializes list view adapter.
        layout.rvDevice.setLayoutManager(new LinearLayoutManager(getContext()));
        layout.rvDevice.setAdapter(adapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            scannerLeDevice(true);
        else
            scanLeDevice(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            scannerLeDevice(false);
        else
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

    // Device scan callback. api21+
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback mLeScannerCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            switch (callbackType) {
                case ScanSettings.CALLBACK_TYPE_ALL_MATCHES:
                case ScanSettings.CALLBACK_TYPE_FIRST_MATCH:
                    getActivity().runOnUiThread(() -> adapter.addDevice(result.getDevice()));
                    break;
                case ScanSettings.CALLBACK_TYPE_MATCH_LOST:
                    getActivity().runOnUiThread(() -> adapter.removeDevice(result.getDevice()));
                    break;
            }
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void setScanFilter() {
        ScanFilter.Builder mBuilder = new ScanFilter.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
        ByteBuffer mManufacturerDataMask = ByteBuffer.allocate(24);
        byte[] uuid = UuidUtil.asBytes(UUID.fromString(SampleGattAttributes.GEAR_S2_LE));
        mManufacturerData.put(0, (byte) 0xBE);
        mManufacturerData.put(1, (byte) 0xAC);
        for (int i = 2; i <= 17; i++) {
            mManufacturerData.put(i, uuid[i - 2]);
        }
        for (int i = 0; i <= 17; i++) {
            mManufacturerDataMask.put((byte) 0x01);
        }
        mBuilder.setManufacturerData(224, mManufacturerData.array(), mManufacturerDataMask.array());
        scanFilter = mBuilder.build();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void setScanFilterMuTag() {
        ScanFilter.Builder mBuilder = new ScanFilter.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
        ByteBuffer mManufacturerDataMask = ByteBuffer.allocate(24);
        byte[] uuid = UuidUtil.asBytes(UUID.fromString("0CF052C297CA407C84F8B62AAC4E9020"));
        mManufacturerData.put(0, (byte) 0xBE);
        mManufacturerData.put(1, (byte) 0xAC);
        for (int i = 2; i <= 17; i++) {
            mManufacturerData.put(i, uuid[i - 2]);
        }
        for (int i = 0; i <= 17; i++) {
            mManufacturerDataMask.put((byte) 0x01);
        }
        mBuilder.setManufacturerData(224, mManufacturerData.array(), mManufacturerDataMask.array());
        scanFilter = mBuilder.build();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        scanSettings = mBuilder.build();
    }

}
