package tech.berjis.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    List<Services> listData;
    Context mContext;
    String currency_symbol;

    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    ServiceAdapter(Context mContext, List<Services> listData, String currency_symbol) {
        this.mContext = mContext;
        this.currency_symbol = currency_symbol;
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Services ld = listData.get(position);
        loadUI(holder, ld);
        loadImages(holder, ld);
        countRequests(holder, ld);
        setOnClicks(holder, ld);

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView serviceTitle;
        public TextView servicePrice;
        public ImageView mainImage;
        public TextView serviceDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceTitle = itemView.findViewById(R.id.serviceTitle);
            mainImage = itemView.findViewById(R.id.mainImage);
            servicePrice = itemView.findViewById(R.id.servicePrice);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadUI(ViewHolder holder, Services ld) {
        holder.serviceTitle.setText(ld.getCategory());
        holder.servicePrice.setText(currency_symbol + " " + ld.getPrice());
    }

    private void loadImages(final ViewHolder holder, Services ld) {

        dbRef.child("ServicesImages").child(ld.getService_id()).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Glide.with(holder.itemView.getContext()).load(Objects.requireNonNull(npsnapshot.child("image").getValue()).toString()).thumbnail(0.25f).into(holder.mainImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(holder.itemView.getContext(), "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countRequests(final ViewHolder holder, final Services ld) {
        dbRef.child("Orders").orderByChild("service").equalTo(ld.getService_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                loadUser(holder, ld, ld.getUser(), count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setOnClicks(final ViewHolder holder, final Services ld) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent s_i = new Intent(holder.itemView.getContext(), MyService.class);
                Bundle s_b = new Bundle();
                s_b.putString("service", ld.getService_id());
                s_i.putExtras(s_b);
                holder.itemView.getContext().startActivity(s_i);
            }
        });
    }

    private void loadUser(final ViewHolder holder, Services ld, String user, final long count) {

        dbRef.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String user_name = Objects.requireNonNull(snapshot.child("first_name").getValue()).toString() + " " + Objects.requireNonNull(snapshot.child("last_name").getValue()).toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.serviceDescription.setText(Html.fromHtml(user_name + " <br /><small>(Requested " + count + " times)</small>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    holder.serviceDescription.setText(Html.fromHtml(user_name + " <br /><small>(Requested " + count + " times)</small>"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
