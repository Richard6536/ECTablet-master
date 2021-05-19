package com.example.richard.ectablet.Services;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.icu.text.Transliterator;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import android.util.Log;
import android.widget.EditText;

import com.example.richard.ectablet.Activity.MainActivity;
import com.example.richard.ectablet.Clases.Almacenamiento;
import com.example.richard.ectablet.Clases.ControllerActivity;
import com.example.richard.ectablet.Clases.HexData;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.Clases.Vehiculo;
import com.example.richard.ectablet.R;
import com.github.pires.obd.commands.protocol.ObdRawCommand;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.android.volley.VolleyLog.TAG;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class LocationService extends Service {

    Timer timer = new Timer();
    Thread threadBLT;

    LocationManager locationManager;
    LocationListener locationListener = new LocationListener();
    SessionManager sessionController;

    public String dateTracklogs = "--";

    public static Location lastLocation;
    public static Location updatedLocation;
    public boolean puntoEnviado = false;

    public float distanciaAcumulada = 0;

    String LLAVE, vehiculoId, flotaId, estadoRuta;
    String fechaInicio = "-";

    public BluetoothSocket socket;
    private InputStream is;
    private OutputStreamWriter os;
    private OutputStream ostream;

    public static String btAdress = "0C:FC:85:19:74:C2";
    private static final UUID UUID_BT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothDevice device;
    BluetoothServerSocket serverSocket;
    public String TAG = "OBDRESULT";

    public double socGlobal = 0.0;
    public int rpmGlobal = 0;
    public double batteryVoltageGlobal = 0.0;
    public double batteryCurrentGlobal = 0.0;
    public int sohGlobal = 0;
    public double cumulativeChCurrentGlobal = 0.0;
    public double cumulativeDischCurrentGlobal = 0.0;

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

        try{
            timer.cancel();
            locationManager.removeUpdates(locationListener);
            socket.close();
            is.close();
            ostream.close();

            threadBLT.interrupt();
        }
        catch (Exception e){}

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

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            //new ActualizarDatosEstadisticas().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "", "");
            //new OBDLogs().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "", "");

            //Para leer directamente
            //threadBLT = new Thread(reader);
            //threadBLT.start();
            //new sendData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "", "");

            //Para usar torque
            new writeOBD2ExcelAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "", "");


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
            updatedLocation = location;
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

    public void enviarDatosVehiculo(Location location, double batteryCurrentValue, double batteryVoltageValue, double cumulativeCharValue,
                                    double cumulativeDiscValue, double driveMotorSpd1Value, double stateOfChargedValue,
                                    double stateOfHealthBValue, String puntoDestino){

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

            if(rutaNueva == true){
                fechaInicio = FormatDate();
                datosVehiculo.put("Inicio", rutaNueva);
                sessionController.levantarSesion("", 0, 0, "", "false");
            }

            datosVehiculo.put("GPSOffBool",false);
            datosVehiculo.put("Altitud", location.getAltitude());
            datosVehiculo.put("Velocidad",velocidadKm);
            datosVehiculo.put("Aceleracion",0);

            datosVehiculo.put("VelocidadRPM", 0);
            datosVehiculo.put("BatteryCurrent_A", batteryCurrentValue);
            datosVehiculo.put("Battery_DC_Voltage_V", batteryVoltageValue);
            datosVehiculo.put("CumulativeChargeCurrent_Ah", cumulativeCharValue);
            datosVehiculo.put("CUmulativeDischargeCurrent_Ah", cumulativeDiscValue);
            datosVehiculo.put("RPM", driveMotorSpd1Value);
            datosVehiculo.put("StateOfCharge_SOC", stateOfChargedValue);
            datosVehiculo.put("StateOfHealth_SOH", stateOfHealthBValue);
            datosVehiculo.put("NombrePunto", puntoDestino);

            JSONArray ja = new JSONArray();
            ja.put(datosVehiculo);

            //Almacenamiento.crearRegistroErroresPosicion("Paso_enviarDatosVehiculo",ja);
            //Enviar datos al webservice
            new Vehiculo.ActualizarDatos().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ja.toString(), vehiculoId, LLAVE, flotaId);

            if(isEmpty(puntoDestino) == false){
                puntoEnviado = true;
            }

            if(lastLocation != location) {
                distanciaEnMetros(location);
            }

            sendMessageToActivity(velocidadKm, distanciaAcumulada, fechaInicio, rutaNueva);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String checkPointsNearPosition(Location position){

        try {

            String nearPointName = "";

            SessionManager session = new SessionManager(getApplicationContext());
            Map<String, String> data = session.getPoints();

            if(data.get(SessionManager.KEY_POINTS) != null){
                String dataPoints = data.get(SessionManager.KEY_POINTS).replaceAll("\\\\", "");

                JSONArray jsonArray = new JSONArray(dataPoints);

                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);

                    String nombre = json_data.getString("Name");
                    String latitud = json_data.getString("Latitude");
                    String longitud = json_data.getString("Longitude");
                    boolean activado = json_data.getBoolean("Activado");

                    if(activado){
                        boolean isNear = arePointsNear(position, new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud)), 0.015);
                        if(isNear)
                        {
                            nearPointName = nombre;
                            break;
                        }
                    }
                }
            }

            return nearPointName;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    private boolean arePointsNear(Location checkPoint, LatLng centerPoint, double km) {
        int ky = 40000 / 360;
        double kx = Math.cos(Math.PI * centerPoint.getLatitude() / 180.0) * ky;
        double dx = Math.abs(centerPoint.getLongitude() - checkPoint.getLongitude()) * kx;
        double dy = Math.abs(centerPoint.getLatitude() - checkPoint.getLatitude()) * ky;
        return Math.sqrt(dx * dx + dy * dy) <= km;
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

    private void sendMessageToActivity(int velocidad, float distanciaAcumulada, String fecha, boolean rutaNueva) {

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
        intent.putExtra("RUTA_NUEVA", rutaNueva);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void dataCompilationToSend(Location location) throws IOException {

        String currentDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

        Object[][] carData = {
                {
                        currentDate,
                        batteryCurrentGlobal,
                        batteryVoltageGlobal,
                        cumulativeChCurrentGlobal,
                        cumulativeDischCurrentGlobal,
                        rpmGlobal,
                        socGlobal,
                        sohGlobal,
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude()
                }
        };

        String nombrePunto = checkPointsNearPosition(location);
        if(isEmpty(nombrePunto) == false){
            Log.d("PUNTO_MARK", nombrePunto + " - " + puntoEnviado);
            if (puntoEnviado){
                nombrePunto = "";
                Log.d("PUNTO_MARK", nombrePunto + " - " + puntoEnviado);
            }
        }
        else{
            puntoEnviado = false;
            Log.d("PUNTO_MARK", nombrePunto + " - " + puntoEnviado);
        }


        createExcelOBD2(carData, nombrePunto);
        enviarDatosVehiculo(location, batteryCurrentGlobal, batteryVoltageGlobal, cumulativeChCurrentGlobal,
                cumulativeDischCurrentGlobal, rpmGlobal, socGlobal, sohGlobal, nombrePunto);

        sendOBD2ToActivity(batteryCurrentGlobal, batteryVoltageGlobal, cumulativeChCurrentGlobal,
                cumulativeDischCurrentGlobal, rpmGlobal, socGlobal, sohGlobal);
    }

    public void readExcelOBD2(Location location){

        File actualFile = null;
        Date actualLastModDate = null;

        String currentDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

        String path = Environment.getExternalStorageDirectory().toString() + "/torqueLogs/";
        File directory = new File(path);
        File[] files = directory.listFiles();

        if (files != null) {

                for (int i = 0; i < files.length; i++) {

                Date lastModDate = new Date(files[i].lastModified());
                Date dateRest10Min = new Date(System.currentTimeMillis() - 600 * 1000);
                if(lastModDate.after(dateRest10Min)){
                    if (actualFile != null) {

                        if (lastModDate.after(actualLastModDate)) {
                            actualFile = files[i];
                            actualLastModDate = new Date(files[i].lastModified());
                        }
                    } else {
                        actualFile = files[i];
                        actualLastModDate = new Date(files[i].lastModified());
                    }
                }
            }
        }

        if(actualFile != null) {
            try {

                BufferedReader br = new BufferedReader(new FileReader(path + actualFile.getName()));
                String line = br.readLine();
                String lastLine = "";

                while ((line = br.readLine()) != null) {
                    // Doing some actions
                    // Overwrite lastLine each time
                    lastLine = line;
                }

                String[] items = lastLine.split(",");
                double batteryCurrentValue = Double.parseDouble(items[12]);
                double batteryVoltageValue = Double.parseDouble(items[13]);
                double cumulativeCharValue = Double.parseDouble(items[14]);
                double cumulativeDiscValue = Double.parseDouble(items[15]);
                double driveMotorSpd1Value = Double.parseDouble(items[16]);
                double stateOfChargedValue = Double.parseDouble(items[17]);
                double stateOfHealthBValue = Double.parseDouble(items[18]);

                Object[][] carData = {
                        {
                                currentDate,
                                batteryCurrentValue,
                                batteryVoltageValue,
                                cumulativeCharValue,
                                cumulativeDiscValue,
                                driveMotorSpd1Value,
                                stateOfChargedValue,
                                stateOfHealthBValue,
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getAltitude()
                        }
                };

                String nombrePunto = checkPointsNearPosition(location);
                if(isEmpty(nombrePunto) == false){
                    Log.d("PUNTO_MARK", nombrePunto + " - " + puntoEnviado);
                    if (puntoEnviado){
                        nombrePunto = "";
                        Log.d("PUNTO_MARK", nombrePunto + " - " + puntoEnviado);
                    }
                }
                else{
                    puntoEnviado = false;
                    Log.d("PUNTO_MARK", nombrePunto + " - " + puntoEnviado);
                }

                createExcelOBD2(carData, nombrePunto);
                enviarDatosVehiculo(location, batteryCurrentValue, batteryVoltageValue, cumulativeCharValue, cumulativeDiscValue, driveMotorSpd1Value, stateOfChargedValue, stateOfHealthBValue, nombrePunto);

                sendOBD2ToActivity(batteryCurrentValue, batteryVoltageValue, cumulativeCharValue,
                        cumulativeDiscValue, driveMotorSpd1Value, stateOfChargedValue, stateOfHealthBValue);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isEmpty(String etText) {
        if (etText.trim().length() > 0)
            return false;
        return true;
    }

    public void readCSV(File file) throws IOException {
        try{
            String path = Environment.getExternalStorageDirectory().toString();
            BufferedReader br = new BufferedReader(new FileReader(path + "/torqueLogs/" + file.getName()));

            String line = br.readLine();
            for (int rows=0; line != null; rows++) {
                String[] items = line.split(",");
                String item = items[1];
                //read next line
                line = br.readLine();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public double checkNull(Cell cell){
        if(cell != null)
            return cell.getNumericCellValue();
        return 0;
    }

    public int getLastRowContent(Sheet sheet){

        int cont = 0;
        for(Row row : sheet){
            if (row.getCell(12) == null || row.getCell(12).getCellType() == Cell.CELL_TYPE_BLANK) break;
            cont++;
        }

        if(cont > 0) cont--;
        return cont;
    }

    public void createExcelOBD2(Object[][] carData, String nombrePunto) throws IOException {

        String path = Environment.getExternalStorageDirectory().toString();

        String fileName = "tracklog_"+dateTracklogs+".xlsx";

        File f = new File(path, fileName);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = null;

        if(f.isFile()){
            FileInputStream inputStream = new FileInputStream(new File(path + "/" + f.getName()));
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
            sheet.setDefaultColumnWidth(30);

            Row headRow = sheet.createRow(0);

            headRow.createCell(0).setCellValue((String) "Fecha");
            headRow.createCell(1).setCellValue((String) "Battery Current (A)");
            headRow.createCell(2).setCellValue((String) "Battery DC Voltage (V)");
            headRow.createCell(3).setCellValue((String) "Cumulative Charge Current (Ah)");
            headRow.createCell(4).setCellValue((String) "Cumulative Discharge Current (Ah)");
            headRow.createCell(5).setCellValue((String) "RPM");
            headRow.createCell(6).setCellValue((String) "State of Charge (SOC)");
            headRow.createCell(7).setCellValue((String) "State of Health (SOH)");
            headRow.createCell(8).setCellValue((String) "Latitude");
            headRow.createCell(9).setCellValue((String) "Longitude");
            headRow.createCell(10).setCellValue((String) "Altitude");
            headRow.createCell(11).setCellValue((String) "Punto Destino");

        }
        else{
            sheet = workbook.createSheet("Vehicle data");
        }

        int rowCount = sheet.getLastRowNum() + 1;

        for (Object[] aCarData : carData) {
            Row row = sheet.createRow(rowCount);

            int columnCount = 0;

            for (Object field : aCarData) {
                Cell cell = row.createCell(columnCount);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Double) {
                    cell.setCellValue((Double) field);
                }
                ++columnCount;
            }

            Cell cell = row.createCell(columnCount);
            cell.setCellValue((String)nombrePunto);

        }

        try (FileOutputStream outputStream = new FileOutputStream(f)) {
            workbook.write(outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class writeOBD2ExcelAsync extends AsyncTask<String,String, JSONObject>
    {
        @Override
        protected JSONObject doInBackground(String... parametros) {

            int begin = 0;
            int timeInterval = 1000;
            dateTracklogs = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(updatedLocation != null){
                        readExcelOBD2(updatedLocation);
                    }
                }
            }, begin, timeInterval);

            return new JSONObject();
        }

        protected void onPostExecute(JSONObject respuestaOdata) {
        }
    }

    public class sendData extends AsyncTask<String,String, JSONObject>
    {
        @Override
        protected JSONObject doInBackground(String... parametros) {

            int begin = 0;
            int timeInterval = 1000;
            dateTracklogs = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(updatedLocation != null){
                        try {
                            dataCompilationToSend(updatedLocation);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, begin, timeInterval);

            return new JSONObject();
        }

        protected void onPostExecute(JSONObject respuestaOdata) {
        }
    }

    private void sendOBD2ToActivity(double batteryCurrentValue, double batteryVoltageValue,
                                    double cumulativeCharValue, double cumulativeDiscValue,
                                    double driveMotorSpd1Value, double stateOfChargedValue,
                                    double stateOfHealthBValue) {


        Intent intentKey = new Intent("intentKey");
        intentKey.putExtra("BATERRYVOLTAGE", new DecimalFormat("#.##").format(batteryVoltageValue)+"");
        intentKey.putExtra("BATTERYCURRENT", new DecimalFormat("#.##").format(batteryCurrentValue)+"");

        LocalBroadcastManager.getInstance(this).sendBroadcast(intentKey);

        Intent intent = new Intent("intentKeyOBD2");

        // You can also include some extra data.
        intent.putExtra("BATTERYCURRENT", new DecimalFormat("#.##").format(batteryCurrentValue)+"");
        intent.putExtra("BATERRYVOLTAGE", new DecimalFormat("#.#").format(batteryVoltageValue)+"");
        intent.putExtra("CUMULATIVECHAR", new DecimalFormat("#.#").format(cumulativeCharValue)+"");
        intent.putExtra("CUMULATIVEDISC", new DecimalFormat("#.#").format(cumulativeDiscValue)+"");
        intent.putExtra("DRIVEMOTORSPD1", driveMotorSpd1Value+"");
        intent.putExtra("STATEOFCHARGED", new DecimalFormat("#.#").format(stateOfChargedValue)+"");
        intent.putExtra("STATEOFHEALTHB", new DecimalFormat("#.#").format(stateOfHealthBValue)+"");

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private Runnable reader = new Runnable() {
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Log.d("ThreadConnection_BT", "BT: " + btAdress);

            try {

                BluetoothDevice device = adapter.getRemoteDevice(btAdress);
                socket = device.createInsecureRfcommSocketToServiceRecord(UUID_BT); // createRfcommSocketToServiceRecord

                Log.d("ThreadConnection_BT", "BT: " + device.getName() + " - " + device.getAddress());

                socket.connect();
                Log.d("ThreadConnection_BT", "Pass Socket connect");
                is = socket.getInputStream();
                ostream = socket.getOutputStream();

                ObdRawCommand obdRawATDCommand = new ObdRawCommand("AT D");
                obdRawATDCommand.run(is, ostream);

                //Thread.sleep(500);

                ObdRawCommand obdRawATZCommand = new ObdRawCommand("AT Z");
                obdRawATZCommand.run(is, ostream);

                //Thread.sleep(500);

                ObdRawCommand obdRawATL0Command = new ObdRawCommand("AT L0");
                obdRawATL0Command.run(is, ostream);

                //Thread.sleep(500);

                ObdRawCommand obdRawATE0Command = new ObdRawCommand("AT E0");
                obdRawATE0Command.run(is, ostream);

                //Thread.sleep(500);

                ObdRawCommand obdRawATS0Command = new ObdRawCommand("AT S0");
                obdRawATS0Command.run(is, ostream);

                //Thread.sleep(500);

                ObdRawCommand obdRawATH1Command = new ObdRawCommand("AT H1");
                obdRawATH1Command.run(is, ostream);

                //Thread.sleep(500);

                ObdRawCommand obdRawATSP0Command = new ObdRawCommand("AT SP 0");
                obdRawATSP0Command.run(is, ostream);

                //Thread.sleep(500);

                Log.d("ThreadConnection_BT", "TODO OK");

                ObdRawCommand obdRawCommand2101 = new ObdRawCommand("2101");
                ObdRawCommand obdRawCommand2105 = new ObdRawCommand("2105");

                Log.d("ThreadConnection_BT", "TODO OK 2");
                while (true) {

                    //Thread.sleep(500);

                    String obdRaw2101Cmd = "";
                    String obdRaw2105Cmd = "";

                    try {

                        obdRawCommand2101.run(is, ostream);
                        obdRawCommand2105.run(is, ostream);

                        obdRaw2101Cmd = obdRawCommand2101.getFormattedResult();
                        obdRaw2105Cmd = obdRawCommand2105.getFormattedResult();

                        Log.d("ThreadConnection_BT", "RESULT: " + obdRaw2101Cmd);

                        decodeHex(obdRaw2101Cmd, obdRaw2105Cmd);

                        Log.d(TAG, obdRaw2105Cmd);

                    } catch (Exception e) {
                        Log.d("ThreadConnection_BT", "ERROR inside while(true): " + e.getMessage());
                        break;
                    }
                }

                try {
                    if (is != null && ostream != null && socket != null){
                        is.close();
                        ostream.close();
                        socket.close();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    Log.e("ThreadConnection_BT","Catch Reader 2: " + ioException.getMessage());
                }

                threadBLT.interrupt();
                threadBLT = new Thread(reader);
                threadBLT.start();

            }
            catch (IOException | InterruptedException e) {
                try {
                    if (is != null && ostream != null && socket != null){
                        is.close();
                        ostream.close();
                        socket.close();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    Log.e("ThreadConnection_BT","Catch Reader 3: " + ioException.getMessage());
                }

                threadBLT.interrupt();
                threadBLT = new Thread(reader);
                threadBLT.start();

                Log.e("ThreadConnection_BT","Catch Reader 4: " + e.getMessage());
                //mostrarDatosError(e.getMessage());
            }
        }
    };


    private void decodeHex(String dataHex2101, String dataHex2105){

        List<HexData> hexDataList2101 = splitHex16Bytes(dataHex2101);
        List<HexData> hexDataList2105 = splitHex16Bytes(dataHex2105);

        socGlobal = getSOC(hexDataList2105);
        rpmGlobal = getRPM(hexDataList2101);
        batteryVoltageGlobal = getBatteryVoltage(hexDataList2101);
        batteryCurrentGlobal = getBatteryCurrent(hexDataList2101);
        sohGlobal = getSOH(hexDataList2105);
        cumulativeChCurrentGlobal = getCumulativeChargeCurrent(hexDataList2101);
        cumulativeDischCurrentGlobal = getCumulativeDiscChargeCurrent(hexDataList2101);
    }

    private List<HexData> splitHex16Bytes(String hex){
        List<HexData> hexDataList = new ArrayList<>();

        String[] splHex = hex.split("7E");

        for (String h : splHex){
            String hexRaw = "7E"+h;

            if(hexRaw.length() > 2){

                String data = hexRaw.substring(5);
                HexData hexData = new HexData();
                hexData.CanID = hexRaw.length() <= 5 ? hexRaw : hexRaw.substring(0, 5);
                hexData.Bytes = data.replaceAll("..(?!$)", "$0 ").split(" ");
                hexData.RawData = hexRaw;

                hexDataList.add(hexData);
            }
        }

        return hexDataList;
    }

    private double getSOC(List<HexData> hexList){
        double soc = 0.0;
        for (HexData h : hexList){
            if(h.CanID.equals("7EC24")){
                String hexSoc = h.Bytes[h.Bytes.length - 1];
                int num = Integer.parseInt(hexSoc,16);
                soc = num / 2.0;
                break;
            }
        }
        return soc;
    }

    private int getRPM(List<HexData> hexList){
        int rpm = 0;
        for (HexData h : hexList){
            if(h.CanID.equals("7EC28")){
                String hexRPM1 = h.Bytes[0];
                String hexRPM2 = h.Bytes[1];

                int num = Integer.parseInt(hexRPM1+hexRPM2,16);
                rpm = num / 4;
                break;
            }
        }
        return rpm;
    }

    private double getBatteryVoltage(List<HexData> hexList){
        double voltage = 0.0;
        for (HexData h : hexList){
            if(h.CanID.equals("7EC22")){
                String hexVolt1 = h.Bytes[1];
                String hexVolt2 = h.Bytes[2];

                int num = Integer.parseInt(hexVolt1+hexVolt2,16);
                voltage = num / 10.0;
                break;
            }
        }
        return voltage;
    }

    private double getBatteryCurrent(List<HexData> hexList){
        double current = 0.0;
        String hexCurrent1 = "";
        String hexCurrent2 = "";

        for (HexData h : hexList){
            if(h.CanID.equals("7EC21")){
                hexCurrent1 =  h.Bytes[h.Bytes.length - 1];
            }

            if(h.CanID.equals("7EC22")){
                hexCurrent2 =  h.Bytes[0];
            }
        }

        int num = Integer.parseInt(hexCurrent1 + hexCurrent2,16);
        current = num / 10.0;

        return current;
    }

    private int getSOH(List<HexData> hexList){
        int soh = 0;
        for (HexData h : hexList){
            if(h.CanID.equals("7EC24")){
                String hexSoh1 = h.Bytes[0];
                String hexSoh2 = h.Bytes[1];

                int num = Integer.parseInt(hexSoh1 + hexSoh2,16);
                soh = num / 10;
                break;
            }
        }
        return soh;
    }

    private double getCumulativeChargeCurrent(List<HexData> hexList){

        double cumulativeChargeCurrent = 0;

        String hexCumulCurrent1 = "";
        String hexCumulCurrent2 = "";

        for (HexData h : hexList){
            if(h.CanID.equals("7EC24")){
                String hexCumul1 = h.Bytes[h.Bytes.length - 2];
                String hexCumul2 =  h.Bytes[h.Bytes.length - 1];
                hexCumulCurrent1 = hexCumul1 + hexCumul2;
            }

            if(h.CanID.equals("7EC25")){
                String hexCumul1 = h.Bytes[0];
                String hexCumul2 =  h.Bytes[1];
                hexCumulCurrent2 = hexCumul1 + hexCumul2;
            }
        }

        int num = Integer.parseInt(hexCumulCurrent1 + hexCumulCurrent2,16);
        cumulativeChargeCurrent = num / 10.0;
        return cumulativeChargeCurrent;
    }

    private double getCumulativeDiscChargeCurrent(List<HexData> hexList){

        double cumulativeDiscChargeCurrent = 0;

        for (HexData h : hexList){
            if(h.CanID.equals("7EC25")){
                String hexCumulDisc1 = h.Bytes[2];
                String hexCumulDisc2 =  h.Bytes[3];
                String hexCumulDisc3 = h.Bytes[4];
                String hexCumulDisc4 =  h.Bytes[5];

                int num = Integer.parseInt(hexCumulDisc1 + hexCumulDisc2 + hexCumulDisc3 + hexCumulDisc4,16);
                cumulativeDiscChargeCurrent = num / 10.0;
            }
        }
        return cumulativeDiscChargeCurrent;
    }

    private int getCumulativeOperatingTime(List<HexData> hexList){
        int operatingTime = 0;

        for (HexData h : hexList){
            if(h.CanID.equals("7EC27")){
                String hexCumulOp1 = h.Bytes[0];
                String hexCumulOp2 =  h.Bytes[1];
                String hexCumulOp3 = h.Bytes[2];
                String hexCumulOp4 =  h.Bytes[3];

                int num = Integer.parseInt(hexCumulOp1 + hexCumulOp2 + hexCumulOp3 + hexCumulOp4,16);
                operatingTime = num;
            }
        }

        return operatingTime;
    }
}
