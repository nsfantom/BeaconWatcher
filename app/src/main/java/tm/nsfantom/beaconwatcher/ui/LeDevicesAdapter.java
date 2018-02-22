package tm.nsfantom.beaconwatcher.ui;

import android.bluetooth.BluetoothDevice;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.databinding.ListitemDeviceBinding;

/**
 * Created by fantom on 27-Sep-17.
 */

public final class LeDevicesAdapter extends RecyclerView.Adapter<LeDevicesAdapter.ResultHolder> {

    private ItemClickedListener itemClickedListener;

    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();

    @Override public ResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListitemDeviceBinding binding = ListitemDeviceBinding.inflate(inflater, parent, false);
        return new ResultHolder(binding.getRoot());
    }

    @Override public void onBindViewHolder(ResultHolder holder, int position) {
        if(position != holder.getAdapterPosition()) return;

        BluetoothDevice device = bluetoothDevices.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            holder.layout.deviceName.setText(deviceName);
        else
            holder.layout.deviceName.setText(R.string.unknown_device);
        holder.layout.deviceAddress.setText(device.getAddress());
//        holder.itemView.setBackgroundResource(R.color.colorItemBackground);
    }

    @Override public int getItemCount() {
        return bluetoothDevices.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addDevice(BluetoothDevice device) {
        if (!bluetoothDevices.contains(device)) {
            bluetoothDevices.add(device);
            notifyDataSetChanged();
        }
    }

    public void removeDevice(BluetoothDevice device){
        if(bluetoothDevices.contains(device)){
            bluetoothDevices.remove(device);
            notifyDataSetChanged();
        }
    }

    public BluetoothDevice getDevice(int position) {
        return bluetoothDevices.get(position);
    }

    public void clear() {
        bluetoothDevices.clear();
        notifyDataSetChanged();
    }

    public void setItemClickedListener(ItemClickedListener itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    public interface ItemClickedListener{
        void onItemClicked(BluetoothDevice bluetoothDevice);
    }

    class ResultHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ListitemDeviceBinding layout;
        View view;

        public ResultHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            layout = DataBindingUtil.bind(itemView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override public void onClick(View view) {
            if(itemClickedListener!=null)
                itemClickedListener.onItemClicked(bluetoothDevices.get(getAdapterPosition()));
        }
    }
}
