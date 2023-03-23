package com.example.myvotingapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myvotingapplication.R;
import com.example.myvotingapplication.activities.AllCandidateActivity;
import com.example.myvotingapplication.activities.Create_Candidate_Activity;
import com.example.myvotingapplication.activities.LoginActivity;
import com.example.myvotingapplication.activities.ResultActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    public static final String PREFERENCES = "prefKey";
    SharedPreferences sharedPreferences;
    public static final String IsLogIn = "islogin";

    private ImageView circleImg;
    private TextView nameTxt, nationalIdTxt;
    private String uid;
    private FirebaseFirestore firebaseFirestore;
    private Button createBtn, voteBtn,startBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseFirestore = FirebaseFirestore.getInstance();
        try {
            uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        } catch (Exception e) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }

        circleImg = findViewById(R.id.circle_image);
        nameTxt = findViewById(R.id.name);
        nationalIdTxt = findViewById(R.id.national_id);
        createBtn = findViewById(R.id.admin_btn);
        voteBtn = findViewById(R.id.give_vote);
        startBtn= findViewById(R.id.candidate_create_voting);


        sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor pref = sharedPreferences.edit();
       pref.putBoolean(IsLogIn, true);
       pref.commit();


        // user do not have to login again if he has already logged in(upar wali lines)

        //findViewById(R.id.log_out).setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //          FirebaseAuth.getInstance().signOut();
        //        pref.putBoolean(IsLogIn,false);
        //      pref.commit();
        //    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        //  finish();
        //   }
        //  });


        firebaseFirestore.collection("Users")
                .document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            String name = task.getResult().getString("name");
                            String nationalId = task.getResult().getString("nationalId");
                            String image = task.getResult().getString("image");


                            if (name != null) {

                                if (name.equals("admin")) {
                                    createBtn.setVisibility(View.VISIBLE);
                                    startBtn.setVisibility(View.VISIBLE);
                                    voteBtn.setVisibility(View.GONE);
                                } else {
                                    createBtn.setVisibility(View.GONE);
                                    startBtn.setVisibility(View.GONE);
                                    voteBtn.setVisibility(View.VISIBLE);
                                }
                                nameTxt.setText(name);
                                nationalIdTxt.setText(nationalId);

                                Glide.with(HomeActivity.this).load(image).into(circleImg);


                            } else {
                                Toast.makeText(HomeActivity.this, "User not found", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, Create_Candidate_Activity.class));

            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AllCandidateActivity.class));

            }
        });

        voteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,AllCandidateActivity.class));

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        SharedPreferences.Editor pref = sharedPreferences.edit();
        switch (id) {
            case R.id.show_result:
                startActivity(new Intent(HomeActivity.this, ResultActivity.class));
                return true;
            case R.id.log_out:
                FirebaseAuth.getInstance().signOut();
                pref.putBoolean(IsLogIn, false);
                pref.commit();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }


    }
}
