package com.dsvoronin.grindfm.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class MusicIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                LocalBroadcastManager.getInstance(ctx).sendBroadcast(new Intent(PlayerService.Action.FORCE_STOP.name()));
            }
        }
    }
}