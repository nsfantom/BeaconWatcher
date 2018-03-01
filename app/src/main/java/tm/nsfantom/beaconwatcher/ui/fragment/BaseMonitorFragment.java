package tm.nsfantom.beaconwatcher.ui.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import tm.nsfantom.beaconwatcher.BeaconApp;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.databinding.FragmentMainBinding;

public abstract class BaseMonitorFragment extends Fragment {
    protected Listener listener;
    protected BluetoothAdapter bluetoothAdapter;
    protected FragmentMainBinding layout;
    protected boolean isScanning;
    protected Handler mHandler;

    public interface Listener {
        void onDeviceClicked(BluetoothDevice device);

        void onAdvertise();

        void onSimulate();
    }

    @Override
    public void onAttach(Context context) {
        if (!(getActivity() instanceof Listener)) {
            throw new IllegalStateException("Activity must implement fragment Listener.");
        }
        super.onAttach(context);
        listener = (Listener) getActivity();
    }

    protected abstract void init();

    protected abstract void startScan(boolean enable);

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
        init();
//        startScan(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        startScan(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        startScan(false);
    }

    protected BluetoothAdapter getBTAdapter() {
        if (bluetoothAdapter != null) return bluetoothAdapter;
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
//        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
//        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter = ((BeaconApp) getActivity().getApplication()).getBluetoothAdapter();
        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        return bluetoothAdapter;
    }
}
