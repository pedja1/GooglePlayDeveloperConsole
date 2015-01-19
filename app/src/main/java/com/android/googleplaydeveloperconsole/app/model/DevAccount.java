package com.android.googleplaydeveloperconsole.app.model;

import android.accounts.Account;

/**
 * Created by pedja on 30.10.14. 15.56.
 * This class is part of the GooglePlayDeveloperConsole
 * Copyright © 2014 ${OWNER}
 */
public class DevAccount
{
    /**
     * Id of the account (email)*/
    public final String id;
    public String name, avatar;

    public DevAccount(Account account)
    {
        this.id = account.name;
    }

    @Override
    public String toString()
    {
        return id;
    }

    public static final String CREATE_TABLE = "";
}
