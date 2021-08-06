package com.example.richard.ectablet.Activity;

import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.example.richard.ectablet.Clases.Almacenamiento;
import com.example.richard.ectablet.Clases.ControllerActivity;
import com.example.richard.ectablet.R;
import com.example.richard.ectablet.Services.BluetoothReceiveService;
import com.example.richard.ectablet.Services.OBDService;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.richard.ectablet.Clases.ActionBarActivity;
import com.example.richard.ectablet.Clases.HideStatusBarNavigation;
import com.example.richard.ectablet.Clases.MapBoxManager;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.Fragments.d.BatteryFragment;
import com.example.richard.ectablet.Fragments.d.MapFragment;
import com.example.richard.ectablet.Fragments.d.StatsFragment;
import com.example.richard.ectablet.Services.LocationService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.Mapbox;
import com.suke.widget.SwitchButton;

import java.text.DecimalFormat;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int DISCOVERABLE_REQUEST_CODE = 2;

    public Intent locationIntent;
    public View layout_toolbar;
    public LinearLayout topBar;

    public FloatingActionButton fab;
    public View toolbarDividerView, toolbarDividerView2, toolbarDividerView3;

    HideStatusBarNavigation hideStatusBarNavigation = new HideStatusBarNavigation();
    public View mContentView;
    SessionManager sessionController;

    public ImageView imgViewGPS, imgViewWifi, imgViewBT, imgViewCloud, imgThemeSun, imgThemeMoon;

    //final NavigationFragment mapFragment = new NavigationFragment();
    final MapFragment mapFragment = new MapFragment();
    final BatteryFragment batteryFragment = new BatteryFragment();
    final StatsFragment statsFragment = new StatsFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = mapFragment;

    BottomNavigationView bottomNavigation;

    public static TextView txtCurrentStreet;

    public TextView kmVelText, txtPatente, tiempoRecorridoPorRuta, txtViewGPS,
            txtViewWifi, txtKMTotales, txtKMFont;

    public LinearLayout txtEnRutaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ControllerActivity.activiyAbiertaActual = this;
        
        //Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), getString(R.string.access_token));

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        fab = findViewById(R.id.floatingActionButton);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        sessionController = new SessionManager(getApplicationContext());
        mContentView = findViewById(R.id.container);

        layout_toolbar = findViewById(R.id.layout_toolbar);
        topBar = findViewById(R.id.topBar);

        ActionBarActivity actionBarActivity = new ActionBarActivity();
        actionBarActivity.view = mContentView;
        actionBarActivity.actionBar = getSupportActionBar();

        kmVelText = (TextView) findViewById(R.id.velocidad_km_text);
        txtPatente = (TextView) findViewById(R.id.txtPatente);
        txtKMTotales = (TextView) findViewById(R.id.txtKMTotales);

        txtEnRutaView = (LinearLayout) findViewById(R.id.txtEnRutaView);

        txtCurrentStreet = (TextView) findViewById(R.id.txtCurrentStreet);
        txtEnRutaView.setVisibility(View.INVISIBLE);

        tiempoRecorridoPorRuta = (TextView) findViewById(R.id.tiempoRecorridoPorRuta);

        imgViewGPS = (ImageView)findViewById(R.id.imgViewGPS);
        txtViewGPS = (TextView) findViewById(R.id.txtViewGPS);

        imgViewWifi = (ImageView)findViewById(R.id.imgViewWifi);
        txtViewWifi = (TextView) findViewById(R.id.txtViewWifi);

        toolbarDividerView = findViewById(R.id.toolbarDividerView);
        toolbarDividerView2 = findViewById(R.id.toolbarDividerView2);
        toolbarDividerView3 = findViewById(R.id.toolbarDividerView3);

        imgThemeSun = findViewById(R.id.imgThemeSun);
        imgThemeMoon = findViewById(R.id.imgThemeMoon);

        ImageView imgViewGPS = findViewById(R.id.imgViewGPS);
        ImageView imgViewWifi = findViewById(R.id.imgViewWifi);
        imgViewBT = findViewById(R.id.imgViewBT);
        imgViewCloud = findViewById(R.id.imgViewCloud);

        txtKMFont = findViewById(R.id.txtKMFont);

        LinearLayout lLayoutCerrarSesion = (LinearLayout)findViewById(R.id.lLayoutCerrarSesion);
        lLayoutCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //startActivity(new Intent(getApplication(), SettingsActivity.class));

                /*detenerServicios();

                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage("Write your message here.");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Sí",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                sessionController.logoutUser();
                                finish();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();*/
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MapBoxManager().findPosition();
            }
        });

        /*
        ImageView iv = (ImageView) findViewById(R.id.vector_battery_status);
        iv.animate().rotation(90).start();

        int width = 120;
        int height = 90;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
        iv.setLayoutParams(params);
*/
        //Define navegación inferior e iconos en la vista principal
        DefineBottomNavigationView();

        //Se agregan los fragments al FragmentManager
        AddFragmentsToBeginTransaction();

        HashMap<String, String> datos = sessionController.obtenerDatosUsuario();
        String vehiculoId = datos.get(SessionManager.KEY_VEHICULOID);
        Almacenamiento.crearDirectorio(vehiculoId);

        txtPatente.setText(datos.get(SessionManager.KEY_PATENTE));

        //Se inicia LocalBroadcastManager para que el BluetoothReceiveService envie datos a la actividad
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("intentKey"));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageLocationReceiver, new IntentFilter("intentKey2"));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                dataOBD2Receiver, new IntentFilter("intentKey_OBD2"));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                dataKeyOBD2, new IntentFilter("intentKeyOBD2"));

        //Se solicitan permisos al usuario para hacer uso del Bluetooth
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, DISCOVERABLE_REQUEST_CODE);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, DISCOVERABLE_REQUEST_CODE);
        }
        else{
            //iniciarServicioOBD2();
        }

        com.suke.widget.SwitchButton switchButton = (com.suke.widget.SwitchButton) findViewById(R.id.switch_button);

        HashMap<String, Boolean> datosTheme = sessionController.getTheme();
        boolean isNightTheme = datosTheme.get(SessionManager.KEY_THEME);
        if(isNightTheme)
        {
            MapBoxManager.styleString = "cknjd6of817xe17o7vjij0cjr";
            switchButton.setChecked(true);
        }
        else
        {
            MapBoxManager.styleString = "ck9vy0gt004u21in7mx9pe4nh";
            switchButton.setChecked(false);
        }

        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked)
                {
                    NightTheme();
                    MapBoxManager mapBoxManager = new MapBoxManager();
                    mapBoxManager.CambiarStyle("cknjd6of817xe17o7vjij0cjr");
                    sessionController.saveTheme(true);
                }
                else{
                    DayTheme();
                    MapBoxManager mapBoxManager = new MapBoxManager();
                    mapBoxManager.CambiarStyle("ck9vy0gt004u21in7mx9pe4nh");
                    sessionController.saveTheme(false);
                }
            }
        });
    }

    private void AddFragmentsToBeginTransaction(){
        fm.beginTransaction().add(R.id.container, statsFragment, "3").hide(statsFragment).commit();
        fm.beginTransaction().add(R.id.container, batteryFragment, "2").hide(batteryFragment).commit();
        fm.beginTransaction().add(R.id.container,mapFragment, "1").commit();
    }
    private void DefineBottomNavigationView(){
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {

            final View iconView = menuView.getChildAt(i).findViewById(com.google.android.material.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, displayMetrics);
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, displayMetrics);
            iconView.setLayoutParams(layoutParams);

        }

        navigation.requestLayout();
        navigation.setY(50f);

       /*
        navigation.setRotation(90f);
        //navigation.getLayoutParams().width=480;
        navigation.requestLayout();
        navigation.setY(-500f);
        navigation.setX(-910f);
        // navigation.requestLayout();
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i);
            iconView.setRotation(-90f);
        }
        */
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_map:
                    fm.beginTransaction().hide(active).show(mapFragment).commit();
                    active = mapFragment;
                    return true;

                case R.id.action_battery:

                    if(active != batteryFragment){
                        fm.beginTransaction().hide(active).show(batteryFragment).commit();
                        active = batteryFragment;
                        batteryFragment.animationCar();
                        try {
                            batteryFragment.animationIcons();
                            batteryFragment.animationTextView(ValueAnimator.ofFloat(0f, 1f), 1500);
                            batteryFragment.animationViews(ValueAnimator.ofFloat(0f, 1f), 1500);
                            batteryFragment.setSOCProgress();
                            
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    return true;

                case R.id.action_stats:
                    fm.beginTransaction().hide(active).show(statsFragment).commit();
                    active = statsFragment;
                    return true;
            }
            return false;
        }
    };

    private Intent getNotificationIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onResume() {
        super.onResume();

        hideStatusBarNavigation.hideUI(mContentView, getSupportActionBar());
        locationIntent = new Intent(MainActivity.this, LocationService.class);
        startService(locationIntent);

        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        registerReceiver(locationSwitchStateReceiver, filter);

        try{
            registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }catch (Exception e){
            // already registered
        }

        HashMap<String, Boolean> themeData = sessionController.getTheme();
        boolean isNightTheme = themeData.get(SessionManager.KEY_THEME);

        if(isNightTheme)
            NightTheme();
        else
            DayTheme();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(locationSwitchStateReceiver);
        try{
            unregisterReceiver(networkStateReceiver);
        }catch (Exception e){
            // already registered
        }
    }

    public void detenerServicios(){
        if(locationIntent != null)
            stopService(locationIntent);
    }

    public void iniciarServicioOBD2(){
        Intent intent = new Intent(getBaseContext(), BluetoothReceiveService.class);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //TODO: START BLUETOOTH
    //****************BLUETOOTH******************

    @Override
    public void onDestroy() {
        super.onDestroy();
        detenerServicios();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == -1){
            //iniciarServicioOBD2();
        }
        else{
            Log.d("BTRESULT","BT desactivado");
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                //Recibe datos directamente desde el BluetoothReceiveService
                //Se envían a los fragments correspondientes

                String rpm = intent.getStringExtra("RPM");
                String vel = intent.getStringExtra("VEL");
                String soc = intent.getStringExtra("SOC");
                String volt = intent.getStringExtra("VOLT");
                String tempMotor = intent.getStringExtra("TEMPMOTOR");
                String tempController = intent.getStringExtra("TEMPCONTROLLER");

                Log.d("BTRESULT","Recibiendo...");

                DecimalFormat df = new DecimalFormat("#.#");


                String tempMotorDecimal = "0.0";
                String tempControllerDecimal = "0.0";

                try {
                    tempMotorDecimal = df.format(Float.parseFloat(tempMotor));
                    tempControllerDecimal = df.format(Float.parseFloat(tempController));

                }
                catch (Exception e){
                    Log.d("ThreadConnection_BT", e.getMessage());
                }

                Bundle args = new Bundle();
                args.putString("RPM", rpm);
                args.putString("VELOCIDAD", vel);
                args.putString("SOC", soc);
                args.putString("VOLT", volt);
                args.putString("TEMPMOTOR", tempMotor);
                args.putString("TEMPCONTROLLER", tempController);


                batteryFragment.putArguments(args);
                kmVelText.setText(vel+"");
            }
            catch (Exception e){

            }
        }
    };

    //****************BLUETOOTH******************
    //TODO: END BLUETOOTH

    private BroadcastReceiver mMessageLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //String velocidad = intent.getStringExtra("VELOCIDAD");
            String distancia = intent.getStringExtra("DISTANCIA");
            String fecha = intent.getStringExtra("FECHA");

            boolean nuevaRuta = intent.getBooleanExtra("RUTA_NUEVA", false);

            try{

                Double kmDouble = Double.parseDouble(distancia) / 1000;
                String kmRecorridosDecimals =  new DecimalFormat("#.##").format(kmDouble);

                if(nuevaRuta){
                    txtEnRutaView.setVisibility(View.VISIBLE);
                }

                //kmVelText.setText(velocidad);
                //txtRecorridoRuta.setText(kmRecorridosDecimals + " km.");
                tiempoRecorridoPorRuta.setText(fecha);
            }
            catch (Exception e){

            }
        }
    };

    private BroadcastReceiver locationSwitchStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {

                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                Log.d("CNT", "Network: " + isNetworkEnabled + " - GPS: " + isGpsEnabled);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

                if (isGpsEnabled || isNetworkEnabled) {
                    imgViewGPS.setImageResource(R.drawable.ic_gps_on);
                    params.setMargins(0, 0, 0, 0);
                    txtViewGPS.setLayoutParams(params);
                    txtViewGPS.setText("");
                } else {
                    imgViewGPS.setImageResource(R.drawable.ic_gps_off);
                    params.setMargins(0, 0, 20, 0);
                    txtViewGPS.setLayoutParams(params);
                    txtViewGPS.setText("El GPS está apagado");
                }
            }
        }
    };

    private BroadcastReceiver dataOBD2Receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String velocidad = intent.getStringExtra("VEL");
            String rpm = intent.getStringExtra("RPM");
            String mass_airflow = intent.getStringExtra("MASS_AIRFLOW");

            Bundle args = new Bundle();
            args.putString("VEL", velocidad);
            args.putString("RPM", rpm);
            args.putString("MASS_AIRFLOW", mass_airflow);

            batteryFragment.putArguments(args);
        }
    };


    private BroadcastReceiver dataKeyOBD2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String batteryCurrentValue = intent.getStringExtra("BATTERYCURRENT");
            String batteryVoltageValue = intent.getStringExtra("BATERRYVOLTAGE");
            String cumulativeCharValue = intent.getStringExtra("CUMULATIVECHAR");
            String cumulativeDiscValue = intent.getStringExtra("CUMULATIVEDISC");
            String driveMotorSpd1Value = intent.getStringExtra("DRIVEMOTORSPD1");
            String stateOfChargedValue = intent.getStringExtra("STATEOFCHARGED");
            String stateOfHealthBValue = intent.getStringExtra("STATEOFHEALTHB");

            Bundle args = new Bundle();

            args.putString("BATTERYCURRENT", batteryCurrentValue + "");
            args.putString("BATERRYVOLTAGE", batteryVoltageValue + "");
            args.putString("CUMULATIVECHAR", cumulativeCharValue + "");
            args.putString("CUMULATIVEDISC", cumulativeDiscValue + "");
            args.putString("DRIVEMOTORSPD1", driveMotorSpd1Value + "");
            args.putString("STATEOFCHARGED", stateOfChargedValue + "");
            args.putString("STATEOFHEALTHB", stateOfHealthBValue + "");

            batteryFragment.putArgumentsOBD2(args);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    /*
    private void installListener() {

        if (broadcastReceiver == null) {

            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d("onRve","Receive");
                    Bundle extras = intent.getExtras();

                    NetworkInfo info = (NetworkInfo) extras
                            .getParcelable("networkInfo2");

                    NetworkInfo.State state = info.getState();

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    if (state == NetworkInfo.State.CONNECTED) {
                        imgViewWifi.setImageResource(R.drawable.ic_signal_wifi_4_bar_black_24dp);
                        params.setMargins(0, 0, 0, 0);
                        txtViewWifi.setLayoutParams(params);
                        txtViewWifi.setText("");

                    } else {
                        imgViewWifi.setImageResource(R.drawable.ic_wifi_off);
                        params.setMargins(0, 0, 20, 0);
                        txtViewWifi.setLayoutParams(params);
                        txtViewWifi.setText("La Red está desconectada");
                    }

                }
            };

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(broadcastReceiver, intentFilter);
        }
    }
    */

    public BroadcastReceiver networkStateReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            Log.d("onRve","Receive: " + ni);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

            if(ni != null){
                Log.d("onRve","NetworkInfo is not null");
                NetworkInfo.State state = ni.getState();
                if (state == NetworkInfo.State.CONNECTED) {
                    imgViewWifi.setImageResource(R.drawable.ic_signal_wifi_4_bar_black_24dp);
                    params.setMargins(0, 0, 0, 0);
                    txtViewWifi.setLayoutParams(params);
                    txtViewWifi.setText("");
                }
            }
            else{
                imgViewWifi.setImageResource(R.drawable.ic_wifi_off);
                params.setMargins(0, 0, 20, 0);
                txtViewWifi.setLayoutParams(params);
                txtViewWifi.setText("La Red está desconectada");
            }
        }
    };

    public void mostrarDatosPantalla(String kmRecorridos){

        String kmRecorridosDecimals =  new DecimalFormat("#.##").format(Double.parseDouble(kmRecorridos));
        Log.d("KMR", kmRecorridosDecimals);
        txtKMTotales.setText(kmRecorridosDecimals + " km.");
    }

    public void DayTheme(){

        int themeDayBackground = ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDay);
        int themeDayText = ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryBlack);
        int themeDayDivider = ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDivider);
        int themeDaySheetTransparent = ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDayTransparent);
        int themeDayWaves = ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDayWaves);
        int themeDaySquare = ContextCompat.getColor(MainActivity.this, R.color.colorSquareDay);

        //Side Toolbar
        layout_toolbar.setBackgroundColor(themeDayBackground);
        kmVelText.setTextColor(themeDayText);
        txtKMTotales.setTextColor(themeDayText);
        txtPatente.setTextColor(themeDayText);
        txtCurrentStreet.setTextColor(themeDayText);
        txtViewGPS.setTextColor(themeDayText);
        txtViewWifi.setTextColor(themeDayText);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color
                .parseColor("#E1E1E1")));

        Drawable myFabSrc = getResources().getDrawable(android.R.drawable.ic_menu_mylocation);
        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
        willBeWhite.mutate().setColorFilter(themeDayText, PorterDuff.Mode.MULTIPLY);

        bottomNavigation.setItemIconTintList(ColorStateList.valueOf(themeDayText));

        imgViewGPS.setColorFilter(themeDayText);
        imgViewWifi.setColorFilter(themeDayText);
        imgViewBT.setColorFilter(themeDayText);
        imgViewCloud.setColorFilter(themeDayText);

        imgThemeSun.setColorFilter(themeDayText);
        imgThemeMoon.setColorFilter(themeDayText);

        txtViewGPS.setTextColor(themeDayText);
        txtViewWifi.setTextColor(themeDayText);
        txtKMFont.setTextColor(themeDayText);

        GradientDrawable drawable = (GradientDrawable) bottomNavigation.getBackground();
        drawable.mutate();
        drawable.setColor(themeDayBackground);

        fab.setImageDrawable(willBeWhite);
        topBar.setBackgroundColor(themeDayBackground);

        toolbarDividerView.setBackgroundColor(themeDayDivider);
        toolbarDividerView2.setBackgroundColor(themeDayDivider);
        toolbarDividerView3.setBackgroundColor(themeDayDivider);

        GradientDrawable drawableSearch = (GradientDrawable) MapFragment.relativeDrawableSearch.getBackground();
        drawableSearch.mutate();
        drawableSearch.setColor(themeDayBackground);

        GradientDrawable drawableSheet = (GradientDrawable) MapFragment.topRouteSheet.getBackground();
        drawableSheet.mutate();
        drawableSheet.setColor(themeDaySheetTransparent);

        statsFragment.content_general_stats.setBackgroundColor(themeDayBackground);
        statsFragment.imgWaveStats.setBackgroundTintList(ColorStateList.valueOf(themeDaySquare));
        statsFragment.xAxisVoltaje.setTextColor(themeDayText);
        statsFragment.yAxisVoltaje.setTextColor(themeDayText);

        statsFragment.xAxisCorriente.setTextColor(themeDayText);
        statsFragment.yAxisCorriente.setTextColor(themeDayText);

        statsFragment.txtCorrienteText.setTextColor(themeDayText);
        statsFragment.txtVoltajeText.setTextColor(themeDayText);

        batteryFragment.content_general.setBackgroundColor(themeDayBackground);
        batteryFragment.imageViewCar.setImageResource(R.drawable.ic_car_vector_white);

        batteryFragment.txtBatteryLife.setTextColor(themeDayText);
        batteryFragment.txtTempMotor.setTextColor(themeDayText);
        batteryFragment.txtTempController.setTextColor(themeDayText);
        batteryFragment.txtSoh.setTextColor(themeDayText);
        batteryFragment.txtRPM.setTextColor(themeDayText);
        batteryFragment.txtVoltaje.setTextColor(themeDayText);
        batteryFragment.txtBatteryCurrent.setTextColor(themeDayText);

        batteryFragment.txtCumulativeDiscMensaje.setTextColor(themeDayText);
        batteryFragment.txtSohMensaje.setTextColor(themeDayText);
        batteryFragment.txtRPMMensaje.setTextColor(themeDayText);
        batteryFragment.txtCumulativeCharMensaje.setTextColor(themeDayText);
        batteryFragment.batteryCurrentMensaje.setTextColor(themeDayText);

        batteryFragment.layoutSquareBattery.setBackgroundTintList(ColorStateList.valueOf(themeDaySquare));
        batteryFragment.layoutSquareBattery2.setBackgroundTintList(ColorStateList.valueOf(themeDaySquare));

        batteryFragment.viewWavesBattery.setBackgroundTintList(ColorStateList.valueOf(themeDaySquare));

        /*
        VectorChildFinder vector = new VectorChildFinder(getApplicationContext(), R.drawable.ic_wave_battery_3, batteryFragment.viewWavesBattery);
        VectorDrawableCompat.VFullPath path1 = vector.findPathByName("pathWaves");
        path1.setFillColor(themeDayWaves);
        */

    }

    public void NightTheme(){

        int themeNightBackground = ContextCompat.getColor(MainActivity.this, R.color.colorPrimary);
        int themeNightText = ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDay);
        int themeDayDivider = ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDividerNight);
        int themeDaySheetTransparent = ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryTransparent);
        int themeDayWaves = ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDayWaves);
        int themeNightSquare = ContextCompat.getColor(MainActivity.this, R.color.colorSquareNight);

        //Side Toolbar
        layout_toolbar.setBackgroundColor(themeNightBackground);
        kmVelText.setTextColor(themeNightText);
        txtKMTotales.setTextColor(themeNightText);
        txtPatente.setTextColor(themeNightText);
        txtCurrentStreet.setTextColor(themeNightText);
        txtViewGPS.setTextColor(themeNightText);
        txtViewWifi.setTextColor(themeNightText);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color
                .parseColor("#273036")));

        Drawable myFabSrc = getResources().getDrawable(android.R.drawable.ic_menu_mylocation);
        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
        willBeWhite.mutate().setColorFilter(themeNightText, PorterDuff.Mode.MULTIPLY);

        bottomNavigation.setItemIconTintList(ColorStateList.valueOf(themeNightText));

        //mapFragment.img_navigation_icon.setColorFilter(themeNightText);
        //mapFragment.txtRouteInfo.setTextColor(themeNightText);

        imgViewGPS.setColorFilter(themeNightText);
        imgViewWifi.setColorFilter(themeNightText);
        imgViewBT.setColorFilter(themeNightText);
        imgViewCloud.setColorFilter(themeNightText);

        imgThemeSun.setColorFilter(themeNightText);
        imgThemeMoon.setColorFilter(themeNightText);

        txtViewGPS.setTextColor(themeNightText);
        txtViewWifi.setTextColor(themeNightText);
        txtKMFont.setTextColor(themeNightText);

        GradientDrawable drawable = (GradientDrawable) bottomNavigation.getBackground();
        drawable.mutate();
        drawable.setColor(themeNightBackground);

        fab.setImageDrawable(willBeWhite);
        topBar.setBackgroundColor(themeNightBackground);

        toolbarDividerView.setBackgroundColor(themeDayDivider);
        toolbarDividerView2.setBackgroundColor(themeDayDivider);
        toolbarDividerView3.setBackgroundColor(themeDayDivider);

        GradientDrawable drawableSearch = (GradientDrawable) MapFragment.relativeDrawableSearch.getBackground();
        drawableSearch.mutate();
        drawableSearch.setColor(themeNightBackground);

        GradientDrawable drawableSheet = (GradientDrawable) MapFragment.topRouteSheet.getBackground();
        drawableSheet.mutate();
        drawableSheet.setColor(themeDaySheetTransparent);

        statsFragment.content_general_stats.setBackgroundColor(themeNightBackground);
        statsFragment.imgWaveStats.setBackgroundTintList(ColorStateList.valueOf(themeNightSquare));

        statsFragment.xAxisVoltaje.setTextColor(themeNightText);
        statsFragment.yAxisVoltaje.setTextColor(themeNightText);
        statsFragment.xAxisCorriente.setTextColor(themeNightText);
        statsFragment.yAxisCorriente.setTextColor(themeNightText);

        statsFragment.txtCorrienteText.setTextColor(themeNightText);
        statsFragment.txtVoltajeText.setTextColor(themeNightText);

        batteryFragment.content_general.setBackgroundColor(themeNightBackground);
        batteryFragment.imageViewCar.setImageResource(R.drawable.ic_car_vector_2);

        batteryFragment.txtBatteryLife.setTextColor(themeNightText);
        batteryFragment.txtTempMotor.setTextColor(themeNightText);
        batteryFragment.txtTempController.setTextColor(themeNightText);
        batteryFragment.txtSoh.setTextColor(themeNightText);
        batteryFragment.txtRPM.setTextColor(themeNightText);
        batteryFragment.txtVoltaje.setTextColor(themeNightText);
        batteryFragment.txtBatteryCurrent.setTextColor(themeNightText);

        batteryFragment.txtCumulativeDiscMensaje.setTextColor(themeNightText);
        batteryFragment.txtSohMensaje.setTextColor(themeNightText);
        batteryFragment.txtRPMMensaje.setTextColor(themeNightText);
        batteryFragment.txtCumulativeCharMensaje.setTextColor(themeNightText);
        batteryFragment.batteryCurrentMensaje.setTextColor(themeNightText);

        batteryFragment.layoutSquareBattery.setBackgroundTintList(ColorStateList.valueOf(themeNightSquare));
        batteryFragment.layoutSquareBattery2.setBackgroundTintList(ColorStateList.valueOf(themeNightSquare));

        batteryFragment.viewWavesBattery.setBackgroundTintList(ColorStateList.valueOf(themeNightSquare));

        /*
        VectorChildFinder vector = new VectorChildFinder(getApplicationContext(), R.drawable.ic_wave_battery_3, batteryFragment.viewWavesBattery);
        VectorDrawableCompat.VFullPath path1 = vector.findPathByName("pathWaves");
        path1.setFillColor(themeDayWaves);
        */
    }

}