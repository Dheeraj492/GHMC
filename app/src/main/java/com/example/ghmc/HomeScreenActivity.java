package com.example.ghmc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeScreenActivity extends AppCompatActivity {

    private Button raiseComplaintButton, helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        raiseComplaintButton = findViewById(R.id.button);
        helpButton = findViewById(R.id.button3);

        // Set up click listener for the Raise Complaint button
        raiseComplaintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ComplaintForm Activity
                Intent intent = new Intent(HomeScreenActivity.this, ComplaintForm.class);
                startActivity(intent);
            }
        });

        // Set up click listener for the Help button
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logging to check if button click works
                Log.d("HomeScreenActivity", "Help Button Clicked");

                // Navigate to HelpActivity
                Intent intent = new Intent(HomeScreenActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });
    }
}
