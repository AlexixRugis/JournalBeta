package com.example.journalbeta;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText username = findViewById(R.id.login_username);
        final EditText password = findViewById(R.id.login_password);

        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().length() > 0 && password.getText().length() > 0) {
                    //log in
                    Toast.makeText(getApplicationContext(), R.string.status_connecting, Toast.LENGTH_SHORT).show();
                    JournalAPI.Login(username.getText().toString(), password.getText().toString(), getApplicationContext());

                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.login_empty_field, Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}