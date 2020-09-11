package tech.berjis.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    public String uid;
    private List<String> listData;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private Context mContext;

    ChatsAdapter(Context mContext, List<String> listData) {
        this.listData = listData;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dms, parent, false);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        uid = mAuth.getCurrentUser().getUid();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final String ld = listData.get(position);

        holder.tick.setVisibility(View.GONE);

        dbRef.child("Users").child(ld).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("user_image").exists() && !dataSnapshot.child("user_image").getValue().toString().isEmpty()) {
                    long unixTime = System.currentTimeMillis() / 1000L;
                    RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).signature(new ObjectKey(unixTime));

                    Glide
                            .with(mContext)
                            .load(dataSnapshot.child("user_image").getValue().toString())
                            .thumbnail(Glide.with(mContext).load(R.drawable.preloader))
                            .centerCrop()
                            .apply(requestOptions)
                            .error(R.drawable.error_loading_image)
                            .into(holder.userImage);
                }
                if (dataSnapshot.child("user_name").exists()) {
                    holder.userName.setText(dataSnapshot.child("user_name").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dbRef.child("Chats").child(uid).child(ld).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                    if (npsnapshot.hasChildren()) {
                        long time = Long.parseLong(npsnapshot.child("time").getValue().toString()) * 1000;
                        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
                        String ago = prettyTime.format(new Date(time));

                        holder.lastTextTime.setText(ago);

                        if (npsnapshot.child("read").getValue().toString().equals("false")) {
                            holder.lastText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        }

                        if (npsnapshot.child("sender").getValue().toString().equals(uid)) {
                            holder.tick.setVisibility(View.VISIBLE);
                            if (npsnapshot.child("read").getValue().toString().equals("false")) {
                                holder.tick.setColorFilter(ContextCompat.getColor(holder.mView.getContext(), R.color.greyTick), PorterDuff.Mode.SRC_ATOP);
                            }
                            if (npsnapshot.child("read").getValue().toString().equals("true")) {
                                holder.tick.setColorFilter(ContextCompat.getColor(holder.mView.getContext(), R.color.blueTick), PorterDuff.Mode.SRC_ATOP);
                            }
                        }

                        if (npsnapshot.child("type").getValue().toString().equals("text")) {
                            if (!TextUtils.isEmpty(npsnapshot.child("text").getValue().toString())) {
                                if (npsnapshot.child("text").getValue().toString().length() > 35) {
                                    holder.lastText.setText(npsnapshot.child("text").getValue().toString().substring(0, 34) + "...");
                                } else {
                                    holder.lastText.setText(npsnapshot.child("text").getValue().toString());
                                }
                            }
                        }

                        if (npsnapshot.child("type").getValue().toString().equals("image")) {
                            holder.lastText.setText("Image \uD83D\uDDBC");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.mView.getContext(), DMActivity.class);

                Bundle extras = new Bundle();

                extras.putString("user", ld);

                intent.putExtras(extras);

                holder.mView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImage;
        EmojiTextView lastText, lastTextTime, userName;
        ImageView tick;


        private View mView;

        ViewHolder(View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            lastText = itemView.findViewById(R.id.lastText);
            lastTextTime = itemView.findViewById(R.id.lastTextTime);
            tick = itemView.findViewById(R.id.tick);

            mView = itemView;

        }
    }
}
