package tech.berjis.tasks;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseFirestoreSettings firestoreSettings;
    private FirestoreRecyclerAdapter<Categories, FeedActivity.CategoriesViewHolder> adapter;
    private FirestoreRecyclerAdapter<Services, FeedActivity.ServicesViewHolder> s_adapter;

    RecyclerView categoryRecycler, postsRecycler;
    ImageView search, services, orders, profile, chats, notifications;
    String UID, currency_symbol = "", category, location;
    long minimum, maximum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firestoreSettings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build();
        firestore.setFirestoreSettings(firestoreSettings);
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        categoryRecycler = findViewById(R.id.categoryRecycler);
        postsRecycler = findViewById(R.id.postsRecycler);
        search = findViewById(R.id.search);
        services = findViewById(R.id.services);
        orders = findViewById(R.id.orders);
        profile = findViewById(R.id.profile);
        chats = findViewById(R.id.chats);
        notifications = findViewById(R.id.notifications);

        staticOnClick();
        loadWorkers();
    }

    private void staticOnClick() {
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, WhichTaskActivity.class));
            }
        });
        services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, MyServicesActivity.class));
            }
        });
        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, MyOrdersActivity.class));
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, ProfileActivity.class));
            }
        });
        chats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, DMsActivity.class));
            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedActivity.this, NotificationsActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadWorkers();
    }

    private void loadWorkers() {
        Intent c_intent = getIntent();
        Bundle c_bundle = c_intent.getExtras();

        assert c_bundle != null;
        category = c_bundle.getString("category");
        location = c_bundle.getString("location");
        minimum = c_bundle.getLong("minimum");
        maximum = c_bundle.getLong("maximum");

        loaduserdata();
        loadcategories();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
        if (s_adapter != null) {
            s_adapter.stopListening();
        }
    }

    private void loadcategories() {
        categoryRecycler.setLayoutManager(new LinearLayoutManager(FeedActivity.this, RecyclerView.HORIZONTAL, false));
        FirestoreRecyclerOptions<Categories> options = new FirestoreRecyclerOptions.Builder<Categories>()
                .setQuery(firestore.collection("Categories"), Categories.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Categories, FeedActivity.CategoriesViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull FeedActivity.CategoriesViewHolder CategoriesViewHolder, int i, @NonNull Categories Categories) {
                CategoriesViewHolder.setCategories(Categories.getName(), Categories.getImage());
            }

            @NonNull
            @Override
            public FeedActivity.CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories, parent, false);
                return new FeedActivity.CategoriesViewHolder(view);
            }
        };
        categoryRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    private void loaduserdata() {
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firestore.collection("Users")
                .document(UID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String user_type = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("user_type")).toString();
                        currency_symbol = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("currency_symbol")).toString();
                        loadServices();
                        if (user_type.equals("tasker")) {
                            services.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void loadServices() {
        CollectionReference servicesReference = firestore.collection("Services");

        if (category != null && !category.equals("")) {
            servicesReference.whereEqualTo("category", category);
        }
        servicesReference.whereGreaterThanOrEqualTo("price", minimum);
        if (maximum > 0) {
            servicesReference.whereLessThanOrEqualTo("price", maximum);
        }

        postsRecycler.setLayoutManager(new LinearLayoutManager(FeedActivity.this, RecyclerView.VERTICAL, false));
        FirestoreRecyclerOptions<Services> options = new FirestoreRecyclerOptions.Builder<Services>()
                .setQuery(servicesReference, Services.class)
                .build();

        s_adapter = new FirestoreRecyclerAdapter<Services, FeedActivity.ServicesViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull FeedActivity.ServicesViewHolder ServicesViewHolder, int i, @NonNull Services Services) {
                ServicesViewHolder.setServices(Services.getService_id(), Services.getCategory(), Services.getLocation(), Services.getPrice(), Services.getRequests(), Services.getText(), Services.getTime(), Services.getUser(), currency_symbol, firestore);
            }

            @NonNull
            @Override
            public FeedActivity.ServicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service, parent, false);
                return new FeedActivity.ServicesViewHolder(view);
            }
        };
        postsRecycler.setAdapter(s_adapter);
        s_adapter.startListening();
    }

    static class CategoriesViewHolder extends RecyclerView.ViewHolder {
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

    static class ServicesViewHolder extends RecyclerView.ViewHolder {
        private View view;

        ServicesViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setServices(final String Service_id, final String Category, final String Location, final long Price, final long Requests, final String Text, final long Time, final String User, String currency_symbol, final FirebaseFirestore firestore) {

            final TextView serviceTitle = view.findViewById(R.id.serviceTitle);
            final ImageView mainImage = view.findViewById(R.id.mainImage);
            final TextView servicePrice = view.findViewById(R.id.servicePrice);

            serviceTitle.setText(Category);
            servicePrice.setText(currency_symbol + " " + Price);

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

            dbRef.child("ServicesImages").child(Service_id).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                            Glide.with(itemView.getContext()).load(Objects.requireNonNull(npsnapshot.child("image").getValue()).toString()).thumbnail(0.25f).into(mainImage);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(itemView.getContext(), "Kuna shida mahali", Toast.LENGTH_SHORT).show();
                }
            });

            firestore.collection("Orders").whereEqualTo("service", Service_id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    long count = queryDocumentSnapshots.size();
                    kitu(count, firestore, User);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent s_i = new Intent(itemView.getContext(), MyService.class);
                    Bundle s_b = new Bundle();
                    s_b.putString("service", Service_id);
                    s_i.putExtras(s_b);
                    itemView.getContext().startActivity(s_i);
                }
            });
        }

        private void kitu(final long count, FirebaseFirestore firestore, String user) {
            final TextView serviceDescription = view.findViewById(R.id.serviceDescription);
            firestore.collection("Users").document(user).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String user_name = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("first_name")).toString() + " " + Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("last_name")).toString();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        serviceDescription.setText(Html.fromHtml(user_name + " <br /><small>(Requested " + count + " times)</small>", Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        serviceDescription.setText(Html.fromHtml(user_name + " <br /><small>(Requested " + count + " times)</small>"));
                    }
                }
            });
        }
    }


}
