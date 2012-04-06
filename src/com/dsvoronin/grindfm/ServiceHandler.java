package com.dsvoronin.grindfm;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * User: dsvoronin
 * Date: 06.04.12
 * Time: 23:43
 */
public abstract class ServiceHandler extends Handler {

    private final String TAG = "GRIND-HANDLER";

    private Context mContext;

    protected ServiceHandler(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void handleMessage(Message msg) {

        if (msg.what == mContext.getResources().getInteger(R.integer.service_intent_message_progress)) {
            Log.d(TAG, "Got Progress message");
            handleProgress();
        } else if (msg.what == mContext.getResources().getInteger(R.integer.service_intent_stop)) {
            Log.d(TAG, "Got Stop message");
            mContext.stopService(new Intent(mContext, GrindService.class));
            handleStop();
        }
    }

    protected abstract void handleProgress();

    protected abstract void handleStop();
}
