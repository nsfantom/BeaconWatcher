package tm.nsfantom.beaconwatcher.ui.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.polidea.rxandroidble.RxBleDevice;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.databinding.ItemBeaconBinding;
import tm.nsfantom.beaconwatcher.databinding.ListitemDeviceBinding;
import tm.nsfantom.beaconwatcher.model.BLEDeviceItem;

public final class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.ResultHolder> {

    private ItemClickedListener itemClickedListener;

    private List<Beacon> beacons = new ArrayList<>();

    @Override
    public ResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemBeaconBinding binding = ItemBeaconBinding.inflate(inflater, parent, false);
        return new ResultHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(ResultHolder holder, int position) {
        if (position != holder.getAdapterPosition()) return;

        Beacon beacon = beacons.get(position);
        final String deviceName = beacon.getBluetoothName();
        if (deviceName != null && deviceName.length() > 0)
            holder.layout.deviceName.setText(deviceName);
        else
            holder.layout.deviceName.setText(R.string.unknown_device);
        holder.layout.deviceAddress.setText(beacon.getBluetoothAddress());
        holder.layout.tvUuid.setText(beacon.getId1().toString());
        holder.layout.tvDistance.setVisibility(View.VISIBLE);
        holder.layout.tvDistance.setText(String.valueOf(beacon.getDistance()));
        holder.layout.tvMajor.setText(holder.view.getResources().getString(R.string.tv_major,beacon.getId2().toInt()));
        holder.layout.tvMinor.setText(holder.view.getResources().getString(R.string.tv_minor,beacon.getId3().toInt()));
        holder.layout.tvTimestamp.setText(String.valueOf(beacon.getManufacturer()));
        holder.layout.tvRSSI.setText(holder.view.getResources().getString(R.string.tv_rssi,beacon.getRssi()));
        holder.layout.tvTxPower.setText(holder.view.getResources().getString(R.string.tv_txpower,beacon.getTxPower()));
//        holder.itemView.setBackgroundResource(R.color.colorItemBackground);
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addBeacon(Beacon beacon) {
        if (!beacons.contains(beacon)) {
            beacons.add(beacon);
//            notifyDataSetChanged();
        }
    }

    public void removeBeacon(Beacon beacon) {
        if (beacons.contains(beacon)) {
            beacons.remove(beacon);
            notifyDataSetChanged();
        }
    }

    public Beacon getBeacon(int position) {
        return beacons.get(position);
    }

    public void clear() {
        beacons.clear();
//        notifyDataSetChanged();
    }

    public void setItemClickedListener(ItemClickedListener itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    public interface ItemClickedListener {
        void onItemClicked(Beacon beacon);
    }

    class ResultHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemBeaconBinding layout;
        View view;

        public ResultHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            layout = DataBindingUtil.bind(itemView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Timber.e("Onclick: %s", getAdapterPosition());
            if (itemClickedListener != null)
                itemClickedListener.onItemClicked(beacons.get(getAdapterPosition()));
        }
    }
}
