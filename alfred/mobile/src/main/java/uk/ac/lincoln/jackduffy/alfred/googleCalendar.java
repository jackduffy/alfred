package uk.ac.lincoln.jackduffy.alfred;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;
import java.util.List;

public class googleCalendar extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

    com.google.api.services.calendar.Calendar mService;

    GoogleAccountCredential credential;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    GoogleApiClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) //when the class is created
    {
        super.onCreate(savedInstanceState);

        try
        {
            googleClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            System.out.println("Google Api Client built");
        }

        catch (Exception e)
        {
            System.out.println("FAILED TO BUILD GOOGLE API CLIENT");
            System.out.println(e);
        }

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();
    }

    @Override
    protected void onResume() //called if the class is resumed
    {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            System.out.println("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) //called when the activity is launched
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null)
                {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null)
                    {
                        System.out.println("Account name is... " + accountName);
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        refreshResults();
                    }
                }

                else if (resultCode == RESULT_CANCELED)
                {
                    System.out.println("Account unspecified.");
                }
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                    System.out.println("Account is ok");
                }

                else
                {
                    System.out.println("Account is not ok");
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void refreshResults() //attempt to get data from calendar
    {
        System.out.println("Refreshing...");
        System.out.println(credential.getSelectedAccount());
        if (credential.getSelectedAccountName() == null)
        {
            chooseAccount();
        }

        else
        {
            if (isDeviceOnline()) {
                new ApiAsyncTask(this).execute();
            } else {
                System.out.println("No network connection available.");
            }
        }
    }

    public void updateResultsText(final List<String> dataStrings) //transmit the data to the watch
    {
        runOnUiThread(new Runnable() 
        {
            @Override
            public void run()
            {
                if (dataStrings == null)
                {
                    System.out.println("Error retrieving data!");
                }

                else if (dataStrings.size() == 0)
                {
                    System.out.println("No data found.");
                }

                else
                {
                    DataMap dataMap = new DataMap();
                    dataMap.putLong("#-TIME-STAMP:", System.nanoTime());
                    dataMap.putLong("#-CONTENT:", 1);

                    for(int i = 0; i < dataStrings.size(); i++)
                    {
                        if(i < 10)
                        {
                            dataMap.putString("0" + Integer.toString(i) + "-" + dataStrings.get(i), "");
                        }

                        else
                        {
                            dataMap.putString(Integer.toString(i) + "-" + dataStrings.get(i), "");
                        }

                        //System.out.println(dataStrings.get(i));
                    }

                    try
                    {
                        //System.out.println("Attempting to send to data layer");
                        new googleCalendar.SendToDataLayerThread("/data_from_phone", dataMap).start();
                    }

                    catch(Exception e)
                    {
                        System.out.println("Error sending data to watch");
                    }
                }
            }
        });
    }

    public void updateStatus(final String message) //send status message
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                System.out.println(message);
            }
        });
    }

    private void chooseAccount() //start account activity
    {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private boolean isDeviceOnline() //check for active network connection
    {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() //check that play services is installed and up to date
    {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) //display error if play services is not installed or up to date
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        googleCalendar.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    //region DataLayer Transmission
    @Override
    protected void onStart()
    {
        super.onStart();
        if (googleClient != null)
        {
            googleClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle)
    {

    }

    @Override
    protected void onStop()
    {
        if (googleClient != null && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        System.out.println("Connection to watch has failed");
    }

    class SendToDataLayerThread extends Thread
    {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data)
        {
            path = p;
            dataMap = data;
        }

        public void run()
        {
            PutDataMapRequest putDMR = PutDataMapRequest.create(path);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleClient, request).await();

            if (result.getStatus().isSuccess())
            {
                //Log.v("myTag", "DataMap: " + dataMap + " sent successfully to data layer ");
            }

            else
            {
                Log.v("myTag", "ERROR: failed to send DataMap to data layer");
            }

            finish();
        }
    }
    //endregion
}