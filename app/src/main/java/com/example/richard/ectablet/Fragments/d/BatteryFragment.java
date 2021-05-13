package com.example.richard.ectablet.Fragments.d;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.richard.ectablet.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.concurrent.TimeUnit;

public class BatteryFragment extends Fragment {

    public TextView txtRPM, txtTempMotor, txtPosAcelerador, txtVoltaje, txtBatteryLife, txtBatteryCurrent,
            txtSoh, txtCumulativeChar, txtCumulativeDisc;
    public ImageView imageViewCar;

    public float xCurrentPos, yCurrentPos;

    public float xCurrentPosVolt, yCurrentPosVolt;

    public ConstraintLayout layoutSquareBattery, layoutSquareBattery2;

    public LinearLayout batteryVolt, batteryTemp, rpmMotor, batterySoh, cumulativeChar, cumulativeDisc;
    CircularProgressBar circularProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_battery, container, false);

        imageViewCar = view.findViewById(R.id.imageViewCarVector);

        batteryVolt = view.findViewById(R.id.voltageBattery);
        batteryTemp = view.findViewById(R.id.tempBattery);
        rpmMotor = view.findViewById(R.id.rpmMotor);

        batterySoh = view.findViewById(R.id.batterySoh);
        cumulativeChar = view.findViewById(R.id.cumulativeChar);
        cumulativeDisc = view.findViewById(R.id.cumulativeDisc);

        layoutSquareBattery = view.findViewById(R.id.layoutSquareBattery);
        layoutSquareBattery2 = view.findViewById(R.id.layoutSquareBattery2);

        txtBatteryLife = view.findViewById(R.id.txtBatteryLife);

        txtVoltaje = view.findViewById(R.id.txtVoltaje);
        txtBatteryCurrent = view.findViewById(R.id.batteryCurrent);
        txtRPM = view.findViewById(R.id.txtRPM);

        txtSoh = view.findViewById(R.id.txtSoh);
        txtCumulativeChar = view.findViewById(R.id.txtCumulativeChar);
        txtCumulativeDisc = view.findViewById(R.id.txtCumulativeDisc);

        xCurrentPos = imageViewCar.getLeft();
        yCurrentPos = imageViewCar.getTop();

        circularProgressBar = view.findViewById(R.id.circularProgressBar);
        // Set Progress
        //circularProgressBar.setProgress(0f);


        // Set Progress Max
        circularProgressBar.setProgressMax(200f);
        // Set ProgressBar Color
        //circularProgressBar.setProgressBarColor(Color.BLACK);

        // or with gradient
        circularProgressBar.setProgressBarColorStart(Color.GREEN);
        circularProgressBar.setProgressBarColorEnd(Color.WHITE);
        circularProgressBar.setProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);

        // Set background ProgressBar Color
        circularProgressBar.setBackgroundProgressBarColor(Color.TRANSPARENT);

        circularProgressBar.setBackgroundProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);

        // Set Width
        circularProgressBar.setProgressBarWidth(20f); // in DP
        circularProgressBar.setBackgroundProgressBarWidth(3f); // in DP

        // Other
        circularProgressBar.setRoundBorder(true);
        circularProgressBar.setStartAngle(270f);
        circularProgressBar.setProgressDirection(CircularProgressBar.ProgressDirection.TO_RIGHT);

        //txtRPM = view.findViewById(R.id.txtRPM);
        //txtTempMotor = view.findViewById(R.id.txtTempMotor);
        //txtPosAcelerador = view.findViewById(R.id.txtPosAcelerador);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void putArguments(Bundle args2){
        //String velocidad = args.getString("VEL");
        //String rpm = args.getString("RPM");
        //String massAirFlow = args.getString("MASS_AIRFLOW");

        //txtRPM.setText(velocidad);
        //txtTempMotor.setText(rpm);
        //txtPosAcelerador.setText(massAirFlow);

        String voltaje = args2.getString("VOLTAJE");
        txtVoltaje.setText(voltaje + " V");
    }


    public void putArgumentsOBD2(Bundle args2){

        String batteryCurrentValue = args2.getString("BATTERYCURRENT");
        String batteryVoltageValue = args2.getString("BATERRYVOLTAGE");
        String cumulativeCharValue = args2.getString("CUMULATIVECHAR");
        String cumulativeDiscValue = args2.getString("CUMULATIVEDISC");
        String driveMotorSpd1Value = args2.getString("DRIVEMOTORSPD1");
        String stateOfChargedValue = args2.getString("STATEOFCHARGED");
        String stateOfHealthBValue = args2.getString("STATEOFHEALTHB");

        txtVoltaje.setText(batteryVoltageValue + " V");
        txtBatteryLife.setText(stateOfChargedValue+"%");
        txtBatteryCurrent.setText(batteryCurrentValue+ " A");
        txtRPM.setText(driveMotorSpd1Value);

        txtSoh.setText(stateOfHealthBValue+"%");
        txtCumulativeChar.setText(cumulativeCharValue);
        txtCumulativeDisc.setText(cumulativeDiscValue);
    }

    public void animationCar(){
        circularProgressBar.setProgress(0f);

        Animation anim = new TranslateAnimation(xCurrentPos, xCurrentPos, yCurrentPos,yCurrentPos-700);
        anim.setDuration(1000);
        anim.setFillAfter(true);
        anim.setFillEnabled(true);
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {}

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationEnd(Animation arg0) {

            }
        });
        imageViewCar.startAnimation(anim);

        // or with animation
        circularProgressBar.setProgressWithAnimation(100f, Long.valueOf(1000)); // =1s

    }

    public void animationIcons() throws InterruptedException {
        Animation anim = new TranslateAnimation(xCurrentPosVolt, xCurrentPosVolt - 535, yCurrentPosVolt, yCurrentPosVolt);
        anim.setDuration(1000);
        anim.setFillAfter(true);
        anim.setFillEnabled(true);
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {}

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationEnd(Animation arg0) {
            }
        });

        Animation anim2 = new TranslateAnimation(xCurrentPosVolt, xCurrentPosVolt + 620, yCurrentPosVolt, yCurrentPosVolt);
        anim2.setDuration(1000);
        anim2.setFillAfter(true);
        anim2.setFillEnabled(true);
        anim2.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {}

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationEnd(Animation arg0) {
            }
        });

        batteryVolt.startAnimation(anim);
        batteryTemp.startAnimation(anim);
        rpmMotor.startAnimation(anim);

        batterySoh.startAnimation(anim2);
        cumulativeChar.startAnimation(anim2);
        cumulativeDisc.startAnimation(anim2);

    }

    public void animationTextView(ValueAnimator valueAnimator, int duration){

        if(valueAnimator != null){
            valueAnimator.setDuration(duration);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float alpha = (float) animation.getAnimatedValue();
                    txtBatteryLife.setAlpha(alpha);
                }
            });
            valueAnimator.start();
        }
    }

    public void animationViews(ValueAnimator valueAnimator, int duration){

        if(valueAnimator != null){
            valueAnimator.setDuration(duration);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float alpha = (float) animation.getAnimatedValue();
                    layoutSquareBattery.setAlpha(alpha);
                    layoutSquareBattery2.setAlpha(alpha);
                }
            });
            valueAnimator.start();
        }
    }

}
