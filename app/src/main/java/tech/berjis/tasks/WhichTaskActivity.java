package tech.berjis.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class WhichTaskActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseFirestoreSettings firestoreSettings;
    private FirestoreRecyclerAdapter<Categories, CategoriesViewHolder> adapter;
    SearchableSpinner categorySpinner, locationSpinner;
    RecyclerView categoryRecycler;
    TextView searchButton;
    EditText max, min;
    String category_name, location;
    ImageView profile, services, orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_which_task);

        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firestoreSettings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build();
        firestore.setFirestoreSettings(firestoreSettings);

        categorySpinner = findViewById(R.id.categorySpinner);
        locationSpinner = findViewById(R.id.locationSpinner);
        categoryRecycler = findViewById(R.id.categoryRecycler);
        searchButton = findViewById(R.id.searchButton);
        max = findViewById(R.id.maximumAmount);
        min = findViewById(R.id.minimumAmount);
        profile = findViewById(R.id.profile);
        orders = findViewById(R.id.orders);
        services = findViewById(R.id.services);

        loadSpinners();
        staticOnClicks();
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

        locationSpinner.setTitle("Choose location");
        locationSpinner.setPositiveButton("Cancel");
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    location = locationSpinner.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String maximum = max.getText().toString();
                final String minimum = min.getText().toString();

                final Intent c_intent = new Intent(WhichTaskActivity.this, FeedActivity.class);
                Bundle c_bundle = new Bundle();
                c_bundle.putString("category", category_name);
                c_bundle.putString("location", location);
                c_bundle.putString("minimum", minimum);
                c_bundle.putString("maximum", maximum);
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
        categorySpinner.setAdapter(adapter);
        firestore.collection("Categories").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        String subject = document.getString("name");
                        categories.add(subject);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        categoryRecycler.setLayoutManager(new LinearLayoutManager(WhichTaskActivity.this, RecyclerView.HORIZONTAL, false));
        FirestoreRecyclerOptions<Categories> options = new FirestoreRecyclerOptions.Builder<Categories>()
                .setQuery(firestore.collection("Categories"), Categories.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Categories, CategoriesViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull CategoriesViewHolder CategoriesViewHolder, int i, @NonNull Categories Categories) {
                CategoriesViewHolder.setCategories(Categories.getName(), Categories.getImage());
            }

            @NonNull
            @Override
            public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories, parent, false);
                return new CategoriesViewHolder(view);
            }
        };
        categoryRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private static class CategoriesViewHolder extends RecyclerView.ViewHolder {
        private View view;

        CategoriesViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setCategories(final String c_name, String c_image) {
            final TextView name = view.findViewById(R.id.name);
            CircleImageView image = view.findViewById(R.id.image);
            name.setText(c_name);
            Picasso.get().load(c_image).into(image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent c_intent = new Intent(view.getContext(), FeedActivity.class);
                    Bundle c_bundle = new Bundle();
                    c_bundle.putString("category", c_name);
                    c_bundle.putString("location", "");
                    c_bundle.putLong("minimum", 0);
                    c_bundle.putLong("maximum", 0);
                    c_intent.putExtras(c_bundle);
                    view.getContext().startActivity(c_intent);
                }
            });
        }
    }
}

