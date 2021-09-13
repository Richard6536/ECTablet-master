package com.example.richard.ectablet.Fragments.d;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.richard.ectablet.Activity.SeleccionarVehiculoActivity;
import com.example.richard.ectablet.Clases.BatteryEqtns;
import com.example.richard.ectablet.Clases.ControllerActivity;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BatteryFragment extends Fragment {

    public static TextView txtVoltaje, txtBatteryLife,
            txtSoh, txtTempController, txtTempMotor;

    public static TextView txtSohMensaje;

    public static ImageView imageViewCar, imgTempMotor, imgTempController;

    public static float xCurrentPos, yCurrentPos;
    LineChart mChartCorriente;
    public static float xCurrentPosVolt, yCurrentPosVolt;


    public static ImageView viewWavesBattery;
    public static ConstraintLayout layoutSquareBattery, layoutSquareBattery2, content_general;
    public LinearLayout batteryVolt, batteryTemp, batterySoh, cumulativeChar,
            cumulativeDisc, maxTemp, minTemp;

    CircularProgressBar circularProgressBar;

    public static XAxis xAxisCorriente;
    public static YAxis yAxisCorriente;

    public float SOCActual = -1;

    public static long lastUpdateDate = 0;
    public static boolean whiling = true;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_battery, container, false);
        viewWavesBattery = view.findViewById(R.id.imgWaveBattery);
        imageViewCar = view.findViewById(R.id.imageViewCarVector);

        batteryVolt = view.findViewById(R.id.voltageBattery);
        batteryTemp = view.findViewById(R.id.tempBattery);

        batterySoh = view.findViewById(R.id.batterySoh);
        cumulativeChar = view.findViewById(R.id.cumulativeChar);
        cumulativeDisc = view.findViewById(R.id.cumulativeDisc);

        imgTempMotor = view.findViewById(R.id.imgTempMotor);
        imgTempController = view.findViewById(R.id.imgTempController);

        txtSohMensaje = view.findViewById(R.id.txtSohMensaje);

        layoutSquareBattery = view.findViewById(R.id.layoutSquareBattery);
        layoutSquareBattery2 = view.findViewById(R.id.layoutSquareBattery2);

        content_general = view.findViewById(R.id.content_general);

        txtBatteryLife = view.findViewById(R.id.txtBatteryLife);

        txtVoltaje = view.findViewById(R.id.txtVoltaje);

        txtSoh = view.findViewById(R.id.txtSoh);
        txtTempController = view.findViewById(R.id.txtTempController);
        txtTempMotor = view.findViewById(R.id.txtTempMotor);

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

        //txtTempMotor = view.findViewById(R.id.txtTempMotor);
        //txtPosAcelerador = view.findViewById(R.id.txtPosAcelerador);
        mChartCorriente = view.findViewById(R.id.chartCorriente);

        startChartCorriente();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SOCActual > 0)
            setSOCProgress();

    }
    //
    public void putArgumentsSTATUS(Bundle args2) throws InterruptedException {
        int status = args2.getInt("STATUS");
        if(status == 0){
            //VEHICULO APAGADO
            animationCar(250);
            animationIcons(0, 0);
            animationTextView(ValueAnimator.ofFloat(1f, 0f), 1500);
            animationViews(ValueAnimator.ofFloat(1f, 0f), 1500);
            circularProgressBar.setProgressWithAnimation(0, Long.valueOf(1000)); // =1s
            SOCActual = 0;
            txtBatteryLife.setText("");
        }
        else{
            animationCar(700);
            animationIcons(535, 620);
            animationTextView(ValueAnimator.ofFloat(0f, 1f), 1500);
            animationViews(ValueAnimator.ofFloat(0f, 1f), 1500);
            setSOCProgress();
        }

    }

    public void putArguments(Bundle args2) {

        String rpm = args2.getString("RPM");
        String soc = args2.getString("SOC");
        String volt = args2.getString("VOLT");
        String tempMotor = args2.getString("TEMPMOTOR");
        String tempController = args2.getString("TEMPCONTROLLER");
        String current = args2.getString("CURRENT");

        float currentD = 0;
        double tempMotorD = 0.0;
        double tempControllerD = 0.0;

        int neutroCurrent = 0;

        try {
            double current_dd = Double.parseDouble(current);

            if(current_dd > 0){
                neutroCurrent = 0;
            }
            else if(current_dd < 0){
                neutroCurrent = 2;
            }
            else {
                neutroCurrent = 1;
            }

            currentD = Float.parseFloat(current);
            tempMotorD = BatteryEqtns.round(Double.parseDouble(tempMotor), 2) / 10;
            tempControllerD = BatteryEqtns.round(Double.parseDouble(tempController), 2);
        }
        catch (Exception e){

        }

        /*
        if(isCarCharging(0)){
            imageViewCar.setImageResource(R.drawable.ic_car_vector_2_charging);
        }
        else{
            imageViewCar.setImageResource(R.drawable.ic_car_vector_2);
        }*/

        addEntry(currentD, neutroCurrent);
        txtBatteryLife.setText(soc+"%");
        txtVoltaje.setText(volt + " V");
        txtTempMotor.setText(0 + " C°");
        txtTempController.setText(0 + " C°");

        try
        {
            float socFloat = Float.parseFloat(soc);

            if(SOCActual != socFloat){
                SOCActual = socFloat;
                setSOCProgress();
            }
        }
        catch (Exception e){ }
    }

    public void setSOCProgress(){

        circularProgressBar.setProgressWithAnimation(SOCActual, Long.valueOf(1000)); // =1s

        if(SOCActual > 45.0){
            circularProgressBar.setProgressBarColorStart(Color.GREEN);
            circularProgressBar.setProgressBarColorEnd(Color.WHITE);
        }
        else if(SOCActual > 25.0 && SOCActual <= 45.0){
            circularProgressBar.setProgressBarColorStart(Color.YELLOW);
            circularProgressBar.setProgressBarColorEnd(Color.WHITE);
        }
        else if(SOCActual <= 25.0){
            circularProgressBar.setProgressBarColorStart(Color.RED);
            circularProgressBar.setProgressBarColorEnd(Color.WHITE);
        }
    }


    public void putArgumentsOBD2(Bundle args2){

        String batteryCurrentValue = args2.getString("BATTERYCURRENT");
        String batteryVoltageValue = args2.getString("BATERRYVOLTAGE");
        String cumulativeCharValue = args2.getString("CUMULATIVECHAR");
        String cumulativeDiscValue = args2.getString("CUMULATIVEDISC");
        String driveMotorSpd1Value = args2.getString("DRIVEMOTORSPD1");
        String stateOfChargedValue = args2.getString("STATEOFCHARGED");
        String stateOfHealthBValue = args2.getString("STATEOFHEALTHB");

        try {
            float socFloat = Float.parseFloat(stateOfChargedValue);
            circularProgressBar.setProgressWithAnimation(socFloat, Long.valueOf(1000)); // =1s

            if(socFloat > 45.0){
                circularProgressBar.setProgressBarColorStart(Color.GREEN);
                circularProgressBar.setProgressBarColorEnd(Color.WHITE);
            }
            else if(socFloat > 25.0 && socFloat <= 45.0){
                circularProgressBar.setProgressBarColorStart(Color.YELLOW);
                circularProgressBar.setProgressBarColorEnd(Color.WHITE);
            }
            else if(socFloat <= 25.0){
                circularProgressBar.setProgressBarColorStart(Color.RED);
                circularProgressBar.setProgressBarColorEnd(Color.WHITE);
            }
        }
        catch (Exception e){ }

        txtVoltaje.setText(batteryVoltageValue + " V");
        txtBatteryLife.setText(stateOfChargedValue+"%");

        txtSoh.setText(stateOfHealthBValue+"%");
    }

    public void animationCar(int val){

        Animation anim = new TranslateAnimation(xCurrentPos, xCurrentPos, yCurrentPos, yCurrentPos - val);
        circularProgressBar.setProgress(0f);
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
        circularProgressBar.setProgressWithAnimation(0f, Long.valueOf(1000)); // =1s

    }

    public void animationCarSemiHide() throws InterruptedException {
        //circularProgressBar.setProgressWithAnimation(0f, Long.valueOf(1000)); // =1s
        float ycp = yCurrentPos + 250;
        Animation anim = new TranslateAnimation(xCurrentPos, xCurrentPos, ycp, yCurrentPos);
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
        //circularProgressBar.setProgressWithAnimation(0f, Long.valueOf(1000)); // =1s

    }

    public void animationIcons(int val, int val2) throws InterruptedException {

        Animation anim = new TranslateAnimation(xCurrentPosVolt, xCurrentPosVolt - val, yCurrentPosVolt, yCurrentPosVolt);
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

        Animation anim2 = new TranslateAnimation(xCurrentPosVolt, xCurrentPosVolt + val2, yCurrentPosVolt, yCurrentPosVolt);
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

        batteryVolt.startAnimation(anim2);
        //batteryTemp.startAnimation(anim);

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

    private void addEntry(float corriente, int color) {
        entryCorriente(corriente, color);

        //entryEstimacionRin(estimacionSompa, confIntervalSompa1, confIntervalSompa2);
        //entryConfIntervalRin1(corriente);
    }

    boolean desdePositivo = false;
    boolean desdeNegativo = false;

    private void entryCorriente(float val, int currentPos){

        LineData data = mChartCorriente.getData();

        if (data != null) {

            ILineDataSet positiveSet = data.getDataSetByIndex(0);
            ILineDataSet negativeSet = data.getDataSetByIndex(1);

            if(positiveSet == null)
            {
                positiveSet = createSetCorrientePositive();
                data.addDataSet(positiveSet);
                positiveSet.setDrawFilled(true);
            }
            if(negativeSet == null)
            {
                negativeSet = createSetCorrienteNegative();
                data.addDataSet(negativeSet);
                negativeSet.setDrawFilled(true);
            }

            if(currentPos == 0) {
                if(desdeNegativo)
                {
                    desdeNegativo = false;
                    data.addEntry(new Entry(data.getEntryCount(), 0), 0);
                    data.addEntry(new Entry(data.getEntryCount(), 0), 0);
                }

                data.addEntry(new Entry(data.getEntryCount(), 0), 1);
                data.addEntry(new Entry(data.getEntryCount(), val), 0);
                desdePositivo = true;
            }
            else if(currentPos == 1) {
                //data.addEntry(new Entry(data.getEntryCount(), 0), 1);
                data.addEntry(new Entry(data.getEntryCount(), val), 0);
                desdeNegativo = false;
                desdePositivo = false;

            }
            else if(currentPos == 2) {
                if(desdePositivo)
                {
                    desdePositivo = false;
                    data.addEntry(new Entry(data.getEntryCount(), 0), 1);
                    data.addEntry(new Entry(data.getEntryCount(), 0), 1);
                }


                data.addEntry(new Entry(data.getEntryCount(), 0), 0);
                data.addEntry(new Entry(data.getEntryCount(), val), 1);
                desdeNegativo = true;
            }

            //mChart.setData(data);
            // let the graph know it's data has changed
            mChartCorriente.notifyDataSetChanged();
            data.notifyDataChanged();


            //mChart.invalidate();
            // limit the number of visible entries
            //mChart.setVisibleXRangeMaximum(120);

            // move to the latest entry
            mChartCorriente.moveViewToX(data.getEntryCount());
            Log.d("____EE", data.getEntryCount() + "");
        }
    }

    private LineDataSet createSetCorrientePositive() {

        LineDataSet set = new LineDataSet(null, "Corriente +");
        set.setDrawCircles(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.RED);
        set.setCircleColor(Color.RED);
        set.setLineWidth(1f);
        set.setCircleRadius(4f);
        set.setFillAlpha(255);
        set.setFillColor(Color.RED);
        set.setHighLightColor(Color.RED);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSetCorrienteNegative() {

        LineDataSet set = new LineDataSet(null, "Corriente -");
        set.setDrawCircles(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.GREEN);
        set.setCircleColor(Color.GREEN);
        set.setLineWidth(1f);
        set.setCircleRadius(4f);
        set.setFillAlpha(255);
        set.setFillColor(Color.GREEN);
        set.setHighLightColor(Color.GREEN);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    public void startChartCorriente(){

        // enable description text
        mChartCorriente.getDescription().setEnabled(false);

        // enable touch gestures
        mChartCorriente.setTouchEnabled(true);

        // enable scaling and dragging
        mChartCorriente.setDragEnabled(true);
        mChartCorriente.setScaleEnabled(true);
        mChartCorriente.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChartCorriente.setPinchZoom(false);

        // set an alternative background color
        mChartCorriente.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChartCorriente.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChartCorriente.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        xAxisCorriente = mChartCorriente.getXAxis();
        xAxisCorriente.setTextColor(Color.WHITE);
        xAxisCorriente.setDrawGridLines(false);
        xAxisCorriente.setAvoidFirstLastClipping(true);
        xAxisCorriente.setEnabled(true);

        yAxisCorriente = mChartCorriente.getAxisLeft();
        yAxisCorriente.setTextColor(Color.WHITE);
        yAxisCorriente.setDrawGridLines(true);

        YAxis rightAxis = mChartCorriente.getAxisRight();
        rightAxis.setEnabled(false);
    }

    public boolean isCarCharging(int bit){
        if(bit == 0) return false;
        else return true;
    }
}
