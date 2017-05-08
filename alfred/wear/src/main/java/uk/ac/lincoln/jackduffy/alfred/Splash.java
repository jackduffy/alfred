package uk.ac.lincoln.jackduffy.alfred;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

/**
 * Created by jack on 10/04/2017.
 */

public class Splash extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) //display the splash screen
    {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, Alfred.class);
        startActivity(intent);
        finish();
    }
}
