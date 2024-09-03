package com.dwaipayan.dex;


//package com.dwaipayan.chatv4;

// MainActivity.java
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "AIzaSyDZCy17cHiQBK9ykBy8MFiefBrXgFTGUG0";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=" + API_KEY;

    private EditText editTextUserInput;
    private Button buttonSend;
    private TextView textViewResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUserInput = findViewById(R.id.editTextUserInput);
        buttonSend = findViewById(R.id.buttonSend);
        textViewResponse = findViewById(R.id.textViewResponse);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInput = editTextUserInput.getText().toString();
                new ApiRequestTask().execute(userInput);
                editTextUserInput.setText(""); // reset EditText after sending a message
            }
        });

    }

    private class ApiRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject partObject = new JSONObject();
                partObject.put("text", strings[0]);

                JSONObject contentsObject = new JSONObject();
                contentsObject.put("role", "user");
                contentsObject.put("parts", new JSONArray().put(partObject));

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("contents", new JSONArray().put(contentsObject));

                HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonBody.toString().getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        try {
                            JSONObject jsonResponse = new JSONObject(response.toString());
                            JSONArray candidates = jsonResponse.getJSONArray("candidates");

                            if (candidates.length() > 0) {
                                JSONObject firstCandidate = candidates.getJSONObject(0);
                                JSONObject content = firstCandidate.getJSONObject("content");
                                JSONArray parts = content.getJSONArray("parts");

                                if (parts.length() > 0) {
                                    return parts.getJSONObject(0).getString("text");
                                }
                            }
                            return "No content found in response";
                        } catch (JSONException e) {
                            Log.e("MainActivity", "Error parsing JSON", e);
                            return "JSON Parsing Error: " + e.getMessage();
                        }


                    }
                } else {
                    Log.e("API Error", "Response Code: " + responseCode);
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        Log.e("API Error Details", response.toString());
                        return "Error: " + responseCode + " - " + response.toString();
                    }
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error in API request", e);
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            textViewResponse.setText("\t"+ result+ "\n" + "\t"+ textViewResponse.getText() + "\n");
            Log.i("API Response", result);
        }

    }

}
