package com.example.richard.ectablet.Activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.example.richard.ectablet.Clases.ConnectionController;
import com.example.richard.ectablet.Clases.ControllerActivity;
import com.example.richard.ectablet.R;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
import com.example.richard.ectablet.Clases.Almacenamiento;
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

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

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

    private static final int DISCOVERABLE_REQUEST_CODE = 0x1;
    //BroadcastReceiver broadcastReceiver;

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

    public TextView kmVelText, txtPatente, txtRecorridoRuta, tiempoRecorridoPorRuta, txtViewGPS, txtViewWifi, txtKMTotales;

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

        txtRecorridoRuta = (TextView) findViewById(R.id.txtKmtRecorridosPorRuta);
        tiempoRecorridoPorRuta = (TextView) findViewById(R.id.tiempoRecorridoPorRuta);

        imgViewGPS = (ImageView)findViewById(R.id.imgViewGPS);
        txtViewGPS = (TextView) findViewById(R.id.txtViewGPS);

        imgViewWifi = (ImageView)findViewById(R.id.imgViewWifi);
        txtViewWifi = (TextView) findViewById(R.id.txtViewWifi);

        LinearLayout lLayoutCerrarSesion = (LinearLayout)findViewById(R.id.lLayoutCerrarSesion);
        lLayoutCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detenerServicios();
                sessionController.logoutUser();
                finish();
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

        //Se solicitan permisos al usuario para hacer uso del Bluetooth

        //Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        //startActivityForResult(discoverableIntent, DISCOVERABLE_REQUEST_CODE);
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
                    fm.beginTransaction().hide(active).show(batteryFragment).commit();
                    active = batteryFragment;
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
        //addViewOnUiThread("TrackingFlow ");
        Log.d("BT","Creating thread to start listening...");

        //Intent intent0 = new Intent(getBaseContext(), LocationService.class);
        //Intent intent = new Intent(getBaseContext(), BluetoothReceiveService.class);
        //startService(intent);
        //startService(intent0);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Recibe datos directamente desde el BluetoothReceiveService
            //Se envían a los fragments correspondientes

            String voltaje = intent.getStringExtra("VOLTAJE");
            String corriente = intent.getStringExtra("CORRIENTE");

            String estimacionsompa = intent.getStringExtra("ESTIMACIONSOMPA");
            String confintervalsompa1 = intent.getStringExtra("CONFINTERVALSOMPA1");
            String confintervalsompa2 = intent.getStringExtra("CONFINTERVALSOMPA2");
            String fecha = intent.getStringExtra("FbluetoothECHA");

            Bundle args = new Bundle();
            args.putString("VOLTAJE", voltaje);
            args.putString("CORRIENTE", corriente);
            args.putString("ESTIMACIONSOMPA", estimacionsompa);
            args.putString("CONFINTERVALSOMPA1", confintervalsompa1);
            args.putString("CONFINTERVALSOMPA2", confintervalsompa2);

            args.putString("FECHA", fecha);

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
            try{
                Double kmDouble = Double.parseDouble(distancia) / 1000;

                String kmRecorridosDecimals =  new DecimalFormat("#.##").format(kmDouble);

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