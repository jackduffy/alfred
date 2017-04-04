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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

            if(apiService.contains("#"))
            {
                String[] input = apiService.split("#");
                apiService = input[0];
                String searchInput = input[1];

                switch (apiService)
                {
                    case "WIKIPEDIA":
                        //region Wikipedia
                        try
                        {
                            searchInput = searchInput.toLowerCase();
                            String[] normalise = searchInput.split("_");

                            try
                            {

                                searchInput = "";
                                for(int i = 0; i < normalise.length; i++)
                                {
                                    normalise[i] = normalise[i].substring(0, 1).toUpperCase() + normalise[i].substring(1);
                                    if(i != (normalise.length - 1))
                                    {
                                        normalise[i] = normalise[i] + "%20";
                                    }

                                    else{}

                                    searchInput = searchInput + normalise[i];
                                }
                            }

                            catch(Exception e)
                            {
                                System.out.println(e);
                            }


                            serviceURL = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles=" + searchInput;
                        }

                        catch(Exception e)
                        {
                            System.out.println("Error with Wikipedia API");
                        }
                        //endregion
                        break;
                    case "LASTFM":
                        try
                        {
                            searchInput = (searchInput.toLowerCase()).substring(1);
                            searchInput = searchInput.replaceAll("_", "");

                            System.out.println("Search term is: " + searchInput);
                            serviceURL = "http://ws.audioscrobbler.com/2.0/?method=track.search&track=" + searchInput + "&api_key=9d1a172866af41f92990acaa0912b638&format=json";
                        }

                        catch(Exception e)
                        {
                            System.out.println("Error with LastFM API");
                            System.out.println(e);
                        }
                        break;
                }
            }

            else
            {
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
                    case "CINEMAS_NEARBY":
                        //region CineList API
                        try
                        {
                            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            double longitude = location.getLongitude();
                            double latitude = location.getLatitude();
                            serviceURL = "https://api.cinelist.co.uk/search/cinemas/coordinates/" + latitude + "/" + longitude;
                        }

                        catch(Exception e)
                        {
                            System.out.println("Error with location sensor, using defaults...");
                            serviceURL = "https://api.cinelist.co.uk/search/cinemas/coordinates/50.7200/-1.8800";
                        }
                        //endregion
                        break;
                    case "NEWS_GENERAL":
                        //region News General
                        try
                        {
                            //https://newsapi.org/bbc-news-api
                            serviceURL = "https://newsapi.org/v1/articles?source=bbc-news&sortBy=top&apiKey=62d6fc1a705742dba9c9395c903138a7";
                        }

                        catch(Exception e)
                        {
                            System.out.println("Error with news API");
                        }
                        //endregion
                        break;
                }
            }

            try
            {
                httpConnect jParser = new httpConnect();

                System.out.println("Retrieved data from " + serviceURL);
                DataMap dataMap = new DataMap();
                dataMap.putLong("#-TIME-STAMP:", System.nanoTime());

                switch(apiService)
                {
                    case "":
                        break;
                    case "WEATHER":
                        //region Weather Parsing
                        JSONObject currentWeatherObject = new JSONObject(jParser.getJSONFromUrl(serviceURL));
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
                        //endregion
                        break;
                    case "CINEMAS_NEARBY":
                        //region Cinema Nearby Parsing
                        JSONObject cinemaObject = new JSONObject(jParser.getJSONFromUrl(serviceURL));
                        JSONArray cinemasARRAY = cinemaObject.getJSONArray("cinemas");
                        List<String> cinemaNames = new ArrayList<String>(cinemasARRAY.length());
                        List<String> cinemaLocations = new ArrayList<String>(cinemasARRAY.length());
                        List<String> cinemaIDs = new ArrayList<String>(cinemasARRAY.length());
                        List<String> cinemaDistances = new ArrayList<String>(cinemasARRAY.length());

                        //region Cinemas Nearby Parsing
                        for (int i = 0; i < cinemasARRAY.length(); i++)
                        {
                            JSONObject ithObject = cinemasARRAY.getJSONObject(i);
                            if ((ithObject.has("name")) && (ithObject.has("id")) && (ithObject.has("distance")))
                            {
                                String temp = ithObject.getString("name");
                                String[] temp2 = temp.split(", ", 2);
                                cinemaNames.add(temp2[0]);
                                cinemaLocations.add(temp2[1]);

                                cinemaIDs.add(ithObject.getString("id"));
                                cinemaDistances.add(ithObject.getString("distance"));
                            }

                            else
                            {
                                //do nothing
                                System.out.println("Warning! A cinema entry has been skipped");
                            }
                        }
                        //endregion
                        //region Put all the cinema data into a dataMap packet
                        Integer numberOfCinemas = cinemaNames.size();
                        dataMap.putLong("#-CONTENT:", 2);
                        dataMap.putLong("00-NUMBEROFCINEMAS:", numberOfCinemas);

                        if(numberOfCinemas > 0)
                        {
                            dataMap.putString("01-cinema1Name", cinemaNames.get(0));
                            dataMap.putString("02-cinema1Location", cinemaLocations.get(0));
                            dataMap.putString("03-cinema1Distance", cinemaDistances.get(0));
                            dataMap.putString("04-cinema1ID", cinemaIDs.get(0));
                        }

                        if(numberOfCinemas > 1)
                        {
                            dataMap.putString("05-cinema2Name", cinemaNames.get(1));
                            dataMap.putString("06-cinema2Location", cinemaLocations.get(1));
                            dataMap.putString("07-cinema2Distance", cinemaDistances.get(1));
                            dataMap.putString("08-cinema2ID", cinemaIDs.get(1));
                        }

                        if(numberOfCinemas > 2)
                        {
                            dataMap.putString("09-cinema3Name", cinemaNames.get(2));
                            dataMap.putString("10-cinema3Location", cinemaLocations.get(2));
                            dataMap.putString("11-cinema3Distance", cinemaDistances.get(2));
                            dataMap.putString("12-cinema3ID", cinemaIDs.get(2));
                        }

                        if(numberOfCinemas > 3)
                        {
                            dataMap.putString("13-cinema4Name", cinemaNames.get(3));
                            dataMap.putString("14-cinema4Location", cinemaLocations.get(3));
                            dataMap.putString("15-cinema4Distance", cinemaDistances.get(3));
                            dataMap.putString("16-cinema4ID", cinemaIDs.get(3));
                        }

                        if(numberOfCinemas > 4)
                        {
                            dataMap.putString("17-cinema5Name", cinemaNames.get(4));
                            dataMap.putString("18-cinema5Location", cinemaLocations.get(4));
                            dataMap.putString("19-cinema5Distance", cinemaDistances.get(4));
                            dataMap.putString("20-cinema5ID", cinemaIDs.get(4));
                        }
                        //endregion

                        //endregion
                        break;
                    case "NEWS_GENERAL":
                        //region News General Parsing
                        JSONObject newsObject = new JSONObject(jParser.getJSONFromUrl(serviceURL));

                        JSONArray articlesArray = newsObject.getJSONArray("articles");
                        String[] articleTitles = new String[5];
                        String[] articleDescriptions = new String[5];

                        Integer numberOfArticles = 0;
                        for(int i = 0; i < articlesArray.length(); i++)
                        {
                            if(i < 5)
                            {
                                JSONObject article = articlesArray.getJSONObject(i);
                                articleTitles[i] = article.getString("title");
                                articleDescriptions[i] = article.getString("description");
                                numberOfArticles++;
                            }
                        }

                        dataMap.putLong("#-CONTENT:", 3);
                        String temp = "";

                        if(numberOfArticles > 0)
                        {
                            temp = ((articleTitles[0].replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("00-articleTitle", temp);
                            temp = ((articleDescriptions[0].replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("01-articleDescription", temp);
                        }

                        if(numberOfArticles > 1)
                        {
                            temp = ((articleTitles[1].replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("02-articleTitle", temp);
                            temp = ((articleDescriptions[1].replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("03-articleDescription", temp);
                        }

                        if(numberOfArticles > 2)
                        {
                            temp = ((articleTitles[2].replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("04-articleTitle", temp);
                            temp = ((articleDescriptions[2].replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("05-articleDescription", temp);
                        }

                        if(numberOfArticles > 3)
                        {
                            temp = ((articleTitles[3].replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("06-articleTitle", temp);
                            temp = ((articleDescriptions[3].replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("07-articleDescription", temp);
                        }

                        if(numberOfArticles > 4)
                        {
                            temp = ((articleTitles[4].replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("08-articleTitle", temp);
                            temp = ((articleDescriptions[4].replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("09-articleDescription", temp);
                        }
                        //endregion
                        break;
                    case "WIKIPEDIA":
                        //region Wikipedia Parsing
                        try
                        {
                            JSONObject wikipediaResult = new JSONObject(jParser.getJSONFromUrl(serviceURL));
                            wikipediaResult = wikipediaResult.getJSONObject("query");
                            wikipediaResult = wikipediaResult.getJSONObject("pages");

                            String pageID = wikipediaResult.toString().substring(2);
                            String[] page = pageID.split("\"");
                            pageID = page[0];

                            wikipediaResult = wikipediaResult.getJSONObject(pageID);

                            String wikipediaTitle = wikipediaResult.getString("title").replaceAll("/", "");
                            String wikipediaExtract = wikipediaResult.getString("extract").replaceAll("/", "");

                            //System.out.println(wikipediaExtract);
                            dataMap.putLong("#-CONTENT:", 4);
                            String tempWiki = "";

                            dataMap.putString("00-pageID", pageID);
                            tempWiki = ((wikipediaTitle.replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("01-title", tempWiki);
                            tempWiki = ((wikipediaExtract.replaceAll("'","[APOSTROPHE]")).replaceAll(",","[COMMA]")).replaceAll(" ", "[SPACE]");
                            dataMap.putString("02-extract", tempWiki);
                        }

                        catch(Exception e)
                        {
                            System.out.println("Error reading from Wikipedia");
                            System.out.println(e);
                        }
                        //endregion
                        break;
                    case "LASTFM":
                        //region LastFM Parsing
                        JSONObject lastFMResult = new JSONObject(jParser.getJSONFromUrl(serviceURL));

                        lastFMResult = lastFMResult.getJSONObject("results");
                        lastFMResult = lastFMResult.getJSONObject("trackmatches");

                        JSONArray tracksArray = lastFMResult.getJSONArray("track");

                        String[] tracks = new String[10];
                        String[] artists = new String[10];

                        for(int i = 0; i < 10; i++)
                        {
                            JSONObject tempTrack = tracksArray.getJSONObject(i);
                            tracks[i] = tempTrack.getString("name").replaceAll("\\\\", "");
                            artists[i] = tempTrack.getString("artist").replaceAll("\\\\", "");
                        }

                        dataMap.putLong("#-CONTENT:", 5);
                        String tempString = "";

                        for(int i = 0; i < 10; i++) {
                            switch (i) {
                                case 0:
                                    tempString = ((tracks[0].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("00-track1title", tempString);
                                    tempString = ((artists[0].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("01-track1artist", tempString);
                                    break;
                                case 1:
                                    tempString = ((tracks[1].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("02-track2title", tempString);
                                    tempString = ((artists[1].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("03-track2artist", tempString);
                                    break;
                                case 2:
                                    tempString = ((tracks[2].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("04-track3title", tempString);
                                    tempString = ((artists[2].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("05-track3artist", tempString);
                                    break;
                                case 3:
                                    tempString = ((tracks[3].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("06-track4title", tempString);
                                    tempString = ((artists[3].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("07-track4artist", tempString);
                                    break;
                                case 4:
                                    tempString = ((tracks[4].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("08-track5title", tempString);
                                    tempString = ((artists[4].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("09-track5artist", tempString);
                                    break;
                                case 5:
                                    tempString = ((tracks[5].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("10-track6title", tempString);
                                    tempString = ((artists[5].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("11-track6artist", tempString);
                                    break;
                                case 6:
                                    tempString = ((tracks[6].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("12-track7title", tempString);
                                    tempString = ((artists[6].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("13-track7artist", tempString);
                                    break;
                                case 7:
                                    tempString = ((tracks[7].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("14-track8title", tempString);
                                    tempString = ((artists[7].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("15-track8artist", tempString);
                                    break;
                                case 8:
                                    tempString = ((tracks[8].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("16-track9title", tempString);
                                    tempString = ((artists[8].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("17-track9artist", tempString);
                                    break;
                                case 9:
                                    tempString = ((tracks[9].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("18-track10title", tempString);
                                    tempString = ((artists[9].replaceAll("'", "[APOSTROPHE]")).replaceAll(",", "[COMMA]")).replaceAll(" ", "[SPACE]");
                                    dataMap.putString("19-track10artist", tempString);
                                    break;
                            }
                        }

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
                System.out.println(e);
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
