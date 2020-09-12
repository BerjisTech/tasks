package tech.berjis.tasks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseFirestoreSettings firestoreSettings;
    DatabaseReference dbRef;

    String UID;
    ImageView profile, services, orders, home, chats, notifications, settings, edit_profile_btn, change_phone_btn, back, terms_of_use_btn, logout_btn, notifications_btn;;
    TextView edit_profile_txt, change_phone_txt, terms_of_use_txt, logout_txt, notifications_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        profile = findViewById(R.id.profile);
        services = findViewById(R.id.services);
        orders = findViewById(R.id.orders);
        chats = findViewById(R.id.chats);
        home = findViewById(R.id.home);
        notifications = findViewById(R.id.notifications);
        settings = findViewById(R.id.settings);

        edit_profile_txt = findViewById(R.id.edit_profile_txt);
        edit_profile_btn = findViewById(R.id.edit_profile_btn);
        change_phone_txt = findViewById(R.id.change_phone_txt);
        change_phone_btn = findViewById(R.id.change_phone_btn);
        terms_of_use_btn = findViewById(R.id.terms_of_use_btn);
        terms_of_use_txt = findViewById(R.id.terms_of_use_txt);
        notifications_txt = findViewById(R.id.notifications_txt);
        notifications_btn = findViewById(R.id.notifications_btn);
        logout_btn = findViewById(R.id.logout_btn);
        logout_txt = findViewById(R.id.logout_txt);

        staticOnClicks();
        loaduserdata();
    }

    private void staticOnClicks() {
        edit_profile_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
            }
        });

        edit_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
            }
        });

        change_phone_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ChangePhoneNumber.class));
            }
        });

        change_phone_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ChangePhoneNumber.class));
            }
        });

        terms_of_use_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, TermsActivity.class));
            }
        });

        terms_of_use_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, TermsActivity.class));
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SettingsActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        logout_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SettingsActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        notifications_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, NotificationsActivity.class));
            }
        });

        notifications_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, NotificationsActivity.class));
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
            }
        });
        services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, MyServicesActivity.class));
            }
        });
        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, MyOrdersActivity.class));
            }
        });
        chats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, DMsActivity.class));
            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, NotificationsActivity.class));
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent c_intent = new Intent(SettingsActivity.this, FeedActivity.class);
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
