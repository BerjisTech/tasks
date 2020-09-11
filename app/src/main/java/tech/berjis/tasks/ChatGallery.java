package tech.berjis.tasks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatGallery extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    List<ImageList> imageList;
    ImagePagerAdapter imagePagerAdapter;
    ViewPager2 imagePager;

    ImageView back;
    EmojiTextView userName;
    ImageView dp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_gallery);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        UID = mAuth.getCurrentUser().getUid();
        dbRef.keepSynced(true);

        back = findViewById(R.id.back);
        userName = findViewById(R.id.userName);
        dp = findViewById(R.id.dp);
        imagePager = findViewById(R.id.imagePager);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatGallery.super.finish();
            }
        });

        imageList = new ArrayList<>();
        loadImageData();
    }

    private void loadImageData() {
        Intent imageIntent = getIntent();
        Bundle imageBundle = imageIntent.getExtras();
        String sender_chat_id = imageBundle.getString("sender_chat_id");
        String receiver_chat_id = imageBundle.getString("receiver_chat_id");
        String sender = imageBundle.getString("sender");
        String receiver = imageBundle.getString("receiver");

        loadReceiver(receiver);
        loadImages(sender_chat_id, sender, receiver);
    }

    private void loadImages(final String sender_chat_id, final String sender, final String receiver) {
        imageList.clear();
        dbRef.child("Chats").child(sender).child(receiver).orderByChild("type").equalTo("photo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int index = 0, offset = 1, count = 0;
                    for (DataSnapshot imagesSnapshot : dataSnapshot.getChildren()) {
                        if (imagesSnapshot.child("delete").getValue().toString().equals("false")) {
                            String image = imagesSnapshot.child("text").getValue().toString();
                            imageList.add(new ImageList(image, sender_chat_id, receiver));
                            count = count + offset;
                            if (imagesSnapshot.child("receiver_chat_id").getValue().toString().equals(sender_chat_id)) {
                                index = count - 1;
                            }
                        }
                    }
                    imagePagerAdapter = new ImagePagerAdapter(ChatGallery.this, imageList, "gallery");
                    imagePagerAdapter.notifyDataSetChanged();
                    imagePager.setAdapter(imagePagerAdapter);
                    imagePager.setCurrentItem(index);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadReceiver(String receiver) {
        dbRef.child("Users").child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("user_name").getValue().toString();
                String userimage = dataSnapshot.child("user_image").getValue().toString();

                if (!userimage.equals("")) {
                    Picasso.get().load(userimage).into(dp);
                }
                if (!username.equals("")) {
                    userName.setText(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
