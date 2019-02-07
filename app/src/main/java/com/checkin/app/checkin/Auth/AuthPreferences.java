package com.checkin.app.checkin.Auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.checkin.app.checkin.Utility.Constants;

public class AuthPreferences {

    public static String getAuthToken(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            return accountManager.peekAuthToken(accounts[0], AccountManager.KEY_AUTHTOKEN);
        }
        return null;
    }
}
