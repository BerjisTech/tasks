package tech.berjis.tasks;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FeedActivity extends AppCompatActivity {

    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    List<Services> serviceList;
    List<Categories> categoriesList;

    ServiceAdapter serviceAdapter;
    CategoriesAdapter CategoriesAdapter;
    SwipeRefreshLayout pageRefresh;

    RecyclerView categoryRecycler, postsRecycler;
    ImageView search, services, orders, profile, chats, notifications, preload;
    ConstraintLayout loadLayout;
    String UID, currency_symbol = "";
    double myLat, myLong;

    final int REQUEST_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        serviceList = new ArrayList<>();
        categoriesList = new ArrayList<>();

        categoryRecycler = findViewById(R.id.categoryRecycler);
        postsRecycler = findViewById(R.id.postsRecycler);
        search = findViewById(R.id.search);
        services = findViewById(R.id.services);
        orders = findViewById(R.id.orders);
        profile = findViewById(R.id.profile);
        chats = findViewById(R.id.chats);
        notifications = findViewById(R.id.notifications);
        pageRefresh = findViewById(R.id.pageRefresh);
        preload = findViewById(R.id.preload);
        loadLayout = findViewById(R.id.loadLayout);

        final ImageView preload = findViewById(R.id.preload);
        Glide.with(FeedActivity.this).asGif().load(R.drawable.preloader).into(preload);

        staticOnClick();
        pageRefresher();
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

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else {
            getUserArea();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                statusCheck();
            }
        }
    }

    private void loadWorkers() {
        loaduserdata();
        loadcategories();
    }

    private void pageRefresher() {
        pageRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                serviceList.clear();
                getUserArea();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pageRefresh.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        pageRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
    }

    private void loadcategories() {
        categoriesList.clear();
        categoriesList.add(new Categories("All Categories", "drawable://" + R.drawable.plus));
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        dbRef.child("Categories").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Categories l = snap.getValue(Categories.class);
                        categoriesList.add(l);
                    }
                }
                CategoriesAdapter = new CategoriesAdapter(FeedActivity.this, categoriesList);
                categoryRecycler.setAdapter(CategoriesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 72913.3858 * 2.54 * 0.00001;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void loaduserdata() {
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        dbRef.child("Users")
                .child(UID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String user_type = snapshot.child("user_type").getValue().toString();
                        currency_symbol = snapshot.child("currency_symbol").getValue().toString();
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
                                loadServices();
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

    private void loadServices() {
        serviceList.clear();
        postsRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        dbRef.child("Services").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (snap.child("user").getValue().toString().equals(UID)) {

                        }
                        double lat = Double.parseDouble(Objects.requireNonNull(snap.child("lat").getValue()).toString());
                        double lng = Double.parseDouble(Objects.requireNonNull(snap.child("long").getValue()).toString());

                        double dist = distance(lat, lng, myLat, myLong);

                        /*Location selected_location = new Location("MyLocation");
                        selected_location.setLatitude(myLat);
                        selected_location.setLongitude(myLong);
                        Location near_locations = new Location("ServiceLocation");
                        near_locations.setLatitude(lat);
                        near_locations.setLongitude(lng);
                        double distance = selected_location.distanceTo(near_locations);*/

                        if (dist <= 20) {
                            Services l = snap.getValue(Services.class);
                            serviceList.add(l);
                        }
                    }
                }
                Collections.shuffle(serviceList);
                serviceAdapter = new ServiceAdapter(FeedActivity.this, serviceList, currency_symbol);
                postsRecycler.setAdapter(serviceAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FeedActivity.super.finish();
    }

    private void getUserArea() {
        Toast.makeText(this, "Fetching nearby taskers", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
        statusCheck();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                            if (String.valueOf(latLng).equals("")) {
                                Toast.makeText(FeedActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(FeedActivity.this, WhichTaskActivity.class));
                            } else {
                                loadLayout.setVisibility(View.GONE);
                                myLat = latLng.latitude;
                                myLong = latLng.longitude;
                                // Toast.makeText(FeedActivity.this, String.valueOf(latLng), Toast.LENGTH_SHORT).show();
                                loadWorkers();
                            }
                        } else {
                            Toast.makeText(FeedActivity.this, "Location services not activated", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(FeedActivity.this, WhichTaskActivity.class));
                        }
                    }
                });
    }
}
