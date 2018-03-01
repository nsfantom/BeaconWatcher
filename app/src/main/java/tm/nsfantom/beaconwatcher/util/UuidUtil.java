package tm.nsfantom.beaconwatcher.util;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by user on 2/22/18.
 */

public class UuidUtil {
    public static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    public static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private static final long leastSigUuidBits = 0x800000805f9b34fbL;

    public static UUID stringToUuid(String uuidString) {
        UUID uuid;
        if (uuidString.length() == 4) {
            /* it is a short form uuid */
            uuid = new UUID((Long.parseLong(uuidString, 16) << 32) | 0x1000, leastSigUuidBits);
        } else {
            uuid = UUID.fromString(uuidString);
        }

        return uuid;
    }

}
