package com.example.myvotingapplication.activities;

import static com.example.myvotingapplication.activities.LoginActivity.UploadData;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myvotingapplication.R;
import com.example.myvotingapplication.UcropperActivity;
import com.example.myvotingapplication.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class SignupActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private ImageView userProfile;
    private EditText userName, userPassword, userEmail, userNationalID;
    private Button signUpBtn;
    private FirebaseAuth mAuth;
    private  Uri uri=null;
   // private ImageView iv_pick_image;
    //private CropImageActivity cropImage;
    ActivityResultLauncher<String> mGetContent;
    ActivityResultLauncher<String> cropImage;

    public static final String PREFERENCES= "prefKey";
    public static final String Name= "nameKey";
    public static final String Email= "emailKey";
    public static final String Password= "passwordKey";
    public static final String NationalId= "nationalIdKey";
    public static final String Image= "imageKey";

    SharedPreferences sharedPreferences;

    String name,password,email,nationalId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding= ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(R.layout.activity_signup);

        sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES,MODE_PRIVATE);

        //setContentView(binding.getRoot());

        cropImage=registerForActivityResult(new ActivityResultContracts.GetContent(),result -> {
            Intent intent=new Intent(SignupActivity.this.getApplicationContext(), UcropperActivity.class);
            intent.putExtra("SendImageData",result.toString());

            startActivityForResult(intent,100);

        });


        findViewById(R.id.profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePermission();
            }
        });

        findViewById(R.id.have_an_acc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        userProfile = findViewById(R.id.profile_image);
        userName = findViewById(R.id.user_name);
        userPassword = findViewById(R.id.user_password);
        userEmail = findViewById(R.id.user_email);
        userNationalID = findViewById(R.id.user_national_id);
        signUpBtn = findViewById(R.id.signup_btn);
        mAuth=FirebaseAuth.getInstance();
        //iv_pick_image = findViewById(R.id.iv_pick_image);


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 name=userName.getText().toString().trim();
                 password=userPassword.getText().toString().trim();
                 email=userEmail.getText().toString().trim();
                 nationalId=userNationalID.getText().toString().trim();



                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches() && !TextUtils.isEmpty(nationalId)){

                    createUser(email,password);


                }else{
                    Toast.makeText(SignupActivity.this,"Please enter your credentials",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void ImagePermission() {

        Dexter.withContext(SignupActivity.this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        cropImage.launch("image/*");
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(SignupActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();

                    }
                }).check();

    }




    private void createUser(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    Toast.makeText(SignupActivity.this,"User Created",Toast.LENGTH_SHORT).show();

                    verifyEmail();

                }else{
                    Toast.makeText(SignupActivity.this,"Fail Tr Again",Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(SignupActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void verifyEmail() {

        FirebaseUser user= mAuth.getCurrentUser();
        if(user!=null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){

                        SharedPreferences.Editor pref= sharedPreferences.edit();
                        pref.putString(Name,name);
                        pref.putString(Password,password);
                        pref.putString(Email,email);
                        pref.putString(NationalId,nationalId);
                        pref.putString(Image,uri.toString());
                        pref.putBoolean(UploadData, false);
                        pref.commit();


                        //email sent

                        Toast.makeText(SignupActivity.this,"Email sent",Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(SignupActivity.this,LoginActivity.class));
                        finish();


                    }else{
                        mAuth.signOut();
                        finish();
                    }
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==101 && requestCode==100)
        {
            String result=data.getStringExtra("CROP");
            uri=data.getData();

            if(result!=null)
            {
                uri=Uri.parse(result);
            }

            userProfile.setImageURI(uri);
        }
    }
}

