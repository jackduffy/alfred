package uk.ac.lincoln.jackduffy.alfred;

import android.content.res.XmlResourceParser;
import android.graphics.drawable.LayerDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Alfred extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    private static final int SPEECH_RECOGNIZER_REQUEST_CODE = 0;
    Boolean listeningForInput = false;
    String userInput;
    Boolean userInputUnderstood = false;
    Boolean wasLastMessageUnderstood = true;
    Boolean alfredResponseReady = false;
    Integer userMessageNumber = 0;
    String alfredResponse;
    String contextualResponse1;
    String contextualResponse1Function;
    String contextualResponse2;
    String contextualResponse2Function;
    Boolean criticalErrorDetected = false;
    XmlResourceParser xpp;
    String[] modules = new String[100];


    Boolean testingMode = false;
    //Boolean testingMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); //create the instance of Alfred
        setContentView(R.layout.activity_alfred); //set the layout
        setAmbientEnabled(); //enable the ambient mode
        readModules(); //read the available modules

        ImageView background_image = (ImageView)findViewById(R.id.background);
        Glide.with(this).load(R.drawable.background_a).asGif().into(background_image);
    }

    public void readModules()
    {
        xpp = getResources().getXml(R.xml.alfred_responses_en);
        Boolean continueSearching = true;
        String comparison;
        int eventType = 0;
        int moduleNumber = 0;

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
                if(eventType == XmlPullParser.START_TAG)
                {
                    if (comparison.contains("AL-"))
                    {
                        modules[moduleNumber] = comparison;
                        moduleNumber++;
                    }
                }

                try{eventType = xpp.next();}
                catch(Exception e){e.printStackTrace();}

                if (eventType == XmlPullParser.END_DOCUMENT)
                {
                    //System.out.println("Finished searching");
                    continueSearching = false;
                    break;
                }
            }
            xpp.close();
        }
    }

    public void resetResponseInterface()
    {
        //region Reinitialize Values
        alfredResponse = null;
        contextualResponse1 = null;
        contextualResponse1Function = null;
        contextualResponse2 = null;
        contextualResponse2Function = null;
        criticalErrorDetected = false;
        //endregion

        //region Hide contextual buttons
        ImageView contextualResponseIcons = (ImageView) findViewById(R.id.contextualIcon1);
        contextualResponseIcons.setVisibility(View.INVISIBLE);
        contextualResponseIcons = (ImageView) findViewById(R.id.contextualIcon2);
        contextualResponseIcons.setVisibility(View.INVISIBLE);

        TextView contextualResponses = (TextView) findViewById(R.id.contextualResponse1);
        contextualResponses.setVisibility(View.INVISIBLE);
        contextualResponses.setText(null);
        contextualResponses = (TextView) findViewById(R.id.contextualResponse2);
        contextualResponses.setVisibility(View.INVISIBLE);
        contextualResponses.setText(null);
        //endregion
    }

    public void moduleInstructions()
    {
        System.out.println("Error: Module not detected, perhaps it wasn't added correctly?");
        System.out.println("STAGES OF ADDING A MODULE :-");
        System.out.println("1) Add an entry in the 'inputs_en.xml' file, these are the trigger words");
        System.out.println("2) Add an entry in the 'alfred_responses_en.xml' file, these are the responses you want Alfred to say and the context buttons");
        System.out.println("3) Make sure your module name in both files starts with 'AL-'");
        System.out.println("4) You're done. Alfred will take care of the rest. Easy right?");
    }

    public void voiceDictation(View view)
    {
        resetResponseInterface();
        if(listeningForInput == false)
        {
            listeningForInput = true;
            alfredFaceAnimation(1, 1);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                public void run()
                {
                    //region Call the voice dictation tool
                    //region Standard Operation
                    if(testingMode == false)
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
                    //endregion

                    //region Debugging Enabled
                    else
                    {
                        userInput = "WHO_ARE_YOU_";
                        try
                        {
                            alfredFaceAnimation(1, 2);
                            AnalyseInput();
                        }

                        catch(Exception e)
                        {

                        }
                        //endregion
                    }
                    //endregion
                    //endregion
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
            //region Bind the returned value from the dictation tool to a string (and perform some alterations for readability)
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); //get each word detected from the speech recognition
            String voiceInput = results.get(0); //concatenate these into a single string
            userInput = voiceInput.replaceAll(" ", "_").toUpperCase(); //transform that string into upper case, replaces spaces with underscores and assign to a global value string
            userInput = userInput + "_";
            //endregion
        }

        catch(Exception e)
        {
            //region If there's an error detected, wipe the user input
            userInput = null;
            //endregion
        }

        if(userInput != "")
        {
            //region If the user input is ok, proceed to analysis
            AnalyseInput();
            //endregion
        }

        listeningForInput = false;
    }

    public void alfredFaceAnimation(int toggle, int mode)
    {
        switch(toggle)
        {
            case 1: //Animation 1 - Listening for input
                //region Animation code
                ImageView mustache = (ImageView) findViewById(R.id.alfred_mustache);
                ImageView specs = (ImageView) findViewById(R.id.alfred_specs);

                TranslateAnimation mustache_animation;
                TranslateAnimation specs_animation;
                switch(mode)
                {
                    case 1: //reveal alfred
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
                    case 2: //hide him
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
                //endregion
                break;
        }
    }

    public void AnalyseInput()
    {
        int numberOfModules = 0;
        //System.out.println("AVAILABLE MODULES :-");
        for (int i = 0; i < modules.length; i ++)
        {
            if (modules[i] != null)
            {
                //System.out.println("Module " + numberOfModules + ": " + modules[i]);
                numberOfModules++;
            }
        }

        //region Initialize values and disengage all active modules
        String moduleName = null;
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
        //endregion

        try
        {
            for(int module = 1; module <= (numberOfModules + 1); module++)
            {
                if(module <= numberOfModules)
                {
                    moduleName = null;
                    userInputUnderstood = false;
                    switch(module)
                    {
                        case 1:
                            moduleName = modules[0];
                            break;
                        case 2:
                            if(module1_ENGAGED != true){moduleName = modules[1];}
                            break;
                        case 3:
                            if(module1_ENGAGED != true){moduleName = modules[2];}
                            break;
                        case 4:
                            if(module1_ENGAGED != true){moduleName = modules[3];}
                            break;
                        case 5:
                            if(module1_ENGAGED != true){moduleName = modules[4];}
                            break;
                        case 6:
                            if (module1_ENGAGED != true) {moduleName = modules[5];}
                            break;
                        case 7:
                            if (module1_ENGAGED != true) {moduleName = modules[6];}
                            break;
                        case 8:
                            if (module1_ENGAGED != true) {moduleName = modules[7];}
                            break;
                        case 9:
                            if (module1_ENGAGED != true) {moduleName = modules[8];}
                            break;
                        case 10:
                            if (module1_ENGAGED != true) {moduleName = modules[9];}
                            break;
                    }
                }

                else if (module == (numberOfModules + 1))
                {
                    if(module1_ENGAGED != true && module2_ENGAGED != true && module3_ENGAGED != true && module4_ENGAGED != true && module5_ENGAGED != true && module6_ENGAGED != true &&  module7_ENGAGED != true && module7_ENGAGED != true && module8_ENGAGED != true && module9_ENGAGED != true && module10_ENGAGED != true)
                    {
                        System.out.println("* Input not understood *");
                        moduleName = null;
                        inputNotUnderstood();
                    }

                    else
                    {
                        moduleName = null;
                    }
                }

                if(moduleName != null)
                {
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
            }
            displayResponse();
        }

        catch (Exception e)
        {
            //System.out.println("CRITICAL ERROR DETECTED: User input is invalid, if this occurs after manually closing the voice dictation tool, ignore this warning!");
        }

    }

    public void formulateResponse(String typeOfResponse)
    {
        String moduleName = typeOfResponse;
        userMessageNumber = 1; //change to increment when ready

        if(userInputUnderstood == true)
        {
            xpp = getResources().getXml(R.xml.alfred_responses_en);
            readResponseXML(moduleName);
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

        String comparison = null;
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
                        //System.out.println("BREAKING");
                        //tagReached = false;
                        break;
                    }
                }

                if (eventType == XmlPullParser.START_TAG)
                {
                    comparison = xpp.getName();
                    if (Objects.equals(targetTag, comparison))
                    {
                        //System.out.println("Checking module: " + targetTag);
                        tagReached = true;
                    }

                    else if(tagReached == true)
                    {

                        if (userInput.contains(comparison)) {
                            //System.out.println("* Match in Module: " + targetTag + "*");
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
                if(tagReached == false)
                {
                    moduleInstructions();
                }
                continueRunning = false;
            }

            xpp.close();
        }

        //endregion

    }

    public void readResponseXML(String responseCriteria)
    {
        try
        {
            //region Initialize values and XML file
            xpp = getResources().getXml(R.xml.alfred_responses_en);

            Boolean tagReached = false;
            Boolean responseFound = false;
            Boolean continueSearching = true;

            String comparison;
            Boolean responseCriteriaReached = false;

            Boolean calculateDialogueOptions = false;

            int dialogueOptions = 0;
            int xmlCounter = 0;

            int responseSelection = 1; //do not touch!
            int eventType = 0;
            //endregion

            //region Examine how many dialogue options are present in module
            try {
                eventType = xpp.getEventType();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            while (continueSearching == true) {
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    //region Emergency break
                    if (continueSearching == false) {
                        break;
                    }
                    //endregion

                    comparison = xpp.getName();

                    if (responseCriteria.equals(comparison) && eventType == XmlPullParser.START_TAG) {
                        calculateDialogueOptions = true;
                    }

                    if (calculateDialogueOptions == true) {
                        xmlCounter++;

                        if ("C1".equals(comparison) && eventType == XmlPullParser.START_TAG) {
                            dialogueOptions = xmlCounter / 3;
                            //System.out.println(dialogueOptions + " potential dialogue options detected");
                            continueSearching = false;
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
            //endregion

            //region Retrieve randomised dialogue option
            Random rand = new Random();
            int max = (dialogueOptions + 1); //max 5
            int min = 2; //min 1
            int randomStatement;

            randomStatement = rand.nextInt((max - min) + 1) + min;

            continueSearching = true;
            xpp = getResources().getXml(R.xml.alfred_responses_en);
            while (continueSearching == true) {
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (continueSearching == false) {
                        break;
                    }

                    comparison = xpp.getText();
                    if (responseFound == true && ("null" != comparison.intern())) {
                        comparison = xpp.getText();
                        if (alfredResponse == null) {
                            alfredResponse = comparison;
                        } else {
                            alfredResponse = alfredResponse + " " + comparison;
                        }

                        tagReached = false;
                        continueSearching = false;
                        break;
                    } else {
                        if (eventType == XmlPullParser.END_TAG) {
                            comparison = xpp.getName();
                            if (Objects.equals(responseCriteria, comparison)) {
                                System.out.println("Criteria ended");
                                responseCriteriaReached = false;
                                continueSearching = false;
                                break;
                            }
                        }

                        if (eventType == XmlPullParser.START_TAG) {
                            comparison = xpp.getName();

                            if (Objects.equals(responseCriteria, comparison)) {
                                responseCriteriaReached = true;
                            }

                            if (responseCriteriaReached == true) {
                                if (Integer.toString(responseSelection).equals(Integer.toString(randomStatement))) {
                                    responseFound = true;
                                }

                                responseSelection++;
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
            //endregion
        }

        catch (Exception e)
        {
            //region Detect Critical Errors and Stop/Reset
            System.out.println("CRITICAL ERROR READING RESPONSES XML");
            criticalErrorDetected = true;
            ScrollView myScroller = (ScrollView) findViewById(R.id.scroll_view);
            myScroller.smoothScrollTo( 0, myScroller.getChildAt( 0 ).getTop() );
            //endregion
        }
    }

    public void readContextualOptions(String responseCriteria)
    {
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
                //System.out.println(comparison);
                currentPositionCounter++;

                if(valuePositionsCalculated != true)
                {
                    if (responseCriteria.equals(comparison) && eventType == XmlPullParser.END_TAG)
                    {
                        //System.out.println(comparison + " end tag located");
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
                        //System.out.println(comparison);
                        if(comparison.equals("-"))
                        {
                            comparison = null;
                        }

                        else if(comparison != null)
                        {
                            //System.out.println("C2 Function = " + comparison);
                            contextualResponse2Function = comparison;
                            continueSearching = false;
                        }
                    }

                    if(currentPositionCounter == (targetPositionCounter - 6))
                    {
                        comparison = xpp.getText();
                        //System.out.println(comparison);
                        if(comparison.equals("-"))
                        {
                            comparison = null;
                        }

                        else if(comparison != null)
                        {
                            //System.out.println("C2 = " + comparison);
                            contextualResponse2 = comparison;
                        }
                    }

                    if(currentPositionCounter == (targetPositionCounter - 9))
                    {
                        comparison = xpp.getText();
                        //System.out.println(comparison);
                        if(comparison.equals("-"))
                        {
                            comparison = null;
                        }

                        else if(comparison != null)
                        {
                            //System.out.println("C1 Function = " + comparison);
                            contextualResponse1Function = comparison;
                        }
                    }

                    if(currentPositionCounter == (targetPositionCounter - 12))
                    {
                        comparison = xpp.getText();
                        //System.out.println(comparison);
                        if(comparison.equals("-"))
                        {
                            comparison = null;
                        }

                        else if(comparison != null)
                        {
                            //System.out.println("C1 = " + comparison);
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
                    //System.out.println("No dialogue options found");
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
        if(criticalErrorDetected == false)
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
                        resetResponseInterface();
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
                        myScroller.smoothScrollTo(5, 321);
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

        else
        {
            System.out.println("Unable to display results of query");
        }
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
        if(function == "VOICE_DICTATION")
        {
            resetResponseInterface();
            ScrollView myScroller = (ScrollView) findViewById(R.id.scroll_view);
            myScroller.smoothScrollTo(0, myScroller.getChildAt(0).getTop());

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    try {
                        ImageView voiceDictationClick = (ImageView) findViewById(R.id.alfred_mustache);
                        voiceDictationClick.performClick();
                    } catch (Exception e) {

                    }
                }
            }, 300);
        }

        else if (function != "VOICE_DICTATION")
        {
            if (Arrays.asList(modules).contains(function))
            {
                resetResponseInterface();
                int numberOfModules = 0;
                for (int i = 0; i < modules.length; i ++)
                {
                    if (modules[i] != null)
                    {
                        numberOfModules++;
                    }
                }

                for(int i = 0; i <= numberOfModules; i++)
                {
                    if(function.equals(modules[i]))
                    {
                        readResponseXML(modules[i]);
                        contextualRefresh();
                        break;
                    }
                }
            }

            else
            {
                moduleInstructions();
            }
        }
    }

    public void contextualRefresh()
    {
        ScrollView myScroller = (ScrollView) findViewById(R.id.scroll_view);
        myScroller.smoothScrollTo( 0, myScroller.getChildAt( 0 ).getTop() );

        if(criticalErrorDetected == false)
        {
            displayResponse();
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
