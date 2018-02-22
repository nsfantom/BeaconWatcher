package tm.nsfantom.beaconwatcher.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by user on 2/22/18.
 */

public class Converter {
    /**
     * Converts a byte array of data to an integer
     * Automatically pads byte arrays with length < 4
     * @param data a byte array
     * @return an integer
     */
    public static int byteToInt(byte[] data) {
        return ByteBuffer.wrap(Arrays.copyOf(data, 4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static int intFromUint16(byte[] data) {
        final byte[] bytes = new byte[4];
        bytes[0] = 0;
        bytes[1] = 0;
        bytes[2] = data[0];
        bytes[3] = data[1];
        return ByteBuffer.wrap(bytes).getInt();
    }
}
