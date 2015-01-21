package com.android.googleplaydeveloperconsole.app.model;

import android.accounts.Account;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.googleplaydeveloperconsole.app.db.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

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
    public String name, avatar, developerId;
    private List<App> apps;

    public DevAccount(Account account)
    {
        this.id = account.name;
    }

    public DevAccount(String id)
    {
        this.id = id;
    }

    public List<App> getApps()
    {
        return getApps(false);
    }

    public List<App> getApps(boolean forceQuery)
    {
        if(apps != null && !forceQuery)return apps;

        apps = App.list(id);
        return apps;
    }

    @Override
    public String toString()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DevAccount that = (DevAccount) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    ////////////////////////////////////////////
    ////DATABASE
    ////////////////////////////////////////////
    public static final String TABLE = "account";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_AVATAR = "avatar";
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
            + KEY_ID + " TEXT PRIMARY KEY NOT NULL,"
            + KEY_NAME + " TEXT,"
            + KEY_AVATAR + " TEXT"
            + ")";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;

    public static void insertOrReplace(DevAccount account)
    {
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, account.id);
        values.put(KEY_NAME, account.name);
        values.put(KEY_AVATAR, account.avatar);
        db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void insertInTransaction(List<DevAccount> accounts)
    {
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        db.beginTransaction();
        try
        {
            for(DevAccount account : accounts)
            {
                insertOrReplace(account);
            }
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
    }

    public static List<DevAccount> list()
    {
        List<DevAccount> list = new ArrayList<>();

        Cursor cursor = DatabaseManager.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE, null);

        while (cursor.moveToNext())
        {
            DevAccount account = new DevAccount(cursor.getString(cursor.getColumnIndex(KEY_ID)));
            account.avatar = cursor.getString(cursor.getColumnIndex(KEY_AVATAR));
            account.name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
            list.add(account);
        }

        cursor.close();
        return list;
    }
}
