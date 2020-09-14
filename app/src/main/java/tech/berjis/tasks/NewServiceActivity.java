package tech.berjis.tasks;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import com.vanniktech.emoji.EmojiEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class NewServiceActivity extends AppCompatActivity implements OnMapReadyCallback {

    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    GoogleMap mMap;
    final int REQUEST_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;

    TextView newImage, saveButton;
    ViewPager2 imagePager;
    EmojiEditText serviceText;

    List<ImageList> imageList;
    ImagePagerAdapter imagePagerAdapter;

    StorageReference storageReference;
    Uri filePath;
    String UID, serviceID = "", hasImage = "";

    SearchableSpinner categorySpinner;
    EditText price;
    String category_name = "";
    ImageView home, chats, notifications, settings;
    SupportMapFragment mapFragment;
    Button selectLocation;
    double myLat, myLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_service);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        storageReference = FirebaseStorage.getInstance().getReference();
        UID = mAuth.getCurrentUser().getUid();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        long unixTime = System.currentTimeMillis() / 1000L;
        serviceID = UID + "_" + unixTime;

        imageList = new ArrayList<>();

        imagePager = findViewById(R.id.imagePager);
        newImage = findViewById(R.id.newImage);
        saveButton = findViewById(R.id.saveButton);
        serviceText = findViewById(R.id.serviceText);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.my_location);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        categorySpinner = findViewById(R.id.categorySpinner);
        chats = findViewById(R.id.chats);
        home = findViewById(R.id.home);
        notifications = findViewById(R.id.notifications);
        settings = findViewById(R.id.settings);
        price = findViewById(R.id.price);
        selectLocation = findViewById(R.id.selectLocation);

        loadSpinners();
        staticOnClicks();
        loadImages();
        getUserArea();
    }

    private void loadSpinners() {
        final List<String> categories = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.add("Select Category");
        dbRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot areaSnapshot : snapshot.getChildren()) {
                    String subject = areaSnapshot.child("name").getValue(String.class);
                    categories.add(subject);
                }
                adapter.notifyDataSetChanged();
                categorySpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void staticOnClicks() {
        newImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceText();
            }
        });

        categorySpinner.setTitle("Choose category");
        categorySpinner.setPositiveButton("Cancel");
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    category_name = categorySpinner.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewServiceActivity.this, SettingsActivity.class));
            }
        });
        chats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewServiceActivity.this, DMsActivity.class));
            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewServiceActivity.this, NotificationsActivity.class));
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent c_intent = new Intent(NewServiceActivity.this, FeedActivity.class);
                Bundle c_bundle = new Bundle();
                c_bundle.putString("category", "");
                c_bundle.putString("location", "");
                c_bundle.putLong("minimum", 0);
                c_bundle.putLong("maximum", 0);
                c_intent.putExtras(c_bundle);
                startActivity(c_intent);
            }
        });
        selectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserArea();
            }
        });
    }

    private void loadImages() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        imageList.clear();
        dbRef.child("ServicesImages").child(serviceID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot imagesSnapshot : dataSnapshot.getChildren()) {
                        ImageList l = imagesSnapshot.getValue(ImageList.class);
                        imageList.add(l);
                    }
                    Collections.reverse(imageList);
                    imagePagerAdapter = new ImagePagerAdapter(NewServiceActivity.this, imageList, "new_service");
                    imagePagerAdapter.notifyDataSetChanged();
                    imagePager.setAdapter(imagePagerAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

            final StorageReference ref = storageReference.child("Services Images/" + serviceID + unixTime + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    final String image_url = downloadUrl.toString();
                                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

                                    final DatabaseReference[] imageRef = {dbRef.child("ServicesImages").child(serviceID).push()};
                                    String image_id = imageRef[0].getKey();
                                    imageRef[0].child("image_id").setValue(image_id);
                                    imageRef[0].child("parent_id").setValue(serviceID);
                                    imageRef[0].child("image").setValue(image_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                imageRef[0] = null;
                                                hasImage = "hasImage";
                                                loadImages();
                                                progressDialog.dismiss();
                                            }
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
                            Toast.makeText(NewServiceActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void serviceText() {

        if (category_name.equals("")) {
            new AlertDialog
                    .Builder(this)
                    .setMessage("You need to specify what you're offering")
                    .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            return;
        }

        String text = serviceText.getText().toString();
        String bei = price.getText().toString();
        long unixTime = System.currentTimeMillis() / 1000L;

        Toast.makeText(this, "Service succesfully published", Toast.LENGTH_SHORT).show();

        HashMap<String, Object> user = new HashMap<>();

        user.put("text", text);
        user.put("category", category_name);
        user.put("lat", myLat);
        user.put("long", myLong);
        user.put("price", Long.parseLong(bei));
        user.put("service_id", serviceID);
        user.put("user", UID);
        user.put("time", unixTime);
        user.put("requests", 0);

        dbRef.child("Services").child(serviceID).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                NewServiceActivity.super.finish();
            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng nairobi = new LatLng(-1.3031934, 36.5672003);
        mMap.addMarker(new MarkerOptions()
                .position(nairobi)
                .title("Nairobi"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(nairobi));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15F));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                myLat = latLng.latitude;
                myLong = latLng.longitude;
                Toast.makeText(NewServiceActivity.this, myLat + "," + myLong, Toast.LENGTH_SHORT).show();
                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Nairobi"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15F));
            }
        });
    }

    private void getUserArea() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("My Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15F));
                            myLat = latLng.latitude;
                            myLong = latLng.longitude;
                            Toast.makeText(NewServiceActivity.this, String.valueOf(latLng), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
