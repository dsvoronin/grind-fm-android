package com.dsvoronin.grindfm.player;

import android.os.Handler;

/**
 * A class used to perform periodical updates,
 * specified inside a runnable object. An update interval
 * may be specified (otherwise, the class will perform the
 * update every 2 seconds).
 *
 * @author Carlos Simões
 */
public class UIUpdater {

    private Handler mHandler = new Handler();

    private Runnable mStatusChecker;
    private int UPDATE_INTERVAL = 2000;

    /**
     * Creates an UIUpdater object, that can be used to
     * perform UIUpdates on a specified time interval.
     *
     * @param uiUpdater A runnable containing the update routine.
     */
    public UIUpdater(final Runnable uiUpdater) {
        mStatusChecker = new Runnable() {
            @Override
            public void run() {
                // Run the passed runnable
                uiUpdater.run();
                // Re-run it after the update interval
                mHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
    }

    /**
     * The same as the default constructor, but specifying the
     * intended update interval.
     *
     * @param uiUpdater A runnable containing the update routine.
     * @param interval  The interval over which the routine
     *                  should run (milliseconds).
     */
    public UIUpdater(Runnable uiUpdater, int interval) {
        this(uiUpdater);
        UPDATE_INTERVAL = interval;
    }

    /**
     * Starts the periodical update routine (mStatusChecker
     * adds the callback to the handler).
     */
    public void startUpdates() {
        mStatusChecker.run();
    }

    /**
     * Stops the periodical update routine from running,
     * by removing the callback.
     */
    public void stopUpdates() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}