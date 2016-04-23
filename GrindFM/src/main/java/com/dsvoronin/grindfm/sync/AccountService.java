package com.dsvoronin.grindfm.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * A bound Service that instantiates the authenticator
 * when started.
 */
public class AccountService extends Service {

    private static final String TAG = "AccountService";

    public static final String ACCOUNT_NAME = "Account";

    /**
     * Instance field that stores the authenticator object
     */
    private Authenticator authenticator;

    /**
     * Obtain a handle to the {@link android.accounts.Account} used for sync in this application.
     * <p>
     * <p>It is important that the accountType specified here matches the value in your sync adapter
     * configuration XML file for android.accounts.AccountAuthenticator (often saved in
     * res/xml/rss_sync_adapter.xml). If this is not set correctly, you'll receive an error indicating
     * that "caller uid XXXXX is different than the authenticator's uid".
     * <p>
     * Note: Normally the account name is set to the user's identity (username or email
     * address). However, since we aren't actually using any user accounts, it makes more sense
     * to use a generic string in this case.
     * <p>
     * This string should *not* be localized. If the user switches locale, we would not be
     * able to locate the old account, and may erroneously register multiple accounts.
     *
     * @param accountType AccountType defined in the configuration XML file for
     *                    android.accounts.AccountAuthenticator (e.g. res/xml/syncadapter.xml).
     * @return Handle to application's account (not guaranteed to resolve unless createSyncAccount()
     * has been called)
     */
    public static Account getAccount(String accountType) {
        return new Account(ACCOUNT_NAME, accountType);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service created");
        authenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
    }

    /**
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }

    /**
     * Implement AbstractAccountAuthenticator and stub out all
     * of its methods
     */
    public class Authenticator extends AbstractAccountAuthenticator {
        public Authenticator(Context context) {
            super(context);
        }

        /**
         * Editing properties is not supported
         */
        @Override
        public Bundle editProperties(
                AccountAuthenticatorResponse r, String s) {
            throw new UnsupportedOperationException();
        }

        /**
         * Don't add additional accounts
         */
        @Override
        public Bundle addAccount(
                AccountAuthenticatorResponse r,
                String s,
                String s2,
                String[] strings,
                Bundle bundle) throws NetworkErrorException {
            return null;
        }

        /**
         * Ignore attempts to confirm credentials
         */
        @Override
        public Bundle confirmCredentials(
                AccountAuthenticatorResponse r,
                Account account,
                Bundle bundle) throws NetworkErrorException {
            return null;
        }

        /**
         * Getting an authentication token is not supported
         */
        @Override
        public Bundle getAuthToken(
                AccountAuthenticatorResponse r,
                Account account,
                String s,
                Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        /**
         * Getting a label for the auth token is not supported
         */
        @Override
        public String getAuthTokenLabel(String s) {
            throw new UnsupportedOperationException();
        }

        /**
         * Updating user credentials is not supported
         */
        @Override
        public Bundle updateCredentials(
                AccountAuthenticatorResponse r,
                Account account,
                String s, Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        /**
         * Checking features for the account is not supported
         */
        @Override
        public Bundle hasFeatures(
                AccountAuthenticatorResponse r,
                Account account, String[] strings) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }
    }
}