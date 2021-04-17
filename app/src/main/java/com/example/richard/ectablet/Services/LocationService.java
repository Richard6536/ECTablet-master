package com.example.richard.ectablet.Services;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import android.util.Log;

import com.example.richard.ectablet.Activity.MainActivity;
import com.example.richard.ectablet.Clases.Almacenamiento;
import com.example.richard.ectablet.Clases.ControllerActivity;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.Clases.Vehiculo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import static com.android.volley.VolleyLog.TAG;

public class LocationService extends Service {

    LocationManager locationManager;
    LocationListener locationListener = new LocationListener();
    SessionManager sessionController;

    public static Location lastLocation;
    public float distanciaAcumulada = 0;

    String LLAVE, vehiculoId, flotaId, estadoRuta;
    String fechaInicio = "-";

    public LocationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("POSITION_TAG", "onCreate");
        try{
            Intent notificationIntent = new Intent(LocationService.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(LocationService.this, 0,
                    notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this)

                    .setContentTitle("My Awesome App")
                    .setContentText("Doing some work...")
                    .setContentIntent(pendingIntent).build();
            startForeground(1456, notification);

        }
        catch (Exception e){
            e.printStackTrace();
        }

        sessionController = new SessionManager(getApplicationContext());
        HashMap<String, String> datos = sessionController.obtenerDatosUsuario();

        LLAVE = datos.get(SessionManager.KEY);
        vehiculoId = datos.get(SessionManager.KEY_VEHICULOID);
        flotaId = datos.get(SessionManager.KEY_FLOTAID);


        try
        {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);

            new ActualizarDatosEstadisticas().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "", "");
        }
        catch (Exception e)
        {
            String mensajeError = e.getMessage();
            int a = 0;
        }
    }

    private class LocationListener implements android.location.LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            float accuracyPosicion = location.getAccuracy();
            Log.e("POSITION_TAG", "lnlat: " + location.getLatitude() + "," + location.getLongitude());
            enviarDatosVehiculo(location);

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    public void enviarDatosVehiculo(Location location){

        JSONObject datosVehiculo = new JSONObject();

        HashMap<String, String> datos_usuario = sessionController.obtenerDatosUsuario();
        estadoRuta = datos_usuario.get(SessionManager.KEY_ESTADO_RUTA);
        boolean rutaNueva = Boolean.parseBoolean(estadoRuta);

        int velocidadKm = (int) ((location.getSpeed() * 3600) / 1000);

        try {

            datosVehiculo.put("FechaHoraString", FormatDate());
            datosVehiculo.put("MetrosTramo",0);
            datosVehiculo.put("Latitud", location.getLatitude());
            datosVehiculo.put("Longitud", location.getLongitude());

            Log.d("VLCD", location.getLatitude() + " - " +location.getLongitude());

            if(rutaNueva == true){
                fechaInicio = FormatDate();
                datosVehiculo.put("Inicio", rutaNueva);
                sessionController.levantarSesion("", 0, 0, "", "false");
            }

            datosVehiculo.put("GPSOffBool",false);
            datosVehiculo.put("Altitud", location.getAltitude());
            datosVehiculo.put("Velocidad",velocidadKm);
            datosVehiculo.put("Aceleracion",0);

            JSONArray ja = new JSONArray();
            ja.put(datosVehiculo);

            //Almacenamiento.crearRegistroErroresPosicion("Paso_enviarDatosVehiculo",ja);
            //Enviar datos al webservice
            new Vehiculo.ActualizarDatos().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ja.toString(), vehiculoId, LLAVE, flotaId);

            if(lastLocation != location) {
                distanciaEnMetros(location);
            }

            sendMessageToActivity(velocidadKm, distanciaAcumulada, fechaInicio);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String FormatDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");

        String seg = "";
        String min = "";
        String hor = "";
        String diaS = "";
        String mesS = "";

        //dd/MM/yyyy kk:mm:ss
        Calendar r = Calendar.getInstance();
        int segundos = r.get(Calendar.SECOND);
        int minutos = r.get(Calendar.MINUTE);
        int hora = r.get(Calendar.HOUR_OF_DAY);
        int dia = r.get(Calendar.DAY_OF_MONTH);
        int mes = (r.get(Calendar.MONTH)+1);
        int anio = r.get(Calendar.YEAR);

        if(hora == 24)
        {
            hor = "00";
        }

        else if(hora <= 9)
        {
            hor = "0"+hora;
        }
        else
        {
            hor = hora+"";
        }

        if(minutos <= 9)
        {
            min = "0"+minutos;
        }
        else
        {
            min = minutos+"";
        }

        if(segundos <= 9)
        {
            seg = "0"+segundos;
        }
        else
        {
            seg = segundos+"";
        }

        if(dia <= 9)
        {
            diaS = "0"+dia;
        }
        else
        {
            diaS = dia+"";
        }

        if(mes <= 9)
        {
            mesS = "0"+mes;
        }
        else
        {
            mesS = mes+"";
        }


        String fecha = diaS+"/"+mesS+"/"+anio+" "+hor+":"+min+":"+seg;

        return fecha;
    }

    public void distanciaEnMetros(Location location){

        if(lastLocation == null)
            lastLocation = location;

        Location loc1 = new Location("");

        loc1.setLatitude(lastLocation.getLatitude());
        loc1.setLongitude(lastLocation.getLongitude());

        Location loc2 = new Location("");
        loc2.setLatitude(location.getLatitude());
        loc2.setLongitude(location.getLongitude());

        distanciaAcumulada += loc1.distanceTo(loc2);

        lastLocation = location;
    }

    private void sendMessageToActivity(int velocidad, float distanciaAcumulada, String fecha) {

        Log.d("VLCD", velocidad+"");

        Intent intent = new Intent("intentKey2");
        // You can also include some extra data.
        intent.putExtra("VELOCIDAD", velocidad+"");
        intent.putExtra("DISTANCIA", distanciaAcumulada+"");

        String[] splFecha = fecha.split(" ");
        String hora = "";

        if(splFecha.length == 2)
            hora = splFecha[1];

        intent.putExtra("FECHA", hora);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    public static class ActualizarDatosEstadisticas extends AsyncTask<String,String, JSONObject>
    {

        @Override
        protected JSONObject doInBackground(String... parametros) {

            String[][] result = null;

            File root = new File(Environment.getExternalStorageDirectory(), "Android/data/com.stapp.ae.android.data");
            String excelFile = "datos_raspy_to_database.xls";

            Workbook workbook = null;

            JSONObject datos = new JSONObject();

            try {
                workbook = Workbook.getWorkbook(new File(root, excelFile));

                Sheet sheet = workbook.getSheet(0);
                int rowCount = sheet.getRows();

                for (int i = 1; i < rowCount; i++) {
                    Cell[] row = sheet.getRow(i);

                    String corriente = row[0].getContents();
                    String voltaje = row[1].getContents();

                    Intent intent = new Intent("intentKey");
                    // You can also include some extra data.
                    intent.putExtra("VOLTAJE", voltaje);
                    intent.putExtra("CORRIENTE",  corriente);

                    intent.putExtra("ESTIMACIONSOMPA", "0");
                    intent.putExtra("CONFINTERVALSOMPA1", "0");
                    intent.putExtra("CONFINTERVALSOMPA2", "0");
                    intent.putExtra("FECHA", "0");

                    LocalBroadcastManager.getInstance(ControllerActivity.activiyAbiertaActual).sendBroadcast(intent);

                    try {
                        TimeUnit.MILLISECONDS.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String voltaje = parametros[0];
                String corriente = parametros[1];


                try {

                    datos.put("voltaje", voltaje);
                    datos.put("corriente", corriente);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return datos;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (BiffException e) {
                e.printStackTrace();
            }

            try {

                datos.put("voltaje", 0);
                datos.put("corriente", 0);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return datos;
        }

        protected void onPostExecute(JSONObject respuestaOdata) {
            new ActualizarDatosEstadisticas().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "", "");
        }
    }
}
