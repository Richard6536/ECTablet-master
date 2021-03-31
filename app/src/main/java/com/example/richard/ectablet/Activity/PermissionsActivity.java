package com.example.richard.ectablet.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.richard.ectablet.Clases.Almacenamiento;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.Clases.Vehiculo;
import com.example.richard.ectablet.R;

import org.json.JSONArray;

import java.util.HashMap;

import afu.org.checkerframework.checker.nullness.qual.NonNull;

public class PermissionsActivity extends AppCompatActivity {

    public final int PERMISSION_ALL = 1;
    SessionManager sessionController;

    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE ,
            Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        sessionController = new SessionManager(getApplicationContext());

        Button btnSolicitarPermisos = (Button) findViewById(R.id.btnSolicitarPermisos);
        btnSolicitarPermisos.setEnabled(false);

        btnSolicitarPermisos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(PermissionsActivity.this, PERMISSIONS, PERMISSION_ALL);
            }
        });

        // Check if permissions are enabled and if not request
        int permissionReadStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED || permissionReadStorage != PackageManager.PERMISSION_GRANTED || permissionLocation != PackageManager.PERMISSION_GRANTED) {
            btnSolicitarPermisos.setEnabled(true);
        }
        else
        {
            Log.d("RESULT","PERMISOS PERMITIDOS");
            //Inicializa Mapas, posición & crea directorios si los permisos se han concedido
            startMainActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case PERMISSION_ALL:

                if (grantResults.length > 0) {

                    boolean ReadExternalStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteExternalStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean LocationPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (ReadExternalStoragePermission && WriteExternalStoragePermission && LocationPermission) {

                        Toast.makeText(PermissionsActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                        startMainActivity();
                    }
                    else {
                        Toast.makeText(PermissionsActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    public Boolean InicializarDirectorios(){

        HashMap<String, String> datos = sessionController.obtenerDatosUsuario();
        String vehiculoId = datos.get(SessionManager.KEY_VEHICULOID);

        Almacenamiento.crearDirectorio(vehiculoId);
        JSONArray listaPosicion = Almacenamiento.leerArchivo(vehiculoId, Almacenamiento.myFilePosicion.toString(), Almacenamiento.filePosicion);

        try {
            if (listaPosicion != null) {
                Vehiculo.listaDatosAEnviar = listaPosicion;
            }

        } catch (Exception e) {
            String err = e.getMessage();
        }

        return true;

    }

    public void startMainActivity(){
        if(InicializarDirectorios()){
            Intent intent = new Intent(PermissionsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
