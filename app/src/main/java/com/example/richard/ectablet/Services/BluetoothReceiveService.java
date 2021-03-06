package com.example.richard.ectablet.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.example.richard.ectablet.Activity.MainActivity;
import com.example.richard.ectablet.Clases.Almacenamiento;
import com.example.richard.ectablet.Clases.ControllerActivity;
import com.example.richard.ectablet.Clases.Vehiculo;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class BluetoothReceiveService extends Service {

    private static String btAdress = "00:00:00:00:00:00";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //08C2B2EF-7C87-3D00-0CDC-9A2ADC420BFF
    public BluetoothDevice device;
    BluetoothServerSocket serverSocket;

    private static final int DISCOVERABLE_REQUEST_CODE = 0x1;
    private boolean CONTINUE_READ_WRITE = true;

    //-----------------
    int valContAcumulados = 0;
    double promedioExitoAPP = 0;
    double mayorTiempoAPP = 0;
    double menorTiempoAPP = 0;
    double promedioTiempoAPP = 0;
    int totalEnviadosAPP = 0;
    int totalErroresAPP = 0;
    int totalRegistradosAPP = 0;
    int acumulados = 0;
    private JSONArray jsonArrayEnviar = new JSONArray();
    private JSONArray jsonArrayRPIPromedios = new JSONArray();
    long actualMill = 0;


    public BluetoothReceiveService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)

                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();
        startForeground(1337, notification);

        new Thread(reader).start();
        //new ActualizarDatosEstadisticas().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "", "");
    }

    private BluetoothSocket socket;
    private InputStream is;
    private OutputStreamWriter os;

    /*
    private Runnable reader = new Runnable() {
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord("RosieProject", MY_UUID);
                Log.d("0092bluet","Listening...");
                Log.d("0092bluet","Socket accepted...");

                int bufferSize = 1008;
                int bytesRead = -1;
                int bytesFinalRead = 0;

                byte[] buffer = new byte[bufferSize];
                Log.d("0092bluet","Keep reading the messages while connection is open...");
                //Keep reading the messages while connection is open...
                while(CONTINUE_READ_WRITE){
                    socket = serverSocket.accept();
                    //addViewOnUiThread("TrackingFlow. Socket accepted...");
                    is = socket.getInputStream();
                    String result = "";
                    final StringBuilder sb = new StringBuilder();

                    bytesRead = is.read(buffer);
                    if (bytesRead != -1) {

                        while (bytesRead == bufferSize){
                            result = result + new String(buffer, 0, bytesRead);
                            bytesRead = is.read(buffer);
                        }

                        result = result + new String(buffer, 0, bytesRead);
                        sb.append(result);

                        os = new OutputStreamWriter(socket.getOutputStream());
                        os.write("ok");

                        os.flush();
                        os.close();

                    }

                    Log.d("0092bluet","Read: " + sb.toString());

                    try {

                        JSONArray jsonArrayEnviar = new JSONArray();
                        JSONArray jsonArray = new JSONArray(sb.toString());
                        String voltaje = "";
                        String fecha = "";

                        for(int x = 0; x <jsonArray.length(); x++){
                            String str = jsonArray.getString(x);
                            JSONObject jsonObject = new JSONObject(str);

                            fecha = jsonObject.getString("Fecha");
                            String estimacionSoc = jsonObject.getString("EstimacionSoc");
                            String confIntervalSoc1 = jsonObject.getString("ConfIntervalSoc1");
                            String confIntervalSoc2 = jsonObject.getString("ConfIntervalSoc2");
                            String estimacionSompa = jsonObject.getString("EstimacionSompa");
                            String confIntervalSompa1 = jsonObject.getString("ConfIntervalSompa1");
                            String confIntervalSompa2 = jsonObject.getString("ConfIntervalSompa2");
                            String estimacionRin = jsonObject.getString("EstimacionRin");
                            String confIntervalRin1 = jsonObject.getString("ConfIntervalRin1");
                            String confIntervalRin2 = jsonObject.getString("ConfIntervalRin2");
                            String corriente = jsonObject.getString("Corriente");
                            voltaje = jsonObject.getString("Voltaje");

                            JSONObject jsonObjectEnviar = new JSONObject();
                            jsonObjectEnviar.put("FechaHoraString", fecha);
                            jsonObjectEnviar.put("EstimacionSoc", estimacionSoc);
                            jsonObjectEnviar.put("ConfIntervalSoc1", confIntervalSoc1);
                            jsonObjectEnviar.put("ConfIntervalSoc2", confIntervalSoc2);
                            jsonObjectEnviar.put("EstimacionSompa", estimacionSompa);
                            jsonObjectEnviar.put("ConfIntervalSompa1", confIntervalSompa1);
                            jsonObjectEnviar.put("ConfIntervalSompa2", confIntervalSompa2);
                            jsonObjectEnviar.put("EstimacionRin", estimacionRin);
                            jsonObjectEnviar.put("ConfIntervalRin1", confIntervalRin1);
                            jsonObjectEnviar.put("ConfIntervalRin2", confIntervalRin2);
                            jsonObjectEnviar.put("Corriente", corriente);
                            jsonObjectEnviar.put("Voltaje", voltaje);

                            jsonArrayEnviar.put(jsonObjectEnviar);

                        }

                        int v = 0;
                        new Vehiculo.ActualizarPosicion().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jsonArrayEnviar.toString());
                        sendMessageToActivity(voltaje, fecha);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                is.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("0092bluet","Error: " + e.getMessage());
            }
        }
    };
    */

    private Runnable reader = new Runnable() {
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord("RosieProject", MY_UUID);
                Log.d("BTERR","Listening...");
                //addViewOnUiThread("TrackingFlow. Listening...");
                socket = serverSocket.accept();
                Log.d("BTERR","Socket accepted...");
                //addViewOnUiThread("TrackingFlow. Socket accepted...");
                is = socket.getInputStream();
                os = new OutputStreamWriter(socket.getOutputStream());
                //new Thread(writter).start();
                int bufferSize = 1008;
                int bytesRead = -1;
                int bytesFinalRead = 0;

                byte[] buffer = new byte[bufferSize];
                Log.d("BTERR","Keep reading the messages while connection is open...");

                //Keep reading the messages while connection is open...
                while(CONTINUE_READ_WRITE){

                    //socket = serverSocket.accept();
                    //addViewOnUiThread("TrackingFlow. Socket accepted...");
                    //is = socket.getInputStream();
                    String result = "";
                    final StringBuilder sb = new StringBuilder();

                    bytesRead = is.read(buffer);

                    if (bytesRead != -1) {

                        while (bytesRead == bufferSize){
                            result = result + new String(buffer, 0, bytesRead);
                            bytesRead = is.read(buffer);
                        }

                        result = result + new String(buffer, 0, bytesRead);
                        sb.append(result);
                    }

                    //os = new OutputStreamWriter(socket.getOutputStream());

                    //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    //String date = sdf.format(new Date());
                    //os.write("ok");

                    //os.flush();
                    //os.close();

                    //is.close();


                    //addViewOnUiThread("TrackingFlow. Read: " + sb.toString());
                    //Show message on UIThread

                    try {

                        JSONArray jsonArray = new JSONArray(sb.toString());
                        Log.d("BTERR","Read: " + jsonArray.toString());

                        int length = 0;
                        String voltaje = "";
                        String corriente = "";
                        String fecha = "";

                        for(int x = 0; x <jsonArray.length(); x++){
                            String str = jsonArray.getString(x);
                            JSONObject jsonObject = new JSONObject(str);

                            String rpm = jsonObject.getString("rpm");
                            String vel = jsonObject.getString("vel");
                            String soc = jsonObject.getString("soc");

                            JSONObject jsonObjectEnviar = new JSONObject();
                            jsonObjectEnviar.put("rpm", rpm);
                            jsonObjectEnviar.put("vel", vel);
                            jsonObjectEnviar.put("soc", soc);

                            jsonArrayEnviar.put(jsonObjectEnviar);

                            Log.e("BTERR","DATOS: " + jsonArrayEnviar);
                            //jsonArrayRPIPromedios.put(jsonObjectPromedios);

                            sendMessageToActivity(rpm, vel, soc);
                        }
                        /*
                        if(jsonArrayEnviar.length() >= 5){
                            Log.d("DAENVIAR", jsonArrayEnviar.toString());
                            acumulados = jsonArrayEnviar.length();
                            JSONArray registrosAEnviarList5 = jsonArrayEnviar;
                            //JSONArray registrosPromedios = jsonArrayRPIPromedios;

                            jsonArrayRPIPromedios = new JSONArray();
                            jsonArrayEnviar = new JSONArray();

                            actualMill = System.currentTimeMillis();

                            new Vehiculo.ActualizarPosicion().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, registrosAEnviarList5.toString());

                        }*/

                    }catch (JSONException e) {
                        Log.e("BTERR","Catch ReaCatch writeExcelFile 6der 2: " + e.getMessage());
                    }
                    //showData(sb);
                }

                //socket.close();
            } catch (IOException e) {
                Log.e("ThreadConnection_BT","Catch Reader 3: " + e.getMessage());
            }
        }
    };

    private void sendMessageToActivity(String rpm, String vel, String soc) {
        Intent intent = new Intent("intentKey");
        // You can also include some extra data.
        intent.putExtra("RPM", rpm);
        intent.putExtra("VEL", vel);
        intent.putExtra("SOC", soc);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /*
    public void readExcelFileFromAssets() throws IOException, BiffException, InterruptedException, JSONException {
        String[][] result = null;

        File root = new File(Environment.getExternalStorageDirectory(), "Android/data/com.stapp.ae.android.data");
        String excelFile = "datos_raspy_to_database.xls";

        Workbook workbook = Workbook.getWorkbook(new File(root, excelFile));
        Sheet sheet = workbook.getSheet(0);
        int rowCount = sheet.getRows();

        for (int i = 1; i < rowCount; i++) {
            Cell[] row = sheet.getRow(i);

            String voltaje = row[0].getContents();
            String corriente = row[1].getContents();

            new ActualizarDatosEstadisticas().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, voltaje, corriente);

            TimeUnit.SECONDS.sleep(1);
        }

    }

    public static class ActualizarDatosEstadisticas extends AsyncTask<String,String, JSONObject>
    {

        @Override
        protected JSONObject doInBackground(String... parametros) {

            String[][] result = null;

            File root = new File(Environment.getExternalStorageDirectory(), "Android/data/com.stapp.ae.android.data");
            String excelFile = "datos_raspy_to_database.xls";

            Workbook workbook = null;
            try {
                workbook = Workbook.getWorkbook(new File(root, excelFile));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BiffException e) {
                e.printStackTrace();
            }
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
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String voltaje = parametros[0];
            String corriente = parametros[1];

            JSONObject datos = new JSONObject();
            try {

                datos.put("voltaje", voltaje);
                datos.put("corriente", corriente);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return datos;
        }

        protected void onPostExecute(JSONObject respuestaOdata) {
            new ActualizarDatosEstadisticas().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "", "");
        }
    }

     */
}
