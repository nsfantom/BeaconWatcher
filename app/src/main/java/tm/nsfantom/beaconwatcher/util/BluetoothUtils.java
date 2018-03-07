package tm.nsfantom.beaconwatcher.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by user on 3/1/18.
 */

public class BluetoothUtils {

    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        }
        return BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * get BluetoothManager
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static BluetoothManager getBTManager(Context context) {
        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }
}
