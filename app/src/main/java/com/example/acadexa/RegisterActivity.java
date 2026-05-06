package com.example.acadexa;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private AuthRepository authRepo;
    private final ExecutorService uiExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView backToLoginText = findViewById(R.id.backToLoginText);
        backToLoginText.setOnClickListener(v -> finish());

        authRepo = new AuthRepository(this);

        final EditText fullNameInput = findViewById(R.id.registerFullNameInput);
        final EditText studentIdInput = findViewById(R.id.registerStudentIdInput);
        final EditText emailInput = findViewById(R.id.registerEmailInput);
        final EditText passwordInput = findViewById(R.id.registerPasswordInput);
        final EditText confirmInput = findViewById(R.id.registerConfirmPasswordInput);
        final MaterialButton registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            final String fullname = fullNameInput.getText().toString().trim();
            final String studentId = studentIdInput.getText().toString().trim();
            final String email = emailInput.getText().toString().trim();
            final String pass = passwordInput.getText().toString();
            final String confirm = confirmInput.getText().toString();

            if (fullname.isEmpty()) {
                Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use a background thread to call repository
            new Thread(() -> {
                AuthRepository.Result res = authRepo.register(fullname, email.isEmpty() ? null : email, studentId.isEmpty() ? null : studentId, pass);
                runOnUiThread(() -> {
                    if (res.success) {
                        Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show();
                        // Redirect back to login screen
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, res.message, Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        });
    }
}
