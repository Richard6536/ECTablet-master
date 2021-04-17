package com.example.richard.ectablet.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.richard.ectablet.Clases.ConnectionController;
import com.example.richard.ectablet.Clases.ControllerActivity;
import com.example.richard.ectablet.Clases.HideStatusBarNavigation;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.R;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginV2Activity extends AppCompatActivity {

    //HideStatusBarNavigation hideStatusBarNavigation = new HideStatusBarNavigation();
    //public View mContentView;

    BroadcastReceiver broadcastReceiver;

    SessionManager sessionController;

    public EditText txtNombreUsuario, txtPassword;
    public ProgressBar progressBarLogin;
    public Button btnComenzarSesion;
    public LinearLayout connectionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_v2);

        ControllerActivity.activiyAbiertaActual = this;
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //mContentView = findViewById(R.id.container);

        txtNombreUsuario = (EditText)findViewById(R.id.editTextNombreUsuario);
        txtPassword = (EditText)findViewById(R.id.editTextPassword);
        progressBarLogin = (ProgressBar)findViewById(R.id.progressBarLogin);

        connectionView = (LinearLayout)findViewById(R.id.connectionViewId);

        btnComenzarSesion = findViewById(R.id.btnLoginDS);

        btnComenzarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtNombreUsuario.getText().toString().equals("") || txtPassword.getText().toString().equals("")){
                    //Debe llenar los campos
                }
                else{
                    JSONObject datos = new JSONObject();

                    try {

                        datos.put("NombreUsuario", txtNombreUsuario.getText());
                        datos.put("Password",txtPassword.getText());

                        progressBarLogin.setVisibility(View.VISIBLE);
                        btnComenzarSesion.setEnabled(false);

                        if(ConnectionController.checkConnection(getApplicationContext())){
                            //Enviar datos al webservice
                            new SessionManager.ValidarLogin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, datos.toString());
                        }
                        else{

                            connectionView.setVisibility(View.VISIBLE);
                            progressBarLogin.setVisibility(View.GONE);
                            btnComenzarSesion.setEnabled(true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //hideStatusBarNavigation.hideUI(mContentView, getSupportActionBar());
        installListener();
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {

        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    public void dataReceiveFromSessionManager(JSONObject str) {
        try {

            String respuesta = str.getString("TipoRespuesta");
            if(respuesta.equals("OK")){

                int flotaId = str.getInt("FlotaId");
                String listaAutos = str.getString("RespuestaArray");

                //viewPager.setCurrentItem(1);

                //Bundle args = new Bundle();
                //args.putInt("flotaId", flotaId);
                //args.putString("listaAutos", listaAutos);
                //seleccionarVehiculoFragment.putArguments(args);

                String nombreUsuario = txtNombreUsuario.getText().toString();
                String password = txtPassword.getText().toString();

                Intent intent = new Intent(LoginV2Activity.this, SeleccionarVehiculoActivity.class);
                intent.putExtra("flotaId", flotaId);
                intent.putExtra("txtNombreUsuario", nombreUsuario);
                intent.putExtra("txtPassword", password);
                intent.putExtra("listaAutos", listaAutos);
                startActivity(intent);
                finish();
            }
            else{
                progressBarLogin.setVisibility(View.GONE);
                btnComenzarSesion.setEnabled(true);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void installListener() {

        if (broadcastReceiver == null) {

            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    Bundle extras = intent.getExtras();

                    NetworkInfo info = (NetworkInfo) extras
                            .getParcelable("networkInfo");

                    NetworkInfo.State state = info.getState();
                    Log.d("TGD", info.toString() + " "
                            + state.toString());

                    if (state == NetworkInfo.State.CONNECTED) {

                        connectionView.setVisibility(View.GONE);
                        btnComenzarSesion.setEnabled(true);

                    } else {

                        connectionView.setVisibility(View.VISIBLE);
                        btnComenzarSesion.setEnabled(false);

                    }

                }
            };

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(broadcastReceiver, intentFilter);
        }
    }
}