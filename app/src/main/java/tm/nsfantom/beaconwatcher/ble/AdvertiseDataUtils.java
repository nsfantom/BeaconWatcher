package tm.nsfantom.beaconwatcher.ble;
/*
 * Copyright 2016 Google Inc. All rights reserved.
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


import android.annotation.TargetApi;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.URLUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import java.util.UUID;

import tm.nsfantom.beaconwatcher.util.InformuMuTagProfile;

/**
 * Helper class to simplify Eddystone URL encoding.
 **/
public class AdvertiseDataUtils {
    private static final ParcelUuid BEACON_UUID = InformuMuTagProfile.MU_DEVICE_UUID;

    private static final byte URL_FRAME_TYPE = 0x10;
    private static final byte FAT_BEACON = 0x0e;


    private static byte[] encodeUrnUuid(String urn, int position, ByteBuffer bb) {
        String uuidString = urn.substring(position, urn.length());
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            //Log.w(TAG, "encodeUrnUuid invalid urn:uuid format - " + urn);
            return null;
        }
        // UUIDs are ordered as byte array, which means most significant first
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return byteBufferToArray(bb);
    }

    private static byte[] byteBufferToArray(ByteBuffer bb) {
        byte[] bytes = new byte[bb.position()];
        bb.rewind();
        bb.get(bytes, 0, bytes.length);
        return bytes;
    }

    // Generate the advertising bytes for the given URL
    @TargetApi(21)
    public static AdvertiseData getAdvertisementData(byte[] urlData) {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeTxPowerLevel(false); // reserve advertising space for URI

        // Manually build the advertising info
        // See https://github.com/google/eddystone/tree/master/eddystone-url
        if (urlData == null || urlData.length == 0) {
            return null;
        }

        byte[] beaconData = new byte[urlData.length + 2];
        System.arraycopy(urlData, 0, beaconData, 2, urlData.length);
        beaconData[0] = URL_FRAME_TYPE; // frame type: url
        beaconData[1] = (byte) 0xBA; // calibrated tx power at 0 m

        builder.addServiceData(BEACON_UUID, beaconData);

        // Adding 0xFEAA to the "Service Complete List UUID 16" (0x3) for iOS compatibility
        builder.addServiceUuid(BEACON_UUID);

        return builder.build();
    }

    // Build and return the advertising bytes for the given FatBeacon advertisement
    @TargetApi(21)
    public static AdvertiseData getFatBeaconAdvertisementData(byte[] fatBeaconAdvertisement) {

        // Manually build the advertising info
        int length = Math.min(fatBeaconAdvertisement.length, 17);
        byte[] beaconData = new byte[length + 3];
        System.arraycopy(fatBeaconAdvertisement, 0, beaconData, 3, length);
        beaconData[0] = URL_FRAME_TYPE;
        beaconData[1] = (byte) 0xBA;
        beaconData[2] = FAT_BEACON;
        return new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(false) // reserve advertising space for URI
                .addServiceData(BEACON_UUID, beaconData)
                // Adding 0xFEAA to the "Service Complete List UUID 16" (0x3) for iOS compatibility
                .addServiceUuid(BEACON_UUID)
                .build();
    }

    // Build and return the ble advertising settings
    @TargetApi(21)
    public static AdvertiseSettings getAdvertiseSettings(boolean connectable) {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
        builder.setConnectable(connectable);

        return builder.build();
    }
}