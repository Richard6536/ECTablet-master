package com.example.richard.ectablet.Clases.Components;

import android.graphics.Color;

import com.example.richard.ectablet.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class RPMView {
    CircularProgressBar circularProgressBar;
    public void DefineRPMProgress(CircularProgressBar circularProgressBarRPM) {
        circularProgressBar = circularProgressBarRPM;
        circularProgressBar.setProgressMax(16000f);
        // Set ProgressBar Color
        //circularProgressBar.setProgressBarColor(Color.BLACK);

        // or with gradient
        circularProgressBar.setProgressBarColorStart(Color.WHITE);
        circularProgressBar.setProgressBarColorEnd(Color.WHITE);
        circularProgressBar.setProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);

        // Set background ProgressBar Color
        circularProgressBar.setBackgroundProgressBarColor(Color.TRANSPARENT);

        circularProgressBar.setBackgroundProgressBarColorDirection(CircularProgressBar.GradientDirection.BOTTOM_TO_END);

        // Set Width
        circularProgressBar.setProgressBarWidth(18f); // in DP
        circularProgressBar.setBackgroundProgressBarWidth(3f); // in DP

        // Other
        circularProgressBar.setRoundBorder(true);
        circularProgressBar.setStartAngle(0f);
        circularProgressBar.setProgressDirection(CircularProgressBar.ProgressDirection.TO_RIGHT);
        circularProgressBar.setProgressWithAnimation(0f, Long.valueOf(1000));
    }

    public void setRPMProgress(float rpm){
        circularProgressBar.setProgressWithAnimation(rpm, Long.valueOf(200));

        if(rpm < 2000){
            circularProgressBar.setProgressBarColorStart(Color.WHITE);
            circularProgressBar.setProgressBarColorEnd(Color.WHITE);
        }
        else if(rpm >= 2500){
            circularProgressBar.setProgressBarColorStart(Color.YELLOW);
            circularProgressBar.setProgressBarColorEnd(Color.YELLOW);
        }
        else if(rpm >= 2800){
            circularProgressBar.setProgressBarColorStart(Color.RED);
            circularProgressBar.setProgressBarColorEnd(Color.RED);
        }
    }
}
