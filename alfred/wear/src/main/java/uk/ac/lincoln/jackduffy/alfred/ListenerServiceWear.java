package uk.ac.lincoln.jackduffy.alfred;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerServiceWear extends WearableListenerService
{
    @Override
    public void onDataChanged(DataEventBuffer dataEvents)
    {
        DataMap dataMap;
        for (DataEvent event : dataEvents)
        {
            Log.v("myTag", "DataMap received on watch: " + DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
        }
    }
}