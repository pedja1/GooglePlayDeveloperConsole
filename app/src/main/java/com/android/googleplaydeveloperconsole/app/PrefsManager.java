package com.android.googleplaydeveloperconsole.app;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by pedja on 31.10.14. 15.03.
 * This class is part of the GooglePlayDeveloperConsole
 * Copyright © 2014 ${OWNER}
 */
public class PrefsManager
{
    public static final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainApp.getContext());

    public enum Key
    {
        active_account
    }

    public static String getActiveAccount()
    {
        return prefs.getString(Key.active_account.toString(), DevAccountManager.getInstance().getFirstAccountNameOrNull());
    }

    public static void setActiveAccount(String account)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Key.active_account.toString(), account);
        editor.apply();
    }
}
