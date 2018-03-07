package tm.nsfantom.beaconwatcher.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.BuildConfig;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.ui.adapter.ColorSpinnerAdapter;
import tm.nsfantom.beaconwatcher.util.BluetoothUtils;
import tm.nsfantom.beaconwatcher.util.PrefStorage;
import tm.nsfantom.beaconwatcher.util.TagProfile;

public final class BleGattServerCallbackWrapper {
    private BluetoothGattServer gattServer;
    private BluetoothGattServerCallback gattCallback;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> managedDevices = new ArrayList<BluetoothDevice>();
    private LogStatus logStatus;
    private Context context;
    private PrefStorage prefStorage;

    public BleGattServerCallbackWrapper(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothUtils.getBluetoothAdapter(context);
    }

    public BleGattServerCallbackWrapper setLogStatusListener(LogStatus logStatusListener) {
        this.logStatus = logStatusListener;
        return this;
    }

    public void startGattServer() {
        getGattServer().clearServices();
//        gattServer.addService(TagProfile.createInformuGenericAccessService());
        gattServer.addService(TagProfile.createConfigurationService());
        gattServer.addService(TagProfile.createOTAService());
    }

    private BluetoothGattServer getGattServer() {
        if (gattServer == null)
            gattServer = BluetoothUtils.getBTManager(context).openGattServer(context, gattCallback);
        return gattServer;
    }

    public void stopGattServer() {
        if (gattServer != null) {
            gattServer.clearServices();
            gattServer.close();
            gattServer = null;
            appendStatus(context.getString(R.string.stop_gatt_server));
        }
    }

    private void appendStatus(final String status) {
        if (logStatus != null)
            logStatus.appendStatus(status);
    }

    private void notifyPrefsChanged() {
        if (logStatus != null)
            logStatus.onPrefsChanged();
    }

    public BleGattServerCallbackWrapper init(PrefStorage prefStorage) {
        this.prefStorage = prefStorage;
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
                    characteristic.setValue(prefStorage.getDeviceName());
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(TagProfile.DEVICE_MAJOR_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue(String.valueOf(prefStorage.getMajor()));
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(TagProfile.DEVICE_MINOR_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue(String.valueOf(prefStorage.getMinor()));
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(TagProfile.TAG_COLOR_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name :%s", device.getName(), prefStorage.getTagColor());
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
                        prefStorage.saveMajor(Integer.parseInt(str));
//                        getActivity().runOnUiThread(() -> layout.etMajor.setText(str));
                        notifyPrefsChanged();
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
                        prefStorage.saveMinor(Integer.parseInt(str));
//                        getActivity().runOnUiThread(() -> layout.etMinor.setText(str));
                        notifyPrefsChanged();
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
//                        getActivity().runOnUiThread(() -> layout.spinnerTagColor.setSelection(position));//.setText(str));
                        notifyPrefsChanged();
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
        return this;
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
        characteristicMajor.setValue(String.valueOf(prefStorage.getMajor()));
        BluetoothGattCharacteristic characteristicMinor = gattServer
                .getService(TagProfile.MU_TAG_CONFIGURATION_SERVICE.getUuid())
                .getCharacteristic(TagProfile.DEVICE_MINOR_UUID.getUuid());
        characteristicMinor.setValue(String.valueOf(prefStorage.getMinor()));
        BluetoothGattCharacteristic characteristicColor = gattServer
                .getService(TagProfile.MU_TAG_CONFIGURATION_SERVICE.getUuid())
                .getCharacteristic(TagProfile.TAG_COLOR_UUID.getUuid());
        characteristicColor.setValue(String.valueOf(prefStorage.getTagColor()));

        for (BluetoothDevice device : managedDevices) {
            Timber.d("Going to notify to %s", device.getName());
            //gattServer.notifyCharacteristicChanged(device, characteristic, true);
            gattServer.notifyCharacteristicChanged(device, characteristicMajor, false);
            gattServer.notifyCharacteristicChanged(device, characteristicMinor, false);
            gattServer.notifyCharacteristicChanged(device, characteristicColor, false);
        }
    }

    public interface LogStatus {
        void appendStatus(final String status);

        void onPrefsChanged();
    }
}
