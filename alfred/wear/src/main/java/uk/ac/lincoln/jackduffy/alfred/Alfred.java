package uk.ac.lincoln.jackduffy.alfred;

import android.content.res.XmlResourceParser;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.util.Xml;
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
    Boolean alfredResponseReady = false;

    Integer userMessageNumber = 0;
    Integer greetingNumber = 0; //log the number of times the user has greeted alfred

    String alfredResponse;
    String contextualResponse1;
    String contextualResponse1Function;
    String contextualResponse2;
    String contextualResponse2Function;

    XmlResourceParser xpp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //create the instance of Alfred
        setContentView(R.layout.activity_alfred); //set the layout
        setAmbientEnabled(); //enable the ambient mode
    }

    public void voiceDictation(View view)
    {
        alfredResponse = null;
        contextualResponse1 = null;
        contextualResponse1Function = null;
        contextualResponse2 = null;
        contextualResponse2Function = null;

        ImageView hideContextualResponseIcons = (ImageView) findViewById(R.id.contextualIcon1);
        hideContextualResponseIcons.setVisibility(View.INVISIBLE);
        hideContextualResponseIcons = (ImageView) findViewById(R.id.contextualIcon2);
        hideContextualResponseIcons.setVisibility(View.INVISIBLE);

        TextView hideContextualResponses = (TextView) findViewById(R.id.contextualResponse1);
        hideContextualResponses.setVisibility(View.INVISIBLE);
        hideContextualResponses = (TextView) findViewById(R.id.contextualResponse2);
        hideContextualResponses.setVisibility(View.INVISIBLE);

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
        Integer numberOfModules = 7;
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
                    if(module1_ENGAGED != true){moduleName = "SMALLTALK_1";}
                    break;
                case 4:
                    if(module1_ENGAGED != true){moduleName = "SMALLTALK_2";}
                    break;
                case 5:
                    if(module1_ENGAGED != true){moduleName = "SMALLTALK_3";}
                    break;
                case 6:
                    if (module1_ENGAGED != true) {moduleName = "WEATHER";}
                    break;
                case 10:
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

    public void formulateResponse(String typeOfResponse)
    {
        Random rand = new Random();
        int max = 6; //max 5
        int min = 2; //min 1
        int randomStatement;
        //String moduleName = "";
        String moduleName = typeOfResponse;
        userMessageNumber = 1; //change to increment when ready

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

    public void readXML(String targetTag)
    {
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
        Boolean continueRunning = true;
        Boolean tagReached = false;

        //region Check for tag
        while(continueRunning == true)
        {
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

                        if (userInput.contains(comparison)) {
                            System.out.println("Match! " + userInput + " contains " + comparison);
                            userInputUnderstood = true;
                            continueRunning = false;
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

            if(eventType == XmlPullParser.END_DOCUMENT)
            {
                continueRunning = false;
            }

            xpp.close();
        }

        //endregion

    }

    public void readResponseXML(String responseCriteria, int randomStatement)
    {
        xpp = getResources().getXml(R.xml.alfred_responses_en);

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







        while (continueSearching == true)
        {
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if (continueSearching == false)
                {
                    break;
                }

                comparison = xpp.getText();
                if (responseFound == true && ("null" != comparison.intern()))
                {
                    comparison = xpp.getText();
                    System.out.println("Response found! - " + comparison);
                    if (alfredResponse == null)
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
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (eventType == XmlPullParser.END_DOCUMENT) {
                    System.out.println("No response found");
                    continueSearching = false;
                    break;
                }
            }
            xpp.close();
        }

        alfredResponseReady = true;
        readContextualOptions(responseCriteria);
    }

    public void readContextualOptions(String responseCriteria)
    {
        System.out.println("Checking for contextual options");
        System.out.println("The response criteria is: " + responseCriteria);
        xpp = getResources().getXml(R.xml.alfred_responses_en);
        Boolean valuePositionsCalculated = false;
        Boolean continueSearching = true;

        String comparison;
        int eventType = 0;
        int targetPositionCounter = 0;
        int currentPositionCounter = 0;

        try
        {
            eventType = xpp.getEventType();
        }

        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }

        while (continueSearching == true)
        {
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                //region Emergency break
                if (continueSearching == false)
                {
                    break;
                }
                //endregion

                comparison = xpp.getName();
                currentPositionCounter++;

                if(valuePositionsCalculated != true)
                {
                    if (responseCriteria.equals(comparison) && eventType == XmlPullParser.END_TAG)
                    {
                        System.out.println(comparison + " end tag located");
                        xpp = getResources().getXml(R.xml.alfred_responses_en);
                        targetPositionCounter = currentPositionCounter;
                        currentPositionCounter = 0;
                        valuePositionsCalculated = true;
                    }
                }

                else
                {
                    if(currentPositionCounter == (targetPositionCounter - 3))
                    {
                        comparison = xpp.getText();
                        if(comparison != "null")
                        {
                            System.out.println("C2 Function = " + comparison);
                            contextualResponse2Function = comparison;
                            continueSearching = false;
                        }
                    }

                    if(currentPositionCounter == (targetPositionCounter - 6))
                    {
                        comparison = xpp.getText();
                        if(comparison != "null")
                        {
                            System.out.println("C2 = " + comparison);
                            contextualResponse2 = comparison;
                        }
                    }

                    if(currentPositionCounter == (targetPositionCounter - 9))
                    {
                        comparison = xpp.getText();
                        if(comparison != "null")
                        {
                            System.out.println("C1 Function = " + comparison);
                            contextualResponse1Function = comparison;
                        }
                    }

                    if(currentPositionCounter == (targetPositionCounter - 12))
                    {
                        comparison = xpp.getText();
                        if(comparison != "null")
                        {
                            System.out.println("C1 = " + comparison);
                            contextualResponse1 = comparison;
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

                if (eventType == XmlPullParser.END_DOCUMENT)
                {
                    System.out.println("No response found");
                    continueSearching = false;
                    break;
                }
            }
            xpp.close();
        }
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

        contextualResponse1 = "Ok, I'll ask again...";
        contextualResponse1Function = "VOICE_DICTATION";
        alfredResponseReady = true;
    }

    public void displayResponse()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                if(alfredResponse != null || alfredResponse != "")
                {
                    final TextView responseText = (TextView) findViewById(R.id.response_text);
                    responseText.setText(alfredResponse);
                }

                if(contextualResponse1 == null && contextualResponse2 == null)
                {
                    System.out.println("Module not using contextual responses");
                }

                else
                {
                    if(contextualResponse1 != null)
                    {
                        final TextView applyContextualResponse = (TextView) findViewById(R.id.contextualResponse1);
                        applyContextualResponse.setText(contextualResponse1);
                        applyContextualResponse.setVisibility(View.VISIBLE);

                        final ImageView displayContextualResponseIcon = (ImageView) findViewById(R.id.contextualIcon1);
                        displayContextualResponseIcon.setVisibility(View.VISIBLE);

                    }

                    if(contextualResponse2 != null)
                    {
                        final TextView applyContextualResponse = (TextView) findViewById(R.id.contextualResponse2);
                        applyContextualResponse.setText(contextualResponse2);
                        applyContextualResponse.setVisibility(View.VISIBLE);

                        final ImageView displayContextualResponseIcon = (ImageView) findViewById(R.id.contextualIcon2);
                        displayContextualResponseIcon.setVisibility(View.VISIBLE);
                    }
                }

                if(alfredResponse != null || alfredResponse != "")
                {
                    final ScrollView myScroller = (ScrollView) findViewById(R.id.scroll_view);
                    myScroller.smoothScrollTo( 0, myScroller.getChildAt( 0 ).getBottom() );
                }

                userInput = null;
                alfredResponse = null;
                contextualResponse1 = null;
                contextualResponse2 = null;

                alfredResponseReady = false;
                userInputUnderstood = false;
                listeningForInput = false;
            }
        }, 1000);
    }

    public void contextualResponse1(View view)
    {
        performContextualAction(contextualResponse1Function);
    }

    public void contextualResponse2(View view)
    {
        performContextualAction(contextualResponse2Function);
    }

    public void performContextualAction(String function)
    {
        Random rand = new Random();
        int max = 6; //max 5
        int min = 2; //min 1
        int randomStatement = rand.nextInt((6 - 2) + 1) + min;

        if(function != null)
        {
            switch(function)
            {
                case "VOICE_DICTATION":
                    ScrollView myScroller = (ScrollView) findViewById(R.id.scroll_view);
                    myScroller.smoothScrollTo( 0, myScroller.getChildAt( 0 ).getTop() );

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                ImageView voiceDictationClick = (ImageView) findViewById(R.id.alfred_mustache);
                                voiceDictationClick.performClick();
                            }

                            catch(Exception e)
                            {

                            }
                        }
                    }, 300);
                    break;
                case "WEATHER":
                    break;
                case "EVENTS":
                    break;
                case "SMALLTALK_1":
                    break;
                case "SMALLTALK_2":
                    break;
                case "SMALLTALK_3":
                    readResponseXML("SMALLTALK_3", randomStatement);
                    contextualRefresh();
                    break;
                case "ALFRED_HELP":
                    break;
            }
        }
    }

    public void contextualRefresh()
    {
        ScrollView myScroller = (ScrollView) findViewById(R.id.scroll_view);
        myScroller.smoothScrollTo( 0, myScroller.getChildAt( 0 ).getTop() );
        displayResponse();
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
