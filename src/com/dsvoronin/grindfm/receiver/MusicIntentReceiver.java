package com.dsvoronin.grindfm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import com.dsvoronin.grindfm.PlayerService;

/**
 * User: dsvoronin
 * Date: 16.06.12
 * Time: 3:42
 */
public class MusicIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Intent send = new Intent(context, PlayerService.class);
            send.setAction(PlayerService.ACTION_STOP);
            context.startService(send);
        }
    }
}
