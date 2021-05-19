package com.example.richard.ectablet.Activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.example.richard.ectablet.Clases.Almacenamiento;
import com.example.richard.ectablet.Clases.ConnectionController;
import com.example.richard.ectablet.Clases.ControllerActivity;
import com.example.richard.ectablet.R;
import com.example.richard.ectablet.Services.MyLocationService;
import com.example.richard.ectablet.Services.OBDService;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.richard.ectablet.Clases.ActionBarActivity;
import com.example.richard.ectablet.Clases.HideStatusBarNavigation;
import com.example.richard.ectablet.Clases.MapBoxManager;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.Clases.Vehiculo;
import com.example.richard.ectablet.Fragments.d.BatteryFragment;
import com.example.richard.ectablet.Fragments.d.MapFragment;
import com.example.richard.ectablet.Fragments.d.StatsFragment;
import com.example.richard.ectablet.Services.BluetoothReceiveService;
import com.example.richard.ectablet.Services.LocationService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.Mapbox;
import com.suke.widget.SwitchButton;

import java.io.File;
import java.io.IOException;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity{

    private static final int DISCOVERABLE_REQUEST_CODE = 2;

    public Intent locationIntent;

    HideStatusBarNavigation hideStatusBarNavigation = new HideStatusBarNavigation();
    public View mContentView;
    SessionManager sessionController;

    public ImageView imgViewGPS, imgViewWifi;

    final MapFragment mapFragment = new MapFragment();
    final BatteryFragment batteryFragment = new BatteryFragment();
    final StatsFragment statsFragment = new StatsFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = mapFragment;

    public TextView kmVelText, txtPatente, txtRecorridoRuta, tiempoRecorridoPorRuta, txtViewGPS,
            txtViewWifi, txtKMTotales;

    public LinearLayout txtEnRutaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ControllerActivity.activiyAbiertaActual = this;
        
        //Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), getString(R.string.access_token));

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sessionController = new SessionManager(getApplicationContext());
        mContentView = findViewById(R.id.container);

        ActionBarActivity actionBarActivity = new ActionBarActivity();
        actionBarActivity.view = mContentView;
        actionBarActivity.actionBar = getSupportActionBar();

        kmVelText = (TextView) findViewById(R.id.velocidad_km_text);
        txtPatente = (TextView) findViewById(R.id.txtPatente);
        txtKMTotales = (TextView) findViewById(R.id.txtKMTotales);

        txtEnRutaView = (LinearLayout) findViewById(R.id.txtEnRutaView);

        txtRecorridoRuta = (TextView) findViewById(R.id.txtKmtRecorridosPorRuta);
        txtEnRutaView.setVisibility(View.INVISIBLE);

        tiempoRecorridoPorRuta = (TextView) findViewById(R.id.tiempoRecorridoPorRuta);

        imgViewGPS = (ImageView)findViewById(R.id.imgViewGPS);
        txtViewGPS = (TextView) findViewById(R.id.txtViewGPS);

        imgViewWifi = (ImageView)findViewById(R.id.imgViewWifi);
        txtViewWifi = (TextView) findViewById(R.id.txtViewWifi);

        LinearLayout lLayoutCerrarSesion = (LinearLayout)findViewById(R.id.lLayoutCerrarSesion);
        lLayoutCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplication(), SettingsActivity.class));
                /*
                detenerServicios();

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

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MapBoxManager().findPosition();
            }
        });

        ImageView iv = (ImageView) findViewById(R.id.vector_battery_status);
        iv.animate().rotation(90).start();

        int width = 120;
        int height = 90;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
        iv.setLayoutParams(params);

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
        //Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        //startActivityForResult(discoverableIntent, DISCOVERABLE_REQUEST_CODE);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, DISCOVERABLE_REQUEST_CODE);
        }
        else{
            //iniciarServicioOBD2();
        }
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
        Intent intent = new Intent(getBaseContext(), OBDService.class);
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
            //Recibe datos directamente desde el BluetoothReceiveService
            //Se envían a los fragments correspondientes

            String voltaje = intent.getStringExtra("BATERRYVOLTAGE");
            String corriente = intent.getStringExtra("BATTERYCURRENT");

            double voltFloat = Float.parseFloat(voltaje);
            double currentFloat = Float.parseFloat(corriente);

            DecimalFormat df = new DecimalFormat("#.#");

            String decimalVolt = "0.0";
            String currentVolt = "0.0";

            try {
                decimalVolt = df.format(voltFloat);
                currentVolt = df.format(currentFloat);
            }
            catch (Exception e){

            }

            Bundle args = new Bundle();
            args.putString("VOLTAJE", decimalVolt);
            args.putString("CORRIENTE", currentVolt);

            statsFragment.putArguments(args);

        }
    };

    //****************BLUETOOTH******************
    //TODO: END BLUETOOTH

    private BroadcastReceiver mMessageLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String velocidad = intent.getStringExtra("VELOCIDAD");
            String distancia = intent.getStringExtra("DISTANCIA");
            String fecha = intent.getStringExtra("FECHA");

            boolean nuevaRuta = intent.getBooleanExtra("RUTA_NUEVA", false);

            try{

                Double kmDouble = Double.parseDouble(distancia) / 1000;
                String kmRecorridosDecimals =  new DecimalFormat("#.##").format(kmDouble);

                if(nuevaRuta){
                    txtEnRutaView.setVisibility(View.VISIBLE);
                }

                kmVelText.setText(velocidad);
                txtRecorridoRuta.setText(kmRecorridosDecimals + " km.");
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
}