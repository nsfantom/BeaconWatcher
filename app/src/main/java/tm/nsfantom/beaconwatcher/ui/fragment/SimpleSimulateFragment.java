/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tm.nsfantom.beaconwatcher.ui.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.TextView;

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.BuildConfig;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.ble.MuTagBuilder;
import tm.nsfantom.beaconwatcher.databinding.FragmentSimpleadvertiserBinding;
import tm.nsfantom.beaconwatcher.ui.adapter.ColorSpinnerAdapter;
import tm.nsfantom.beaconwatcher.util.Constants;
import tm.nsfantom.beaconwatcher.util.TagProfile;
import tm.nsfantom.beaconwatcher.util.PrefStorage;
import tm.nsfantom.beaconwatcher.util.UuidUtil;

/**
 * Allows user to start & stop Bluetooth LE Advertising of their device.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class SimpleSimulateFragment extends Fragment implements TextView.OnEditorActionListener {

    /**
     * Listens for notifications that the {@code AdvertiserService} has failed to start advertising.
     * This Receiver deals with Fragment UI elements and only needs to be active when the Fragment
     * is on-screen, so it's defined and registered in code instead of the Manifest.
     */
    private FragmentSimpleadvertiserBinding layout;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bTAdvertiser;
    private AdvertiseCallback advCallback;
    private BluetoothGattServer gattServer;
    private BluetoothGattServerCallback gattCallback;
    private boolean isAdvertised = false;
    private List<BluetoothDevice> managedDevices = new ArrayList<BluetoothDevice>();

    static int minor = 1;
    static int major = 1;

    private PrefStorage prefStorage;


    public static SimpleSimulateFragment newInstance(BluetoothAdapter bluetoothAdapter) {

        SimpleSimulateFragment advertiserFragment = new SimpleSimulateFragment();
        advertiserFragment.bluetoothAdapter = bluetoothAdapter;
        return advertiserFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_simpleadvertiser, container, false);
        init();
        prefStorage = new PrefStorage(getContext());
        return layout.getRoot();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layout.switchAdvertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startAdvertise();
                startGattServer();
                layout.llControls.setKeepScreenOn(true);
            } else {
                stopGattServer();
                stopAdvertise();
                layout.llControls.setKeepScreenOn(false);
            }
        });
