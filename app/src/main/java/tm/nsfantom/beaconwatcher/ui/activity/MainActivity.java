package tm.nsfantom.beaconwatcher.ui.activity;

import android.bluetooth.BluetoothDevice;
import android.os.Build;

import com.polidea.rxandroidble.RxBleDevice;

import org.altbeacon.beacon.Beacon;

import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.ui.fragment.AltAdvertiserFragment;
import tm.nsfantom.beaconwatcher.ui.fragment.BaseMonitorFragment;
import tm.nsfantom.beaconwatcher.ui.fragment.BeaconFragment;
import tm.nsfantom.beaconwatcher.ui.fragment.DeviceFragment;
import tm.nsfantom.beaconwatcher.ui.fragment.MainFragment;
import tm.nsfantom.beaconwatcher.ui.fragment.MainFragmentAlt;
import tm.nsfantom.beaconwatcher.ui.fragment.MainFragmentL;
import tm.nsfantom.beaconwatcher.ui.fragment.MainFragmentRX;
import tm.nsfantom.beaconwatcher.ui.fragment.SimpleAdvertiserFragment;
import tm.nsfantom.beaconwatcher.ui.fragment.SimpleSimulateFragment;
import tm.nsfantom.beaconwatcher.util.Constants;


public final class MainActivity extends BasePermissionActivity implements BaseMonitorFragment.Listener, DeviceFragment.Listener, BeaconFragment.Listener {

    @Override
    void init() {
        if(Constants.isAlt)
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, MainFragmentAlt.newInstance())
                    .commit();
        else if(Constants.isRxBle){
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, MainFragmentRX.newInstance())
                    .commit();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, MainFragmentL.newInstance())
                    .commit();
        else
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, MainFragment.newInstance())
                    .commit();
    }

    @Override
    public void onDeviceClicked(BluetoothDevice device) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(android.R.id.content, DeviceFragment.newInstance(device))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRXDeviceClicked(RxBleDevice device) {

    }

    @Override
    public void onBeaconClicked(Beacon beacon) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(android.R.id.content, BeaconFragment.newInstance(beacon))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAdvertise() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(android.R.id.content, SimpleAdvertiserFragment.newInstance(getBTAdapter()))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSimulate() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(android.R.id.content, SimpleSimulateFragment.newInstance(getBTAdapter()))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAltAdvertise() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(android.R.id.content, AltAdvertiserFragment.newInstance(getBTAdapter()))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBack() {
        onBackPressed();
    }
}
