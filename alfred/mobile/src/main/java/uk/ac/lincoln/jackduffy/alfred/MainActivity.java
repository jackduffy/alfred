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
        retrieveWebData webData = new retrieveWebData();
        webData.execute();
    }

    private class retrieveWebData extends AsyncTask<Integer, Void, String>
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

                    System.out.println("Checking permissions");

                    try
                    {
                        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();
                        serviceURL = "https://api.darksky.net/forecast/87a57fb875fe5b8587e37d88ecfe6290/" + latitude + "," + longitude;
                        System.out.println(serviceURL);
                    }

                    catch(Exception e)
                    {
                        System.out.println("Error with location sensor");
                    }
                    break;
            }

            try
            {
                System.out.println("RETRIEVING DATA");
                httpConnect jParser = new httpConnect();
                JSONObject currentWeatherObject = new JSONObject(jParser.getJSONFromUrl(serviceURL));
                currentWeatherObject = currentWeatherObject.optJSONObject("currently");
                System.out.println("DATA RETRIEVED!");

                String[] currentWeather = new String[17];
                currentWeather[0] = "time: " + currentWeatherObject.optString("time");
                currentWeather[1] = "summary: " + currentWeatherObject.optString("summary");
                currentWeather[2] = "icon: " + currentWeatherObject.optString("icon");
                currentWeather[3] = "nearestStormDistance: " + currentWeatherObject.optString("nearestStormDistance");
                currentWeather[4] = "nearestStormBearing: " + currentWeatherObject.optString("nearestStormBearing");
                currentWeather[5] = "precipIntensity: " + currentWeatherObject.optString("precipIntensity");
                currentWeather[6] = "precipProbability: " + currentWeatherObject.optString("precipProbability");
                currentWeather[7] = "temperature: " + currentWeatherObject.optString("temperature");
                currentWeather[8] = "apparentTemperature: " + currentWeatherObject.optString("apparentTemperature");
                currentWeather[9] = "dewPoint: " + currentWeatherObject.optString("dewPoint");
                currentWeather[10] = "humidity: " + currentWeatherObject.optString("humidity");
                currentWeather[11] = "windSpeed: " + currentWeatherObject.optString("windSpeed");
                currentWeather[12] = "windBearing: " + currentWeatherObject.optString("windBearing");
                currentWeather[13] = "visibility: " + currentWeatherObject.optString("visibility");
                currentWeather[14] = "cloudCover: " + currentWeatherObject.optString("cloudCover");
                currentWeather[15] = "pressure: " + currentWeatherObject.optString("pressure");
                currentWeather[16] = "ozone: " + currentWeatherObject.optString("ozone");

                //System.out.println(currentWeather.length);

                for(int i = 0; i < currentWeather.length; i++)
                {
                    System.out.println(currentWeather[i]);
                }

            }
            catch (Exception e)
            {
                System.out.println("ERROR ERROR ERROR ERROR");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

















































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
}
