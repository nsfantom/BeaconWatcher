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

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.util.TagProfile;

/**
 * Allows user to start & stop Bluetooth LE Advertising of their device.
 */
public class AltAdvertiserFragment extends AdvertiseFragment{

    private BeaconTransmitter beaconTransmitter;

    public static AltAdvertiserFragment newInstance() {
        return new AltAdvertiserFragment();
    }

    @Override
    public void init() {
        Timber.d(getString(R.string.ble_initialized));
    }

    public void startAdvertise() {
        Beacon beacon = new Beacon.Builder()
                .setId1(TagProfile.MU_DEVICEUUID)
                .setId2(String.valueOf(prefStorage.getMajor()))
                .setId3(String.valueOf(prefStorage.getMinor()))
                .setManufacturer(APPLE) // Radius Networks.  Change this for other beacon layouts
                .setTxPower(-59)
//                    .setDataFields(Arrays.asList(new Long[] {0l})) // Remove this for beacon layouts without d: fields
                .build();
// Change the layout below for other beacon types
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24");
//                    .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        if (beaconTransmitter == null)
            beaconTransmitter = new BeaconTransmitter(getContext(), beaconParser);

        if (beaconTransmitter.isStarted()) stopAdvertise();

        beaconTransmitter.startAdvertising(beacon, advCallback);
        appendStatus(getString(R.string.ble_start_adv));
    }

    public void stopAdvertise() {
        if (beaconTransmitter != null && beaconTransmitter.isStarted()) {
            beaconTransmitter.stopAdvertising();
            appendStatus(getString(R.string.ble_stop_adv));
        }
    }

    @Override
    public void onPrefsChanged() {
        super.onPrefsChanged();

        //FIXME: restart
        if (beaconTransmitter != null && beaconTransmitter.isStarted())
            startAdvertise();
    }
}