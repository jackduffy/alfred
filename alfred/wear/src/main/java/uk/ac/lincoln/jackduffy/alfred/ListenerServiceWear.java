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
        String[] incomingData = new String[100];
        Integer elementNumber = 0;
        for (DataEvent event : dataEvents)
        {
            DataMapItem dataItem = DataMapItem.fromDataItem (event.getDataItem());
            incomingData = dataItem.getDataMap().getStringArray("contents"); //?!?!?!?!?!?!?!?!
            Log.v("myTag", "DataMap item: " + DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
        }
    }
}