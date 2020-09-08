package tech.berjis.tasks;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore dbFire;
    FirebaseFirestoreSettings fireSettings;
    String UID, U_Phone;
    StorageReference storageReference;
    Uri filePath;

    ImageView back, edit;
    CircleImageView dp;
    TextView updateProfile;
    EditText userEmail, userName, firstName, lastName, userDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_edit_profile);

        initLayouts();
        staticOnClicks();
        loadUserData();
    }

    private void initLayouts() {
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        dbFire = FirebaseFirestore.getInstance();
        fireSettings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        dbFire.setFirestoreSettings(fireSettings);
        UID = mAuth.getCurrentUser().getUid();
        U_Phone = mAuth.getCurrentUser().getPhoneNumber();

        edit = findViewById(R.id.edit);
        dp = findViewById(R.id.dp);
        back = findViewById(R.id.back);
        userEmail = findViewById(R.id.userEmail);
        userName = findViewById(R.id.userName);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        updateProfile = findViewById(R.id.updateProfile);
        userDescription = findViewById(R.id.userDescription);
    }

    private void staticOnClicks() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    private void selectImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
        }*/

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error : " + error, Toast.LENGTH_SHORT).show();
            }
        }

        postImage();
    }

    private void postImage() {
        long unixTime = System.currentTimeMillis() / 1000L;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        if (filePath != null) {

            final StorageReference ref = storageReference.child("Profile Images/" + UID + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    final String image_url = downloadUrl.toString();
                                    Map<String, Object> user = new HashMap<>();

                                    user.put("user_image", image_url);

                                    dbFire.collection("Users").document(UID).set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            loadUserData();
                                            progressDialog.dismiss();
                                        }
                                    });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        } else {
            progressDialog.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateProfile() {

        String user_email = userEmail.getText().toString();
        String user_name = userName.getText().toString();
        String first_name = firstName.getText().toString();
        String last_name = lastName.getText().toString();
        String user_description = userDescription.getText().toString();


        if (user_name.isEmpty()) {
            userName.setError("You need a user name", getDrawable(android.R.drawable.ic_dialog_alert));
            userName.requestFocus();
            return;
        }

        if (first_name.isEmpty()) {
            firstName.setError("This field is required", getDrawable(android.R.drawable.ic_dialog_alert));
            firstName.requestFocus();
            return;
        }

        if (last_name.isEmpty()) {
            lastName.setError("This field is required", getDrawable(android.R.drawable.ic_dialog_alert));
            lastName.requestFocus();
            return;
        }

        if (user_email.isEmpty()) {
            userEmail.setError("This field is required to process your payments", getDrawable(android.R.drawable.ic_dialog_alert));
            userEmail.requestFocus();
            return;
        }

        if (user_description.isEmpty()) {
            userDescription.setError("This field is required", getDrawable(android.R.drawable.ic_dialog_alert));
            userDescription.requestFocus();
            return;
        }

        Map<String, Object> user = new HashMap<>();

        user.put("user_name", user_name);
        user.put("first_name", first_name);
        user.put("last_name", last_name);
        user.put("user_email", user_email);
        user.put("user_description", user_description);

        dbFire.collection("Users").document(UID).set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(EditProfileActivity.this, "Profile Successfully Update", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dbFire.collection("Users").document(UID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String user_email = Objects.requireNonNull(documentSnapshot.getData().get("user_email")).toString();
                String user_name = Objects.requireNonNull(documentSnapshot.getData().get("user_name")).toString();
                String first_name = Objects.requireNonNull(documentSnapshot.getData().get("first_name")).toString();
                String last_name = Objects.requireNonNull(documentSnapshot.getData().get("last_name")).toString();
                if (user_email.equals("") ||
                        user_name.equals("") ||
                        first_name.equals("") ||
                        last_name.equals("")) {
                    new AlertDialog
                            .Builder(EditProfileActivity.this)
                            .setTitle("Incomplete Profile")
                            .setMessage("You have a few important profile details that you haven't updated")
                            .setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    EditProfileActivity.super.finish();
                                }
                            })
                            .setNegativeButton("Later", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    EditProfileActivity.super.finish();
                }
            }
        });
    }

    private void loadUserData() {
        dbFire.collection("Users").document(UID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String user_description = Objects.requireNonNull(documentSnapshot.getData().get("user_description")).toString();
                String user_email = Objects.requireNonNull(documentSnapshot.getData().get("user_email")).toString();
                String user_name = Objects.requireNonNull(documentSnapshot.getData().get("user_name")).toString();
                String first_name = Objects.requireNonNull(documentSnapshot.getData().get("first_name")).toString();
                String last_name = Objects.requireNonNull(documentSnapshot.getData().get("last_name")).toString();
                String user_image = Objects.requireNonNull(documentSnapshot.getData().get("user_image")).toString();

                if(!user_image.equals("")){
                    Picasso.get().load(user_image).error(R.drawable.logo).into(dp);
                }
                firstName.setText(first_name);
                lastName.setText(last_name);
                userName.setText(user_name);
                userEmail.setText(user_email);
                userDescription.setText(user_description);
            }
        });
    }
}
