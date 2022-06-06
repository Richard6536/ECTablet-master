package com.example.richard.ectablet.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.richard.ectablet.Clases.SessionManager;
import com.example.richard.ectablet.R;

import java.util.HashMap;

public class AjustesActivity extends AppCompatActivity {

    public Button btnReiniciar;
    SessionManager sessionController;
    public Spinner spinnerTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);
        sessionController = new SessionManager(getApplicationContext());

        spinnerTheme = findViewById(R.id.spinnerTheme);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.theme_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinnerTheme.setAdapter(adapter);
            }
        });

        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sessionController.saveThemeList(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        try{
            HashMap<String, String> themeDataList = sessionController.getThemeList();
            int themePos = Integer.parseInt(themeDataList.get(SessionManager.KEY_THEME_LIST));

            if(themePos > 5)
                themePos = 0;

            spinnerTheme.setSelection(themePos);
        }
        catch (Exception exc){ }

        btnReiniciar = findViewById(R.id.btnReiniciarKilometraje);

        btnReiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sessionController.reiniciarKM(0);
                    Context context = getApplicationContext();
                    CharSequence text = "Kilometraje reiniciado";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                catch (Exception ex){
                    Toast.makeText(getApplicationContext(), "Error: " + ex.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}