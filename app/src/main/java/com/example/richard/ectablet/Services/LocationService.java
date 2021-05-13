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

import com.example.richard.ectablet.Activity.MainActivity;
import com.example.richard.ectablet.Clases.Almacenamiento;
import com.example.richard.ectablet.Clases.ControllerActivity;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.Clases.Vehiculo;

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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.android.volley.VolleyLog.TAG;

public class LocationService extends Service {

    Timer timer = new Timer();

    LocationManager locationManager;
    LocationListener locationListener = new LocationListener();
    SessionManager sessionController;

    public String dateTracklogs = "--";

    public static Location lastLocation;
    public static Location updatedLocation;

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

        try{
            timer.cancel();
            locationManager.removeUpdates(locationListener);
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
            //float accuracyPosicion = location.getAccuracy();
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

        updatedLocation = location;

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

            //readExcelOBD2(location);

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

            sendMessageToActivity(velocidadKm, distanciaAcumulada, fechaInicio, rutaNueva);

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
                        }
                };

                createExcelOBD2(carData);
                sendOBD2ToActivity(batteryCurrentValue, batteryVoltageValue, cumulativeCharValue,
                        cumulativeDiscValue, driveMotorSpd1Value, stateOfChargedValue, stateOfHealthBValue);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    public void createExcelOBD2(Object[][] carData) throws IOException {

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
                    readExcelOBD2(updatedLocation);
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
}
