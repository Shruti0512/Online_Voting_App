package com.example.myvotingapplication.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myvotingapplication.HomeActivity;
import com.example.myvotingapplication.R;
import com.example.myvotingapplication.UcropperActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.HashMap;
import java.util.Map;

public class Create_Candidate_Activity extends AppCompatActivity {

    private ImageView candidateImg;
    private EditText candidateName,candidateParty;
    private Spinner candidateSpinner;
    private String [] candPost = {"President","Prime-Minister"};
    private Button submitBtn;
    private Uri uri=null;
    ActivityResultLauncher<String> cropImage;
    StorageReference reference;
    FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_candidate);

        reference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        cropImage=registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            Intent intent=new Intent(Create_Candidate_Activity.this.getApplicationContext(), UcropperActivity.class);
            intent.putExtra("SendImageData",result.toString());

            startActivityForResult(intent,100);

        });

        candidateImg=findViewById(R.id.candidate_image);
        candidateName=findViewById(R.id.candidate_name);
        candidateParty=findViewById(R.id.candidate_party_name);
        candidateSpinner=findViewById(R.id.candidate_spinner);
        submitBtn=findViewById(R.id.candidate_submit_btn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,candPost);

        candidateSpinner.setAdapter(adapter);



        candidateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePermission();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = candidateName.getText().toString().trim();
                String party = candidateParty.getText().toString().trim();
                String post= candidateSpinner.getSelectedItem().toString();

                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(party) && !TextUtils.isEmpty(post) && uri!=null){

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    StorageReference imagePath = reference.child("candidate_img").child(uid + ".jpg");
                    imagePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        Map<String, Object> map = new HashMap<>();
                                        map.put("name", name);
                                        map.put("party", party);
                                        map.put("post", post);
                                        map.put("image", uri.toString());
                                        map.put("timestamp", FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Candidate")
                                                .add(map)
                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if(task.isSuccessful()){
                                                            startActivity(new Intent(Create_Candidate_Activity.this, HomeActivity.class));
                                                            finish();
                                                        }else{
                                                            Toast.makeText(Create_Candidate_Activity.this,"Data not store",Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                });

                                    }
                                });

                            } else {
                                Toast.makeText(Create_Candidate_Activity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });



                }else{
                    Toast.makeText(Create_Candidate_Activity.this,"Enter details",Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void ImagePermission() {

        Dexter.withContext(Create_Candidate_Activity.this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        cropImage.launch("image/*");
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(Create_Candidate_Activity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();

                    }
                }).check();

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

            candidateImg.setImageURI(uri);
        }
    }
}