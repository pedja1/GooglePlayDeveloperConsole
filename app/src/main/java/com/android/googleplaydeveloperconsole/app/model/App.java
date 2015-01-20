package com.android.googleplaydeveloperconsole.app.model;

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
public class App
{
    /**primary key - package name of the app*/
    public final String id;
    public String name;
    /**foreign key*/
    public final String accountId;

    public App(String packageName, String accountId)
    {
        this.id = packageName;
        this.accountId = accountId;
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

        App app = (App) o;

        if (!accountId.equals(app.accountId)) return false;
        if (!id.equals(app.id)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + accountId.hashCode();
        return result;
    }

    ////////////////////////////////////////////
    ////DATABASE
    ////////////////////////////////////////////
    public static final String TABLE = "app";
    public static final String KEY_ID = "packageName";
    public static final String KEY_NAME = "name";
    public static final String KEY_ACCOUNT_ID = "account_id";
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
            + KEY_ID + " TEXT PRIMARY KEY NOT NULL,"
            + KEY_NAME + " TEXT,"
            + KEY_ACCOUNT_ID + " TEXT,"
            + "FOREIGN KEY(" + KEY_ACCOUNT_ID + ") REFERENCES " + DevAccount.TABLE + "(" + DevAccount.KEY_ID + ")"
            + ")";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;

    public static void insertOrReplace(App app)
    {
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, app.id);
        values.put(KEY_NAME, app.name);
        values.put(KEY_ACCOUNT_ID, app.accountId);
        db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void insertInTransaction(List<App> apps)
    {
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        db.beginTransaction();
        try
        {
            for(App account : apps)
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

    public static List<App> list(String accountId)
    {
        List<App> list = new ArrayList<>();

        Cursor cursor = DatabaseManager.getInstance().getDatabase().rawQuery("SELECT * FROM " + TABLE + " WHERE " + App.KEY_ACCOUNT_ID + " = " + accountId, null);

        while (cursor.moveToNext())
        {
            App account = new App(cursor.getString(cursor.getColumnIndex(KEY_ID)), cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT_ID)));
            account.name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
            list.add(account);
        }

        cursor.close();
        return list;
    }

}
