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
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.nio.ByteBuffer;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.ui.fragment.base.AdvertiseFragment;
import tm.nsfantom.beaconwatcher.util.Constants;
import tm.nsfantom.beaconwatcher.util.TagProfile;

/**
 * Allows user to start & stop Bluetooth LE Advertising of their device.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class SimpleAdvertiserFragment extends AdvertiseFragment {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bTAdvertiser;
    private boolean isAdvertised = false;

    public static SimpleAdvertiserFragment newInstance(BluetoothAdapter bluetoothAdapter) {

        SimpleAdvertiserFragment advertiserFragment = new SimpleAdvertiserFragment();
        advertiserFragment.bluetoothAdapter = bluetoothAdapter;
        return advertiserFragment;
    }


    public void init() {
        Timber.d(getString(R.string.ble_initialized));
    }

    public void startAdvertise() {
        if (bluetoothAdapter != null && !isAdvertised) {
            if (bTAdvertiser == null) {
                bTAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            }
            bTAdvertiser.startAdvertising(createAdvSettings(), createAdvData(), advCallback);
            appendStatus(getString(R.string.ble_start_adv));
        }
    }

    public void stopAdvertise() {
        if (bTAdvertiser != null) {
            bTAdvertiser.stopAdvertising(advCallback);
            isAdvertised = false;
            bTAdvertiser = null;
            appendStatus(getString(R.string.ble_stop_adv));
        }
    }

    private static AdvertiseData createAdvData() {
        final byte[] manufacturerData = createManufactureData();
        AdvertiseData.Builder builder = new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(false)
//                .addServiceData(TagProfile.DEVICE_NAME_UUID, "Imformu Mu Tag".getBytes(Charset.forName( "UTF-8" )))
                .addManufacturerData(APPLE, manufacturerData);

        return builder.build();
    }

    private static AdvertiseSettings createAdvSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder()
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setConnectable(true)
                .setTimeout(Constants.ADVERTISE_TIMEOUT)
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        return builder.build();
    }

    private static byte[] createManufactureData() {
        ByteBuffer bb = ByteBuffer.allocate(23);

        bb.putShort((short) 0x0215); //iBeacon
        bb.putLong(TagProfile.MU_DEVICE_UUID.getUuid().getMostSignificantBits());
        bb.putLong(TagProfile.MU_DEVICE_UUID.getUuid().getLeastSignificantBits());
//        bb.putShort((short) 0x0001); //major
//        bb.putShort((short) 0x0001); //minor
        bb.putShort((short) major); //major
        bb.putShort((short) minor); //minor
        bb.put((byte) 0xc5); //Tx Power

        return bb.array();
    }

    @Override
    public void onPrefsChanged() {
        super.onPrefsChanged();
        stopAdvertise();
        startAdvertise();
    }
}