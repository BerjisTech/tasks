package tech.berjis.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
import com.vanniktech.emoji.EmojiTextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore dbFire;
    FirebaseFirestoreSettings fireSettings;
    String UID;

    ImageView home, chats, profile, menu;
    CircleImageView dp, addTask;
    EmojiTextView full_name, username;
    TextView editProfileTxt, activateTasker, createTask;
    RecyclerView servicesRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        UID = mAuth.getCurrentUser().getUid();
        dbFire = FirebaseFirestore.getInstance();
        fireSettings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        dbFire.setFirestoreSettings(fireSettings);

        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);
        chats = findViewById(R.id.chats);
        profile = findViewById(R.id.profile);
        username = findViewById(R.id.username);
        full_name = findViewById(R.id.full_name);
        editProfileTxt = findViewById(R.id.editProfileTxt);
        dp = findViewById(R.id.dp);
        activateTasker = findViewById(R.id.activateTasker);
        addTask = findViewById(R.id.addTask);
        createTask = findViewById(R.id.createTask);
        servicesRecycler = findViewById(R.id.servicesRecycler);

        newUserState();
        staticOnclicks();
    }

    private void staticOnclicks() {

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent c_intent = new Intent(ProfileActivity.this, FeedActivity.class);
                Bundle c_bundle = new Bundle();
                c_bundle.putString("category", "");
                c_bundle.putString("location", "");
                c_bundle.putString("minimum", "");
                c_bundle.putString("maximum", "");
                c_intent.putExtras(c_bundle);
                startActivity(c_intent);
            }
        });
        chats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, DMsActivity.class));
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        editProfileTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            }
        });
        activateTasker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> user = new HashMap<>();
                user.put("user_type", "tasker");

                dbFire.collection("Users").document(UID).set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(chats, "Your tasker account has been activated", Snackbar.LENGTH_LONG)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                })
                                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                                .show();
                        createTask.setVisibility(View.VISIBLE);
                        activateTasker.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void loaduserdata() {
        dbFire.collection("Users")
                .document(UID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String alias = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("user_name")).toString();
                        String user_type = Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get("user_type")).toString();
                        String fullname = Objects.requireNonNull(documentSnapshot.getData().get("first_name")).toString() + " " + Objects.requireNonNull(documentSnapshot.getData().get("last_name")).toString();
                        username.setText("@" + alias);
                        full_name.setText(fullname);

                        if (!documentSnapshot.getData().get("user_image").equals("")) {
                            long unixTime = System.currentTimeMillis() / 1000L;
                            RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).signature(new ObjectKey(unixTime));

                            Glide
                                    .with(ProfileActivity.this)
                                    .load(documentSnapshot.getData().get("user_image").toString())
                                    .thumbnail(Glide.with(ProfileActivity.this).load(R.drawable.preloader))
                                    .centerCrop()
                                    .apply(requestOptions)
                                    .error(R.drawable.error_loading_image)
                                    .into(dp);
                        }

                        if (user_type.equals("default")) {
                            activateTasker.setVisibility(View.VISIBLE);
                            servicesRecycler.setVisibility(View.GONE);
                            addTask.setVisibility(View.GONE);
                            createTask.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void newUserState() {
        dbFire.collection("Users")
                .document(UID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String user_email = Objects.requireNonNull(documentSnapshot.getData().get("user_email")).toString();
                        String user_name = Objects.requireNonNull(documentSnapshot.getData().get("user_name")).toString();
                        String first_name = Objects.requireNonNull(documentSnapshot.getData().get("first_name")).toString();
                        String last_name = Objects.requireNonNull(documentSnapshot.getData().get("last_name")).toString();
                        if (user_email.equals("") ||
                                user_name.equals("") ||
                                first_name.equals("") ||
                                last_name.equals("")) {
                            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                        } else {
                            loaduserdata();
                        }
                    }
                });
    }

}
