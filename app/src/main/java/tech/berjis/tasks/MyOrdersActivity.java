package tech.berjis.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyOrdersActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID, currency_symbol = "";

    List<Orders> ordersList;
    OrdersAdapter ordersAdapter;

    ImageView profile, services, home, chats, notifications, settings;
    RecyclerView ordersRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        init_vars();
    }

    private void init_vars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        ordersList = new ArrayList<>();
        profile = findViewById(R.id.profile);
        services = findViewById(R.id.services);
        chats = findViewById(R.id.chats);
        home = findViewById(R.id.home);
        notifications = findViewById(R.id.notifications);
        settings = findViewById(R.id.settings);
        ordersRecycler = findViewById(R.id.ordersRecycler);

        staticOnClicks();
        loadUserData();
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
        dbRef.child("Users")
                .child(UID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currency_symbol = snapshot.child("currency_symbol").getValue().toString();
                        ordersRecycler.setLayoutManager(new LinearLayoutManager(MyOrdersActivity.this, RecyclerView.VERTICAL, false));
                        loadOrders();
                        String user_type = snapshot.child("user_type").getValue().toString();
                        if (user_type.equals("tasker")) {
                            services.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders() {
        ordersRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        dbRef.child("Orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (snap.child("user").getValue().toString().equals(UID) || snap.child("seller").getValue().toString().equals(UID)) {
                            Orders l = snap.getValue(Orders.class);
                            ordersList.add(l);
                        }
                    }
                }
                ordersAdapter = new OrdersAdapter(MyOrdersActivity.this, ordersList, currency_symbol);
                ordersRecycler.setAdapter(ordersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
