package com.android.googleplaydeveloperconsole.app;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.android.googleplaydeveloperconsole.app.model.DevAccount;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.ApksListResponse;
import com.melnykov.fab.FloatingActionButton;

import java.io.IOException;
import java.util.Collections;

public class MainActivity extends ActionBarActivity
{

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;
    static final int REQUEST_ACCOUNT_PICKER = 2;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();

    final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    GoogleAccountCredential credential;
    AndroidPublisher publisher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Spinner spAccount = (Spinner) findViewById(R.id.spAccount);
        ArrayAdapter<DevAccount> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DevAccountManager.getInstance().accounts);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAccount.setAdapter(spinnerAdapter);

        ListView list = (ListView)findViewById(R.id.list);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(list);

        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
        credential.setSelectedAccountName(PrefsManager.getActiveAccount());

        publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).build();

        new ATLoadAccounts(this).execute();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (checkGooglePlayServicesAvailable())
        {
            haveGooglePlayServices();
        }
    }

    /*package*/ void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, MainActivity.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK)
                {
                    haveGooglePlayServices();
                }
                else
                {
                    checkGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK)
                {
                    //AsyncLoadTasks.run(this);
                }
                else
                {
                    chooseAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null)
                {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null)
                    {
                        credential.setSelectedAccountName(accountName);
                        PrefsManager.setActiveAccount(accountName);
                        //AsyncLoadTasks.run(this);*/
                    }
                }
                break;
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     */
    private boolean checkGooglePlayServicesAvailable()
    {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode))
        {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    private void haveGooglePlayServices()
    {
        // check if there is already an account selected
        if (credential.getSelectedAccountName() == null)
        {
            // ask user to choose account
            chooseAccount();
        }
        else
        {
            // load calendars
            //AsyncLoadTasks.run(this);
        }
    }

    private void chooseAccount()
    {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }


    public static class ATLoadAccounts extends AsyncTask<Void, Void, Void>
    {
        GoogleAccountCredential credential;
        AndroidPublisher publisher;
        MainActivity activity;

        public ATLoadAccounts(MainActivity activity)
        {
            this.activity = activity;
            this.credential = activity.credential;
            this.publisher = activity.publisher;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                AndroidPublisher.Edits.Apks.List edits = publisher.edits().apks().list("rs.pedjaapps.eventlogger", "rs.pedjaapps.eventlogger");
                ApksListResponse response = edits.execute();
                System.out.println("edits:" + response);
            }
            catch (final GooglePlayServicesAvailabilityIOException availabilityException)
            {
                activity.showGooglePlayServicesAvailabilityErrorDialog(availabilityException.getConnectionStatusCode());
            }
            catch (UserRecoverableAuthIOException userRecoverableException)
            {
                activity.startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            /*try
            {
                String token = credential.getToken();
                System.out.println("token:" + token);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (GoogleAuthException e)
            {
                e.printStackTrace();
            }*/
            return null;
        }
    }
}
