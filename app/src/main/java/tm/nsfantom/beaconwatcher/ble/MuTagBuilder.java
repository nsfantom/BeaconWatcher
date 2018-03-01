package tm.nsfantom.beaconwatcher.ble;

import com.neovisionaries.bluetooth.ble.advertising.ADManufacturerSpecific;
import com.neovisionaries.bluetooth.ble.advertising.ADManufacturerSpecificBuilder;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;

/**
 * Created by user on 3/1/18.
 */

public class MuTagBuilder implements ADManufacturerSpecificBuilder {
    private int type = 0xFF;

    @Override
    public ADManufacturerSpecific build(int length, int type, byte[] data, int companyId) {

        return IBeacon.create(length,this.type, data,companyId);
    }
}
