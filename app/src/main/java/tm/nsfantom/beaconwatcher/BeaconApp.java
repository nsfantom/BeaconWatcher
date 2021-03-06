package tm.nsfantom.beaconwatcher;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.util.BluetoothUtils;


public final class BeaconApp extends Application {

    private BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

    }

    public BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) bluetoothAdapter = BluetoothUtils.getBluetoothAdapter(getApplicationContext());
        return bluetoothAdapter;
    }
}
