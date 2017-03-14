package uk.ac.lincoln.jackduffy.alfred;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class verifyInput extends WearableActivity
{
    public static boolean verificationInterrupted = false;
    int i=0;
    ProgressBar mProgressBar;
    CountDownTimer mCountDownTimer;
    String userData = null;
    String wordToEdit = "";
    String previousWord;
    String wordToReplace;
    private TextView inputTextDisplay;
    Integer numberOfSplitWords = 25;
    String[] wordsInInput;
    Boolean listeningForInput = false;
    Boolean editorActive = false;
    private static final int SPEECH_RECOGNIZER_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //create the instance of Alfred
        setContentView(R.layout.activity_verify); //set the layout
        ScrollView scroller = (ScrollView)findViewById(R.id.verify_scroller);
        scroller.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener()
        {
            @Override
            public void onScrollChanged()
            {
                fadeOutProgressBar();
            }
        });

        Alfred.editorResponse = "null";
        if(Alfred.userInput != null)
        {
            inputTextDisplay = (TextView)findViewById(R.id.input_text);
            inputTextDisplay.setText(Alfred.userInput);
            inputTextDisplay.bringToFront();


            mProgressBar=(ProgressBar)findViewById(R.id.verify_progressbar);
            mProgressBar.setProgress(i);
            mCountDownTimer=new CountDownTimer(3000,2)
            {
                @Override
                public void onTick(long millisUntilFinished)
                {
                    i++;
                    mProgressBar.setProgress(i);
                }

                @Override
                public void onFinish()
                {
                    i++;
                    mProgressBar.setProgress(i);

                    if(verificationInterrupted != true)
                    {
                        Alfred.editorResponse = "continue";
                        finish();
                    }

                    else
                    {
                        fadeOutProgressBar();
                    }
                }
            };
            mCountDownTimer.start();
        }

        else
        {
            finish();
        }
    }

    public void fadeOutProgressBar()
    {
        if(verificationInterrupted != true)
        {
            verificationInterrupted = true;
            final ProgressBar progress = (ProgressBar) findViewById(R.id.verify_progressbar);

            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(500);

            fadeOut.setAnimationListener(new Animation.AnimationListener()
            {
                public void onAnimationEnd(Animation animation)
                {
                    progress.setVisibility(View.INVISIBLE);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });

            progress.startAnimation(fadeOut);
        }
    }

    public void transformInterface(String mode, Integer option)
    {
        //region Initialize All Views
        final View linearLayout = findViewById(R.id.verify_layout);
        final ImageView background = (ImageView) findViewById(R.id.verify_background);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.verify_progressbar);
        final ImageView icon1 = (ImageView) findViewById(R.id.verification_icon_1);
        final ImageView icon2 = (ImageView) findViewById(R.id.verification_icon_2);
        final ImageView icon3 = (ImageView) findViewById(R.id.verification_icon_3);
        final ImageView icon4 = (ImageView) findViewById(R.id.verification_icon_4);
        final TextView label1 = (TextView) findViewById(R.id.verification_retry_text);
        final TextView label2 = (TextView) findViewById(R.id.verification_edit_text);
        final TextView label3 = (TextView) findViewById(R.id.verification_back_text);
        final TextView label4 = (TextView) findViewById(R.id.verification_cancel_text);

        final TextView splitWord0 = (TextView) findViewById(R.id.input_text_split_0);
        final TextView splitWord1 = (TextView) findViewById(R.id.input_text_split_1);
        final TextView splitWord2 = (TextView) findViewById(R.id.input_text_split_2);
        final TextView splitWord3 = (TextView) findViewById(R.id.input_text_split_3);
        final TextView splitWord4 = (TextView) findViewById(R.id.input_text_split_4);
        final TextView splitWord5 = (TextView) findViewById(R.id.input_text_split_5);
        final TextView splitWord6 = (TextView) findViewById(R.id.input_text_split_6);
        final TextView splitWord7 = (TextView) findViewById(R.id.input_text_split_7);
        final TextView splitWord8 = (TextView) findViewById(R.id.input_text_split_8);
        final TextView splitWord9 = (TextView) findViewById(R.id.input_text_split_9);
        final TextView splitWord10 = (TextView) findViewById(R.id.input_text_split_10);
        final TextView splitWord11 = (TextView) findViewById(R.id.input_text_split_11);
        final TextView splitWord12 = (TextView) findViewById(R.id.input_text_split_12);
        final TextView splitWord13 = (TextView) findViewById(R.id.input_text_split_13);
        final TextView splitWord14 = (TextView) findViewById(R.id.input_text_split_14);
        final TextView splitWord15 = (TextView) findViewById(R.id.input_text_split_15);
        final TextView splitWord16 = (TextView) findViewById(R.id.input_text_split_16);
        final TextView splitWord17 = (TextView) findViewById(R.id.input_text_split_17);
        final TextView splitWord18 = (TextView) findViewById(R.id.input_text_split_18);
        final TextView splitWord19 = (TextView) findViewById(R.id.input_text_split_19);
        final TextView splitWord20 = (TextView) findViewById(R.id.input_text_split_20);
        final TextView splitWord21 = (TextView) findViewById(R.id.input_text_split_21);
        final TextView splitWord22 = (TextView) findViewById(R.id.input_text_split_22);
        final TextView splitWord23 = (TextView) findViewById(R.id.input_text_split_23);
        final TextView splitWord24 = (TextView) findViewById(R.id.input_text_split_24);
        final TextView splitWord25 = (TextView) findViewById(R.id.input_text_split_25);

        final ImageView editorBackground = (ImageView) findViewById(R.id.editor_background);
        final TextView suggestionsTitle = (TextView) findViewById(R.id.suggestions_title);
        final ImageView editorIcon1 = (ImageView) findViewById(R.id.editor_icon_1);
        final ImageView editorIcon2 = (ImageView) findViewById(R.id.editor_icon_2);
        final ImageView editorIcon3 = (ImageView) findViewById(R.id.editor_icon_3);
        //endregion

        float pixelPosition;

        switch (mode) {
            case "init_editor":
                //region Initialize Editor
                if (background.getVisibility() == View.VISIBLE)
                {
                    linearLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white));

                    //region Quick Remove UI
                    background.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    icon1.setVisibility(View.INVISIBLE);
                    icon2.setVisibility(View.INVISIBLE);
                    icon3.setVisibility(View.INVISIBLE);
                    icon4.setVisibility(View.INVISIBLE);
                    label1.setVisibility(View.INVISIBLE);
                    label2.setVisibility(View.INVISIBLE);
                    label3.setVisibility(View.INVISIBLE);
                    label4.setVisibility(View.INVISIBLE);
                    ///endregion

                    String[] userInputSplit = Alfred.userInput.split("\\s+");
                    System.out.println("User input has been split");

                    for (int i = 0; i < userInputSplit.length; i++) {
                        TextView splitTextView = (TextView) findViewById(R.id.input_text_split_0);
                        switch (i) {
                            case 0:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_0);
                                break;
                            case 1:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_1);
                                break;
                            case 2:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_2);
                                break;
                            case 3:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_3);
                                break;
                            case 4:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_4);
                                break;
                            case 5:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_5);
                                break;
                            case 6:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_6);
                                break;
                            case 7:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_7);
                                break;
                            case 8:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_8);
                                break;
                            case 9:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_9);
                                break;
                            case 10:
                                splitTextView = (TextView) findViewById(R.id.input_text_split_10);
                                break;
                        }

                        splitTextView.setText(userInputSplit[i]);
                        splitTextView.setVisibility(View.VISIBLE);

                        //Toast.makeText(this, "Tap on the word you want to edit for additional options", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                //endregion
                break;
            case "edit_word":
                //region Word Editor
                editorActive = true;
                //region Loop to determine size of array
                String item = "";
                Integer sizeOfArray = 0;
                for(int i = 0; i < numberOfSplitWords; i++)
                {
                    switch(i)
                    {
                        case 0:
                            item = splitWord0.getText().toString();
                            break;
                        case 1:
                            item = splitWord1.getText().toString();
                            break;
                        case 2:
                            item = splitWord2.getText().toString();
                            break;
                        case 3:
                            item = splitWord3.getText().toString();
                            break;
                        case 4:
                            item = splitWord4.getText().toString();
                            break;
                        case 5:
                            item = splitWord5.getText().toString();
                            break;
                        case 6:
                            item = splitWord6.getText().toString();
                            break;
                        case 7:
                            item = splitWord7.getText().toString();
                            break;
                        case 8:
                            item = splitWord8.getText().toString();
                            break;
                        case 9:
                            item = splitWord9.getText().toString();
                            break;
                        case 10:
                            item = splitWord10.getText().toString();
                            break;
                        case 11:
                            item = splitWord11.getText().toString();
                            break;
                        case 12:
                            item = splitWord12.getText().toString();
                            break;
                        case 13:
                            item = splitWord13.getText().toString();
                            break;
                        case 14:
                            item = splitWord14.getText().toString();
                            break;
                        case 15:
                            item = splitWord15.getText().toString();
                            break;
                        case 16:
                            item = splitWord16.getText().toString();
                            break;
                        case 17:
                            item = splitWord17.getText().toString();
                            break;
                        case 18:
                            item = splitWord18.getText().toString();
                            break;
                        case 19:
                            item = splitWord19.getText().toString();
                            break;
                        case 20:
                            item = splitWord20.getText().toString();
                            break;
                        case 21:
                            item = splitWord21.getText().toString();
                            break;
                        case 22:
                            item = splitWord22.getText().toString();
                             break;
                        case 23:
                            item = splitWord23.getText().toString();
                            break;
                        case 24:
                            item = splitWord24.getText().toString();
                            break;
                        case 25:
                            item = splitWord25.getText().toString();
                            break;
                    }

                    if((!item.equals("split")) && (!item.equals("null")))
                    {
                        sizeOfArray++;
                    }
                }
                //endregion

                //System.out.println("THE SIZE OF THE ARRAY IS " + sizeOfArray);
                wordsInInput = new String[sizeOfArray];

                //region Loop to retrieve all the individual words
                item = "";
                for(int i = 0; i < numberOfSplitWords; i++)
                {
                    switch(i)
                    {
                        case 0:
                            item = splitWord0.getText().toString();
                            break;
                        case 1:
                            item = splitWord1.getText().toString();
                            break;
                        case 2:
                            item = splitWord2.getText().toString();
                            break;
                        case 3:
                            item = splitWord3.getText().toString();
                            break;
                        case 4:
                            item = splitWord4.getText().toString();
                            break;
                        case 5:
                            item = splitWord5.getText().toString();
                            break;
                        case 6:
                            item = splitWord6.getText().toString();
                            break;
                        case 7:
                            item = splitWord7.getText().toString();
                            break;
                        case 8:
                            item = splitWord8.getText().toString();
                            break;
                        case 9:
                            item = splitWord9.getText().toString();
                            break;
                        case 10:
                            item = splitWord10.getText().toString();
                            break;
                        case 11:
                            item = splitWord11.getText().toString();
                            break;
                        case 12:
                            item = splitWord12.getText().toString();
                            break;
                        case 13:
                            item = splitWord13.getText().toString();
                            break;
                        case 14:
                            item = splitWord14.getText().toString();
                            break;
                        case 15:
                            item = splitWord15.getText().toString();
                            break;
                        case 16:
                            item = splitWord16.getText().toString();
                            break;
                        case 17:
                            item = splitWord17.getText().toString();
                            break;
                        case 18:
                            item = splitWord18.getText().toString();
                            break;
                        case 19:
                            item = splitWord19.getText().toString();
                            break;
                        case 20:
                            item = splitWord20.getText().toString();
                            break;
                        case 21:
                            item = splitWord21.getText().toString();
                            break;
                        case 22:
                            item = splitWord22.getText().toString();
                            break;
                        case 23:
                            item = splitWord23.getText().toString();
                            break;
                        case 24:
                            item = splitWord24.getText().toString();
                            break;
                        case 25:
                            item = splitWord25.getText().toString();
                            break;
                    }

                    if((!item.equals("split")) && (!item.equals("null")))
                    {
                        wordsInInput[i] = item;
                    }
                }
                //endregion

                //region Temporarily Hide all Views
                splitWord0.setVisibility(View.INVISIBLE);
                splitWord1.setVisibility(View.INVISIBLE);
                splitWord2.setVisibility(View.INVISIBLE);
                splitWord3.setVisibility(View.INVISIBLE);
                splitWord4.setVisibility(View.INVISIBLE);
                splitWord5.setVisibility(View.INVISIBLE);
                splitWord6.setVisibility(View.INVISIBLE);
                splitWord7.setVisibility(View.INVISIBLE);
                splitWord8.setVisibility(View.INVISIBLE);
                splitWord9.setVisibility(View.INVISIBLE);
                splitWord10.setVisibility(View.INVISIBLE);
                //endregion

                //region Show all editor elements
                editorBackground.setVisibility(View.VISIBLE);
                suggestionsTitle.setVisibility(View.VISIBLE);
                editorIcon1.setVisibility(View.VISIBLE);
                //endregion

                pixelPosition = 0f;
                switch (option) {
                    case 0:
                        splitWord0.setVisibility(View.VISIBLE);
                        pixelPosition = 700.0f;
                        break;
                    case 1:
                        splitWord1.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        splitWord2.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        splitWord3.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        splitWord4.setVisibility(View.VISIBLE);
                        break;
                    case 5:
                        splitWord5.setVisibility(View.VISIBLE);
                        break;
                    case 6:
                        splitWord6.setVisibility(View.VISIBLE);
                        break;
                    case 7:
                        splitWord7.setVisibility(View.VISIBLE);
                        break;
                    case 8:
                        splitWord8.setVisibility(View.VISIBLE);
                        break;
                    case 9:
                        splitWord9.setVisibility(View.VISIBLE);
                        break;
                    case 10:
                        splitWord10.setVisibility(View.VISIBLE);
                        break;
                }

                editorBackground.animate().translationY(pixelPosition).start();
                suggestionsTitle.animate().translationY(pixelPosition).start();
                editorIcon1.animate().translationY(pixelPosition).start();
                editorIcon2.animate().translationY(pixelPosition).start();
                editorIcon3.animate().translationY(pixelPosition).start();
                //endregion
                break;
            case "stop_editing":

                pixelPosition = 0f;
                switch (option)
                {
                    case 0:
                        pixelPosition = -700.0f;
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        break;
                    case 9:
                        break;
                    case 10:
                        break;
                }

                editorBackground.animate().translationY(pixelPosition).start();
                suggestionsTitle.animate().translationY(pixelPosition).start();
                editorIcon1.animate().translationY(pixelPosition).start();
                editorIcon2.animate().translationY(pixelPosition).start();
                editorIcon3.animate().translationY(pixelPosition).start();

                splitWord0.setVisibility(View.VISIBLE);
                splitWord1.setVisibility(View.VISIBLE);
                splitWord2.setVisibility(View.VISIBLE);
                splitWord3.setVisibility(View.VISIBLE);
                splitWord4.setVisibility(View.VISIBLE);
                splitWord5.setVisibility(View.VISIBLE);
                splitWord6.setVisibility(View.VISIBLE);
                splitWord7.setVisibility(View.VISIBLE);
                splitWord8.setVisibility(View.VISIBLE);
                splitWord9.setVisibility(View.VISIBLE);
                splitWord10.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void editorRedo(View view)
    {
        Alfred.editorResponse = "redo";
        finish();
    }

    public void editorEdit(View view)
    {
        final TextView userTextInput = (TextView) findViewById(R.id.input_text);
        userTextInput.setVisibility(view.INVISIBLE);

        ScrollView myScroller = (ScrollView) findViewById(R.id.verify_scroller);
        myScroller.smoothScrollTo(0, myScroller.getChildAt(0).getTop());
        transformInterface("init_editor", 0);
    }

    public void editorBack(View view)
    {
        Alfred.editorResponse = "continue";
        finish();
    }

    public void editorCancel(View view)
    {
        Alfred.editorResponse = "cancel";
        finish();
    }

    public void editWord(View view)
    {
        wordToEdit = view.getTag().toString();
        System.out.println("You tapped on word " + wordToEdit);
        transformInterface("edit_word", Integer.parseInt(wordToEdit));
    }

    public void editorRedoWord(View view)
    {
        System.out.println("Redo word!");
        if (listeningForInput == false)
        {
            listeningForInput = true;

            try
            {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, SPEECH_RECOGNIZER_REQUEST_CODE);
            }

            catch (Exception e)
            {

            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        try
        {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String voiceInput = results.get(0);

            try
            {
                Integer targetWord = Integer.parseInt(wordToEdit);
                previousWord = wordsInInput[targetWord];
                wordToReplace = voiceInput;

                System.out.println("Input before modification");
                for(int i = 0; i < wordsInInput.length; i++)
                {
                    System.out.println(wordsInInput[i]);
                }

                //replace word


                final TextView suggestionsTitle = (TextView) findViewById(R.id.suggestions_title);
                final ImageView editorIcon1 = (ImageView) findViewById(R.id.editor_icon_1);
                final ImageView editorIcon2 = (ImageView) findViewById(R.id.editor_icon_2);
                final ImageView editorIcon3 = (ImageView) findViewById(R.id.editor_icon_3);

                final TextView word;
                switch(targetWord)
                {
                    case 0:
                        word = (TextView) findViewById(R.id.input_text_split_0);
                        word.setText(wordToReplace);
                        break;
                }

                suggestionsTitle.setVisibility(View.INVISIBLE);
                editorIcon1.setVisibility(View.INVISIBLE);
                editorIcon2.setVisibility(View.VISIBLE);
                editorIcon3.setVisibility(View.VISIBLE);
            }

            catch (Exception e)
            {

            }
        }

        catch (Exception e)
        {

        }

        listeningForInput = false;
    }

    public void editorConfirmChange(View view)
    {
        wordsInInput[Integer.parseInt(wordToEdit)] = wordToReplace;
        System.out.println("Input after modification");
        for(int i = 0; i < wordsInInput.length; i++)
        {
            System.out.println(wordsInInput[i]);
        }

        reconstructSplitPhrase();
        transformInterface("stop_editing", Integer.parseInt(wordToEdit));
    }

    public void editorRejectChange (View view)
    {

    }

    public void reconstructSplitPhrase()
    {
        String reconstructedString = TextUtils.join(" ", wordsInInput);
        System.out.println("Reconstructed string is...");
        System.out.println(reconstructedString);
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
