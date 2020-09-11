package tech.berjis.tasks;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Objects;

public class MyServicesActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseFirestoreSettings firestoreSettings;
    private FirestoreRecyclerAdapter<Services, MyServicesActivity.ServicesViewHolder> adapter;
    String UID, currency_symbol = "";
    ImageView profile, orders, home, chats, notifications, addTask;
    RecyclerView ordersRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_services);

        init_vars();
    }

    private void init_vars() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firestoreSettings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build();
        firestore.setFirestoreSettings(firestoreSettings);
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();


        profile = findViewById(R.id.profile);
        orders = findViewById(R.id.orders);
        chats = findViewById(R.id.chats);
        home = findViewById(R.id.home);
        notifications = findViewById(R.id.notifications);
        addTask = findViewById(R.id.addTask);
        ordersRecycler = findViewById(R.id.ordersRecycler);

        statickOnClicks();
    }


    private void loaduserdata() {
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firestore.collection("Users")
                .document(UID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        currency_symbol = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("currency_symbol")).toString();
                        loadServices();
                    }
                });
    }

    private void statickOnClicks() {
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyServicesActivity.this, ProfileActivity.class));
            }
        });
        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyServicesActivity.this, MyOrdersActivity.class));
            }
        });
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyServicesActivity.this, NewServiceActivity.class));
            }
        });
        chats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyServicesActivity.this, DMsActivity.class));
            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyServicesActivity.this, NotificationsActivity.class));
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent c_intent = new Intent(MyServicesActivity.this, FeedActivity.class);
                Bundle c_bundle = new Bundle();
                c_bundle.putString("category", "");
                c_bundle.putString("location", "");
                c_bundle.putString("minimum", "");
                c_bundle.putString("maximum", "");
                c_intent.putExtras(c_bundle);
                startActivity(c_intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loaduserdata();
    }

    private void loadServices() {
        ordersRecycler.setLayoutManager(new LinearLayoutManager(MyServicesActivity.this, RecyclerView.VERTICAL, false));
        FirestoreRecyclerOptions<Services> options = new FirestoreRecyclerOptions.Builder<Services>()
                .setQuery(firestore.collection("Services").whereEqualTo("user", UID), Services.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Services, MyServicesActivity.ServicesViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull MyServicesActivity.ServicesViewHolder ServicesViewHolder, int i, @NonNull Services Services) {
                ServicesViewHolder.setServices(Services.getService_id(), Services.getCategory(), Services.getLocation(), Services.getPrice(), Services.getRequests(), Services.getText(), Services.getTime(), Services.getUser(), currency_symbol);
            }

            @NonNull
            @Override
            public MyServicesActivity.ServicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service, parent, false);
                return new MyServicesActivity.ServicesViewHolder(view);
            }
        };
        ordersRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    static class ServicesViewHolder extends RecyclerView.ViewHolder {
        private View view;

        ServicesViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setServices(final String Service_id,
                         final String Category,
                         final String Location,
                         final long Price,
                         final long Requests,
                         final String Text,
                         final long Time,
                         String User,
                         String currency_symbol) {

            final TextView serviceTitle = view.findViewById(R.id.serviceTitle);
            final ImageView mainImage = view.findViewById(R.id.mainImage);
            final TextView servicePrice = view.findViewById(R.id.servicePrice);
            final TextView serviceDescription = view.findViewById(R.id.serviceDescription);

            serviceTitle.setText(Category);
            servicePrice.setText(currency_symbol + " " + Price);
            serviceDescription.setText(Requests + " requests");

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
    }
}
