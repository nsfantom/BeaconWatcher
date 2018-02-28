package tm.nsfantom.beaconwatcher.model;

import android.bluetooth.BluetoothGattCharacteristic;

public class DisplayPermissions {
    private int permissions;
    private int properties;

    public DisplayPermissions() {

    }

    public DisplayPermissions setPermissions(int permissions) {
        this.permissions = permissions;
        return this;
    }

    public String getPropertiesText() {
        switch (properties){
            case BluetoothGattCharacteristic.PROPERTY_BROADCAST:
                return "BROADCAST";
            case BluetoothGattCharacteristic.PROPERTY_READ:
                return "READ";
            case BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE:
                return "WRITE_NO_RESPONSE";
            case BluetoothGattCharacteristic.PROPERTY_WRITE:
                return "WRITE";
            case BluetoothGattCharacteristic.PROPERTY_NOTIFY:
                return "NOTIFY";
            case BluetoothGattCharacteristic.PROPERTY_INDICATE:
                return "INDICATE";
            case BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE:
                return "SIGNED_WRITE";
            case BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS:
                return "EXTENDED_PROPS";
        }
        return "_";
    }

    public DisplayPermissions setProperties(int properties) {
        this.properties = properties;
        return this;
    }

    public String getPermissionsText() {
        switch (permissions){
            case BluetoothGattCharacteristic.PERMISSION_READ:
                return "READ";
            case BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED:
                return "READ_ENCRYPTED";
            case BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM:
                return "READ_ENCRYPTED_MITM";
            case BluetoothGattCharacteristic.PERMISSION_WRITE:
                return "WRITE";
            case BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED:
                return "WRITE_ENCRYPTED";
            case BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM:
                return "WRITE_ENCRYPTED_MITM";
            case BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED:
                return "WRITE_SIGNED";
            case BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM:
                return "WRITE_SIGNED_MITM";
        }
        return "_";
    }

    public String getText(){
        return getPropertiesText() + " " + getPermissionsText();
    }
}
