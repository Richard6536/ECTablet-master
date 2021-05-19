package com.example.richard.ectablet.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.example.richard.ectablet.Clases.Point;
import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    public EditText latPuntoAEdit, latPuntoBEdit, latPuntoCEdit, latPuntoDEdit, latPuntoEEdit,
            latPuntoFEdit;

    public EditText lngPuntoAEdit, lngPuntoBEdit, lngPuntoCEdit, lngPuntoDEdit, lngPuntoEEdit,
            lngPuntoFEdit;

    public CheckBox checkBoxPuntoA, checkBoxPuntoB, checkBoxPuntoC, checkBoxPuntoD, checkBoxPuntoE,
            checkBoxPuntoF;

    public TextView txtTitlePuntoA, txtTitlePuntoB, txtTitlePuntoC, txtTitlePuntoD, txtTitlePuntoE,
            txtTitlePuntoF;

    public Button btnGuardarCambios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        //TODO: PUNTO A
        latPuntoAEdit = findViewById(R.id.latPuntoA);
        lngPuntoAEdit = findViewById(R.id.lngPuntoA);
        checkBoxPuntoA = findViewById(R.id.checkPuntoA);
        txtTitlePuntoA = findViewById(R.id.titlePuntoA);

        //TODO: PUNTO B
        latPuntoBEdit = findViewById(R.id.latPuntoB);
        lngPuntoBEdit = findViewById(R.id.lngPuntoB);
        checkBoxPuntoB = findViewById(R.id.checkPuntoB);
        txtTitlePuntoB = findViewById(R.id.titlePuntoB);

        //TODO: PUNTO C
        latPuntoCEdit = findViewById(R.id.latPuntoC);
        lngPuntoCEdit = findViewById(R.id.lngPuntoC);
        checkBoxPuntoC = findViewById(R.id.checkPuntoC);
        txtTitlePuntoC = findViewById(R.id.titlePuntoC);

        //TODO: PUNTO D
        latPuntoDEdit = findViewById(R.id.latPuntoD);
        lngPuntoDEdit = findViewById(R.id.lngPuntoD);
        checkBoxPuntoD = findViewById(R.id.checkPuntoD);
        txtTitlePuntoD = findViewById(R.id.titlePuntoD);

        //TODO: PUNTO E
        latPuntoEEdit = findViewById(R.id.latPuntoE);
        lngPuntoEEdit = findViewById(R.id.lngPuntoE);
        checkBoxPuntoE = findViewById(R.id.checkPuntoE);
        txtTitlePuntoE = findViewById(R.id.titlePuntoE);

        //TODO: PUNTO F
        latPuntoFEdit = findViewById(R.id.latPuntoF);
        lngPuntoFEdit = findViewById(R.id.lngPuntoF);
        checkBoxPuntoF = findViewById(R.id.checkPuntoF);
        txtTitlePuntoF = findViewById(R.id.titlePuntoF);

        setData();

        //TODO: BUTTON
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnGuardarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    SessionManager session = new SessionManager(getApplicationContext());

                    JSONArray jsonArrayPoints = new JSONArray();

                    JSONObject point = null;
                    if(!isEmpty(latPuntoAEdit) && !isEmpty(lngPuntoAEdit)){

                        boolean checkb = false;
                        if (checkBoxPuntoA.isChecked()) checkb = true;

                        point = createPoint(txtTitlePuntoA.getText().toString(),
                                latPuntoAEdit.getText().toString(), lngPuntoAEdit.getText().toString(), checkb);

                        jsonArrayPoints.put(point);
                    }

                    if(!isEmpty(latPuntoBEdit) && !isEmpty(lngPuntoBEdit)){

                        boolean checkb = false;
                        if (checkBoxPuntoB.isChecked()) checkb = true;

                        point = createPoint(txtTitlePuntoB.getText().toString(),
                                latPuntoBEdit.getText().toString(), lngPuntoBEdit.getText().toString(), checkb);

                        jsonArrayPoints.put(point);
                    }

                    if(!isEmpty(latPuntoCEdit) && !isEmpty(lngPuntoCEdit)){

                        boolean checkb = false;
                        if (checkBoxPuntoC.isChecked()) checkb = true;

                        point = createPoint(txtTitlePuntoC.getText().toString(),
                                latPuntoCEdit.getText().toString(), lngPuntoCEdit.getText().toString(), checkb);

                        jsonArrayPoints.put(point);
                    }

                    if(!isEmpty(latPuntoDEdit) && !isEmpty(lngPuntoDEdit)){

                        boolean checkb = false;
                        if (checkBoxPuntoD.isChecked()) checkb = true;

                        point = createPoint(txtTitlePuntoD.getText().toString(),
                                latPuntoDEdit.getText().toString(), lngPuntoDEdit.getText().toString(), checkb);

                        jsonArrayPoints.put(point);

                    }

                    if(!isEmpty(latPuntoEEdit) && !isEmpty(lngPuntoEEdit)){

                        boolean checkb = false;
                        if (checkBoxPuntoE.isChecked()) checkb = true;

                        point = createPoint(txtTitlePuntoE.getText().toString(),
                                latPuntoEEdit.getText().toString(), lngPuntoEEdit.getText().toString(), checkb);

                        jsonArrayPoints.put(point);
                    }

                    if(!isEmpty(latPuntoFEdit) && !isEmpty(lngPuntoFEdit)){

                        boolean checkb = false;
                        if (checkBoxPuntoF.isChecked()) checkb = true;

                        point = createPoint(txtTitlePuntoF.getText().toString(),
                                latPuntoFEdit.getText().toString(), lngPuntoFEdit.getText().toString(), checkb);

                        jsonArrayPoints.put(point);

                    }

                    session.savePoints(jsonArrayPoints);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;

        return true;
    }

    public JSONObject createPoint(String nombre, String lat, String lng, boolean activado) throws JSONException {

        JSONObject point = new JSONObject();
        point.put("Name", nombre);
        point.put("Latitude", Double.parseDouble(lat));
        point.put("Longitude", Double.parseDouble(lng));
        point.put("Activado", activado);

        return point;
    }

    public void setData(){

        try{
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

                    if(nombre.equals("Punto A")){
                        latPuntoAEdit.setText(latitud);
                        lngPuntoAEdit.setText(longitud);
                        checkBoxPuntoA.setChecked(activado);
                    }
                    else if(nombre.equals("Punto B")){
                        latPuntoBEdit.setText(latitud);
                        lngPuntoBEdit.setText(longitud);
                        checkBoxPuntoB.setChecked(activado);
                    }
                    else if(nombre.equals("Punto C")){
                        latPuntoCEdit.setText(latitud);
                        lngPuntoCEdit.setText(longitud);
                        checkBoxPuntoC.setChecked(activado);
                    }
                    else if(nombre.equals("Punto D")){
                        latPuntoDEdit.setText(latitud);
                        lngPuntoDEdit.setText(longitud);
                        checkBoxPuntoD.setChecked(activado);
                    }
                    else if(nombre.equals("Punto E")){
                        latPuntoEEdit.setText(latitud);
                        lngPuntoEEdit.setText(longitud);
                        checkBoxPuntoE.setChecked(activado);
                    }
                    else if(nombre.equals("Punto F")){
                        latPuntoFEdit.setText(latitud);
                        lngPuntoFEdit.setText(longitud);
                        checkBoxPuntoF.setChecked(activado);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}