package tm.nsfantom.beaconwatcher.fragment;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.databinding.FragmentDeviceBinding;
import tm.nsfantom.beaconwatcher.service.BluetoothLeService;
import tm.nsfantom.beaconwatcher.util.SampleGattAttributes;

public class DeviceFragment extends Fragment {

    private BluetoothDevice device;
    private String deviceName;
    private String deviceAddress;
    private Listener listener;
    private FragmentDeviceBinding layout;
    private BluetoothLeService bluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> gattCharacteristicsList = new ArrayList<>();
    private boolean isConnected = false;
    private BluetoothGattCharacteristic notifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";


    public static DeviceFragment newInstance(BluetoothDevice device) {
        DeviceFragment deviceFragment = new DeviceFragment();
        deviceFragment.device = device;
        deviceFragment.deviceName = device.getName();
        deviceFragment.deviceAddress = device.getAddress();
        return deviceFragment;
    }

    public interface Listener {
        void onBack();
    }

    @Override
    public void onAttach(Context context) {
        if (!(getActivity() instanceof Listener)) {
            throw new IllegalStateException("Activity must implement fragment Listener.");
        }
        super.onAttach(context);
        listener = (Listener) getActivity();
        Intent gattServiceIntent = new Intent(getActivity().getApplicationContext(), BluetoothLeService.class);
        getActivity().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_device, container, false);
        return layout.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layout.gattServicesList.setOnChildClickListener(servicesListClickListner);
        layout.toggleConnect.setChecked(isConnected);
        layout.tvDeviceName.setText(deviceName);
        layout.tvDeviceAddress.setText(deviceAddress);
        layout.toggleConnect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                bluetoothLeService.connect(deviceAddress);
            else
                bluetoothLeService.disconnect();
        });

    }

    // Code to manage Service lifecycle.
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Timber.e("Unable to initialize Bluetooth");
                getActivity().finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            bluetoothLeService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothLeService != null) {
            final boolean result = bluetoothLeService.connect(deviceAddress);
            Timber.d("Connect request result=%s", result);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(serviceConnection);
        bluetoothLeService = null;
    }

    private void clearUI() {
        layout.gattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        layout.tvData.setText(R.string.no_data);
    }

    private void updateConnectionState(final int resourceId) {
        getActivity().runOnUiThread(() -> layout.tvConnectionState.setText(resourceId));
    }

    private void displayData(String data) {
        if (data != null) {
            layout.tvData.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
        gattCharacteristicsList = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            gattCharacteristicsList.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                getContext(),
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        layout.gattServicesList.setAdapter(gattServiceAdapter);
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                updateConnectionState(R.string.connected);
                layout.toggleConnect.setChecked(isConnected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                updateConnectionState(R.string.disconnected);
                layout.toggleConnect.setChecked(isConnected);
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(bluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            (parent, v, groupPosition, childPosition, id) -> {
                if (gattCharacteristicsList != null) {
                    final BluetoothGattCharacteristic characteristic =
                            gattCharacteristicsList.get(groupPosition).get(childPosition);
                    final int charaProp = characteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (notifyCharacteristic != null) {
                            bluetoothLeService.setCharacteristicNotification(
                                    notifyCharacteristic, false);
                            notifyCharacteristic = null;
                        }
                        bluetoothLeService.readCharacteristic(characteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        notifyCharacteristic = characteristic;
                        bluetoothLeService.setCharacteristicNotification(
                                characteristic, true);
                    }
                    return true;
                }
                return false;
            };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
