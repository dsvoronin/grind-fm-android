package fm.grind.android.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RssSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static RssSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new RssSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
