package tech.berjis.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyService extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseFirestoreSettings firestoreSettings;
    DatabaseReference dbRef;

    ViewPager2 imagePager;
    List<ImageList> imageList;
    ImagePagerAdapter imagePagerAdapter;
    String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_service);

        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firestoreSettings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build();
        firestore.setFirestoreSettings(firestoreSettings);
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = mAuth.getCurrentUser().getUid();
        imageList = new ArrayList<>();

        imagePager = findViewById(R.id.imagePager);

        startPage();
    }

    private void startPage() {
        Intent s_i = getIntent();
        Bundle s_b = s_i.getExtras();
        String serviceID = s_b.getString("service");
        loadImages(serviceID);
        loadOffer(serviceID);
    }

    private void loadImages(String serviceID) {
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
                    imagePagerAdapter = new ImagePagerAdapter(MyService.this, imageList, "new_service");
                    imagePagerAdapter.notifyDataSetChanged();
                    imagePager.setAdapter(imagePagerAdapter);
                }else{
                    Toast.makeText(MyService.this, "Don't Exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadOffer(String serviceID) {
    }
}
