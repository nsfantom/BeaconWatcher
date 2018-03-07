package tm.nsfantom.beaconwatcher.ui.fragment.base;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.TextView;

import timber.log.Timber;
import tm.nsfantom.beaconwatcher.R;
import tm.nsfantom.beaconwatcher.ble.BleGattServerCallbackWrapper;
import tm.nsfantom.beaconwatcher.databinding.FragmentSimpleadvertiserBinding;
import tm.nsfantom.beaconwatcher.ui.adapter.ColorSpinnerAdapter;
import tm.nsfantom.beaconwatcher.util.PrefStorage;

public abstract class AdvertiseFragment extends Fragment implements TextView.OnEditorActionListener, BleGattServerCallbackWrapper.LogStatus {
    private FragmentSimpleadvertiserBinding layout;
    private BleGattServerCallbackWrapper bleGattServerCallbackWrapper;
    protected AdvertiseCallback advCallback;

    protected static final int APPLE = 0x004c;

    protected static int minor = 1;
    protected static int major = 1;

    protected PrefStorage prefStorage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_simpleadvertiser, container, false);
        init();
        advCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                if (settingsInEffect != null) {
                    appendStatus(settingsInEffect.toString());
                } else {
                    appendStatus("onStartSuccess: settingInEffect = null");
                }
            }

            @Override
            public void onStartFailure(int errorCode) {
                appendStatus("onStartFailure: errorCode = " + errorCode);
            }
        };
        Timber.d(getString(R.string.ble_initialized));
        prefStorage = new PrefStorage(getContext());
        bleGattServerCallbackWrapper = new BleGattServerCallbackWrapper(getContext())
                .setLogStatusListener(this)
                .init(prefStorage);

        return layout.getRoot();
    }

    abstract protected void init();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layout.switchAdvertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) startAdvertise();
            else stopAdvertise();
            toggleGattServer(isChecked);
            layout.llControls.setKeepScreenOn(isChecked);
        });

        layout.etMajor.setOnEditorActionListener(this);
        layout.etMinor.setOnEditorActionListener(this);
        layout.etDeviceName.setOnEditorActionListener(this);
        layout.spinnerTagColor.setAdapter(new ColorSpinnerAdapter());
        layout.spinnerTagColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefStorage.saveTagColor(position);
                layout.llControls.setBackgroundResource(ColorSpinnerAdapter.TagColor.values()[position].colorResource);
                bleGattServerCallbackWrapper.notifyCharacteristicChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(prefStorage.getTagColor());
            }
        });

        onPrefsChanged();

        layout.tvLogger.setOnLongClickListener(v -> {
            layout.tvLogger.setText("");
            return true;
        });
    }

    abstract protected void startAdvertise();

    abstract protected void stopAdvertise();

    private void toggleGattServer(boolean start) {
        if (bleGattServerCallbackWrapper == null) return;
        if (start) bleGattServerCallbackWrapper.startGattServer();
        else bleGattServerCallbackWrapper.stopGattServer();
    }

    @Override
    public void onDestroyView() {
        layout.llControls.setKeepScreenOn(false);
        bleGattServerCallbackWrapper.stopGattServer();
        stopAdvertise();
        super.onDestroyView();
    }

    @Override
    public void appendStatus(final String status) {
        getActivity().runOnUiThread(() -> {
            String current = layout.tvLogger.getText().toString();
            layout.tvLogger.setText(status.concat("\n").concat(current));
//            layout.tvLogger.setText(current + "\n" + status);
        });
    }

    @Override
    public void onPrefsChanged() {
        getActivity().runOnUiThread(() -> {
            layout.etMajor.setText(String.valueOf(prefStorage.getMajor()));
            major = prefStorage.getMajor();
            layout.etMinor.setText(String.valueOf(prefStorage.getMinor()));
            minor = prefStorage.getMinor();
            layout.etDeviceName.setText(prefStorage.getDeviceName());
            layout.spinnerTagColor.setSelection(prefStorage.getTagColor());
        });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            switch (v.getId()) {
                case R.id.etMajor:
                    major = Integer.parseInt(v.getText().toString());
                    prefStorage.saveMajor(major);
                    break;
                case R.id.etMinor:
                    minor = Integer.parseInt(v.getText().toString());
                    prefStorage.saveMinor(minor);
                    break;
                case R.id.etDeviceName:
                    prefStorage.saveDeviceName(layout.etDeviceName.getText().toString());
                    break;
            }
            bleGattServerCallbackWrapper.notifyCharacteristicChanged();
        }

        return false;
    }
}
