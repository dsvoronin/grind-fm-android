package com.dsvoronin.grindfm;

import android.os.Handler;
import android.os.Message;

/**
 * User: dsvoronin
 * Date: 06.04.12
 * Time: 23:43
 * <p/>
 * Обработка сообщений приходящих из сервиса @see GrindService
 */
public abstract class ServiceHandler extends Handler {

    public static final int COMMAND_PROGRESS = 1;
    public static final int COMMAND_STOP = 2;
    public static final int COMMAND_START = 3;

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case COMMAND_PROGRESS:
                handleProgress();
                break;
            case COMMAND_STOP:
                handleStop();
                break;
            case COMMAND_START:
                handleStart();
                break;
            default:
                break;
        }
    }

    //методы для UI обработки команд

    protected abstract void handleProgress();

    protected abstract void handleStop();

    protected abstract void handleStart();
}
