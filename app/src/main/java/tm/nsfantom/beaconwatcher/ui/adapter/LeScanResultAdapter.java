package tm.nsfantom.beaconwatcher.ui.adapter;

import android.bluetooth.BluetoothDevice;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.databinding.ListitemDeviceBinding;
import tm.nsfantom.beaconwatcher.model.BTDeviceItem;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public final class LeScanResultAdapter extends RecyclerView.Adapter<LeScanResultAdapter.ResultHolder> {

    private ItemClickedListener itemClickedListener;

    private List<BTDeviceItem> bluetoothDevices = new ArrayList<>();

    @Override
    public ResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListitemDeviceBinding binding = ListitemDeviceBinding.inflate(inflater, parent, false);
        return new ResultHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(ResultHolder holder, int position) {
        if (position != holder.getAdapterPosition()) return;

        BTDeviceItem device = bluetoothDevices.get(position);
        final String deviceName = device.getDevice().getName();
        if (deviceName != null && deviceName.length() > 0)
            holder.layout.deviceName.setText(deviceName);
        else
            holder.layout.deviceName.setText(R.string.unknown_device);
        holder.layout.deviceAddress.setText(device.getDevice().getAddress());
//        holder.layout.tvDistance.setVisibility(View.VISIBLE);
//        holder.layout.tvDistance.setText(String.valueOf(calculateDistance(device.getTxPower(),device.getRssi())));
//        holder.itemView.setBackgroundResource(R.color.colorItemBackground);
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addResult(BTDeviceItem btDeviceItem) {
        if (!bluetoothDevices.contains(btDeviceItem)) {
            bluetoothDevices.add(btDeviceItem);
            notifyDataSetChanged();
        }
    }

    public void removeResult(BTDeviceItem btDeviceItem) {
        if (bluetoothDevices.contains(btDeviceItem)) {
            bluetoothDevices.remove(btDeviceItem);
            notifyDataSetChanged();
        }
    }

    public BTDeviceItem getDevice(int position) {
        return bluetoothDevices.get(position);
    }

    public void clear() {
        bluetoothDevices.clear();
        notifyDataSetChanged();
    }

    public void setItemClickedListener(ItemClickedListener itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    public interface ItemClickedListener {
        void onItemClicked(BluetoothDevice bluetoothDevice);
    }

    class ResultHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ListitemDeviceBinding layout;
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
                itemClickedListener.onItemClicked(bluetoothDevices.get(getAdapterPosition()).getDevice());
        }
    }
}
