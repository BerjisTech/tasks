package tech.berjis.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class MyService extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseFirestoreSettings firestoreSettings;
    DatabaseReference dbRef;

    ViewPager2 imagePager;
    List<ImageList> imageList;
    ImagePagerAdapter imagePagerAdapter;
    String UID;
    TextView serviceName, servicePrice, serviceRequests, serviceUser, serviceDescription, requestService;
    ImageView profile, services, orders, home, chats, notifications, settings;

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
        serviceName = findViewById(R.id.serviceName);
        servicePrice = findViewById(R.id.servicePrice);
        serviceRequests = findViewById(R.id.serviceRequests);
        serviceUser = findViewById(R.id.serviceUser);
        serviceDescription = findViewById(R.id.serviceDescription);
        requestService = findViewById(R.id.requestService);

        profile = findViewById(R.id.profile);
        services = findViewById(R.id.services);
        orders = findViewById(R.id.orders);
        chats = findViewById(R.id.chats);
        home = findViewById(R.id.home);
        notifications = findViewById(R.id.notifications);
        settings = findViewById(R.id.settings);

        startPage();
    }

    private void staticOnClicks(final String serviceID, final String user, final String price, final String name, final String text, final long requests, final String currency) {
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyService.this, ProfileActivity.class));
            }
        });
        services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyService.this, MyServicesActivity.class));
            }
        });
        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyService.this, MyOrdersActivity.class));
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyService.this, SettingsActivity.class));
            }
        });
        chats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyService.this, DMsActivity.class));
            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyService.this, NotificationsActivity.class));
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });
        requestService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestService(serviceID, user, price, name, text, requests, currency);
            }
        });
    }

    private void startPage() {
        Intent s_i = getIntent();
        Bundle s_b = s_i.getExtras();
        String serviceID = s_b.getString("service");
        loadImages(serviceID);
        loadOffer(serviceID);
        loadMe();
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
                    ScrollingPagerIndicator recyclerIndicator = findViewById(R.id.indicator);
                    recyclerIndicator.attachToPager(imagePager);
                } else {
                    Toast.makeText(MyService.this, "Don't Exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadOffer(final String serviceID) {
        firestore.collection("Services").document(serviceID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name = Objects.requireNonNull(documentSnapshot.get("category")).toString();
                String user = Objects.requireNonNull(documentSnapshot.getData().get("user")).toString();
                String text = Objects.requireNonNull(documentSnapshot.get("text")).toString();
                String price = Objects.requireNonNull(documentSnapshot.get("price")).toString();

                if (user.equals(UID)) {
                    requestService.setVisibility(View.GONE);
                }

                serviceName.setText(name);
                serviceDescription.setText(text);

                count(serviceID, user, price, name, text);
            }
        });
    }

    private void count(final String serviceID, final String user, final String price, final String name, final String text) {
        firestore.collection("Orders").whereEqualTo("service", serviceID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                long count = queryDocumentSnapshots.size();
                serviceRequests.setText("Requested "+ count + " times");
                loadUser(serviceID, user, price, name, text, count);
            }
        });
    }

    private void loadUser(final String serviceID, final String user, final String price, final String name, final String text, final long requests) {
        firestore.collection("Users").document(user).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String username = documentSnapshot.get("first_name").toString() + " " + documentSnapshot.get("last_name").toString();
                String currency = documentSnapshot.get("currency_symbol").toString();

                serviceUser.setText(username);
                servicePrice.setText("(" + currency + " " + price + ")");
                staticOnClicks(serviceID, user, price, name, text, requests, currency);
            }
        });
    }

    private void loadMe() {
        firestore.collection("Users").document(UID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String user_type = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("user_type")).toString();
                if (user_type.equals("tasker")) {
                    services.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void requestService(final String serviceID, final String user, final String price, final String name, final String text, final long requests, final String currency) {

        final long unixTime = System.currentTimeMillis() / 1000L;

        List<String> parties = new ArrayList<>();

        parties.add(UID);
        parties.add(user);

        DocumentReference reference = firestore.collection("Orders").document();
        String requestID = reference.getId();

        HashMap<String, Object> request = new HashMap<>();

        request.put("time", unixTime);
        request.put("service", serviceID);
        request.put("seller", user);
        request.put("user", UID);
        request.put("currency", currency);
        request.put("status", "pending");
        request.put("request", requestID);
        request.put("parties", parties);


        reference.set(request).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent o_i = new Intent(MyService.this, MyOrder.class);
                Bundle o_b = new Bundle();
                o_b.putString("UID", UID);
                o_b.putString("seller", user);
                o_b.putString("serviceID", serviceID);
                o_b.putString("text", text);
                o_b.putString("category", name);
                o_b.putLong("time", unixTime);
                o_b.putString("currency", currency);
                o_b.putString("price", price);
                o_b.putLong("requests", requests);
                o_i.putExtras(o_b);
                startActivity(o_i);
                requestService.setVisibility(View.GONE);
            }
        });

    }

    private void goHome() {
        final Intent c_intent = new Intent(MyService.this, FeedActivity.class);
        Bundle c_bundle = new Bundle();
        c_bundle.putString("category", "");
        c_bundle.putString("location", "");
        c_bundle.putLong("minimum", 0);
        c_bundle.putLong("maximum", 0);
        c_intent.putExtras(c_bundle);
        startActivity(c_intent);
    }
}
