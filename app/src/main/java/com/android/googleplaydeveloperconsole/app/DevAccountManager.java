package com.android.googleplaydeveloperconsole.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.android.googleplaydeveloperconsole.app.model.DevAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedja on 30.10.14. 15.56.
 * This class is part of the GooglePlayDeveloperConsole
 * Copyright © 2014 ${OWNER}
 */
public class DevAccountManager
{
    private static DevAccountManager instance;

    public final List<DevAccount> accounts;

    public static DevAccountManager getInstance()
    {
        if(instance == null)
        {
            instance = new DevAccountManager();
        }
        return instance;
    }

    public DevAccountManager()
    {
        accounts = new ArrayList<>();
    }

    public void init(Context context)
    {
        Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
        List<DevAccount> dbAccounts = DevAccount.list();
        List<DevAccount> accountsForInsert = new ArrayList<>();
        for (Account account : accounts)
        {
            DevAccount devAccount = new DevAccount(account);
            if(dbAccounts.contains(devAccount))
            {
                DevAccount tmp = dbAccounts.get(dbAccounts.indexOf(devAccount));
                devAccount.avatar = tmp.avatar;
                devAccount.name = tmp.name;
            }
            else
            {
                accountsForInsert.add(devAccount);
            }
            this.accounts.add(devAccount);
        }
        DevAccount.insertInTransaction(accountsForInsert);
    }

    public String getFirstAccountNameOrNull()
    {
        if(!accounts.isEmpty())
        {
            return accounts.get(0).id;
        }
        return null;
    }
}
