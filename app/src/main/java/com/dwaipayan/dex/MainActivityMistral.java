package com.dwaipayan.dex;


// /path/to/MainActivity.java

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivityMistral extends AppCompatActivity {
    private static final String API_TOKEN = "dz6Iml8WEhm-cmZTssM5j9Qxirl1Z6mqVddoUjQH";  // Use actual API token
    private static final String API_URL = "https://api.cloudflare.com/client/v4/accounts/752323c92ced7033d92cd70a4e55f748/ai/run/@cf/meta/llama-2-7b-chat-int8";

    private EditText editTextUserInput;
    private Button buttonSend;
    private TextView textViewResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mistral);

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
                JSONArray messagesArray = new JSONArray();
                messagesArray.put(new JSONObject().put("role", "system")
                        .put("content", "You are a friendly assistant that answers users queries in detail . Be verbose .Do not Remind me that you are an AI"));
                messagesArray.put(new JSONObject().put("role", "user")
                        .put("content", strings[0]));

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("messages", messagesArray);

                HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
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

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        if (jsonResponse.getBoolean("success")) {
                            JSONObject result = jsonResponse.getJSONObject("result");
                            String story = result.getString("response");
                            return story;
                        } else {
                            return "No content found in response";
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
                textViewResponse.setText("\t"+ "INVALID API CREDENTIALS . GENERATE A NEW ONE"+ "\n" + "\t"+ textViewResponse.getText() + "\n");
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
