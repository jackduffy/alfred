package uk.ac.lincoln.jackduffy.alfred;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    GoogleApiClient googleClient;
    String apiService = "";
    Boolean permissionsGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Build a new GoogleApiClient
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //checkAndRequestPermissions();

//        if(permissionsGranted = true)
//        {
//            System.out.println("Proceed");
//        }
    }

    private void checkAndRequestPermissions()
    {
        try
        {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                {

                }

                else
                {
                    //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            }

            permissionsGranted = true;
        }

        catch(Exception e)
        {
            permissionsGranted = false;
            System.out.println("Permissions not granted");
        }
    }












    public void sendTestMessage(View view)
    {
        String DATA_PATH = "/data_from_phone";
        DataMap dataMap = new DataMap();
        dataMap.putString("TEST MESSAGE EVENT!", "1");
        dataMap.putLong("timestamp", System.nanoTime());
        new SendToDataLayerThread(DATA_PATH, dataMap).start();
    }

    public void sendWeatherDetails(View view)
    {
        apiService = "weather";
        accessAPIData api = new accessAPIData();
        api.execute();
    }

    private class accessAPIData extends AsyncTask<Integer, Void, String>
    {
        @Override
        protected String doInBackground(Integer[] service)
        {
            String serviceURL = "";
            switch(apiService)
            {
                case "":
                    break;
                case "weather":
                    //region Weather API
                    try
                    {
                        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();
                        serviceURL = "https://api.darksky.net/forecast/87a57fb875fe5b8587e37d88ecfe6290/" + latitude + "," + longitude;
                    }

                    catch(Exception e)
                    {
                        System.out.println("Error with location sensor");
                    }
                    //endregion
                    break;
            }

            try
            {
                httpConnect jParser = new httpConnect();
                JSONObject currentWeatherObject = new JSONObject(jParser.getJSONFromUrl(serviceURL));
                System.out.println("Retrieved data from " + serviceURL);
                DataMap dataMap = new DataMap();
                dataMap.putLong("#-TIME-STAMP:", System.nanoTime());

                switch(apiService)
                {
                    case "":
                        break;
                    case "weather":
                        currentWeatherObject = currentWeatherObject.optJSONObject("currently");
                        String[] currentWeather = new String[17];
                        //region Populate currentWeather with all elements from the 'current' JSON object
                        currentWeather[0] = currentWeatherObject.optString("time");
                        currentWeather[1] = currentWeatherObject.optString("summary");
                        currentWeather[2] = currentWeatherObject.optString("icon");
                        currentWeather[3] = currentWeatherObject.optString("nearestStormDistance");
                        currentWeather[4] = currentWeatherObject.optString("nearestStormBearing");
                        currentWeather[5] = currentWeatherObject.optString("precipIntensity");
                        currentWeather[6] = currentWeatherObject.optString("precipProbability");
                        currentWeather[7] = currentWeatherObject.optString("temperature");
                        currentWeather[8] = currentWeatherObject.optString("apparentTemperature");
                        currentWeather[9] = currentWeatherObject.optString("dewPoint");
                        currentWeather[10] =currentWeatherObject.optString("humidity");
                        currentWeather[11] = currentWeatherObject.optString("windSpeed");
                        currentWeather[12] =  currentWeatherObject.optString("windBearing");
                        currentWeather[13] = currentWeatherObject.optString("visibility");
                        currentWeather[14] =  currentWeatherObject.optString("cloudCover");
                        currentWeather[15] = currentWeatherObject.optString("pressure");
                        currentWeather[16] = currentWeatherObject.optString("ozone");
                        //endregion
                        //region Put all the weather data into a dataMap packet
                        dataMap.putLong("#-CONTENT:", 0);
                        dataMap.putString("00-time", currentWeather[0]);
                        dataMap.putString("01-summary", currentWeather[1]);
                        dataMap.putString("02-icon", currentWeather[2]);
                        dataMap.putString("03-nearestStormDistance", currentWeather[3]);
                        dataMap.putString("04-nearestStormBEaring", currentWeather[4]);
                        dataMap.putString("05-precipIntensity", currentWeather[5]);
                        dataMap.putString("06-precipPRobability", currentWeather[6]);
                        dataMap.putString("07-temperature", currentWeather[7]);
                        dataMap.putString("08-apparentTemperature", currentWeather[8]);
                        dataMap.putString("09-dewPoint", currentWeather[9]);
                        dataMap.putString("10-humidity", currentWeather[10]);
                        dataMap.putString("11-windSpeed", currentWeather[11]);
                        dataMap.putString("12-windBearing", currentWeather[12]);
                        dataMap.putString("13-visibility", currentWeather[13]);
                        dataMap.putString("14-cloudCover", currentWeather[14]);
                        dataMap.putString("15-pressure", currentWeather[15]);
                        dataMap.putString("16-ozone", currentWeather[16]);
                        //endregion
                        break;
                }

                try
                {
                    System.out.println("Attempting to send to data layer");
                    new SendToDataLayerThread("/data_from_phone", dataMap).start();
                }

                catch(Exception e)
                {
                    System.out.println("Error sending data to watch");
                }
            }

            catch (Exception e)
            {
                System.out.println("General error with API data");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message)
        {
            //process message
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    //region DataLayer Transmission
    @Override
    protected void onStart()
    {
        super.onStart();
        googleClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle)
    {

    }

    @Override
    protected void onStop()
    {
//        if (null != googleClient && googleClient.isConnected()) {
//            googleClient.disconnect();
//        }

        googleClient.disconnect();
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
                Log.v("myTag", "DataMap: " + dataMap + " sent successfully to data layer ");
            }

            else
            {
                // Log an error
                Log.v("myTag", "ERROR: failed to send DataMap to data layer");
            }
        }
    }
    //endregion
}
