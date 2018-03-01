package tm.nsfantom.beaconwatcher.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

/**
 * Created by user on 3/1/18.
 */

public class BluetoothUtils {

    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        return mBluetoothManager.getAdapter();
    }
}