//        layout.llControls.setBackgroundResource(ColorSpinnerAdapter.TagColor.values()[prefStorage.getTagColor()].colorResource);
        layout.etMajor.setText(String.valueOf(prefStorage.getMajor()));
        major = prefStorage.getMajor();
        layout.etMinor.setText(String.valueOf(prefStorage.getMinor()));
        minor = prefStorage.getMinor();
        layout.etMajor.setOnEditorActionListener(this);
        layout.etMinor.setOnEditorActionListener(this);
        layout.etDeviceName.setText(prefStorage.getDeviceName());
        layout.etDeviceName.setOnEditorActionListener(this);
        layout.spinnerTagColor.setAdapter(new ColorSpinnerAdapter());
        layout.spinnerTagColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefStorage.saveTagColor(position);
                layout.llControls.setBackgroundResource(ColorSpinnerAdapter.TagColor.values()[position].colorResource);
                notifyCharacteristicChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(prefStorage.getTagColor());
            }
        });
        layout.spinnerTagColor.setSelection(prefStorage.getTagColor());
        layout.tvLogger.setOnLongClickListener(v -> {
            layout.tvLogger.setText("");
            return true;
        });
    }

    private void init() {
        ADPayloadParser.getInstance().registerManufacturerSpecificBuilder(0x02FF, new MuTagBuilder());

        advCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                if (settingsInEffect != null) {
                    appendStatus(settingsInEffect.toString());
                } else {
                    appendStatus("onStartSuccess: settingInEffect = null");
                }
            }

            @Override
            public void onStartFailure(int errorCode) {
                appendStatus("onStartFailure: errorCode = " + errorCode);
            }
        };

        gattCallback = new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                Timber.d("onConnectionStateChange: " + device.getName() + " status=" + status + "->" + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if (!managedDevices.contains(device) && (!device.getAddress().equals(bluetoothAdapter.getAddress()))) {
                        managedDevices.add(device);
                        appendStatus("name: " + device.getName() + " device: " + device.getAddress() + " connected");
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    managedDevices.remove(device);
                    appendStatus("name: " + device.getName() + " device: " + device.getAddress() + " disconnected");
                }

            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    appendStatus("onServiceAdded: status = GATT_SUCCESS service = " + service.getUuid().toString());
                    Timber.d("onServiceAdded: status==GATT_SUCCESS service = %s", service.getUuid().toString());
                } else {
                    appendStatus("onServiceAdded: status = " + status + " service = " + service.getUuid().toString());
                    Timber.d("onServiceAdded: status!=GATT_SUCCESS service = %s", service.getUuid().toString());
                }

            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                Timber.d("onCharacteristicReadRequest: requestId=" + requestId + " offset=" + offset);
                Timber.d("uuid: %s", characteristic.getUuid());
                if (characteristic.getUuid().equals(TagProfile.DEVICE_NAME_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue(layout.etDeviceName.getText().toString());
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(TagProfile.DEVICE_MAJOR_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue(layout.etMajor.getText().toString());
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(TagProfile.DEVICE_MINOR_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue(layout.etMinor.getText().toString());
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(TagProfile.TAG_COLOR_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name :%s", device.getName(),prefStorage.getTagColor());
                    characteristic.setValue(String.valueOf(prefStorage.getTagColor()));
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(TagProfile.MODEL_NUMBER_STRING_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue("android1");
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(TagProfile.FIRMWARE_REVISION_STRING_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue(BuildConfig.VERSION_NAME);
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(TagProfile.SYSTEM_ID_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue("010101");
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(TagProfile.BATTERY_LEVEL_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue("3");
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                }
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, "0".getBytes());
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                Timber.d("onCharacteristicWriteRequest: requestId=" + requestId + " preparedWrite="
                        + Boolean.toString(preparedWrite) + " responseNeeded="
                        + Boolean.toString(responseNeeded) + " offset=" + offset);

                if (characteristic.getUuid().equals(TagProfile.DEVICE_MAJOR_UUID.getUuid())) {
                    Timber.d("%s is writing characteristic", device.getName());
                    if (value != null && value.length > 0) {
                        String str = new String(value);
                        Timber.d("data: %s", str);
                        getActivity().runOnUiThread(() -> layout.etMajor.setText(str));
                        appendStatus(str);
                    } else {
                        Timber.d("Invalid value.");
                    }
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
                } else if (characteristic.getUuid().equals(TagProfile.DEVICE_MINOR_UUID.getUuid())) {
                    Timber.d("%s is writing characteristic", device.getName());
                    if (value != null && value.length > 0) {
                        String str = new String(value);
                        Timber.d("data: %s", str);
                        getActivity().runOnUiThread(() -> layout.etMinor.setText(str));
                        appendStatus(str);
                    } else {
                        Timber.d("Invalid value.");
                    }
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
                } else if (characteristic.getUuid().equals(TagProfile.TAG_COLOR_UUID.getUuid())) {
                    Timber.d("%s is writing characteristic", device.getName());
                    if (value != null && value.length > 0) {
                        int position = ColorSpinnerAdapter.TagColor.getIndex(value[0]);
                        Timber.d("data: %s", position);
                        prefStorage.saveTagColor(position);
                        getActivity().runOnUiThread(() -> layout.spinnerTagColor.setSelection(position));//.setText(str));
                        appendStatus(String.valueOf(position));
                    } else {
                        Timber.d("Invalid value.");
                    }
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
                }
            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                Timber.d("onDescriptorReadRequest: ");
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                Timber.d("onDescriptorWriteRequest: ");
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                Timber.d("onExecuteWrite: ");
            }

            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                Timber.d("onNotificationSent: %s", device.getName());
            }
        };

        Timber.d(getString(R.string.ble_initialized));
    }

    @Override
    public void onDestroyView() {
        layout.llControls.setKeepScreenOn(false);
        stopGattServer();
        stopAdvertise();
        super.onDestroyView();
    }

    private void startAdvertise() {
        if (bluetoothAdapter != null && !isAdvertised) {
            if (bTAdvertiser == null) {
                bTAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            }
            bTAdvertiser.startAdvertising(createAdvSettings(), createAdvData(), advCallback);
            appendStatus(getString(R.string.ble_start_adv));
            appendStatus(bluetoothAdapter.getAddress());
        }
    }

    private void stopAdvertise() {
        if (bTAdvertiser != null) {
            bTAdvertiser.stopAdvertising(advCallback);
            isAdvertised = false;
            bTAdvertiser = null;
            appendStatus(getString(R.string.ble_stop_adv));
        }
    }

    /**
     * get BluetoothManager
     */
    public BluetoothManager getBTManager() {
        return (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
    }

    private void startGattServer() {
        gattServer = getBTManager().openGattServer(getContext(), gattCallback);

//        gattServer.addService(TagProfile.createInformuGenericAccessService());
        gattServer.addService(TagProfile.createConfigurationService());
        gattServer.addService(TagProfile.createOTAService());
    }

    private void stopGattServer() {
        if (gattServer != null) {
            gattServer.clearServices();
            gattServer.close();
            gattServer = null;
            appendStatus(getString(R.string.stop_gatt_server));
        }
    }

    public void notifyCharacteristicChanged() {
        if (managedDevices.isEmpty()) return;
//        BluetoothGattService service = gattServer.getService(TagProfile.GENERIC_ACCESS_SERVICE.getUuid());
//        BluetoothGattCharacteristic characteristic = gattServer
//                .getService(TagProfile.GENERIC_ACCESS_SERVICE.getUuid())
//                .getCharacteristic(TagProfile.DEVICE_NAME_UUID.getUuid());
//        characteristic.setValue(layout.etDeviceName.getText().toString());

        BluetoothGattCharacteristic characteristicMajor = gattServer
                .getService(TagProfile.MU_TAG_CONFIGURATION_SERVICE.getUuid())
                .getCharacteristic(TagProfile.DEVICE_MAJOR_UUID.getUuid());
        characteristicMajor.setValue(layout.etMajor.getText().toString());
        BluetoothGattCharacteristic characteristicMinor = gattServer
                .getService(TagProfile.MU_TAG_CONFIGURATION_SERVICE.getUuid())
                .getCharacteristic(TagProfile.DEVICE_MINOR_UUID.getUuid());
        characteristicMinor.setValue(layout.etMinor.getText().toString());
        BluetoothGattCharacteristic characteristicColor = gattServer
                .getService(TagProfile.MU_TAG_CONFIGURATION_SERVICE.getUuid())
                .getCharacteristic(TagProfile.TAG_COLOR_UUID.getUuid());
        characteristicColor.setValue(String.valueOf(prefStorage.getTagColor()));

        for (BluetoothDevice device : managedDevices) {
            Timber.d("Going to notify to %s", device.getName());
            //gattServer.notifyCharacteristicChanged(device, characteristic, true);
            gattServer.notifyCharacteristicChanged(device, characteristicMajor, false);
            gattServer.notifyCharacteristicChanged(device, characteristicMinor, false);
            gattServer.notifyCharacteristicChanged(device, characteristicColor,false);
        }
    }

    private AdvertiseData createAdvData() {
        ByteBuffer mManufacturerData = ByteBuffer.allocate(24);
        byte[] uuid = UuidUtil.asBytes(TagProfile.MU_DEVICE_UUID.getUuid());
        mManufacturerData.put(0, (byte)0xBE); // Beacon Identifier
        mManufacturerData.put(1, (byte)0xAC); // Beacon Identifier
        for (int i=2; i<=17; i++) {
            mManufacturerData.put(i, uuid[i-2]); // adding the UUID
        }
        mManufacturerData.put(18, (byte)((major >> 8) & 0xFF)); // first byte of Major
        mManufacturerData.put(19, (byte)((major     ) & 0xFF)); // second byte of Major
        mManufacturerData.put(20, (byte)((minor >> 8) & 0xFF)); // first minor
        mManufacturerData.put(21, (byte)((minor     ) & 0xFF)); // second minor
        mManufacturerData.put(22, (byte)0xB5); // txPower
        return new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(false)
                .setIncludeDeviceName(false)
                .addManufacturerData(0x02FF,mManufacturerData.array())
                .build();
    }

    private static AdvertiseSettings createAdvSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder()
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setConnectable(true)
                .setTimeout(Constants.ADVERTISE_TIMEOUT)
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        return builder.build();
    }

    public void appendStatus(final String status) {
        getActivity().runOnUiThread(() -> {
            String current = layout.tvLogger.getText().toString();
            layout.tvLogger.setText(status.concat("\n").concat(current));
//            layout.tvLogger.setText(current + "\n" + status);
        });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            switch (v.getId()) {
                case R.id.etMajor:
                    major = Integer.parseInt(v.getText().toString());
                    prefStorage.saveMajor(major);
                    break;
                case R.id.etMinor:
                    minor = Integer.parseInt(v.getText().toString());
                    prefStorage.saveMinor(minor);
                    break;
                case R.id.etDeviceName:
                    prefStorage.saveDeviceName(layout.etDeviceName.getText().toString());
                    break;
            }
            notifyCharacteristicChanged();
        }

        return false;
    }
}