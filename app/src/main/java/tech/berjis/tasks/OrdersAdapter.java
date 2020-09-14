package tech.berjis.tasks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    List<Orders> listData;
    Context mContext;
    String currency_symbol;

    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

    OrdersAdapter(Context mContext, List<Orders> listData, String currency_symbol) {
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
        Orders ld = listData.get(position);
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
        public ImageView call;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceTitle = itemView.findViewById(R.id.serviceTitle);
            mainImage = itemView.findViewById(R.id.mainImage);
            servicePrice = itemView.findViewById(R.id.servicePrice);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
            call = itemView.findViewById(R.id.call);
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadUI(ViewHolder holder, Orders ld) {
        holder.call.setVisibility(View.VISIBLE);
        if (ld.getSeller().equals(UID)) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.colorCrimson));
            // itemView.getBackground().setAlpha(45);
            holder.mainImage.setVisibility(View.GONE);
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.colorWhite));
        }
        getFromService(holder, ld);
    }

    private void getFromService(final ViewHolder holder, final Orders ld) {
        dbRef.child("Services").child(ld.getService()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String text = Objects.requireNonNull(snapshot.child("text").getValue()).toString();
                String category = Objects.requireNonNull(snapshot.child("category").getValue()).toString();
                String price = Objects.requireNonNull(snapshot.child("price").getValue()).toString();
                String requests = Objects.requireNonNull(snapshot.child("requests").getValue()).toString();
                otherClicks(holder, ld, text, category, price, requests);
                holder.serviceTitle.setText(category);
                holder.servicePrice.setText(currency_symbol + " " + price);

                if (ld.getSeller().equals(UID)) {
                    holder.serviceDescription.setText("Offered To: ");
                } else {
                    holder.serviceDescription.setText("Offered By: ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void goHome(final ViewHolder holder, final Orders ld, final String text, final String category, final String price, final String requests) {
        Intent o_i = new Intent(holder.itemView.getContext(), MyOrder.class);
        Bundle o_b = new Bundle();
        o_b.putString("UID", ld.getUser());
        o_b.putString("seller", ld.getSeller());
        o_b.putString("serviceID", ld.getService());
        o_b.putString("text", text);
        o_b.putString("category", category);
        o_b.putLong("time", ld.getTime());
        o_b.putString("currency", currency_symbol);
        o_b.putString("price", price);
        o_b.putString("requests", requests);
        o_i.putExtras(o_b);
        holder.itemView.getContext().startActivity(o_i);
    }

    private void loadImages(final ViewHolder holder, Orders ld) {

        dbRef.child("ServicesImages").child(ld.getService()).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Glide.with(mContext).load(Objects.requireNonNull(npsnapshot.child("image").getValue()).toString()).thumbnail(0.25f).into(holder.mainImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(holder.itemView.getContext(), "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countRequests(final ViewHolder holder, final Orders ld) {
        dbRef.child("Orders").orderByChild("service").equalTo(ld.getService()).addValueEventListener(new ValueEventListener() {
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

    private void setOnClicks(final ViewHolder holder, final Orders ld) {
        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ld.getSeller().equals(UID)) {
                    call(holder, ld, ld.getUser());
                }
                if (ld.getUser().equals(UID)) {
                    call(holder, ld, ld.getSeller());
                }
            }
        });
    }

    private void call(final ViewHolder holder, Orders ld, String mteja) {
        final int REQUEST_PHONE_CALL = 1;
        dbRef.child("Users").child(mteja).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String phone = snapshot.child("user_phone").getValue().toString();
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                if (ActivityCompat.checkSelfPermission(holder.itemView.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) holder.itemView.getContext(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                    return;
                }
                holder.itemView.getContext().startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void otherClicks(final ViewHolder holder, final Orders ld, final String text, final String category, final String price, final String requests) {

        holder.serviceDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome(holder, ld, text, category, price, requests);
            }
        });
        holder.mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome(holder, ld, text, category, price, requests);
            }
        });
        holder.servicePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome(holder, ld, text, category, price, requests);
            }
        });
        holder.serviceTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome(holder, ld, text, category, price, requests);
            }
        });
    }

    private void loadUser(final ViewHolder holder, Orders ld, String user, final long count) {

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
