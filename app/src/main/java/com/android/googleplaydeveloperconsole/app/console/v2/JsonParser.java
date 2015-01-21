package com.android.googleplaydeveloperconsole.app.console.v2;

import android.util.Log;

import com.android.googleplaydeveloperconsole.app.console.DevConsoleException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * This class contains static methods used to parse JSON from {@link DevConsoleV2}
 * <p/>
 * See {@link https://github.com/AndlyticsProject/andlytics/wiki/Developer-Console-v2} for some more
 * documentation
 */
public class JsonParser
{

    private static final String TAG = JsonParser.class.getSimpleName();

    private static final boolean DEBUG = false;

    private JsonParser()
    {

    }

    /**
     * Parses the supplied JSON string and adds the extracted ratings to the supplied
     * {@link AppStats} object
     *
     * @param json
     * @param stats
     * @throws org.json.JSONException
     */
    static void parseRatings(String json, AppStats stats) throws JSONException
    {
        // Extract just the array with the values
        JSONObject values = new JSONObject(json).getJSONObject("result").getJSONArray("1").getJSONObject(0);

        // Ratings are at index 2 - 6
        stats.setRating(values.getInt("2"), values.getInt("3"), values.getInt("4"), values.getInt("5"), values.getInt("6"));

    }

    /**
     * Parses the supplied JSON string and builds a list of apps from it
     *
     * @param json
     * @param accountName
     * @param skipIncomplete
     * @return List of apps
     * @throws org.json.JSONException
     */
    static List<AppInfo> parseAppInfos(String json, String accountName, boolean skipIncomplete)
            throws JSONException
    {

        Date now = new Date();
        List<AppInfo> apps = new ArrayList<AppInfo>();
        // Extract the base array containing apps
        JSONObject result = new JSONObject(json).getJSONObject("result");
        if (DEBUG)
        {
            pp("result", result);
        }

        JSONArray jsonApps = result.optJSONArray("1");
        if (DEBUG)
        {
            pp("jsonApps", jsonApps);
        }
        if (jsonApps == null)
        {
            // no apps yet?
            return apps;
        }

        int numberOfApps = jsonApps.length();
        Log.d(TAG, String.format("Found %d apps in JSON", numberOfApps));
        for (int i = 0; i < numberOfApps; i++)
        {
            AppInfo app = new AppInfo();
            app.setAccount(accountName);
            app.setLastUpdate(now);
            // Per app:
            // 1 : { 1: package name,
            // 2 : { 1: [{1 : lang, 2: name, 3: description, 4: ??, 5: what's new}], 2 : ?? },
            // 3 : ??,
            // 4 : update history,
            // 5 : price,
            // 6 : update date,
            // 7 : state?
            // }
            // 2 : {}
            // 3 : { 1: active dnd, 2: # ratings, 3: avg rating, 4: ???, 5: total dnd }

            // arrays have changed to objects, with the index as the key
			/*
			 * Per app:
			 * null
			 * [ APP_INFO_ARRAY
			 * * null
			 * * packageName
			 * * Nested array with details
			 * * null
			 * * Nested array with version details
			 * * Nested array with price details
			 * * Last update Date
			 * * Number [1=published, 5 = draft?]
			 * ]
			 * null
			 * [ APP_STATS_ARRAY
			 * * null,
			 * * Active installs
			 * * Total ratings
			 * * Average rating
			 * * Errors
			 * * Total installs
			 * ]
			 */
            JSONObject jsonApp = jsonApps.getJSONObject(i);
            JSONObject jsonAppInfo = jsonApp.getJSONObject("1");
            if (DEBUG)
            {
                pp("jsonAppInfo", jsonAppInfo);
            }
            String packageName = jsonAppInfo.getString("1");
            // Look for "tmp.7238057230750432756094760456.235728507238057230542"
            if (packageName == null
                    || (packageName.startsWith("tmp.") && Character.isDigit(packageName.charAt(4))))
            {
                Log.d(TAG, String.format("Skipping draft app %d, package name=%s", i, packageName));
                continue;
                // Draft app
            }

            // Check number code and last updated date
            // Published: 1
            // Unpublished: 2
            // Draft: 5
            // Draft w/ in-app items?: 6
            // TODO figure out the rest and add don't just skip, filter, etc. Cf. #223
            int publishState = jsonAppInfo.optInt("7");
            Log.d(TAG, String.format("%s: publishState=%d", packageName, publishState));
            if (publishState != 1)
            {
                // Not a published app, skipping
                Log.d(TAG, String.format(
                        "Skipping app %d with state != 1: package name=%s: state=%d", i,
                        packageName, publishState));
                continue;
            }
            app.setPublishState(publishState);
            app.setPackageName(packageName);

			/*
			 * Per app details:
			 * 1: Country code
			 * 2: App Name
			 * 3: Description
			 * 4: Promo text
			 * 5: Last what's new
			 */
            // skip if we can't get all the data
            // XXX should we just let this crash so we know there is a problem?
            if (!jsonAppInfo.has("2"))
            {
                if (skipIncomplete)
                {
                    Log.d(TAG, String.format(
                            "Skipping app %d because no app details found: package name=%s", i,
                            packageName));
                }
                else
                {
                    Log.d(TAG, "Adding incomplete app: " + packageName);
                    apps.add(app);
                }
                continue;
            }
            if (!jsonAppInfo.has("4"))
            {
                if (skipIncomplete)
                {
                    Log.d(TAG, String.format(
                            "Skipping app %d because no versions info found: package name=%s", i,
                            packageName));
                }
                else
                {
                    Log.d(TAG, "Adding incomplete app: " + packageName);
                    apps.add(app);
                }
                continue;
            }

            JSONObject appDetails = jsonAppInfo.getJSONObject("2").getJSONArray("1")
                    .getJSONObject(0);
            if (DEBUG)
            {
                pp("appDetails", appDetails);
            }
            app.setName(appDetails.getString("2"));

            String description = appDetails.getString("3");
            String changelog = appDetails.optString("5");
            Long lastPlayStoreUpdate = jsonAppInfo.getJSONObject("11").getLong("1");
            AppDetails details = new AppDetails(description, changelog, lastPlayStoreUpdate);
            app.setDetails(details);

			/*
			 * Per app version details:
			 * null
			 * null
			 * packageName
			 * versionNumber
			 * versionName
			 * null
			 * Array with app icon [null,null,null,icon]
			 */
            // XXX
            JSONArray appVersions = jsonAppInfo.getJSONObject("4").getJSONObject("1")
                    .optJSONArray("1");
            if (DEBUG)
            {
                pp("appVersions", appVersions);
            }
            if (appVersions == null)
            {
                if (skipIncomplete)
                {
                    Log.d(TAG, String.format(
                            "Skipping app %d because no versions info found: package name=%s", i,
                            packageName));
                }
                else
                {
                    Log.d(TAG, "Adding incomplete app: " + packageName);
                    apps.add(app);
                }
                continue;
            }
            JSONObject lastAppVersionDetails = appVersions.getJSONObject(appVersions.length() - 1)
                    .getJSONObject("2");
            if (DEBUG)
            {
                pp("lastAppVersionDetails", lastAppVersionDetails);
            }
            app.setVersionName(lastAppVersionDetails.getString("4"));
            app.setIconUrl(lastAppVersionDetails.getJSONObject("6").getString("3"));

            // App stats
			/*
			 * null,
			 * Active installs
			 * Total ratings
			 * Average rating
			 * Errors
			 * Total installs
			 */
            // XXX this index might not be correct for all apps?
            // 3 : { 1: active dnd, 2: # ratings, 3: avg rating, 4: #errors?, 5: total dnd }
            JSONObject jsonAppStats = jsonApp.optJSONObject("3");
            if (DEBUG)
            {
                pp("jsonAppStats", jsonAppStats);
            }
            if (jsonAppStats == null)
            {
                if (skipIncomplete)
                {
                    Log.d(TAG, String.format(
                            "Skipping app %d because no stats found: package name=%s", i,
                            packageName));
                }
                else
                {
                    Log.d(TAG, "Adding incomplete app: " + packageName);
                    apps.add(app);
                }
                continue;
            }
            AppStats stats = new AppStats();
            stats.setDate(now);
            if (jsonAppStats.length() < 4)
            {
                // no statistics (yet?) or weird format
                // TODO do we need differentiate?
                stats.setActiveInstalls(0);
                stats.setTotalDownloads(0);
                stats.setNumberOfErrors(0);
            }
            else
            {
                stats.setActiveInstalls(jsonAppStats.getInt("1"));
                stats.setTotalDownloads(jsonAppStats.getInt("5"));
                stats.setNumberOfErrors(jsonAppStats.optInt("4"));
            }
            app.setLatestStats(stats);

            apps.add(app);
        }

        return apps;
    }

    private static void pp(String name, JSONArray jsonArr)
    {
        try
        {
            String pp = jsonArr == null ? "null" : jsonArr.toString(2);
            Log.d(TAG, String.format("%s: %s", name, pp));
            FileUtils.writeToDebugDir(name + "-pp.json", pp);
        }
        catch (JSONException e)
        {
            Log.w(TAG, "Error printing JSON: " + e.getMessage(), e);
        }
    }

    private static void pp(String name, JSONObject jsonObj)
    {
        try
        {
            String pp = jsonObj == null ? "null" : jsonObj.toString(2);
            Log.d(TAG, String.format("%s: %s", name, pp));
            FileUtils.writeToDebugDir(name + "-pp.json", pp);
        }
        catch (JSONException e)
        {
            Log.w(TAG, "Error printing JSON: " + e.getMessage(), e);
        }
    }

    private static DevConsoleException parseError(JSONObject jsonObj, String message) throws JSONException
    {
        JSONObject errorObj = jsonObj.getJSONObject("error");
        String data = errorObj.getJSONObject("data").optString("1");
        String errorCode = errorObj.optString("code");

        return new DevConsoleException(String.format("Error %s: %s, errorCode=%s", message, data,
                errorCode));
    }


    /**
     * Parses the given date
     *
     * @param unixDateCode
     * @return
     */
    private static Date parseDate(long unixDateCode)
    {
        return new Date(unixDateCode);
    }

}
