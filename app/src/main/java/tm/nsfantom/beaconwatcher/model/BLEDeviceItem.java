package tm.nsfantom.beaconwatcher.model;

import com.polidea.rxandroidble.RxBleDevice;

/**
 * Created by user on 2/27/18.
 */

public class BLEDeviceItem {
    private RxBleDevice bluetoothDevice;
    private int txPower = 0;
    private int rssi =0;

    public BLEDeviceItem(RxBleDevice bluetoothDevice, int rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
    }

    public RxBleDevice getDevice() {
        return bluetoothDevice;
    }

    public void setDevice(RxBleDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }


    public double calculateDistance() {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    public double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BLEDeviceItem){
            if(((BLEDeviceItem) obj).getDevice().getMacAddress().equals(bluetoothDevice.getMacAddress()))return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return bluetoothDevice.getMacAddress().hashCode();
    }
}
