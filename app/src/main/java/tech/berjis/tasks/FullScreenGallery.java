package tech.berjis.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class FullScreenGallery extends AppCompatActivity {

    DatabaseReference dbRef;

    List<ImageList> imageList;
    ImagePagerAdapter imagePagerAdapter;
    ViewPager2 imagePager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_full_screen_gallery);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        imagePager = findViewById(R.id.imagePager);
        imageList = new ArrayList<>();

        Intent imageIntent = getIntent();
        Bundle imageBundle = imageIntent.getExtras();
        String parent = imageBundle.getString("parent");
        loadImages(parent);
    }

    private void loadImages(String parent) {
        imageList.clear();
        dbRef.child("ServicesImages").child(parent).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot imagesSnapshot : dataSnapshot.getChildren()) {
                        ImageList l = imagesSnapshot.getValue(ImageList.class);
                        imageList.add(l);
                    }
                    Collections.reverse(imageList);
                    imagePagerAdapter = new ImagePagerAdapter(FullScreenGallery.this, imageList, "gallery");
                    imagePagerAdapter.notifyDataSetChanged();
                    imagePager.setAdapter(imagePagerAdapter);
                    ScrollingPagerIndicator recyclerIndicator = findViewById(R.id.indicator);
                    recyclerIndicator.attachToPager(imagePager);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
