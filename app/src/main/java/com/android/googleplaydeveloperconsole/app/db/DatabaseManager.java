package com.android.googleplaydeveloperconsole.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.googleplaydeveloperconsole.app.model.App;
import com.android.googleplaydeveloperconsole.app.model.DevAccount;

import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager
{
    private AtomicInteger mOpenCounter = new AtomicInteger();

    private static DatabaseManager instance;
    private static DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(Context context)
    {
        if (instance == null)
        {
            instance = new DatabaseManager();
            mDatabaseHelper = new DatabaseHelper(context);
            instance.mDatabase = mDatabaseHelper.getWritableDatabase();
        }
    }

    public static synchronized DatabaseManager getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    public SQLiteDatabase getDatabase()
    {
        return mDatabase;
    }
}

class DatabaseHelper extends SQLiteOpenHelper
{
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    public static final String DATABASE_NAME = "gpdc";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(DevAccount.CREATE_TABLE);
        db.execSQL(App.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(DevAccount.DROP_TABLE);
        db.execSQL(App.DROP_TABLE);
        onCreate(db);
    }

}
