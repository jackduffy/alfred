package uk.ac.lincoln.jackduffy.alfred;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

public class verifyInput extends Activity
{
    public static boolean verificationInterrupted = false;
    int i=0;
    ProgressBar mProgressBar;
    CountDownTimer mCountDownTimer;

    String userData = null;
    private TextView inputTextDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //create the instance of Alfred
        setContentView(R.layout.activity_verify); //set the layout


        Intent intent = getIntent();
        Bundle userInputData = intent.getExtras();

        if(userInputData != null)
        {
            userData = (String) userInputData.get("DATA:");
        }

        if(userData != null)
        {
            inputTextDisplay = (TextView)findViewById(R.id.input_text);
            System.out.println("INPUT = " + userData);
            inputTextDisplay.setText(userData);
            inputTextDisplay.bringToFront();

            mProgressBar=(ProgressBar)findViewById(R.id.progressbar);
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
                    //finish();
                }
            };
            mCountDownTimer.start();
        }

        else
        {
            finish();
        }




    }
}
