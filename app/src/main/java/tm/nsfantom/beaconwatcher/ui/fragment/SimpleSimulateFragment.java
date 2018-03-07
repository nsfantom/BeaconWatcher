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

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;

import java.nio.ByteBuffer;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.ble.MuTagBuilder;
import tm.nsfantom.beaconwatcher.ui.fragment.base.AdvertiseFragment;
import tm.nsfantom.beaconwatcher.util.Constants;
import tm.nsfantom.beaconwatcher.util.TagProfile;
import tm.nsfantom.beaconwatcher.util.UuidUtil;

/**
 * Allows user to start & stop Bluetooth LE Advertising of their device.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public final class SimpleSimulateFragment extends AdvertiseFragment {


    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bTAdvertiser;

    private boolean isAdvertised = false;

    public static SimpleSimulateFragment newInstance(BluetoothAdapter bluetoothAdapter) {

        SimpleSimulateFragment advertiserFragment = new SimpleSimulateFragment();
        advertiserFragment.bluetoothAdapter = bluetoothAdapter;
        return advertiserFragment;
    }

    public void init() {
        ADPayloadParser.getInstance().registerManufacturerSpecificBuilder(0x02FF, new MuTagBuilder());

        Timber.d(getString(R.string.ble_initialized));
    }

    public void startAdvertise() {
        if (bluetoothAdapter != null && !isAdvertised) {
            if (bTAdvertiser == null) {
                bTAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            }
            bTAdvertiser.startAdvertising(createAdvSettings(), createAdvData(), advCallback);
            appendStatus(getString(R.string.ble_start_adv));
            appendStatus(bluetoothAdapter.getAddress());
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
}