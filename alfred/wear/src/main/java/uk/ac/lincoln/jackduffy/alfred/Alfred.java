package uk.ac.lincoln.jackduffy.alfred;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Alfred extends WearableActivity {
    //region Initialize Values
    //region Booleans
    Boolean listeningForInput = false;
    Boolean userInputUnderstood = false;
    Boolean wasLastMessageUnderstood = true;
    Boolean alfredResponseReady = true;
    Boolean criticalErrorDetected = false;
    Boolean testingMode = true;
    //endregion
    //region Strings
    public static String userInput;
    String dataFromPhoneSubject;
    String alfredResponse;
    String contextualResponse1;
    String contextualResponse1Function;
    String contextualResponse2;
    String contextualResponse2Function;
    //endregion
    //region String Arrays
    String[] dataFromPhone;
    String[] modules = new String[100];
    private String nodeId;
    //endregion
    //region Integers
    Integer userMessageNumber = 0;
    Integer dataFromPhoneTimestamp = 0;
    Integer systemCallTimestamp = 0;
    //endregion
    //region Statics
    private static final SimpleDateFormat AMBIENT_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    private static final int SPEECH_RECOGNIZER_REQUEST_CODE = 0;
    private static final long CONNECTION_TIME_OUT_MS = 1000;
    private static String MESSAGE = "default";
    static final int VERIFY_INPUT_REQUEST = 1;  // The request code
    public static String editorResponse = null;
    public static Boolean sharedPreferencesReady = false;
    //endregion
    //region Miscelanious Values
    XmlResourceParser xpp;
    GoogleApiClient googleClient;
    //endregion
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //create the instance of Alfred
        setContentView(R.layout.activity_alfred); //set the layout
        setAmbientEnabled(); //enable the ambient mode
        readModules(); //read the available modules
        ImageView background_image = (ImageView) findViewById(R.id.background); //set background gif
        Glide.with(this).load(R.drawable.background_a).asGif().into(background_image); //apply background gif with Glide
        initApi(); //initialize the google client
    }

    private void initApi() //initialize the google client api for watch -> phone communication
    {
        googleClient = getGoogleApiClient(this); //initialize it
        retrieveDeviceNode();
    }

    private GoogleApiClient getGoogleApiClient(Context context) //build a new google client
    {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private void retrieveDeviceNode() //retrieve the node id of everything connected to the wearable
    {
        new Thread(new Runnable() //run it on a new thread
        {
            @Override
            public void run() {
                googleClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS); //set timeout limit
                NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(googleClient).await(); //wait for connection
                List<Node> nodes = result.getNodes(); //if connection is successful, retrieve nodes

                if (nodes.size() > 0) //if there is at least one node active
                {
                    nodeId = nodes.get(0).getId(); //get its id
                    //System.out.println("Node Detected: " + nodeId);
                }

                googleClient.disconnect(); //disconnect from the client
            }
        }).start();
    }

    private void sendMessageToPhone(String inputPhrase)
    {
        if (nodeId != null) {
            MESSAGE = inputPhrase;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    googleClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(googleClient, nodeId, MESSAGE, null);
                    googleClient.disconnect();
                    //System.out.println("Message sent");
                }
            }).start();
        } else {
            System.out.println("Message - '" + inputPhrase + "' failed to send");
        }
    }

    public void readModules() {
        xpp = getResources().getXml(R.xml.alfred_responses_en);
        Boolean continueSearching = true;
        String comparison;
        int eventType = 0;
        int moduleNumber = 0;

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
                if (eventType == XmlPullParser.START_TAG) {
                    if (comparison.contains("AL-")) {
                        modules[moduleNumber] = comparison;
                        moduleNumber++;
                    }
                }

                try {
                    eventType = xpp.next();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (eventType == XmlPullParser.END_DOCUMENT) {
                    //System.out.println("Finished searching");
                    continueSearching = false;
                    break;
                }
            }
            xpp.close();
        }
    }

    public void resetResponseInterface() {
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

    public void moduleInstructions() {
        System.out.println("Error: Module not detected, perhaps it wasn't added correctly?");
        System.out.println("STAGES OF ADDING A MODULE :-");
        System.out.println("1) Add an entry in the 'inputs_en.xml' file, these are the trigger words");
        System.out.println("2) Add an entry in the 'alfred_responses_en.xml' file, these are the responses you want Alfred to say and the context buttons");
        System.out.println("3) Make sure your module name in both files starts with 'AL-'");
        System.out.println("4) You're done. Alfred will take care of the rest. Easy right?");
    }

    public void alfredThinking()
    {
        final ImageView mustache = (ImageView) findViewById(R.id.alfred_mustache);
        final ProgressBar progress_spinner = (ProgressBar) findViewById(R.id.alfred_progress);

        if (mustache.getVisibility() == View.VISIBLE)
        {
            Animation mustacheAnimation = new AlphaAnimation(1, 0);
            mustacheAnimation.setInterpolator(new AccelerateInterpolator());
            mustacheAnimation.setDuration(1000);

            mustacheAnimation.setAnimationListener(new Animation.AnimationListener()
            {
                public void onAnimationEnd(Animation animation)
                {
                    mustache.setVisibility(View.INVISIBLE);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });

            mustache.startAnimation(mustacheAnimation);

            Animation spinnerAnimation = new AlphaAnimation(0, 1);
            spinnerAnimation.setInterpolator(new AccelerateInterpolator());
            spinnerAnimation.setDuration(1000);

            spinnerAnimation.setAnimationListener(new Animation.AnimationListener()
            {
                public void onAnimationEnd(Animation animation)
                {
                    progress_spinner.setVisibility(View.VISIBLE);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });

            progress_spinner.startAnimation(spinnerAnimation);
        }

        else
        {
            Animation mustacheReduce = new AlphaAnimation(0, 1);
            mustacheReduce.setInterpolator(new AccelerateInterpolator());
            mustacheReduce.setDuration(1000);

            mustacheReduce.setAnimationListener(new Animation.AnimationListener()
            {
                public void onAnimationEnd(Animation animation)
                {
                    mustache.setVisibility(View.VISIBLE);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });
            mustache.startAnimation(mustacheReduce);

            Animation spinnerAnimation = new AlphaAnimation(1, 0);
            spinnerAnimation.setInterpolator(new AccelerateInterpolator());
            spinnerAnimation.setDuration(1000);

            spinnerAnimation.setAnimationListener(new Animation.AnimationListener()
            {
                public void onAnimationEnd(Animation animation)
                {
                    progress_spinner.setVisibility(View.INVISIBLE);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });

            progress_spinner.startAnimation(spinnerAnimation);
        }

    }

    public void voiceDictation(View view) {
        resetResponseInterface();
        if (listeningForInput == false) {
            listeningForInput = true;

            //alfredFaceAnimation(1, 1);
            alfredThinking();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    //region Call the voice dictation tool
                    //region Standard Operation
                    if (testingMode == false) {
                        try {
                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            startActivityForResult(intent, SPEECH_RECOGNIZER_REQUEST_CODE);
                        } catch (Exception e) {

                        }
                    }
                    //endregion

                    //region Debugging Enabled
                    else {
                        userInput = "what events do I have today";
                        System.out.println(userInput);
                        try {



                            verifyInput();
                            //AnalyseInput();
                        } catch (Exception e) {

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode)
        {
            case SPEECH_RECOGNIZER_REQUEST_CODE:
                //region Speech Recogniser
                try
                {
                    //region Bind the returned value from the dictation tool to a string (and perform some alterations for readability)
                    List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); //get each word detected from the speech recognition
                    String voiceInput = results.get(0); //concatenate these into a single string

                    userInput = voiceInput;
                    optimiseInput();
                    //endregion
                } catch (Exception e) {
                    //region If there's an error detected, wipe the user input
                    userInput = null;
                    //endregion
                }

                if (userInput != "") {
                    //region If the user input is ok, proceed to verification process
                    verifyInput();
                    //endregion
                }

                listeningForInput = false;
                //endregion
                break;

            case VERIFY_INPUT_REQUEST:
                System.out.println("Returned from verification intent with message: " + editorResponse);

                switch(editorResponse)
                {
                    case "continue":
                        optimiseInput();
                        AnalyseInput();
                        break;
                    case "redo":
                        alfredThinking();
                        listeningForInput = false;
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
                        break;
                    case "cancel":
                        alfredThinking();
                        resetResponseInterface();
                        listeningForInput = false;
                        break;
                }

                break;
        }

    }

    public void optimiseInput() {
        userInput = userInput.replaceAll(" ", "_").toUpperCase(); //transform that string into upper case, replaces spaces with underscores and assign to a global value string
        //System.out.println("RAW INPUT: "+ userInput);

        if (userInput.contains("_+_")) {
            userInput = userInput.replaceAll("\\+", "SF-PLUS");
        }

        if (userInput.contains("_-_")) {
            userInput = userInput.replaceAll("\\-", "SF-MINUS");
        }

        if (userInput.contains("_X_")) {
            userInput = userInput.replaceAll("_X_", "_SF-MULTIPLY_");
        }

        if (userInput.contains("_÷_")) {
            userInput = userInput.replaceAll("_÷_", "_SF-DIVIDE_");
        }

        userInput = userInput + "_";

        System.out.println("FINAL INPUT: "+ userInput);
    }

    public void verifyInput()
    {
        Intent verifyInputIntent = new Intent(Alfred.this, verifyInput.class);
        verifyInputIntent.putExtra("DATA:", userInput);
        startActivityForResult(verifyInputIntent, VERIFY_INPUT_REQUEST);

//        System.out.println("Input is OK. Proceeding to analysis");
//        AnalyseInput();
    }

    public void AnalyseInput()
    {
        int numberOfModules = 0;
        System.out.println("AVAILABLE MODULES :-");
        for (int i = 0; i < modules.length; i++) {
            if (modules[i] != null) {
                System.out.println("Module " + numberOfModules + ": " + modules[i]);
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

        try {
            for (int module = 1; module <= (numberOfModules + 1); module++) {
                if (module <= numberOfModules) {
                    moduleName = null;
                    userInputUnderstood = false;
                    switch (module) {
                        case 1:
                            moduleName = modules[0];
                            break;
                        case 2:
                            if (module1_ENGAGED != true) {
                                moduleName = modules[1];
                            }
                            break;
                        case 3:
                            if (module1_ENGAGED != true) {
                                moduleName = modules[2];
                            }
                            break;
                        case 4:
                            if (module1_ENGAGED != true) {
                                moduleName = modules[3];
                            }
                            break;
                        case 5:
                            if (module1_ENGAGED != true) {
                                moduleName = modules[4];
                            }
                            break;
                        case 6:
                            if (module1_ENGAGED != true) {
                                moduleName = modules[5];
                            }
                            break;
                        case 7:
                            if (module1_ENGAGED != true) {
                                moduleName = modules[6];
                            }
                            break;
                        case 8:
                            if (module1_ENGAGED != true) {
                                moduleName = modules[7];
                            }
                            break;
                        case 9:
                            if (module1_ENGAGED != true) {
                                moduleName = modules[8];
                            }
                            break;
                        case 10:
                            if (module1_ENGAGED != true) {
                                moduleName = modules[9];
                            }
                            break;
                    }
                } else if (module == (numberOfModules + 1)) {
                    if (module1_ENGAGED != true && module2_ENGAGED != true && module3_ENGAGED != true && module4_ENGAGED != true && module5_ENGAGED != true && module6_ENGAGED != true && module7_ENGAGED != true && module7_ENGAGED != true && module8_ENGAGED != true && module9_ENGAGED != true && module10_ENGAGED != true) {
                        System.out.println("* Input not understood *");
                        moduleName = null;
                        inputNotUnderstood();
                    } else {
                        moduleName = null;
                    }
                }

                if (moduleName != null) {
                    readXML(moduleName);
                    if (userInputUnderstood == true) {
                        wasLastMessageUnderstood = true;
                        switch (module) {
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

                        if (moduleName == "FAREWELLS") {
                            module = 10;
                            break;
                        }
                    } else {

                    }
                }
            }
            displayResponse();
        } catch (Exception e) {
            //System.out.println("CRITICAL ERROR DETECTED: User input is invalid, if this occurs after manually closing the voice dictation tool, ignore this warning!");
        }

    }

    public void formulateResponse(String typeOfResponse)
    {
        String moduleName = typeOfResponse;
        userMessageNumber = 1; //change to increment when ready

        if (userInputUnderstood == true) {
            xpp = getResources().getXml(R.xml.alfred_responses_en);
            readResponseXML(moduleName);
        } else if (userInputUnderstood == false) {
            userInputUnderstood = false;
            userInput = "";
            inputNotUnderstood();
        }

        while (alfredResponseReady == false) {
            //do nothing
        }
    }

    public void readXML(String targetTag)
    {
        XmlResourceParser xpp = getResources().getXml(R.xml.inputs_en);
        int eventType = 0;

        try {
            eventType = xpp.getEventType();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        String comparison = null;
        Boolean continueRunning = true;
        Boolean tagReached = false;

        //region Check for tag
        while (continueRunning == true) {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.END_TAG) {
                    comparison = xpp.getName();
                    if (Objects.equals(targetTag, comparison)) {
                        //System.out.println("BREAKING");
                        //tagReached = false;
                        break;
                    }
                }

                if (eventType == XmlPullParser.START_TAG) {
                    comparison = xpp.getName();
                    if (Objects.equals(targetTag, comparison)) {
                        //System.out.println("Checking module: " + targetTag);
                        tagReached = true;
                    } else if (tagReached == true) {

                        if (userInput.contains(comparison)) {
                            //System.out.println("* Match in Module: " + targetTag + "*");
                            userInputUnderstood = true;
                            continueRunning = false;
                            break;
                        }
                    }
                }

                try {
                    eventType = xpp.next();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (eventType == XmlPullParser.END_DOCUMENT) {
                if (tagReached == false) {
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

            if (alfredResponse.contains("SF-"))
            {
                System.out.println("Special function(s) detected");
                specialFunctionsReader();
            }

        }

        catch (Exception e)
        {
            //region Detect Critical Errors and Stop/Reset
            System.out.println("CRITICAL ERROR READING RESPONSES XML");
            criticalErrorDetected = true;
            ScrollView myScroller = (ScrollView) findViewById(R.id.scroll_view);
            myScroller.smoothScrollTo(0, myScroller.getChildAt(0).getTop());
            //endregion
        }
    }

    public void specialFunctionsReader()
    {
        if (alfredResponse != "" || alfredResponse != null || alfredResponse != "null")
        {
            if (alfredResponse.contains("SF-MATHMATICS"))
            {
                //region Mathmatic Functions
                alfredResponse = alfredResponse.replaceAll("SF-MATHMATICS", "");

                //region Count functions in input string
                Integer numberOfFunctions = 0;
                Pattern functionSearch = Pattern.compile("SF-");
                Matcher functionMatch = functionSearch.matcher(userInput);

                while (functionMatch.find()) {
                    numberOfFunctions++;
                }
                //endregion

                //region Create and Initialise Values
                String[] userInputArray = userInput.split("_");
                String[] mathmaticalFunctions = {"SF-PLUS", "SF-MINUS", "SF-MULTIPLY", "SF-DIVIDE"};
                Boolean continueSearching = true;
                Integer functionsFound = 0;

                String function1 = "";
                String function2 = "";
                String function3 = "";
                String function4 = "";
                String function5 = "";

                Integer function1Position = 0;
                Integer function2Position = 0;
                Integer function3Position = 0;
                Integer function4Position = 0;
                Integer function5Position = 0;
                //endregion

                while (continueSearching == true) {
                    for (int i = 0; i < userInputArray.length; i++) {
                        for (int x = 0; x < mathmaticalFunctions.length; x++) {
                            if (userInputArray[i].equals(mathmaticalFunctions[x])) {
                                functionsFound++;
                                //region Assign functions and locations
                                switch (functionsFound) {
                                    case 1:
                                        function1Position = i;
                                        function1 = userInputArray[i];
                                        break;
                                    case 2:
                                        function2Position = i;
                                        function2 = userInputArray[i];
                                        break;
                                    case 3:
                                        function3Position = i;
                                        function3 = userInputArray[i];
                                        break;
                                    case 4:
                                        function4Position = i;
                                        function4 = userInputArray[i];
                                        break;
                                    case 5:
                                        function5Position = i;
                                        function5 = userInputArray[i];
                                        break;
                                }
                                //endregion

                                if (functionsFound == numberOfFunctions) {
                                    //System.out.println("All functions found");
                                    continueSearching = false;
                                }
                            } else {

                            }
                        }
                    }
                    continueSearching = false;
                }

                Integer output1 = 0;
                alfredResponse = alfredResponse + "I think I've got it:\n";
                String symbol1 = "";

                Integer value1 = Integer.parseInt(userInputArray[function1Position - 1]);
                Integer value2 = Integer.parseInt(userInputArray[function1Position + 1]);
                switch (function1) {
                    case "SF-PLUS":
                        output1 = (value1 + value2);
                        symbol1 = " + ";
                        break;
                    case "SF-MINUS":
                        output1 = (value1 - value2);
                        symbol1 = " - ";
                        break;
                    case "SF-MULTIPLY":
                        output1 = (value1 * value2);
                        symbol1 = " × ";
                        break;
                    case "SF-DIVIDE":
                        output1 = (value1 / value2);
                        symbol1 = " ÷ ";
                        break;
                }

                if (numberOfFunctions > 1) {
                    String symbol2 = "";
                    Integer output2 = 0;
                    Integer value3 = Integer.parseInt(userInputArray[function2Position + 1]);
                    switch (function2) {
                        case "SF-PLUS":
                            output2 = (output1 + value3);
                            symbol2 = " + ";
                            break;
                        case "SF-MINUS":
                            output2 = (output1 - value3);
                            symbol2 = " - ";
                            break;
                        case "SF-MULTIPLY":
                            output2 = (output1 * value3);
                            symbol2 = " × ";
                            break;
                        case "SF-DIVIDE":
                            output2 = (output1 / value3);
                            symbol2 = " ÷ ";
                            break;
                    }

                    if (numberOfFunctions > 2) {
                        String symbol3 = "";
                        Integer output3 = 0;
                        Integer value4 = Integer.parseInt(userInputArray[function3Position + 1]);
                        switch (function2) {
                            case "SF-PLUS":
                                output3 = (output2 + value4);
                                symbol3 = " + ";
                                break;
                            case "SF-MINUS":
                                output3 = (output2 - value4);
                                symbol3 = " - ";
                                break;
                            case "SF-MULTIPLY":
                                output3 = (output2 * value4);
                                symbol3 = " × ";
                                break;
                            case "SF-DIVIDE":
                                output3 = (output2 / value4);
                                symbol3 = " ÷ ";
                                break;
                        }

                        if (numberOfFunctions > 3) {
                            String symbol4 = "";
                            Integer output4 = 0;
                            Integer value5 = Integer.parseInt(userInputArray[function4Position + 1]);
                            switch (function2) {
                                case "SF-PLUS":
                                    output4 = (output3 + value5);
                                    symbol4 = " + ";
                                    break;
                                case "SF-MINUS":
                                    output4 = (output3 - value5);
                                    symbol4 = " - ";
                                    break;
                                case "SF-MULTIPLY":
                                    output4 = (output3 * value5);
                                    symbol4 = " × ";
                                    break;
                                case "SF-DIVIDE":
                                    output4 = (output3 / value5);
                                    symbol4 = " ÷ ";
                                    break;
                            }

                            if (numberOfFunctions > 4) {
                                String symbol5 = "";
                                Integer output5 = 0;
                                Integer value6 = Integer.parseInt(userInputArray[function5Position + 1]);
                                switch (function2) {
                                    case "SF-PLUS":
                                        output5 = (output4 + value6);
                                        symbol5 = " + ";
                                        break;
                                    case "SF-MINUS":
                                        output5 = (output4 - value6);
                                        symbol5 = " - ";
                                        break;
                                    case "SF-MULTIPLY":
                                        output5 = (output4 * value6);
                                        symbol5 = " × ";
                                        break;
                                    case "SF-DIVIDE":
                                        output5 = (output4 / value6);
                                        symbol4 = " ÷ ";
                                        break;
                                }

                                alfredResponse = alfredResponse + value1 + symbol1 + value2 + symbol2 + value3 + symbol3 + value4 + symbol4 + value5 + symbol5 + value6 + " = " + output5;
                            } else {
                                alfredResponse = alfredResponse + value1 + symbol1 + value2 + symbol2 + value3 + symbol3 + value4 + symbol4 + value5 + " = " + output4;
                            }
                        } else {
                            alfredResponse = alfredResponse + value1 + symbol1 + value2 + symbol2 + value3 + symbol3 + value4 + " = " + output3;
                        }

                    } else {
                        alfredResponse = alfredResponse + value1 + symbol1 + value2 + symbol2 + value3 + " = " + output2;
                    }
                } else {
                    alfredResponse = alfredResponse + value1 + symbol1 + value2 + " = " + output1;
                }
                //endregion
            }

            if (alfredResponse.contains("SF-WEATHER_API"))
            {
                systemCallTimestamp = (int) (System.currentTimeMillis() / 1000l);
                sendMessageToPhone("SF-WEATHER");
                sharedPreferencesReady = false;
                new waitForResponse().execute();
            }

            if (alfredResponse.contains("SF-GOOGLE_CALENDAR"))
            {
                systemCallTimestamp = (int) (System.currentTimeMillis() / 1000l);
                sendMessageToPhone("SF-GOOGLE_CALENDAR");
                sharedPreferencesReady = false;
                new waitForResponse().execute();
            }
        }
    }

    public void specialFunctionsController()
    {
        //region Generate Response from retrieved data
        if (dataFromPhoneSubject != null)
        {
            //System.out.println("Current Time: " + systemCallTimestamp + " :: Packet Time: " + dataFromPhone[0].substring(8));
            System.out.println("The data packet has the subject: " + dataFromPhoneSubject);
            switch(dataFromPhoneSubject)
            {
                case "WEATHER":
                    //region Weather Function
                    for (int i = 0; i < dataFromPhone.length; i++) {
                        dataFromPhone[i] = (dataFromPhone[i].substring(dataFromPhone[i].indexOf("=") + 1)).replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
                    }

                    //region Full Weather Details Response
                    if (alfredResponse.contains("SF-WEATHER_API_FULL"))
                    {
                        Integer tempCelsius1 = (((Integer.parseInt(dataFromPhone[7].substring(0, dataFromPhone[7].length()-3))) - 32)*5)/9;
                        Integer tempCelsius2 = (((Integer.parseInt(dataFromPhone[8].substring(0, dataFromPhone[8].length()-3))) - 32)*5)/9;
                        alfredResponse = "Ah. Here are the full weather details for today sir." +
                                "\n\nSummary:\n" + dataFromPhone[1] +
                                "\n\nTemperature:\n" + tempCelsius1 + "°C/" + dataFromPhone[7] + "°F" +
                                "\n\nFeels Like:\n" + tempCelsius2 + "°C/" + dataFromPhone[8] + "°F" +
                                "\n\nHumidity:\n" + dataFromPhone[10] + "%" +
                                "\n\nWind:\n" + dataFromPhone[11] + "mph" +
                                "\n\nVisibility:\n" + dataFromPhone[13] + "ft" +
                                "\n\nCloud Cover:\n" + dataFromPhone[14] +
                                "\n\nPressure:\n" + dataFromPhone[15] + " psi" +
                                "\n\nPrecipitation Probability:\n" + dataFromPhone[6] + "%" +
                                "\n\nPrecipitation Intensity:\n" + dataFromPhone[5] + "";

                        if (dataFromPhone[3] != "") {
                            alfredResponse = alfredResponse + "\n\nNearest Storm Distance:\n" + dataFromPhone[3] + " miles";
                        }

                        alfredResponse = alfredResponse + "\n\nDew Point\n: " + dataFromPhone[9] + "°F" +
                                "\n\nOzone:\n" + dataFromPhone[16];

                    }
                    //endregion

                    //region Partial Weather Details Response
                    else if (alfredResponse.contains("SF-WEATHER_API"))
                    {
                        Integer tempCelsius = (((Integer.parseInt(dataFromPhone[7].substring(0, dataFromPhone[7].length()-3))) - 32)*5)/9;
                        alfredResponse = "Certainly sir. It is currently " + dataFromPhone[1] + " with the temperature of " + tempCelsius + "°C";
                    }
                    //endregion
                    //endregion
                    break;
                case "CALENDAR":
                    alfredResponse = "Most certainly sir. Your upcoming calendar events are as follows:";

                    //region Cleans up the data
                    for (int i = 0; i < dataFromPhone.length; i++)
                    {
                        //System.out.println("I am on loop " + i + " - there are " + dataFromPhone.length + " loops in total");
                        dataFromPhone[i] = dataFromPhone[i].replaceAll("([A-Z])", " $1");
                        dataFromPhone[i] = dataFromPhone[i].substring(3);

                        if(dataFromPhone[i].substring(0, 1).contains(" "))
                        {
                            dataFromPhone[i] = dataFromPhone[i].substring(1);
                        }

                        dataFromPhone[i] = dataFromPhone[i].replaceAll("\\(", "\n");
                        dataFromPhone[i] = dataFromPhone[i].replaceAll(" T", "\n");
                        dataFromPhone[i] = dataFromPhone[i].substring(0,dataFromPhone[i].length()-11);
                        alfredResponse = alfredResponse + "\n\n" + dataFromPhone[i];
                        System.out.println(dataFromPhone[i]);

                        if(i == 9)
                        {
                            break;
                        }

                    }
                    //endregion

                    System.out.println("Successfully broken out of loop");
                    break;
            }
            displayResponse();
        }
        //endregion
    }

    class waitForResponse extends AsyncTask<Void, Integer, String>
    {
        protected void onPreExecute ()
        {

        }

        protected String doInBackground(Void...arg0)
        {
            while(sharedPreferencesReady == false)
            {
                //false
            }

            if(sharedPreferencesReady == true)
            {
                readSharedPrefs();
            }
            return null;
        }

        protected void onProgressUpdate(Integer...a)
        {

        }

        protected void onPostExecute(String result)
        {
            displayResponse();
        }
    }

    public void readSharedPrefs()
    {
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            dataFromPhone = new String[0];
            dataFromPhoneSubject = null;
            dataFromPhoneTimestamp = 0;
            String[] sharedPrefsData = new String[100];
            int numberOfElements = 0;
            String temp;
            String temp2;
            for (int i = 0; i <= 1000; i++) {
                try {
                    temp = (preferences.getString(Integer.toString(i), ""));
                    if (temp != "") {
                        temp2 = temp.substring(0, 2);
                        if (temp2.equals("##")) {
                            temp2 = temp.substring(3);
                            dataFromPhoneSubject = temp2;
                        } else {
                            sharedPrefsData[i] = temp;
                            numberOfElements++;
                        }
                    }
                } catch (Exception e) {
                    break;
                }
            }

            dataFromPhone = new String[(sharedPrefsData.length - (100 - numberOfElements))];
            for (int i = 0; i < dataFromPhone.length; i++) {
                dataFromPhone[i] = sharedPrefsData[i];
                //System.out.println(dataFromPhone[i]);
            }

            //System.out.println("Read shared preferences successfully");
            specialFunctionsController();
        }

        catch(Exception e)
        {
            System.out.println("Critical Error reading Shared Preferences. Operation aborted");
            System.out.println(e);
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
        if(alfredResponse.contains("AL-") || alfredResponse.contains("SF-"))
        {
            System.out.println("Not displaying this...");
            System.out.println(alfredResponse);
            //do nothing
        }

        else
        {
            if(criticalErrorDetected == false)
            {
                //Looper.prepare();
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

                        alfredThinking();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable()
                        {
                            public void run()
                            {
                                if(alfredResponse != null || alfredResponse != "")
                                {
                                    final ScrollView myScroller = (ScrollView) findViewById(R.id.scroll_view);
                                    myScroller.smoothScrollTo(5, 321);
                                }
                            }
                        }, 1000);

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
        alfredThinking();
        if(function.equals("VOICE_DICTATION"))
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