package com.naveennaidu.opc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LaunchScreenActivity extends AppCompatActivity {

    Button changeNameButton;
    String doc;
    EditText doctorName;
    String doctorNameText;
    String hosp;
    EditText hospitalName;
    String hospitalNameText;
    Button newPatient;
    Button oldPatient;
    Button saveButton;
    Button detectButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);
        setTitle("Patient");

        newPatient = findViewById(R.id.newPatient);
        oldPatient = findViewById(R.id.oldPatient);
        hospitalName = findViewById(R.id.hospitalEditText);
        doctorName = findViewById(R.id.doctorEditText);
        saveButton = findViewById(R.id.saveButton);
        changeNameButton = findViewById(R.id.changeButton);
        changeNameButton.setVisibility(View.GONE);

        detectButton = findViewById(R.id.detection_buttton);

        SharedPreferences preferences = getSharedPreferences("LOGIN", 0);
        hosp = preferences.getString("hosp", "");
        doc = preferences.getString("doc", "");

        if (!(hosp.matches("") || doc.matches(""))) {
            hospitalName.setVisibility(View.GONE);
            doctorName.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            changeNameButton.setVisibility(View.VISIBLE);
        }

        newPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hosp.matches("") || doc.matches("")) {
                    Toast.makeText(getApplicationContext(), "Hospital or Doctor name is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent goToNewPatient = new Intent(LaunchScreenActivity.this, CollectInformationActivity.class);

                goToNewPatient.putExtra("hospital", hosp);
                goToNewPatient.putExtra("doctor", doc);
                startActivity(goToNewPatient);
            }
        });

        oldPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToOldPatient = new Intent(LaunchScreenActivity.this, PatientDatabaseActivity.class);
                startActivity(goToOldPatient);
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hospitalNameText = hospitalName.getText().toString();
                doctorNameText = doctorName.getText().toString();
                SharedPreferences.Editor editor = getSharedPreferences("LOGIN", 0).edit();

                editor.putString("hosp", hospitalNameText);
                editor.putString("doc", doctorNameText);
                editor.apply();

                hosp = hospitalNameText;
                doc = doctorNameText;
                hospitalName.setVisibility(View.GONE);
                doctorName.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
                changeNameButton.setVisibility(View.VISIBLE);
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(doctorName.getWindowToken(), 0);
            }
        });

        changeNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hospitalName.setVisibility(View.VISIBLE);
                doctorName.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                changeNameButton.setVisibility(View.GONE);
                hospitalName.setText(hosp);
                doctorName.setText(doc);
                hospitalNameText = hospitalName.getText().toString();
                doctorNameText = doctorName.getText().toString();
                SharedPreferences.Editor editor = getSharedPreferences("LOGIN", 0).edit();

                editor.putString("hosp", hospitalNameText);
                editor.putString("doc", doctorNameText);
                editor.apply();
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(doctorName.getWindowToken(), 0);
            }
        });

        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToDetect = new Intent(LaunchScreenActivity.this, DetectionScreenActivity.class);
                startActivity(goToDetect);
            }
        });
    }
}
