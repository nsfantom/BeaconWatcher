/*
 * Copyright (C) 2013 The Android Open Source Project
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

package tm.nsfantom.beaconwatcher.util;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public final class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap<>();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String INFORMU_MU_TAG = "DE7EC7ED-1055-B055-C0DE-DEFEA7EDFA7E";
    public static String GEAR_S2_LE = "b7256b94-c119-48ae-84b8-08a14508921f";
    public static String MU_TAG_DEVICE_NAME = "";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(INFORMU_MU_TAG, "MU TAG");

        attributes.put(TagProfile.GENERIC_ACCESS_SERVICE.toString(), "Generic Access Service");
        attributes.put(TagProfile.DEVICE_NAME_UUID.toString(), "Device Name");
        attributes.put(TagProfile.APPEARANCE_UUID.toString(), "Appearance");


        //attributes.put("","Mu Tag Device Info Service");
        attributes.put(TagProfile.MANUFACTURER_NAME_UUID.toString(), "Manufacturer Name");
        attributes.put(TagProfile.FIRMWARE_REVISION_STRING_UUID.toString(), "Firmware Revision String");
        attributes.put(TagProfile.MODEL_NUMBER_STRING_UUID.toString(), "Model Number String");
        attributes.put(TagProfile.SYSTEM_ID_UUID.toString(), "System ID");
        attributes.put(TagProfile.BATTERY_LEVEL_UUID.toString(), "Battery Level");

        attributes.put(TagProfile.MU_TAG_CONFIGURATION_SERVICE.toString(), "Mu Tag Configuration Service");
        attributes.put(TagProfile.DEVICE_UUID.toString(), "Device UUID");
        attributes.put(TagProfile.DEVICE_MAJOR_UUID.toString(), "Device Major");
        attributes.put(TagProfile.DEVICE_MINOR_UUID.toString(), "Device Minor");
        attributes.put(TagProfile.TX_POWER_UUID.toString(), "Tx Power");
        attributes.put(TagProfile.AUTHENTICATE_CONNECTION_UUID.toString(), "Authenticate Connection");
        attributes.put(TagProfile.TAG_COLOR_UUID.toString(), "Tag Color");
        attributes.put(TagProfile.DEEP_SLEEP_UUID.toString(), "Deep Sleep");
        attributes.put(TagProfile.PROVISIONED_UUID.toString(), "Provisioned");
        attributes.put(TagProfile.ADVERTISING_INTERVAL_UUID.toString(), "Advertising Interval");

        attributes.put(TagProfile.SILICON_LABS_OTA.toString(),"Silicon Labs OTA");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
