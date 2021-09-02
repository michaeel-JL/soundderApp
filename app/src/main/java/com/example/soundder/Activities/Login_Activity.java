package com.example.soundder.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.soundder.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Login_Activity extends AppCompatActivity {

    Button btnlogin, btnsignup, btnreset;
    EditText mMail, mPassword;

    private Switch mRemember;
    SharedPreferences sharedPreferences;

    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // cargamos los elementos del layout
        items();

        mAuth = FirebaseAuth.getInstance();


        // Button - Registrarse
        btnsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //cargamos el registro
                startActivity(new Intent(Login_Activity.this, SignUp_Activity.class));
                finish();
            }
        });

        // Button - Restablecer contraseña
        btnreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(Login_Activity.this, example.clas);
                //startActivity(intent);  // lo enviamos a otro activity
            }
        });

        // Button - Iniciar sesión
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inicioSesion();
            }
        });


    }

    private void inicioSesion() {

        // Se recogen las credenciales para loguear al usuario
        String email= mMail.getText().toString();
        String password= mPassword.getText().toString();

        //Comprobamos que los datos estén bien
        if(login(email,password)) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Login_Activity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //Si el usuario y contraseña son correctos, se carga el PacienteActivity.
                            if (task.isSuccessful()) {
                                //Cogemos los datos del usuario y su ID
                                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                String uid = user.getUid();



                                if(mRemember.isChecked()) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("EMAIL", email);
                                    editor.putString("PASSWORD", password);
                                    editor.apply();
                                }

                                startActivity(new Intent(Login_Activity.this, Home_Activity.class));
                                finish();

                            } else {
                                Toast.makeText(Login_Activity.this, "Error, compruebe el usuario o contraseña", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    //Comprobamos sus datos
    private boolean login(String email, String password) {
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email no válido, por favor inténtalo de nuevo", Toast.LENGTH_LONG).show();
            return false;
        } else if (!isValidPassword(password)) {
            Toast.makeText(this, "Contraseña incorrecta.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    //Comprobamos EMAIL
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Comprobamos PASSWORD
    private boolean isValidPassword(String password) {
        //return password.length() >= 4;
        return true;
    }

    // Referrenciamos los elementos de la vista
    private void items() {
        btnlogin = findViewById(R.id.btn_login);
        btnsignup = findViewById(R.id.btn_signup);
        btnreset = findViewById(R.id.btn_resetPassword);
        mMail = findViewById(R.id.mail);
        mPassword = findViewById(R.id.password);
        mRemember = (Switch) findViewById(R.id.remember_switch);

        sharedPreferences = getSharedPreferences("SHARED_PREF", MODE_PRIVATE);
    }

}