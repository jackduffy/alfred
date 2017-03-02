package uk.ac.lincoln.jackduffy.alfred;

import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiAsyncTask extends AsyncTask<Void, Void, Void>
{
    private googleCalendar mActivity;
    ApiAsyncTask(googleCalendar activity)
    {
        this.mActivity = activity;
    }

    @Override
    protected Void doInBackground(Void... params) //do this in the background
    {
        try
        {
            mActivity.updateResultsText(getDataFromApi());
        }

        catch (final GooglePlayServicesAvailabilityIOException availabilityException)
        {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(availabilityException.getConnectionStatusCode());
        }

        catch (UserRecoverableAuthIOException userRecoverableException)
        {
            mActivity.startActivityForResult(userRecoverableException.getIntent(),googleCalendar.REQUEST_AUTHORIZATION);
        }

        catch (IOException e)
        {
            mActivity.updateStatus("The following error occurred: " + e.getMessage());
        }
        return null;
    }

    private List<String> getDataFromApi() throws IOException  //fetch the next 10 events in the calendar
    {
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        List<String> eventStrings = new ArrayList<String>();
        Events events = mActivity.mService.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                // All-day events don't have start times, so just use
                // the start date.
                start = event.getStart().getDate();
            }
            eventStrings.add(
                    String.format("%s (%s)", event.getSummary(), start));
        }
        return eventStrings;
    }

}