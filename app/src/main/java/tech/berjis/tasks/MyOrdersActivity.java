package tech.berjis.tasks;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class MyOrdersActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseFirestoreSettings firestoreSettings;
    String UID;

    ImageView profile, services, home, chats, notifications, settings;
    RecyclerView ordersRecycler;

    private FirestoreRecyclerAdapter<Orders, MyOrdersActivity.OrdersViewHolder> s_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

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
        services = findViewById(R.id.services);
        chats = findViewById(R.id.chats);
        home = findViewById(R.id.home);
        notifications = findViewById(R.id.notifications);
        settings = findViewById(R.id.settings);
        ordersRecycler = findViewById(R.id.ordersRecycler);

        staticOnClicks();
    }

    private void staticOnClicks() {
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyOrdersActivity.this, ProfileActivity.class));
            }
        });
        services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyOrdersActivity.this, MyServicesActivity.class));
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyOrdersActivity.this, SettingsActivity.class));
            }
        });
        chats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyOrdersActivity.this, DMsActivity.class));
            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyOrdersActivity.this, NotificationsActivity.class));
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent c_intent = new Intent(MyOrdersActivity.this, FeedActivity.class);
                Bundle c_bundle = new Bundle();
                c_bundle.putString("category", "");
                c_bundle.putString("location", "");
                c_bundle.putLong("minimum", 0);
                c_bundle.putLong("maximum", 0);
                c_intent.putExtras(c_bundle);
                startActivity(c_intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadUserData();
    }

    private void loadUserData() {
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firestore.collection("Users")
                .document(UID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ordersRecycler.setLayoutManager(new LinearLayoutManager(MyOrdersActivity.this, RecyclerView.VERTICAL, false));
                        loadOrders();
                        String user_type = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("user_type")).toString();
                        if (user_type.equals("tasker")) {
                            services.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void loadOrders() {

        Query userRef = firestore.collection("Orders").
                whereArrayContains("parties", UID)
                .orderBy("time", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Orders> options = new FirestoreRecyclerOptions.Builder<Orders>()
                .setQuery(userRef, Orders.class)
                .build();

        s_adapter = new FirestoreRecyclerAdapter<Orders, OrdersViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull OrdersViewHolder OrdersViewHolder, int i, @NonNull Orders Orders) {
                OrdersViewHolder.setOrders(Orders.getService(), Orders.getSeller(), Orders.getUser(), firestore, Orders.getTime(), Orders.getCurrency(), UID);
            }

            @NonNull
            @Override
            public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service, parent, false);
                return new OrdersViewHolder(view);
            }
        };
        ordersRecycler.setAdapter(s_adapter);
        s_adapter.startListening();
    }

    static class OrdersViewHolder extends RecyclerView.ViewHolder {
        private View view;

        OrdersViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setOrders(final String service, final String seller, final String user, final FirebaseFirestore firestore, final long time, final String currency, final String UID) {

            final TextView serviceTitle = view.findViewById(R.id.serviceTitle);
            final ImageView mainImage = view.findViewById(R.id.mainImage);
            final ImageView call = view.findViewById(R.id.call);
            final TextView serviceDescription = view.findViewById(R.id.serviceDescription);
            final TextView servicePrice = view.findViewById(R.id.servicePrice);

            call.setVisibility(View.VISIBLE);

            if (seller.equals(UID)) {
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorCrimson));
                // itemView.getBackground().setAlpha(45);
                mainImage.setVisibility(View.GONE);
            } else {
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorWhite));
            }

            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (seller.equals(UID)) {
                        call(user, firestore);
                    }
                    if (user.equals(UID)) {
                        call(seller, firestore);
                    }
                }
            });

            firestore.collection("Services").document(service).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                    String text = Objects.requireNonNull(documentSnapshot.get("text")).toString();
                    String category = Objects.requireNonNull(documentSnapshot.get("category")).toString();
                    String price = Objects.requireNonNull(documentSnapshot.get("price")).toString();
                    String requests = Objects.requireNonNull(documentSnapshot.get("requests")).toString();
                    goHome(text, category, price, requests);
                    serviceTitle.setText(category);
                    servicePrice.setText(currency + " " + price);

                    if (seller.equals(UID)) {
                        serviceDescription.setText("Offered To: ");
                    } else {
                        serviceDescription.setText("Offered By: ");
                    }
                }

                private void goHome(final String text, final String category, final String price, final String requests) {
                    serviceTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent o_i = new Intent(itemView.getContext(), MyOrder.class);
                            Bundle o_b = new Bundle();
                            o_b.putString("UID", user);
                            o_b.putString("seller", seller);
                            o_b.putString("serviceID", service);
                            o_b.putString("text", text);
                            o_b.putString("category", category);
                            o_b.putLong("time", time);
                            o_b.putString("currency", currency);
                            o_b.putString("price", price);
                            o_b.putString("requests", requests);
                            o_i.putExtras(o_b);
                            itemView.getContext().startActivity(o_i);
                        }
                    });
                    serviceDescription.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent o_i = new Intent(itemView.getContext(), MyOrder.class);
                            Bundle o_b = new Bundle();
                            o_b.putString("UID", user);
                            o_b.putString("seller", seller);
                            o_b.putString("serviceID", service);
                            o_b.putString("text", text);
                            o_b.putString("category", category);
                            o_b.putLong("time", time);
                            o_b.putString("currency", currency);
                            o_b.putString("price", price);
                            o_b.putString("requests", requests);
                            o_i.putExtras(o_b);
                            itemView.getContext().startActivity(o_i);
                        }
                    });
                    mainImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent o_i = new Intent(itemView.getContext(), MyOrder.class);
                            Bundle o_b = new Bundle();
                            o_b.putString("UID", user);
                            o_b.putString("seller", seller);
                            o_b.putString("serviceID", service);
                            o_b.putString("text", text);
                            o_b.putString("category", category);
                            o_b.putLong("time", time);
                            o_b.putString("currency", currency);
                            o_b.putString("price", price);
                            o_b.putString("requests", requests);
                            o_i.putExtras(o_b);
                            itemView.getContext().startActivity(o_i);
                        }
                    });
                    servicePrice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent o_i = new Intent(itemView.getContext(), MyOrder.class);
                            Bundle o_b = new Bundle();
                            o_b.putString("UID", user);
                            o_b.putString("seller", seller);
                            o_b.putString("serviceID", service);
                            o_b.putString("text", text);
                            o_b.putString("category", category);
                            o_b.putLong("time", time);
                            o_b.putString("currency", currency);
                            o_b.putString("price", price);
                            o_b.putString("requests", requests);
                            o_i.putExtras(o_b);
                            itemView.getContext().startActivity(o_i);
                        }
                    });
                }
            });
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

            dbRef.child("ServicesImages").child(service).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
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
        }

        private void call(String mteja, FirebaseFirestore firestore) {
            final int REQUEST_PHONE_CALL = 1;
            firestore.collection("Users").document(mteja).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String phone = Objects.requireNonNull(documentSnapshot.get("user_phone")).toString();
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                    if (ActivityCompat.checkSelfPermission(itemView.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) itemView.getContext(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                        return;
                    }
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }

}
