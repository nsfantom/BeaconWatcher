package tm.nsfantom.beaconwatcher.ui.fragment;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.model.BTDeviceItem;
import tm.nsfantom.beaconwatcher.ui.adapter.LeScanResultAdapter;
import tm.nsfantom.beaconwatcher.util.Constants;
import tm.nsfantom.beaconwatcher.util.InformuMuTagProfile;


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class MainFragmentL extends BaseMonitorFragment {

    private LeScanResultAdapter adapter;
    private ScanSettings scanSettings;

    public static MainFragmentL newInstance() {
        return new MainFragmentL();
    }

    @Override
    protected void init() {
        adapter = new LeScanResultAdapter();
        adapter.setHasStableIds(true);
        getBTAdapter();
        // Initializes list view adapter.
        layout.rvDevice.setLayoutManager(new LinearLayoutManager(getContext()));
        layout.rvDevice.setAdapter(adapter);
        setScanSettings();
        Timber.w("AdvertisementSupported: %s", bluetoothAdapter.isMultipleAdvertisementSupported());
        Toast.makeText(getContext(),
                "AdvertisementSupported: " + bluetoothAdapter.isMultipleAdvertisementSupported(),
                Toast.LENGTH_SHORT).show();

        adapter.setItemClickedListener(device -> {
            if (device == null) return;
            listener.onDeviceClicked(device);
            Toast.makeText(getContext(), "device: " + device.getName(), Toast.LENGTH_SHORT).show();
        });
        layout.toggleRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

            }
            startScan(isChecked);
        });
        layout.btnAdvertise.setOnClickListener(v -> listener.onAdvertise());
        layout.btnSimulate.setOnClickListener(v-> listener.onSimulate());

    }

    @Override
    protected void startScan(boolean enable) {
        if (getBTAdapter() == null) return;
        final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) return;
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                isScanning = false;
                bluetoothLeScanner.stopScan(mLeScannerCallback);
                bluetoothLeScanner.flushPendingScanResults(mLeScannerCallback);
                layout.progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
                layout.toggleRefresh.setChecked(isScanning);
            }, Constants.SCAN_PERIOD);

            isScanning = true;
            adapter.clear();
            if (layout.switchMuTag.isChecked()) {
                bluetoothLeScanner.startScan(getScanFilterMuTag(), scanSettings, mLeScannerCallback);
            } else {
                bluetoothLeScanner.startScan(mLeScannerCallback);
            }
        } else {
            isScanning = false;
            bluetoothLeScanner.stopScan(mLeScannerCallback);
            bluetoothLeScanner.flushPendingScanResults(mLeScannerCallback);
        }
        layout.progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
    }

    private ScanCallback mLeScannerCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            switch (callbackType) {
                case ScanSettings.CALLBACK_TYPE_ALL_MATCHES:
                case ScanSettings.CALLBACK_TYPE_FIRST_MATCH:
                    getActivity().runOnUiThread(() -> adapter.addResult(new BTDeviceItem(result.getDevice(), result.getRssi())));
                    break;
                case ScanSettings.CALLBACK_TYPE_MATCH_LOST:
                    getActivity().runOnUiThread(() -> adapter.removeResult(new BTDeviceItem(result.getDevice(), result.getRssi())));
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

    private List<ScanFilter> getScanFilterMuTag() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(
                new ScanFilter.Builder()
                        .setServiceUuid(InformuMuTagProfile.MU_DEVICE_UUID)
                        .build()
        );
        return scanFilters;
    }

    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
//        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        scanSettings = mBuilder.build();
    }
}
