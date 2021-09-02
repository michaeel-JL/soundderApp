package com.example.soundder.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.soundder.Models.User;
import com.example.soundder.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUp_Activity extends AppCompatActivity {

        private EditText mEditTextEmail,mEditTextPassword;
        private Button mButtonRegistro;

        private String name = "", email = "", password = "";

        FirebaseAuth mAuth;
        DatabaseReference mDatabase;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_sign_up);

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();

            mEditTextEmail = findViewById(R.id.etxt_email);
            mEditTextPassword = findViewById(R.id.etxt_password);
            mButtonRegistro = findViewById(R.id.btn_registrar);

            mButtonRegistro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    email = mEditTextEmail.getText().toString();
                    password = mEditTextPassword.getText().toString();

                    if (!email.isEmpty() && !password.isEmpty()){
                        if(password.length() >= 6) {
                            registrarUsuario();
                        }
                    }


                }
            });
        }

        private void registrarUsuario() {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {

                        String id = mAuth.getCurrentUser().getUid();

                        User user = new User(id, email, password);

                        mDatabase.child("Users").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task2) {
                                if (task2.isSuccessful()){
                                    startActivity(new Intent(SignUp_Activity.this, Home_Activity.class));
                                    finish();
                                }else{
                                    Toast.makeText(SignUp_Activity.this, "Error datos", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }else {
                        Toast.makeText(SignUp_Activity.this, "Error auth", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
}