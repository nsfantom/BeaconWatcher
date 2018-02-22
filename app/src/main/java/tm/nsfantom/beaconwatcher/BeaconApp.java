package tm.nsfantom.beaconwatcher;

import android.app.Application;

import timber.log.Timber;


public class BeaconApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

    }
}
