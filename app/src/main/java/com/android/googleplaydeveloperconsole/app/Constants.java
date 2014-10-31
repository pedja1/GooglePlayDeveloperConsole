package com.android.googleplaydeveloperconsole.app;

import android.os.Environment;

import java.io.File;

/**
 * @author Predrag Čokulov*/

public class Constants
{

    // **************************************************
    // Misc
    // **************************************************
    /**
     * Debugging log tag
     * */
    public static final String LOG_TAG = "gpdc";

    /**
     * HTTP connection timeout
     * */
    public static final int CONN_TIMEOUT = 2 * 60 * 1000;

    /**
     * URL encoding
     * */
    public static final String ENCODING = "UTF-8";
    public static final boolean LOGGING = BuildConfig.DEBUG;
    public static final boolean STRICT_MODE = LOGGING && false;


    private static final String CRASH_REPORTS_FOLDER = ".crashes";
    public static final String CACHE_FOLDER_NAME = ".cache";
    private static final String EXTERNAL_FOLDER_ROOT = "data/gpdc";
    public static final File INTERNAL_CACHE_DIR = MainApp.getContext().getFilesDir();
    public static final String EXTERNAL_CACHE_DIR = Environment.getExternalStorageDirectory()
            + File.separator + EXTERNAL_FOLDER_ROOT;
    public static final String CRASHES_FULL_PATH = EXTERNAL_CACHE_DIR
            + File.separator + CRASH_REPORTS_FOLDER;
    static
    {
        new File(CRASHES_FULL_PATH).mkdirs();
    }

    public static final String DB_TABLE_NAME = "gpdc.db";
}
