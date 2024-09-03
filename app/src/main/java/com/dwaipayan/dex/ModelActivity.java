package com.dwaipayan.dex;


// path/filename: LoginActivity.java
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ModelActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);

        Button loginButton = findViewById(R.id.gemini);
        Button vision = findViewById(R.id.vision);
        Button mistralbutton = findViewById((R.id.gemini2));
        vision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(ModelActivity.this,VisionActivity.class);
                startActivity(intent1);
            }
        });
        mistralbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(ModelActivity.this,MainActivityMistral.class);
                startActivity(intent1);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to start the Chat Activity
                Intent intent = new Intent(ModelActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}

