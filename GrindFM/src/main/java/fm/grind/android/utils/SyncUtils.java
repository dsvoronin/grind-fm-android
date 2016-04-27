package fm.grind.android.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;

import fm.grind.android.sync.AccountService;
import fm.grind.android.sync.GrindProvider;

import java.util.concurrent.TimeUnit;

/**
 * Static helper methods for working with the sync framework.
 * from BasicSyncAdapter sample
 */
public class SyncUtils {

    private static final long SYNC_FREQUENCY_SECONDS = TimeUnit.HOURS.toSeconds(24);

    private static final String CONTENT_AUTHORITY = GrindProvider.Contract.CONTENT_AUTHORITY;

    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    /**
     * Value below must match the account type specified in res/xml/syncadapter.xml
     */
    public static final String ACCOUNT_TYPE = "grind.fm.account";

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    public static void createSyncAccount(Context context) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = AccountService.getAccount(ACCOUNT_TYPE);
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {

            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);

            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);

            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(
                    account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY_SECONDS);

            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            triggerRefresh();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     * <p>
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     * <p>
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void triggerRefresh() {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                AccountService.getAccount(ACCOUNT_TYPE),
                GrindProvider.Contract.CONTENT_AUTHORITY,
                b);
    }
}
