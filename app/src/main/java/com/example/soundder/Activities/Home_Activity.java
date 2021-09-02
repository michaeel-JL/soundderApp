package com.example.soundder.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.soundder.Adaptrers.User_List_Adapter;
import com.example.soundder.Models.User;
import com.example.soundder.R;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Home_Activity extends AppCompatActivity {
    private User_List_Adapter adapter;
    private ArrayList<User> usersArrayList;
    private RecyclerView rv;

    private String audio_link, id_actual, archivoSalida = null;

    private Button btn_grabar, btn_escuchar;

    private MediaPlayer mediaPlayer;
    Map<String, Object> map;



    private DatabaseReference databaseReference;
    private StorageReference mStorage;

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(Home_Activity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        mStorage = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Audios");

        btn_escuchar = findViewById(R.id.btn_escuchar);
        btn_grabar = findViewById(R.id.btn_grabar);

        rv = findViewById(R.id.rv);
        rv.setLayoutManager(mLayoutManager);

        usersArrayList = new ArrayList<>();
        id_actual = user.getUid();

        adapter = new User_List_Adapter(usersArrayList, Home_Activity.this);
        rv.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Home_Activity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);
        }

        DatabaseReference ref_user = FirebaseDatabase.getInstance().getReference("Users");
        ref_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    usersArrayList.removeAll(usersArrayList);

                    // agregamos los usuarios al arraylist
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        usersArrayList.add(user);
                    }
                }else{
                    Toast.makeText(Home_Activity.this, "No existen usuarios.", Toast.LENGTH_SHORT).show();
                }

                adapter = new User_List_Adapter(usersArrayList, Home_Activity.this);
                rv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        DatabaseReference ref_audio = FirebaseDatabase.getInstance().getReference();
        ref_audio.child("Audios").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    //bloque bueno
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        // bloque duda
                        mediaPlayer = new MediaPlayer();

                        map = (Map<String, Object>) dataSnapshot.getValue();
                        Log.d("MAP", "Value is: " + map);
                        audio_link = map.get("id_audio").toString();
                        Log.d("LINK AUDIO", "Value is: " + audio_link);

                        //bloque bueno
                        try {
                            mediaPlayer.setDataSource(audio_link);
                            mediaPlayer.prepare();

                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mediaPlayer.start();
                                    Toast.makeText(Home_Activity.this, "Reproduciendo Audio.", Toast.LENGTH_SHORT).show();
                                }
                            });

                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                    mediaPlayer = null;
                                }
                            });

                        } catch (IOException e) {
                            Log.d("LINK AUDIO 2", "Value is: " + audio_link);
                        }

                    }

                }else{
                    Toast.makeText(Home_Activity.this, "No hay audio nuevos.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


//        DatabaseReference ref_audio = FirebaseDatabase.getInstance().getReference();
//        ref_audio.child("Audios").child(user.getUid()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                if (snapshot.exists()) {
//
//                    //bloque bueno
//                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        Toast.makeText(Home_Activity.this, dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
//                        //bloque prueba
//                        String id_us = dataSnapshot.getKey();
//                        ref_audio.child("Audios").child(user.getUid()).child(id_us).addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                mediaPlayer = new MediaPlayer();
//
//                                audio_link = snapshot.child("id_audio").getValue(String.class);
//                                Log.d("LINK AUDIO 1", "Value is: " + audio_link);
//
//                                //bloque bueno
//                                try {
//                                    mediaPlayer.setDataSource(audio_link);
//                                    mediaPlayer.prepare();
//
//                                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                                        @Override
//                                        public void onPrepared(MediaPlayer mp) {
//                                            mediaPlayer.start();
//                                            Toast.makeText(Home_Activity.this, "Reproduciendo Audio", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//
//                                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                                        @Override
//                                        public void onCompletion(MediaPlayer mp) {
//                                            mediaPlayer.stop();
//                                            mediaPlayer.release();
//                                            mediaPlayer = null;
//                                        }
//                                    });
//
//                                } catch (IOException e) {
//                                    Log.d("LINK AUDIO 2", "Value is: " + audio_link);
//                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
//
//                    }
//
//
//                }else{
//                    Toast.makeText(Home_Activity.this, "No hay audios nuevos", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

    }
