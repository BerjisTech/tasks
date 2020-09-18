package tech.berjis.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WhichTaskActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    List<Categories> categoriesList;
    CategoriesAdapter CategoriesAdapter;

    SearchableSpinner categorySpinner;
    RecyclerView categoryRecycler;
    TextView searchButton;
    String category_name, location;
    ImageView profile, services, orders, settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_which_task);

        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        categoriesList = new ArrayList<>();

        categorySpinner = findViewById(R.id.categorySpinner);
        categoryRecycler = findViewById(R.id.categoryRecycler);
        searchButton = findViewById(R.id.searchButton);
        profile = findViewById(R.id.profile);
        orders = findViewById(R.id.orders);
        services = findViewById(R.id.services);
        settings = findViewById(R.id.settings);

        loadSpinners();
        loadcategories();
        staticOnClicks();
        loaduserdata();
    }

    private void staticOnClicks() {

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WhichTaskActivity.this, ProfileActivity.class));
            }
        });

        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WhichTaskActivity.this, MyOrdersActivity.class));
            }
        });

        services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WhichTaskActivity.this, MyServicesActivity.class));
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WhichTaskActivity.this, SettingsActivity.class));
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

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final Intent c_intent = new Intent(WhichTaskActivity.this, ByCategoryActivity.class);
                Bundle c_bundle = new Bundle();
                c_bundle.putString("category", category_name);
                c_intent.putExtras(c_bundle);

                final ImageView preload = findViewById(R.id.preload);
                preload.setVisibility(View.VISIBLE);

                Glide.with(WhichTaskActivity.this).asGif().load(R.drawable.preloader).into(preload);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                startActivity(c_intent);
                            }
                        },
                        500);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                preload.setVisibility(View.GONE);
                            }
                        },
                        1000);
            }
        });
    }

    private void loadSpinners() {
        final List<String> categories = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.add("Choose tasker");
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


    private void loadcategories() {
        categoriesList.clear();
        categoriesList.add(new Categories("All Categories", "drawable://" + R.drawable.plus));
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        dbRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Categories l = snap.getValue(Categories.class);
                        categoriesList.add(l);
                    }
                }
                CategoriesAdapter = new CategoriesAdapter(WhichTaskActivity.this, categoriesList);
                categoryRecycler.setAdapter(CategoriesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loaduserdata() {
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        dbRef.child("Users")
                .child(UID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String user_type = snapshot.child("user_type").getValue().toString();
                        if (snapshot.child("subscription").exists()) {
                            long today = System.currentTimeMillis() / 1000L;
                            long nextMonth = Long.parseLong(Objects.requireNonNull(snapshot.child("next_month").getValue()).toString());

                            if (today > nextMonth) {
                                Intent mainActivity = new Intent(getApplicationContext(), RenewSubscriptionActivity.class);
                                startActivity(mainActivity);
                                finish();
                            } else {
                                if (user_type.equals("tasker")) {
                                    services.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            Intent mainActivity = new Intent(getApplicationContext(), ChooseSubscriptionActivity.class);
                            startActivity(mainActivity);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}

