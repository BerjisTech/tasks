package tech.berjis.tasks;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyOrder extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    String UID, seller, serviceID, text, currency, price, category;
    long time, requests;
    TextView serviceTitle, servicePrice, serviceRequests, serviceDescription, sellerName, clientName, call;
    CircleImageView sellerImage, clientImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);

        getOrder();
    }

    private void getOrder() {
        Intent o_i = getIntent();
        Bundle o_b = o_i.getExtras();
        assert o_b != null;
        UID = o_b.getString("UID");
        seller = o_b.getString("seller");
        serviceID = o_b.getString("serviceID");
        text = o_b.getString("text");
        time = o_b.getLong("time");
        currency = o_b.getString("currency");
        price = o_b.getString("price");
        category = o_b.getString("category");
        requests = o_b.getLong("requests");

        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        serviceTitle = findViewById(R.id.serviceTitle);
        servicePrice = findViewById(R.id.servicePrice);
        serviceRequests = findViewById(R.id.serviceRequests);
        serviceDescription = findViewById(R.id.serviceDescription);
        sellerName = findViewById(R.id.sellerName);
        sellerImage = findViewById(R.id.sellerImage);
        clientName = findViewById(R.id.clientName);
        clientImage = findViewById(R.id.clientImage);
        call = findViewById(R.id.call);

        loadOfferDetails();
        loadSeller();
        loadClient();
        loadImages();
        count();
        staticOnClicks();
        String me = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        if (me.equals(UID)) {
            call.setText("Call Tasker");
        } else {
            call.setText("Call Client");
        }
    }

    private void staticOnClicks() {
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String me = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                if (me.equals(UID)) {
                    call(seller);
                } else {
                    call(UID);
                }
            }
        });
    }

    private void call(String mteja) {
        final int REQUEST_PHONE_CALL = 1;
        dbRef.child("Users").child(mteja).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String phone = snapshot.child("user_phone").getValue().toString();
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                if (ActivityCompat.checkSelfPermission(MyOrder.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MyOrder.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                    return;
                }
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void count() {
        dbRef.child("Orders").orderByChild("service").equalTo(serviceID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                serviceRequests.setText("Requested " + count + " times");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadImages() {

    }

    private void loadOfferDetails() {
        serviceTitle.setText(category);
        servicePrice.setText(currency + " " + price);
        serviceRequests.setText(requests + " requests");
        serviceDescription.setText(text);
    }

    private void loadSeller() {
        dbRef.child("Users").child(seller).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String f_name = snapshot.child("first_name").getValue().toString();
                String l_name = snapshot.child("last_name").getValue().toString();
                String image = snapshot.child("user_image").getValue().toString();
                sellerName.setText(Html.fromHtml("<small><sub>Done by</sub></small><br />" +
                        f_name + " " + l_name));
                if (!image.equals("")) {
                    Picasso.get().load(image).into(sellerImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadClient() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String f_name = snapshot.child("first_name").getValue().toString();
                String l_name = snapshot.child("last_name").getValue().toString();
                String image = snapshot.child("user_image").getValue().toString();
                clientName.setText(Html.fromHtml("<small><sub>Client</sub></small><br />" +
                        f_name + " " + l_name));
                if (!image.equals("")) {
                    Picasso.get().load(image).into(clientImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}