package uk.ac.lincoln.jackduffy.alfred;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.List;
import java.text.NumberFormat;

public class Alfred extends WearableActivity
{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);

    private static final int SPEECH_RECOGNIZER_REQUEST_CODE = 0;

    String userInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); //create the instance of Alfred
        setContentView(R.layout.activity_alfred); //set the layout
        setAmbientEnabled(); //enable the ambient mode
    }

    public void voiceDictation (View view)
    {
        for(int i = 0; i < 50; i++)
        {
            SystemClock.sleep(10);
            ImageView imageView = (ImageView) findViewById(R.id.alfred_mustache);
            float y = imageView.getY();
            y++;
            imageView.setY(y);
        }


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_RECOGNIZER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); //get each word detected from the speech recognition
        String voiceInput = results.get(0); //concatenate these into a single string
        userInput = voiceInput.toUpperCase(); //transform that string into uppercase and assign to a global value string
        Toast.makeText(this, userInput, Toast.LENGTH_LONG).show(); //toast to demonstrate the string has been correctly translated
    }

    public class inputAnalysis extends AsyncTask<String, String, String>
    {
        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected String doInBackground(String... arg0)
        {
            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg)
        {

        }
    }











    @Override
    public void onEnterAmbient(Bundle ambientDetails)
    {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient()
    {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient()
    {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay()
    {
        if (isAmbient())
        {
//            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
//            mTextView.setTextColor(getResources().getColor(android.R.color.white));
//            mClockView.setVisibility(View.VISIBLE);
//
//            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
//        } else {
//            mContainerView.setBackground(null);
//            mTextView.setTextColor(getResources().getColor(android.R.color.black));
//            mClockView.setVisibility(View.GONE);
        }
    }


}
