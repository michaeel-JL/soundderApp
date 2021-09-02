package com.example.soundder.Adaptrers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundder.Activities.Home_Activity;
import com.example.soundder.Models.User;
import com.example.soundder.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User_List_Adapter extends RecyclerView.Adapter<User_List_Adapter.viewHolderAdapter> {

    List<User> userList;
    Context context;

    private String archivoSalida, audio_link;

    private MediaRecorder grabacion;
    private MediaPlayer mediaPlayer;

    private DatabaseReference databaseReference;
    private StorageReference mStorage;
    Map<String, Object> map;




    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public User_List_Adapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }


    @NonNull
    @Override
    public viewHolderAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_user, parent, false);
        viewHolderAdapter holder = new viewHolderAdapter(v);

        return holder;    }

    @Override
    public void onBindViewHolder(@NonNull viewHolderAdapter holder, int position) {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Audios");
        mStorage = FirebaseStorage.getInstance().getReference();

        // creamos un objeto del usuario escogido
        User userss = userList.get(position);

        // mostramos el email de cada usuario
        holder.tv_email.setText(userss.getEmail());

        //Si el usuario que está recorriendo es igual al que tenemos lo oculta
        if (userss.getId().equals(user.getUid())){
            holder.cardView.setVisibility(View.GONE);
        }else{
            holder.cardView.setVisibility(View.VISIBLE);
        }

        holder.btn_grabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (grabacion == null){

                    holder.btn_grabar.setBackgroundColor(Color.RED);

                    archivoSalida = view.getContext().getExternalFilesDir(null).getAbsolutePath() + "/Grabacion.mp3";
                    grabacion = new MediaRecorder();
                    grabacion.setAudioSource(MediaRecorder.AudioSource.MIC);
                    grabacion.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    grabacion.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                    grabacion.setOutputFile(archivoSalida);

                    // comienza a grabar
                    try {
                        grabacion.prepare();
                        grabacion.start();
                    }catch (IOException e){

                    }

                    Toast.makeText(view.getContext(), "Grabando...", Toast.LENGTH_SHORT).show();

                }else if(grabacion != null){

                    holder.btn_grabar.setBackgroundColor(Color.BLACK);

                    grabacion.stop();
                    grabacion.release();
                    grabacion = null;

                    Toast.makeText(view.getContext(), "Grabacion finalizada...", Toast.LENGTH_SHORT).show();

                    String nombre_audio = databaseReference.push().getKey();

                    // obtenemos el enlace de STORAGE
                    StorageReference filepath = mStorage.child("audio").child(nombre_audio); //llamamos al audio igual que al id
                    Uri uri = Uri.fromFile(new File(archivoSalida));

                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    databaseReference.child(userss.getId()).child(user.getUid()).child("id_audio").setValue(uri.toString());
                                    Toast.makeText(view.getContext(), "Archivo subido", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }); // filepath
                }   // if
            }   // onclick
        }); // holder button

        holder.btn_reproducir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference ref_audio = FirebaseDatabase.getInstance().getReference();
                ref_audio.child("Audios").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            //bloque bueno
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                // bloque duda
                                mediaPlayer = new MediaPlayer();

                                //bloque prueba
                                String id_us = dataSnapshot.getKey().toString();
                                ref_audio.child("Audios").child(user.getUid()).child(id_us).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        audio_link = snapshot.child("id_audio").getValue(String.class);
                                        Log.d("LINK AUDIO 1", "Value is: " + audio_link);

                                        //bloque bueno
                                        try {
                                            mediaPlayer.setDataSource(audio_link);
                                            mediaPlayer.prepare();

                                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                @Override
                                                public void onPrepared(MediaPlayer mp) {
                                                    mediaPlayer.start();
                                                    Toast.makeText(view.getContext(), "Reproduciendo Audio.", Toast.LENGTH_SHORT).show();
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

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

//                                map = (Map<String, Object>) dataSnapshot.getValue();
//                                Log.d("MAP", "Value is: " + map);
//                                audio_link = map.get("id_audio").toString();
//                                Log.d("LINK AUDIO", "Value is: " + audio_link);



                            }


                        }else{
                            Toast.makeText(context, "No hay audio nuevos.", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class viewHolderAdapter extends RecyclerView.ViewHolder {
        TextView tv_email;
        ImageView img_user;
        CardView cardView;
        Button btn_grabar, btn_reproducir;

        public viewHolderAdapter(@NonNull View itemView) {
            super(itemView);

            tv_email = itemView.findViewById(R.id.tv_email);
            img_user = itemView.findViewById(R.id.img_user);
            cardView = itemView.findViewById(R.id.cardiew);
            btn_grabar = itemView.findViewById(R.id.btn_grabar);
            btn_reproducir = itemView.findViewById(R.id.btn_escuchar);


        }
    }
}
