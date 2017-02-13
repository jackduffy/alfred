package uk.ac.lincoln.jackduffy.alfred;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.data.FreezableUtils;
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


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    GoogleApiClient googleClient;

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
        APIAccess(1);
        retrieveWebData webData = new retrieveWebData();
        webData.execute(1);
    }

    private class retrieveWebData extends AsyncTask<Integer, Void, String>
    {
        @Override
        protected String doInBackground(Integer[] service)
        {
            String serviceURL = "";
//            switch(service)
//            {
//                case 1: //weather
                    serviceURL = "http://datapoint.metoffice.gov.uk/public/data/val/wxfcs/all/xml/3840?res=3hourly&key=b2133d57-87fc-49f8-9381-6d3498a0595a";
//                    break;
//            }

            try {
                URL url = new URL(serviceURL);
                URLConnection conn = url.openConnection();
                System.out.println(conn);


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

    public void APIAccess(int service)
    {

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
