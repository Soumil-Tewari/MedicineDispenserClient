package com.example.medicinedispenserclient;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private TextView medicinesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        medicinesTextView =
                findViewById(R.id.medicinesTextView);

        loadMedicines();
    }

    public void addMedicine(View view) {

        TimePicker timePicker =
                findViewById(R.id.timePicker);

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        String time = String.format(
                "%02d:%02d:00",
                hour,
                minute
        );

        String name =
                ((EditText) findViewById(R.id.medicineName))
                        .getText()
                        .toString()
                        .trim();

        String dosage =
                ((EditText) findViewById(R.id.dosage))
                        .getText()
                        .toString()
                        .trim();

        new Thread(() -> {

            try {

                URL url = new URL(
                        "https://esaproject.onrender.com/medicines"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                connection.setDoOutput(true);

                String json =
                        "{"
                                + "\"medicine_name\":\"" + name + "\","
                                + "\"dosage\":\"" + dosage + "\","
                                + "\"timing\":\"" + time + "\""
                                + "}";

                OutputStream outputStream =
                        connection.getOutputStream();

                outputStream.write(
                        json.getBytes(StandardCharsets.UTF_8)
                );

                outputStream.close();

                int responseCode =
                        connection.getResponseCode();

                if (responseCode == 201) {

                    runOnUiThread(() -> {

                        Toast.makeText(
                                MainActivity.this,
                                "Medicine added!",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadMedicines();
                    });

                } else {

                    runOnUiThread(() ->
                            Toast.makeText(
                                    MainActivity.this,
                                    "Failed: HTTP " + responseCode,
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }

                connection.disconnect();

            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }

        }).start();
    }

    private void loadMedicines() {

        new Thread(() -> {

            try {

                URL url = new URL(
                        "https://esaproject.onrender.com/medicines"
                );

                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream()
                                )
                        );

                StringBuilder response =
                        new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                JSONArray medicines =
                        new JSONArray(response.toString());

                StringBuilder display =
                        new StringBuilder();

                for (int i = 0; i < medicines.length(); i++) {

                    JSONObject medicine =
                            medicines.getJSONObject(i);

                    String name =
                            medicine.getString("medicine_name");

                    String dosage =
                            medicine.getString("dosage");

                    String timing =
                            medicine.getString("timing");

                    display.append(name)
                            .append(" - ")
                            .append(dosage)
                            .append(" - ")
                            .append(timing)
                            .append("\n\n");
                }

                runOnUiThread(() -> {

                    if (display.length() == 0) {

                        medicinesTextView.setText(
                                "No medicines found"
                        );

                    } else {

                        medicinesTextView.setText(
                                display.toString()
                        );
                    }
                });

            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() ->
                        medicinesTextView.setText(
                                "Error: " + e.getMessage()
                        )
                );
            }
        }).start();
    }
}