package tech.berjis.tasks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.StorageReference;
import com.vanniktech.emoji.EmojiEditText;

import java.util.List;
import java.util.Objects;

public class MyOrdersActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseFirestoreSettings firestoreSettings;
    String UID;

    ImageView profile, services, home, chats, notifications, settings;

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

        statickOnClicks();
    }

    private void statickOnClicks() {
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

    private void loaduserdata() {
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firestore.collection("Users")
                .document(UID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String user_type = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("user_type")).toString();
                        if (user_type.equals("tasker")) {
                            services.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}
