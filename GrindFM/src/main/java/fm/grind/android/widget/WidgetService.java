package fm.grind.android.widget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService {

    private static final String TAG = "GrindWidget";

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(TAG, "onGetViewFactory: ");
        return new WidgetViewsFactory(getApplicationContext(), intent);
    }
}