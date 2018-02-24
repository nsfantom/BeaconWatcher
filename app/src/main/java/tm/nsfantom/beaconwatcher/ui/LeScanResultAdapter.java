package tm.nsfantom.beaconwatcher.ui;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
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
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public final class LeScanResultAdapter extends RecyclerView.Adapter<LeScanResultAdapter.ResultHolder> {

    private ItemClickedListener itemClickedListener;

    private List<ScanResult> bluetoothDevices = new ArrayList<>();

    @Override public ResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListitemDeviceBinding binding = ListitemDeviceBinding.inflate(inflater, parent, false);
        return new ResultHolder(binding.getRoot());
    }

    @Override public void onBindViewHolder(ResultHolder holder, int position) {
        if(position != holder.getAdapterPosition()) return;

        ScanResult device = bluetoothDevices.get(position);
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

    @Override public int getItemCount() {
        return bluetoothDevices.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addResult(ScanResult device) {
        if (!bluetoothDevices.contains(device)) {
            bluetoothDevices.add(device);
            notifyDataSetChanged();
        }
    }

    public void removeResult(ScanResult device){
        if(bluetoothDevices.contains(device)){
            bluetoothDevices.remove(device);
            notifyDataSetChanged();
        }
    }

    public ScanResult getDevice(int position) {
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
                itemClickedListener.onItemClicked(bluetoothDevices.get(getAdapterPosition()).getDevice());
        }
    }
}
