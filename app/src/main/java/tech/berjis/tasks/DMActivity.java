package tech.berjis.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class DMActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    static DatabaseReference dbRef, senderRef, receiverRef;
    StorageReference storageReference;
    Uri filePath;
    String UID, receiver_chat_id, sender_chat_id, user;

    List<Chat> listData;
    ChatAdapter chatAdapter;

    ImageView back, send;
    EmojiTextView userName;
    ImageView dp;
    CircleImageView btn_link, btn_emoji;
    EmojiEditText ed_emoji;
    ConstraintLayout rootView;
    RecyclerView chatRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_d_m);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        UID = mAuth.getCurrentUser().getUid();
        dbRef.keepSynced(true);

        listData = new ArrayList<>();

        back = findViewById(R.id.back);
        userName = findViewById(R.id.userName);
        dp = findViewById(R.id.dp);
        btn_link = findViewById(R.id.btn_link);
        ed_emoji = findViewById(R.id.ed_emoji);
        btn_emoji = findViewById(R.id.btn_emoji);
        send = findViewById(R.id.send);
        rootView = findViewById(R.id.root_view);
        chatRecycler = findViewById(R.id.chatRecycler);

        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(ed_emoji);
        btn_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggles visibility of the Popup.
                if (emojiPopup.isShowing()) {
                    Picasso.get().load(R.drawable.emoji).into(btn_emoji);
                    emojiPopup.toggle();
                } else {
                    Picasso.get().load(R.drawable.keyboard).into(btn_emoji);
                    emojiPopup.toggle();
                }
            }
        });

        //emojiPopup.dismiss();  Dismisses the Popup.
        //emojiPopup.isShowing();  Returns true when Popup is showing.

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DMActivity.super.finish();
            }
        });
        getuser();

        chatRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        chatAdapter = new ChatAdapter(this, listData);
        chatRecycler.setAdapter(chatAdapter);

    }

    private void getuser() {
        Intent userIntent = getIntent();
        Bundle userBundle = userIntent.getExtras();
        user = userBundle.getString("user");

        receiverRef = dbRef.child("Chats").child(user).child(UID).push();
        receiver_chat_id = receiverRef.getKey();
        senderRef = dbRef.child("Chats").child(UID).child(user).push();
        sender_chat_id = senderRef.getKey();

        loaduserData(user);
        loadChats();
        btn_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkText();
            }
        });
    }

    private void checkText() {
        String text = ed_emoji.getText().toString();

        if (text.equals("")) {
            Toast.makeText(this, "You can't send an empty message", Toast.LENGTH_SHORT).show();
            ed_emoji.requestFocus();
            return;
        }

        postSenderChat(text, "text");
        postReceiverChat(text, "text");

        ed_emoji.setText("");
    }

    private void loaduserData(String user) {
        dbRef.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void postSenderChat(String text, String type) {

        long unixTime = System.currentTimeMillis() / 1000L;

        HashMap<String, Object> chatHash = new HashMap<>();

        chatHash.put("sender", UID);
        chatHash.put("receiver", user);
        chatHash.put("time", unixTime);
        chatHash.put("text", text);
        chatHash.put("type", type);
        chatHash.put("receiver_chat_id", receiver_chat_id);
        chatHash.put("sender_chat_id", sender_chat_id);
        chatHash.put("read", false);
        chatHash.put("delete", false);

        senderRef.updateChildren(chatHash);
        dbRef.child("ChatsMetaData").child(UID).child(user).child("last_update").setValue(unixTime);
    }

    private void postReceiverChat(String text, String type) {

        long unixTime = System.currentTimeMillis() / 1000L;

        HashMap<String, Object> chatHash = new HashMap<>();

        chatHash.put("sender", user);
        chatHash.put("receiver", UID);
        chatHash.put("time", unixTime);
        chatHash.put("text", text);
        chatHash.put("type", type);
        chatHash.put("receiver_chat_id", receiver_chat_id);
        chatHash.put("sender_chat_id", sender_chat_id);
        chatHash.put("read", false);
        chatHash.put("delete", false);

        receiverRef.updateChildren(chatHash);
        dbRef.child("ChatsMetaData").child(user).child(UID).child("last_update").setValue(unixTime);
    }

    private void selectImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
        }*/

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error : " + error, Toast.LENGTH_SHORT).show();
            }
        }

        postImage();
    }

    private void postImage() {
        long unixTime = System.currentTimeMillis() / 1000L;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        Random random = new Random();

        if (filePath != null) {

            final StorageReference ref = storageReference.child("Chat Images/" + random + unixTime + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    final String image_url = downloadUrl.toString();
                                    postSenderChat(image_url, "photo");
                                    postReceiverChat(image_url, "photo");
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(DMActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        } else {
            progressDialog.dismiss();
        }
    }

    private void loadChats() {
        listData.clear();

        dbRef.child("Chats").child(UID).child(user).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat messages = dataSnapshot.getValue(Chat.class);

                listData.add(messages);

                chatAdapter.notifyDataSetChanged();

                chatRecycler.smoothScrollToPosition(listData.size());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    static void replyTo(ChatAdapter.ViewHolder holder, Chat ld, int position) {

    }

    static void deleteChatForMe(ChatAdapter.ViewHolder holder, Chat ld, int position) {
        dbRef.child("Chats").child(ld.getSender()).child(ld.getReceiver()).child(ld.getSender_chat_id()).child("delete").setValue(true);
    }

    static void deleteChatForAll(ChatAdapter.ViewHolder holder, Chat ld, int position) {
        dbRef.child("Chats").child(ld.getSender()).child(ld.getReceiver()).child(ld.getSender_chat_id()).child("delete").setValue(true);
        dbRef.child("Chats").child(ld.getReceiver()).child(ld.getSender()).child(ld.getReceiver_chat_id()).child("delete").setValue(true);

    }

    static void manageSelection(ChatAdapter.ViewHolder holder, Chat ld, int position) {

    }
}
