package tm.nsfantom.beaconwatcher.common;
import java.nio.charset.Charset;

public class AdvertisementRecord {

    private final int length, type;
    private final byte[] data;

    public AdvertisementRecord(int length, int type, byte[] data) {
        this.length = length;
        this.type = type;
        this.data = data;
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    public int getType() {
        return type;
    }

    public String getTypeDescription() {
        return ADType.descriptionForType(this.type);
    }

    @Override
    public String toString() {
        return "[Record length: " + length + " data: " + new String(data, Charset.forName("UTF-8")) + "]";//DatatypeConverter.printHexBinary(data) + "]";
    }
}
