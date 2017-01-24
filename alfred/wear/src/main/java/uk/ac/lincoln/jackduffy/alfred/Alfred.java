package uk.ac.lincoln.jackduffy.alfred;

import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Alfred extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    private static final int SPEECH_RECOGNIZER_REQUEST_CODE = 0;
    Integer alfredFormality = 1; //DEFAULT FOR NOW, 1,2,3
    Boolean listeningForInput = false;
    Boolean searchingForInputMatch = false;
    String userInput;
    Boolean userInputUnderstood = false;
    Boolean wasLastMessageUnderstood = true;
    Integer userInputFunctionNumber = 0;
    String alfredResponse;
    Boolean alfredResponseReady = false;

    Integer userMessageNumber = 0;
    Integer greetingNumber = 0; //log the number of times the user has greeted alfred

    XmlResourceParser xpp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //create the instance of Alfred
        setContentView(R.layout.activity_alfred); //set the layout
        setAmbientEnabled(); //enable the ambient mode

        ScrollView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                return false;
            }});
    }

    public void voiceDictation(View view)
    {

        if(listeningForInput == false)
        {
            listeningForInput = true;
            alfredFaceAnimation(1, 1);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        startActivityForResult(intent, SPEECH_RECOGNIZER_REQUEST_CODE);
                    }

                    catch(Exception e)
                    {

                    }
                }
            }, 600);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        alfredFaceAnimation(1, 2);

        try
        {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); //get each word detected from the speech recognition
            String voiceInput = results.get(0); //concatenate these into a single string
            userInput = voiceInput.replaceAll(" ", "_").toUpperCase(); //transform that string into upper case, replaces spaces with underscores and assign to a global value string
            userInput = userInput + "_";
        }

        catch(Exception e)
        {
            userInput = "";
        }

        if(userInput != "")
        {
            System.out.println("User input is applicable");
            Toast.makeText(this, userInput, Toast.LENGTH_LONG).show(); //toast to demonstrate the string has been correctly translated
            AnalyseInput();
        }

        listeningForInput = false;
    }

    public void alfredFaceAnimation(int toggle, int mode)
    {
        switch(toggle)
        {
            case 1:
                ImageView mustache = (ImageView) findViewById(R.id.alfred_mustache);
                ImageView specs = (ImageView) findViewById(R.id.alfred_specs);

                TranslateAnimation mustache_animation;
                TranslateAnimation specs_animation;
                switch(mode)
                {
                    case 1: //the reveal
                        specs.setVisibility(View.VISIBLE);

                        mustache_animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 75.0f);
                        mustache_animation.setDuration(500);
                        mustache_animation.setRepeatCount(0);
                        mustache_animation.setRepeatMode(0);
                        mustache_animation.setFillAfter(true);
                        mustache.startAnimation(mustache_animation);

                        specs_animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 150.0f);
                        specs_animation.setDuration(500);
                        specs_animation.setRepeatCount(0);
                        specs_animation.setRepeatMode(0);
                        specs_animation.setFillAfter(true);

                        specs.startAnimation(specs_animation);
                        break;
                    case 2: //hiding
                        specs.setVisibility(View.INVISIBLE);

                        mustache_animation = new TranslateAnimation(0.0f, 0.0f, 75.0f, 0.0f);
                        mustache_animation.setDuration(500);
                        mustache_animation.setRepeatCount(0);
                        mustache_animation.setRepeatMode(0);
                        mustache_animation.setFillAfter(true);
                        mustache.startAnimation(mustache_animation);

                        specs_animation = new TranslateAnimation(0.0f, 0.0f, 150.0f, 0.0f);
                        specs_animation.setDuration(500);
                        specs_animation.setRepeatCount(0);
                        specs_animation.setRepeatMode(0);
                        specs_animation.setFillAfter(true);

                        specs.startAnimation(specs_animation);
                        break;
                }
                break;
        }
    }

    public void AnalyseInput()
    {
        Integer numberOfModules = 4;
        String moduleName = "";

        Boolean module1_ENGAGED = false;
        Boolean module2_ENGAGED = false;
        Boolean module3_ENGAGED = false;
        Boolean module4_ENGAGED = false;
        Boolean module5_ENGAGED = false;
        Boolean module6_ENGAGED = false;
        Boolean module7_ENGAGED = false;
        Boolean module8_ENGAGED = false;
        Boolean module9_ENGAGED = false;
        Boolean module10_ENGAGED = false;


        for(int module = 1; module <= numberOfModules; module++)
        {
            moduleName = null;
            userInputUnderstood = false;
            switch(module)
            {
                case 1:
                    moduleName = "FAREWELLS";
                    break;
                case 2:
                    if(module1_ENGAGED != true){moduleName = "GREETINGS";}
                    break;
                case 3:
                    if(module3_ENGAGED != true){moduleName = "SMALLTALK_1";}
                    break;
                case 4:
                    if(module1_ENGAGED != true && module2_ENGAGED != true && module3_ENGAGED != true && module4_ENGAGED != true && module5_ENGAGED != true && module6_ENGAGED != true &&  module7_ENGAGED != true && module7_ENGAGED != true && module8_ENGAGED != true && module9_ENGAGED != true && module10_ENGAGED != true)
                    {
                        System.out.println("Input not understood");
                        inputNotUnderstood();
                    }
                    break;
            }

            readXML(moduleName);
            if(userInputUnderstood == true)
            {
                wasLastMessageUnderstood = true;
                switch(module)
                {
                    case 1:
                        module1_ENGAGED = true;
                        break;
                    case 2:
                        module2_ENGAGED = true;
                        break;
                    case 3:
                        module3_ENGAGED = true;
                        break;
                    case 4:
                        module4_ENGAGED = true;
                        break;
                    case 5:
                        module5_ENGAGED = true;
                        break;
                    case 6:
                        module6_ENGAGED = true;
                        break;
                    case 7:
                        module7_ENGAGED = true;
                        break;
                    case 8:
                        module8_ENGAGED = true;
                        break;
                    case 9:
                        module9_ENGAGED = true;
                        break;
                    case 10:
                        module10_ENGAGED = true;
                        break;
                }

                formulateResponse(moduleName);

                if(moduleName == "FAREWELLS")
                {
                    module = 10;
                    break;
                }

            }

            else
            {

            }
        }

        displayResponse();
    }

    public void readXML(String targetTag)
    {
        //searchingForInputMatch = true;
        XmlResourceParser xpp = getResources().getXml(R.xml.inputs_en);

        int eventType = 0;

        try
        {
            eventType = xpp.getEventType();
        }

        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }

        String comparison;
        Boolean tagReached = false;

        //region Check for tag
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.END_TAG)
            {
                comparison = xpp.getName();
                if (Objects.equals(targetTag, comparison))
                {
                    System.out.println("Finished searching tag - " + comparison);
                    tagReached = false;
                    break;
                }
            }

            if (eventType == XmlPullParser.START_TAG)
            {
                comparison = xpp.getName();
                if (Objects.equals(targetTag, comparison))
                {
                    System.out.println("Checking " + targetTag + " for a match.");
                    tagReached = true;
                }

                else if(tagReached == true)
                {
                    //System.out.println("Does " + userInput + " match :- " + comparison);
                    if (userInput.contains(comparison))
                    {
                        System.out.println("Match! " + userInput + " contains the greeting " + comparison);
                        //userMessageNumber++;
                        userInputUnderstood = true;
                        //userInputFunctionNumber++;
                        //greetingNumber++;
                        break;
                    }
                }
            }

            try
            {
                eventType = xpp.next();
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        //System.out.println("Exiting search loop");
        xpp.close();
        //endregion

        //searchingForInputMatch = false;
    }

    public void formulateResponse(String typeOfResponse)
    {
        Random rand = new Random();
        int max = 6; //max 5
        int min = 2; //min 1
        int randomStatement;
        //String moduleName = "";
        String moduleName = typeOfResponse;
        userMessageNumber = 1; //change to increment when ready

//        switch(typeOfResponse)
//        {
//            case "GREETING":
//                if(userInputUnderstood == true)
//                {
//                    moduleName = "GREETINGS";
//                }
//                break;
//            case "FAREWELL":
//                if(userInputUnderstood == true)
//                {
//                    moduleName = "FAREWELLS";
//                }
//                break;
//            case "SMALLTALK_1":
//                if(userInputUnderstood == true)
//                {
//                    moduleName = "SMALLTALK_1";
//                }
//                break;
//            case "NOT_UNDERSTOOD":
//                userInputUnderstood = false;
//                userInput = "";
//                inputNotUnderstood();
//                break;
//        }

        if(userInputUnderstood == true)
        {
            randomStatement = rand.nextInt((max - min) + 1) + min;
            xpp = getResources().getXml(R.xml.alfred_responses_en);
            readResponseXML(moduleName, randomStatement);
        }

        else if (userInputUnderstood == false)
        {
            userInputUnderstood = false;
            userInput = "";
            inputNotUnderstood();
        }

        while(alfredResponseReady == false)
        {
            //do nothing
        }
    }

    public void readResponseXML(String responseCriteria, int randomStatement)
    {
        Boolean tagReached = false;
        Boolean responseFound = false;
        Boolean continueSearching = true;

        String comparison;
        Boolean responseCriteriaReached = false;
        int formalityList = 1; //do not touch!
        int eventType = 0;

        try
        {
            eventType = xpp.getEventType();
        }

        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }

        //region Fetch alfred's response
        while (continueSearching == true)
        {
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if(continueSearching == false)
                {
                    break;
                }

                comparison = xpp.getText();
                if(responseFound == true && ("null" != comparison.intern()))
                {
                    comparison = xpp.getText();
                    if(alfredResponse == null)
                    {
                        alfredResponse = comparison;
                    }

                    else
                    {
                        alfredResponse = alfredResponse + " " + comparison;
                    }
                    tagReached = false;
                    continueSearching = false;
                    break;
                }

                else
                {
                    //System.out.println("Searching...");
                    if (eventType == XmlPullParser.END_TAG)
                    {
                        comparison = xpp.getName();
                        if (Objects.equals(responseCriteria, comparison))
                        {
                            System.out.println("Criteria ended");
                            responseCriteriaReached = false;
                            continueSearching = false;
                            break;
                        }
                    }

                    if (eventType == XmlPullParser.START_TAG)
                    {
                        comparison = xpp.getName();

                        if (Objects.equals(responseCriteria, comparison))
                        {
                            responseCriteriaReached = true;
                        }

//                        if(responseCriteriaReached == true)
//                        {
//                            switch (alfredFormality)
//                            {
//                                case 1:
//                                    if (Objects.equals("FORMALITY_1", comparison)) {
//                                        System.out.println("Formality Level 1:");
//                                        tagReached = true;
//                                    }
//                                    break;
//                                case 2:
//                                    if (Objects.equals("FORMALITY_2", comparison)) {
//                                        System.out.println("Formality Level 2:");
//                                        tagReached = true;
//                                    }
//                                    break;
//                                case 3:
//                                    if (Objects.equals("FORMALITY_3", comparison)) {
//                                        System.out.println("Formality Level 3:");
//                                        tagReached = true;
//                                    }
//                                    break;
//                                default:
//                                    break;
//                            }
//                        }

                        if (responseCriteriaReached == true)
                        {
                            if (Integer.toString(formalityList).equals(Integer.toString(randomStatement)))
                            {
                                responseFound = true;
                            }

                            formalityList++;
                        }
                    }
                }

                try {
                    eventType = xpp.next();
                }

                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (eventType == XmlPullParser.END_DOCUMENT)
                {
                    System.out.println("No response found");
                    continueSearching = false;
                    break;
                }
            }

            xpp.close();
        }
        //endregion

        alfredResponseReady = true;
    }

    public void inputNotUnderstood()
    {
        Random rand = new Random();
        int max = 10;
        int min = 1;
        int randomStatement = rand.nextInt((max - min) + 1) + min;

        if(wasLastMessageUnderstood == true) //if the last message WAS understood
        {
            switch (randomStatement)
            {
                case 1:
                    alfredResponse = "I'm sorry, I'm afraid I didn't understand that.";
                    break;
                case 2:
                    alfredResponse = "I'm terribly sorry but I'm afraid I didn't understand... well, any of what you said.";
                    break;
                case 3:
                    alfredResponse = "I'm sorry, could you repeat the question? My hearing isn't as good as it used to be.";
                    break;
                case 4:
                    alfredResponse = "I didn't quite catch that I'm afraid. Could you repeat the question?";
                    break;
                case 5:
                    alfredResponse = "Could you try repeating the question Sir, I didn't quite catch that.";
                    break;
                case 6:
                    alfredResponse = "I'm not sure that I understand Sir. Could you reiterate?";
                    break;
                case 7:
                    alfredResponse = "Sorry, can you say that again? I didn't really catch that.";
                    break;
                case 8:
                    alfredResponse = "Would you mind repeating the question? I didn't quite understand what you meant.";
                    break;
                case 9:
                    alfredResponse = "I'm afraid I don't understand what you're asking of me Sir.";
                    break;
                case 10:
                    alfredResponse = "Come again?";
                    break;
            }
            wasLastMessageUnderstood = false;
        }

        else if (wasLastMessageUnderstood == false)
        {
            switch (randomStatement)
            {
                case 1:
                    alfredResponse = "I'm still not really understanding you Sir. My apologies, but could you ask that again?";
                    break;
                case 2:
                    alfredResponse = "I'm still not really understanding you Sir. My apologies, but could you ask that again?";
                    break;
                case 3:
                    alfredResponse = "I'm still not really understanding you Sir. My apologies, but could you ask that again?";
                    break;
                case 4:
                    alfredResponse = "I'm still not really understanding you Sir. My apologies, but could you ask that again?";
                    break;
                case 5:
                    alfredResponse = "I'm still not really understanding you Sir. My apologies, but could you ask that again?";
                    break;
                case 6:
                    alfredResponse = "I'm still not really understanding you Sir. My apologies, but could you ask that again?";
                    break;
                case 7:
                    alfredResponse = "I'm still not really understanding you Sir. My apologies, but could you ask that again?";
                    break;
                case 8:
                    alfredResponse = "I'm still not really understanding you Sir. My apologies, but could you ask that again?";
                    break;
                case 9:
                    alfredResponse = "I'm still not really understanding you Sir. My apologies, but could you ask that again?";
                    break;
                case 10:
                    alfredResponse = "I'm still not really understanding you Sir. My apologies, but could you ask that again?";
                    break;
            }
        }

        alfredResponseReady = true;
    }

    public void displayResponse()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                final TextView responseText = (TextView) findViewById(R.id.response_text);
                responseText.setText(alfredResponse);

                final ScrollView myScroller = (ScrollView) findViewById(R.id.scroll_view);
                myScroller.smoothScrollTo( 0, myScroller.getChildAt( 0 ).getBottom() );

                userInput = "";
                alfredResponse = "";
                alfredResponseReady = false;
                userInputUnderstood = false;
                listeningForInput = false;
            }
        }, 1000);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
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
