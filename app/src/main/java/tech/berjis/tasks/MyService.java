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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class MyService extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    ViewPager2 imagePager;
    List<ImageList> imageList;
    ImagePagerAdapter imagePagerAdapter;
    String UID;
    TextView serviceName, servicePrice, serviceRequests, serviceUser, serviceDescription, requestService, pageTitle;
    ImageView profile, services, orders, home, chats, notifications, settings;
    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_service);

        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = mAuth.getCurrentUser().getUid();
        imageList = new ArrayList<>();

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

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
        pageTitle = findViewById(R.id.pageTitle);

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
        if (UID.equals(user)) {
            Picasso.get().load(R.drawable.edit).into(settings);
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent s_i = new Intent(MyService.this, EditServiceActivity.class);
                    Bundle s_b = new Bundle();
                    s_b.putString("service", serviceID);
                    s_i.putExtras(s_b);
                    startActivity(s_i);
                }
            });

        } else {
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MyService.this, SettingsActivity.class));
                }
            });
        }
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
                    Toast.makeText(MyService.this, "This service has no Images", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadOffer(final String serviceID) {
        dbRef.child("Services").child(serviceID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = Objects.requireNonNull(snapshot.child("category").getValue()).toString();
                String user = Objects.requireNonNull(snapshot.child("user").getValue()).toString();
                String text = Objects.requireNonNull(snapshot.child("text").getValue()).toString();
                String price = Objects.requireNonNull(snapshot.child("price").getValue()).toString();

                if (user.equals(UID)) {
                    requestService.setVisibility(View.GONE);
                }

                serviceName.setText(name);
                pageTitle.setText(name);
                serviceDescription.setText(text);

                count(serviceID, user, price, name, text);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void count(final String serviceID, final String user, final String price, final String name, final String text) {
        dbRef.child("Orders").orderByChild("service").equalTo(serviceID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                serviceRequests.setText("Requested " + count + " times");
                loadUser(serviceID, user, price, name, text, count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUser(final String serviceID, final String user, final String price, final String name, final String text, final long requests) {
        dbRef.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = Objects.requireNonNull(snapshot.child("first_name").getValue()).toString() + " " + Objects.requireNonNull(snapshot.child("last_name").getValue()).toString();
                String currency = Objects.requireNonNull(snapshot.child("currency_symbol").getValue()).toString();

                serviceUser.setText(username);
                servicePrice.setText("(" + currency + " " + price + ")");
                staticOnClicks(serviceID, user, price, name, text, requests, currency);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMe() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String user_type = Objects.requireNonNull(snapshot.child("user_type").getValue()).toString();
                if (user_type.equals("tasker")) {
                    services.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void requestService(final String serviceID, final String user, final String price, final String name, final String text, final long requests, final String currency) {

        final long unixTime = System.currentTimeMillis() / 1000L;

        List<String> parties = new ArrayList<>();

        parties.add(UID);
        parties.add(user);

        HashMap<String, Object> request = new HashMap<>();

        request.put("time", unixTime);
        request.put("service", String.valueOf(serviceID));
        request.put("seller", user);
        request.put("user", UID);
        request.put("currency", currency);
        request.put("status", "pending");
        request.put("request", String.valueOf(unixTime));
        request.put("parties", parties);


        dbRef.child("Orders").child(String.valueOf(unixTime)).setValue(request).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                tokenShit(user, name);
            }
        });

    }

    private void tokenShit(final String seller, final String service) {
        Toast.makeText(this, "Notifying seller", Toast.LENGTH_SHORT).show();
        dbRef.child("Users").child(seller).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot
                                                     dataSnapshot) {
                        String user_token = Objects.requireNonNull(dataSnapshot.child("device").getValue()).toString();
                        sendNotifications(seller, user_token, "New Service Request", "You have a new service request.\nService requested: " + service);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError
                                                    databaseError) {
                    }
                });
    }

    public void sendNotifications(final String seller, final String user_token, final String title, final String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, user_token);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().success != 1) {
                        Toast.makeText(MyService.this, "Failed", Toast.LENGTH_SHORT).show();
                    } else {
                        final long unixTime = System.currentTimeMillis() / 1000L;
                        HashMap<String, Object> notif = new HashMap<>();
                        notif.put("title", title);
                        notif.put("message", message);
                        notif.put("time", unixTime);
                        notif.put("device", user_token);
                        notif.put("id", String.valueOf(unixTime));

                        dbRef.child("Notifications").child(seller).child(String.valueOf(unixTime)).setValue(notif);
                        Toast.makeText(MyService.this, "Seller notified", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
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
