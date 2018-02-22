package tm.nsfantom.beaconwatcher.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.databinding.FragmentMainBinding;
import tm.nsfantom.beaconwatcher.ui.LeDevicesAdapter;
import tm.nsfantom.beaconwatcher.util.Constants;

public class MainFragment extends Fragment {

    private Listener listener;
    private FragmentMainBinding layout;
    private LeDevicesAdapter adapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;


    public static MainFragment newInstance(BluetoothAdapter bluetoothAdapter) {
        MainFragment mainFragment = new MainFragment();
        mainFragment.mBluetoothAdapter = bluetoothAdapter;
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
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                layout.progressBar.setVisibility(mScanning?View.VISIBLE:View.GONE);
                layout.toggleRefresh.setChecked(mScanning);
            }, Constants.SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        layout.progressBar.setVisibility(mScanning?View.VISIBLE:View.GONE);
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

}
