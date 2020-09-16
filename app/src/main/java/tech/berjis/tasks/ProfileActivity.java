package tech.berjis.tasks;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiTextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    ImageView home, chats, profile, menu;
    CircleImageView dp, addTask;
    EmojiTextView full_name, username;
    TextView editProfileTxt, activateTasker, activateTaskerText, createTask;
    RecyclerView servicesRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        UID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);
        chats = findViewById(R.id.chats);
        profile = findViewById(R.id.profile);
        username = findViewById(R.id.username);
        full_name = findViewById(R.id.full_name);
        editProfileTxt = findViewById(R.id.editProfileTxt);
        dp = findViewById(R.id.dp);
        activateTasker = findViewById(R.id.activateTasker);
        activateTaskerText = findViewById(R.id.activateTaskerText);
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
                startActivity(new Intent(ProfileActivity.this, FeedActivity.class));
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
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, NewServiceActivity.class));
            }
        });
        activateTasker.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Seller account")
                        .setMessage(Html.fromHtml("<p>The seller account allows you to:</p>" +
                                "<ul>" +
                                "<li>Create services</li>" +
                                "<li>Manage orders</li>" +
                                "<li>Request for services like normal users</li>" +
                                "</ul><br />" +
                                "<hr />"))
                        .setCancelable(false)
                        .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String, Object> user = new HashMap<>();
                                user.put("user_type", "tasker");

                                dbRef.child("Users").child(UID).updateChildren(user).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                        activateTaskerText.setVisibility(View.GONE);
                                        createTask.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                startActivity(new Intent(ProfileActivity.this, NewServiceActivity.class));
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
    }

    private void loaduserdata() {
        dbRef.child("Users")
                .child(UID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String alias = snapshot.child("user_name").getValue().toString();
                        String user_type = snapshot.child("user_type").getValue().toString();
                        String fullname = snapshot.child("first_name").getValue().toString() + " " + snapshot.child("last_name").getValue().toString();
                        username.setText("@" + alias);
                        full_name.setText(fullname);

                        if (!snapshot.child("user_image").getValue().toString().equals("")) {
                            long unixTime = System.currentTimeMillis() / 1000L;
                            RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).signature(new ObjectKey(unixTime));

                            Glide
                                    .with(ProfileActivity.this)
                                    .load(snapshot.child("user_image").getValue().toString())
                                    .thumbnail(Glide.with(ProfileActivity.this).load(R.drawable.preloader))
                                    .centerCrop()
                                    .apply(requestOptions)
                                    .error(R.drawable.error_loading_image)
                                    .into(dp);
                        }

                        if (user_type.equals("default")) {
                            activateTasker.setVisibility(View.VISIBLE);
                            activateTaskerText.setVisibility(View.VISIBLE);
                            servicesRecycler.setVisibility(View.GONE);
                            addTask.setVisibility(View.GONE);
                            createTask.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void newUserState() {
        dbRef.child("Users")
                .child(UID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String user_type = Objects.requireNonNull(snapshot.child("user_type").getValue()).toString();
                        String user_email = Objects.requireNonNull(snapshot.child("user_email").getValue()).toString();
                        String user_name = Objects.requireNonNull(snapshot.child("user_name").getValue()).toString();
                        String first_name = Objects.requireNonNull(snapshot.child("first_name").getValue()).toString();
                        String last_name = Objects.requireNonNull(snapshot.child("last_name").getValue()).toString();

                        if (user_email.equals("") ||
                                user_name.equals("") ||
                                first_name.equals("") ||
                                last_name.equals("")) {
                            Intent mainActivity = new Intent(getApplicationContext(), EditProfileActivity.class);
                            startActivity(mainActivity);
                            finish();
                        } else {
                            loaduserdata();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this, FeedActivity.class));
    }
}
