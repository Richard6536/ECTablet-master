package com.example.richard.ectablet.Clases.Components;

import android.graphics.Color;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class CurrentView {
    CircularProgressBar circularProgressBar;
    public void DefineCurrentProgress(CircularProgressBar circularProgressBarCRR) {
        circularProgressBar = circularProgressBarCRR;
        circularProgressBar.setProgressMax(100f);
        // Set ProgressBar Color
        //circularProgressBar.setProgressBarColor(Color.BLACK);

        // or with gradient
        circularProgressBar.setProgressBarColorStart(Color.WHITE);
        circularProgressBar.setProgressBarColorEnd(Color.RED);
        circularProgressBar.setProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);

        // Set background ProgressBar Color
        circularProgressBar.setBackgroundProgressBarColor(Color.TRANSPARENT);

        circularProgressBar.setBackgroundProgressBarColorDirection(CircularProgressBar.GradientDirection.BOTTOM_TO_END);

        // Set Width
        circularProgressBar.setProgressBarWidth(18f); // in DP
        circularProgressBar.setBackgroundProgressBarWidth(3f); // in DP

        // Other
        circularProgressBar.setRoundBorder(true);
        circularProgressBar.setStartAngle(90f);
        circularProgressBar.setProgressDirection(CircularProgressBar.ProgressDirection.TO_RIGHT);
        circularProgressBar.setProgressWithAnimation(0f, Long.valueOf(1000)); // =1s
    }

    public void setCurrentProgress(float value, TextView _txtCurrent) {
        circularProgressBar.setProgressWithAnimation(value, Long.valueOf(200)); // =1s
        _txtCurrent.setText(value + " A.");

        if (value > 0) {
            circularProgressBar.setProgressBarColorStart(Color.GREEN);
            circularProgressBar.setProgressBarColorEnd(Color.GREEN);
        }
        else if (value > 200) {
            circularProgressBar.setProgressBarColorStart(Color.YELLOW);
            circularProgressBar.setProgressBarColorEnd(Color.YELLOW);
        }
        else if (value > 250) {
            circularProgressBar.setProgressBarColorStart(Color.parseColor("#ff6f00"));
            circularProgressBar.setProgressBarColorEnd(Color.parseColor("#ff6f00"));
        }
        else if (value > 300) {
            circularProgressBar.setProgressBarColorStart(Color.RED);
            circularProgressBar.setProgressBarColorEnd(Color.RED);
        }else if (value < 0) {
            circularProgressBar.setProgressBarColorStart(Color.BLUE);
            circularProgressBar.setProgressBarColorEnd(Color.BLUE);
        }
    }
}
