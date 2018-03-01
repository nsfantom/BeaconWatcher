package tm.nsfantom.beaconwatcher.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import tm.nsfantom.beaconwatcher.BeaconApp;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.util.Constants;

/**
 * Created by user on 2/27/18.
 */

public abstract class BasePermissionActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    public BluetoothAdapter mBluetoothAdapter;

    abstract void init();

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(BasePermissionActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.REQUEST_COARSE);
        }
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!getBTAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }

        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == Constants.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_COARSE) {
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //init();
            } else {
                requestCoarsePermission();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestCoarsePermission() {
        Snackbar.make(this.findViewById(android.R.id.content), R.string.needs_coarse, Snackbar.LENGTH_LONG)
                .setAction("OK", v -> ActivityCompat.requestPermissions(BasePermissionActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.REQUEST_COARSE))
                .show();
    }

    protected BluetoothAdapter getBTAdapter() {
        if (mBluetoothAdapter != null) return mBluetoothAdapter;
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
//        final BluetoothManager bluetoothManager = (BluetoothManager) getApplication().getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothAdapter = ((BeaconApp) getApplication()).getBluetoothAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        return mBluetoothAdapter;
    }
}
