package tm.nsfantom.beaconwatcher.ui.fragment;

import android.bluetooth.le.BluetoothLeScanner;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanSettings;

import java.nio.ByteBuffer;
import java.util.UUID;

import rx.Subscription;
import timber.log.Timber;
import tm.nsfantom.beaconwatcher.model.BLEDeviceItem;
import tm.nsfantom.beaconwatcher.ui.adapter.BLEScanResultAdapter;
import tm.nsfantom.beaconwatcher.ui.fragment.base.BaseMonitorFragment;
import tm.nsfantom.beaconwatcher.util.Constants;
import tm.nsfantom.beaconwatcher.util.TagProfile;

public class MainFragmentRX extends BaseMonitorFragment {

    private BLEScanResultAdapter adapter;

    public static MainFragmentRX newInstance() {
        return new MainFragmentRX();
    }

    private RxBleClient rxBleClient;
    private Subscription scanSubscription;
    static final int APPLE = 0x004c;
    static final UUID uuid = UUID.fromString(TagProfile.MU_DEVICEUUID);

    @Override
    protected void init() {
        adapter = new BLEScanResultAdapter();
        adapter.setHasStableIds(true);
        getBTAdapter();
        rxBleClient = RxBleClient.create(getContext());

        // Initializes list view adapter.
        layout.rvDevice.setLayoutManager(new LinearLayoutManager(getContext()));
        layout.rvDevice.setAdapter(adapter);

        Timber.w("AdvertisementSupported: %s", bluetoothAdapter.isMultipleAdvertisementSupported());
        Toast.makeText(getContext(),
                "AdvertisementSupported: " + bluetoothAdapter.isMultipleAdvertisementSupported(),
                Toast.LENGTH_SHORT).show();

        adapter.setItemClickedListener(device -> {
            if (device == null) return;
            listener.onRXDeviceClicked(device);
            Toast.makeText(getContext(), "device: " + device.getName(), Toast.LENGTH_SHORT).show();
        });
        layout.toggleRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

            }
            startScan(isChecked);
        });
        layout.btnAdvertise.setOnClickListener(v -> listener.onAdvertise());
        layout.btnSimulate.setOnClickListener(v -> listener.onSimulate());

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
                scanSubscription.unsubscribe();
//                bluetoothLeScanner.stopScan(mLeScannerCallback);
//                bluetoothLeScanner.flushPendingScanResults(mLeScannerCallback);
                layout.progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
                layout.toggleRefresh.setChecked(isScanning);
            }, Constants.SCAN_PERIOD);

            isScanning = true;
            adapter.clear();
            scanSubscription = rxBleClient.scanBleDevices(
                    new ScanSettings.Builder()
                            // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                            // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                            .build(),
                    // add filters if needed
                    layout.switchMuTag.isChecked()? new ScanFilter.Builder()
//                            .setServiceUuid(TagProfile.MU_DEVICE_UUID)
//                            .setServiceUuid(TagProfile.GENERIC_ACCESS_SERVICE)
                            .setManufacturerData(APPLE, createManufactureData())
                            .build(): new ScanFilter.Builder().build()
            )
                    .subscribe(scanResult -> {
                                // Process scan result here.
                                getActivity().runOnUiThread(() -> adapter.addResult(new BLEDeviceItem(scanResult.getBleDevice(), scanResult.getRssi())));
                            }, throwable -> {
                                // Handle an error here.
                            }
                    );
            if (layout.switchMuTag.isChecked()) {
//                bluetoothLeScanner.startScan(getScanFilterMuTag(), scanSettings, mLeScannerCallback);
            } else {
//                bluetoothLeScanner.startScan(mLeScannerCallback);
            }
        } else {
            isScanning = false;
            scanSubscription.unsubscribe();
//            bluetoothLeScanner.stopScan(mLeScannerCallback);
//            bluetoothLeScanner.flushPendingScanResults(mLeScannerCallback);
        }
        layout.progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (scanSubscription != null)
            scanSubscription.unsubscribe();
    }

    private static byte[] createManufactureData() {
        ByteBuffer bb = ByteBuffer.allocate(23);

        bb.putShort((short) 0x0215); //iBeacon
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
//        bb.putShort((short) 0x0001); //major
//        bb.putShort((short) 0x0001); //minor
//        bb.putShort((short) major); //major
//        bb.putShort((short) minor); //minor
//        bb.put((byte) 0xc5); //Tx Power

        return bb.array();
    }
}
