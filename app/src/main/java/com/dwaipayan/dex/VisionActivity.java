package com.dwaipayan.dex;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import java.io.ByteArrayOutputStream;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.FutureCallback;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
//import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import androidx.appcompat.app.AppCompatActivity;

public class VisionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Bitmap selectedImage;
    GenerativeModel gm = new GenerativeModel("gemini-pro-vision","AIzaSyDZCy17cHiQBK9ykBy8MFiefBrXgFTGUG0");
    GenerativeModelFutures model = GenerativeModelFutures.from(gm);
    private TextView responseTextView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Bitmap resizedBitmap = resizeImage(imageBitmap);
                imageView.setImageBitmap(resizedBitmap);
                selectedImage = resizedBitmap; // Store the selected image
                processImageWithGeminiApi(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision);
        responseTextView = findViewById(R.id.responseTextView);
        imageView = findViewById(R.id.imageView);
        Button button = findViewById(R.id.button);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processImageWithGeminiApi(selectedImage);
            }
        });
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        // Initialize the Generative Model
        GenerativeModel gm = new GenerativeModel("gemini-vision-pro","AIzaSyDZCy17cHiQBK9ykBy8MFiefBrXgFTGUG0");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    }
    private JSONObject createRequestBody(String encodedImage) {
        JSONObject requestBody = new JSONObject();
        try {
            // Create the inline data part with the base64-encoded image
            JSONObject inlineData = new JSONObject();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", encodedImage);

            // Create the parts array
            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put("text", "What is this picture?"));
            parts.put(new JSONObject().put("inline_data", inlineData));

            // Create the contents array
            JSONArray contents = new JSONArray();
            contents.put(new JSONObject().put("parts", parts));

            // Put the contents array into the request body
            requestBody.put("contents", contents);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestBody;
    }
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }



    private Bitmap resizeImage(Bitmap imageBitmap) {
        int targetWidth = 512;
        int targetHeight = (int) (imageBitmap.getHeight() * targetWidth / imageBitmap.getWidth());
        return Bitmap.createScaledBitmap(imageBitmap, targetWidth, targetHeight, false);
    }

    private void processImageWithGeminiApi(Bitmap image) {
        // Define your Executor
        Executor executor = Executors.newFixedThreadPool(5);

        // Create content with your text and image
        Content content = new Content.Builder()
                .addText("What's in the image ?")
                .addImage(image) // Using the selected image
                .build();

        // Generate content
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                final String resultText = result.getText();
                Log.d("VisionActivity",result.toString());

                System.out.println(resultText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        responseTextView.setText(resultText); // Update UI with result
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                Log.d("VisionActivity", "FCUK");

            }
        }, executor);
    }
    private void sendPostRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;

                try {
                    // Convert the Bitmap to Base64
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    // Create the JSON request
                    JSONObject requestBody = createRequestBody(encodedImage);

                    String apiKey = "AIzaSyDZCy17cHiQBK9ykBy8MFiefBrXgFTGUG0"; // Ensure this is correctly retrieved
                    URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-vision:generateContent?key=" + apiKey);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Sending the request
                    OutputStream os = connection.getOutputStream();
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    Log.d("VisionActivity", "Request: " + requestBody.toString());
                    os.close();
                    int responseCode = connection.getResponseCode();
                    Log.d("VisionActivity", "Response Code: " + responseCode);
                    // Reading the response
                    InputStream responseStream = connection.getInputStream();
                    Scanner scanner = new Scanner(responseStream).useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    Log.d("VisionActivity", "Response: " + response); // Debug: Log the response

                    // Extract "text" field from response
                    JSONObject jsonResponse = new JSONObject(response);
                    final String textResponse = jsonResponse.getString("text");

                    // Update UI on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            responseTextView.setText(textResponse);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("VisionActivity", "Error: " + e.getMessage()); // Log errors
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
