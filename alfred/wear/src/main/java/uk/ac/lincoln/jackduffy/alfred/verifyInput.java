package uk.ac.lincoln.jackduffy.alfred;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class verifyInput extends Activity
{
    public static boolean verificationInterrupted = false;
    int i=0;
    ProgressBar mProgressBar;
    CountDownTimer mCountDownTimer;

    String userData = null;
    private TextView inputTextDisplay;
    Integer numberOfSplitWords = 25;

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

    public void transformInterface(String mode, Integer option) {
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

        switch (mode) {
            case "init_editor":
                //region Initialize Editor
                if (background.getVisibility() == View.VISIBLE) {
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

                for (int i = (option + 1); i < numberOfSplitWords; i++)
                {
                    String element = "input_text_split_" + i;
                    TextView toRemove = (TextView) findViewById(res.getIdentifier(element, "id", getPackageName()));

                }

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
        String wordToEdit = view.getTag().toString();
        System.out.println("You tapped on word " + wordToEdit);
        transformInterface("edit_word", Integer.parseInt(wordToEdit));
    }
}
