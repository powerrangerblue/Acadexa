package com.example.acadexa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final AuthRepository authRepo = new AuthRepository(this);

        // If already logged in, go straight to dashboard
        if (authRepo.getCurrentUser() != null) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        final android.widget.EditText emailInput = findViewById(R.id.emailInput);
        final android.widget.EditText passwordInput = findViewById(R.id.passwordInput);
        final com.google.android.material.button.MaterialButton loginButton = findViewById(R.id.loginButton);
        TextView signUpText = findViewById(R.id.signUpText);

        signUpText.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));

        loginButton.setOnClickListener(v -> {
            final String emailOrId = emailInput.getText().toString().trim();
            final String pass = passwordInput.getText().toString();
            new Thread(() -> {
                AuthRepository.Result res = authRepo.login(emailOrId, pass);
                runOnUiThread(() -> {
                    if (res.success) {
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        android.widget.Toast.makeText(MainActivity.this, res.message, android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
    }
}