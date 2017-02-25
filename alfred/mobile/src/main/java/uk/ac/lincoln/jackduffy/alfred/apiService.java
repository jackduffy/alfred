package uk.ac.lincoln.jackduffy.alfred;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONObject;

import static java.security.AccessController.getContext;

public class apiService extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    GoogleApiClient googleClient;
    //Boolean googleApiClientBuilt = false;
    String apiService = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
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
            //googleApiClientBuilt = true;
        }

        catch (Exception e)
        {
            System.out.println("FAILED TO BUILD GOOGLE API CLIENT");
            System.out.println(e);
            //googleApiClientBuilt = false;
        }

        //APIController("WEATHER");
        APIController(ListenerService.apiRequest);

    }

    public void APIController(String input)
    {
        System.out.println("Input: " + input);
        apiService = input;
        accessAPIData api = new accessAPIData();
        api.execute();

    }

//    public void buildGoogleApiClient()
//    {    }

    private class accessAPIData extends AsyncTask<Integer, Void, String>
    {
        @Override
        protected String doInBackground(Integer[] service)
        {
            String serviceURL = "";
            //System.out.println("Searching for " + apiService);
            switch(apiService)
            {
                case "":
                    break;
                case "WEATHER":
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
                        System.out.println("Error with location sensor, using defaults...");
                        serviceURL = "https://api.darksky.net/forecast/87a57fb875fe5b8587e37d88ecfe6290/37.8267,-122.4233";
                    }
                    //endregion
                    break;
            }

            try
            {
                httpConnect jParser = new httpConnect();
                JSONObject currentWeatherObject = new JSONObject(jParser.getJSONFromUrl(serviceURL));
                //System.out.println("Retrieved data from " + serviceURL);
                DataMap dataMap = new DataMap();
                dataMap.putLong("#-TIME-STAMP:", System.nanoTime());

                switch(apiService)
                {
                    case "":
                        break;
                    case "WEATHER":
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
                    //System.out.println("Attempting to send to data layer");
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
