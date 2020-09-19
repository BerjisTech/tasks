package tech.berjis.tasks;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllCategoriesActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    RecyclerView categoryRecycler;
    DatabaseReference dbRef;

    List<Categories> categoriesList;
    AllCategoriesAdapter allCategoriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_categories);

        categoryRecycler = findViewById(R.id.categoryRecycler);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        categoryRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        categoriesList = new ArrayList<>();
        dbRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Categories l = snap.getValue(Categories.class);
                    categoriesList.add(l);
                }
                Collections.shuffle(categoriesList);
                allCategoriesAdapter = new AllCategoriesAdapter(AllCategoriesActivity.this, categoriesList);
                categoryRecycler.setAdapter(allCategoriesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}