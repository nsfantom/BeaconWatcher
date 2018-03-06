package tm.nsfantom.beaconwatcher.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.Iterator;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.ui.adapter.BeaconAdapter;
import tm.nsfantom.beaconwatcher.util.TagProfile;

public class MainFragmentAlt extends BaseMonitorFragment implements BeaconConsumer {

    private BeaconAdapter adapter;

    public static MainFragmentAlt newInstance() {
        return new MainFragmentAlt();
    }

    private BeaconManager beaconManager;

    @Override
    protected void init() {
        adapter = new BeaconAdapter();
        adapter.setHasStableIds(true);
        beaconManager = BeaconManager.getInstanceForApplication(getContext());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        // Initializes list view adapter.
        layout.rvDevice.setLayoutManager(new LinearLayoutManager(getContext()));
        layout.rvDevice.setAdapter(adapter);

        adapter.setItemClickedListener(beacon -> {
            if (beacon == null) return;
            listener.onBeaconClicked(beacon);
            Toast.makeText(getContext(), "device: " + beacon.getBluetoothName(), Toast.LENGTH_SHORT).show();
        });
        layout.toggleRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> startScan(isChecked));
        layout.btnAdvertise.setOnClickListener(v -> listener.onAdvertise());
        layout.btnSimulate.setOnClickListener(v -> listener.onSimulate());

    }

    @Override
    protected void startScan(boolean enable) {
        if (enable){
            beaconManager.bind(this);
            layout.llControls.setKeepScreenOn(true);
        } else{
            layout.llControls.setKeepScreenOn(false);
            beaconManager.unbind(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        this.beaconManager.addRangeNotifier((beacons, region) -> {
            Timber.e("Beacons: %s", beacons.size());
            if (beacons.size() > 0) {
                adapter.clear();
                for (Iterator<Beacon> iterator = beacons.iterator(); iterator.hasNext(); ) {
                    adapter.addBeacon(iterator.next());
                }
                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        });
        try {
            this.beaconManager.startRangingBeaconsInRegion(new Region("MyRegionId", null, null, null));
            this.beaconManager.startRangingBeaconsInRegion(new Region("gimbal", Identifier.parse("9A4D89AE-EC35-4191-AC68-888D132FB786"), null, null));
            this.beaconManager.startRangingBeaconsInRegion(new Region("radnetworks", Identifier.parse("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6"), null, null));
            this.beaconManager.startRangingBeaconsInRegion(new Region("muspot", Identifier.parse(TagProfile.MU_DEVICEUUID), null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        getActivity().unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return getActivity().bindService(intent,serviceConnection,i);
    }

}
