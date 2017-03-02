package uk.ac.lincoln.jackduffy.alfred;

import android.content.Intent;
import android.widget.Toast;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService
{
    public static String apiRequest = "";

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        parseInput(messageEvent.getPath());
    }

    private void parseInput(String input)
    {
        System.out.println("Message from Wearable :- " + input);
        Toast.makeText(this, input, Toast.LENGTH_LONG).show();

        switch((input.substring(0, Math.min(input.length(), 3))))
        {
            case "SF-":
                System.out.println("SPECIAL FUNCTION DETECTED");

                if(input.contains("GOOGLE"))
                {
                    apiRequest = input.substring(7);
                    startActivity(new Intent(ListenerService.this, googleCalendar.class));
                }

                else
                {
                    apiRequest = input.substring(3);
                    startActivity(new Intent(ListenerService.this, apiService.class));
                }

                break;
        }
    }
}