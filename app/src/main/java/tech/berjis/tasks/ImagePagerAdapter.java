package tech.berjis.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {

    private List<ImageList> listData;
    private LayoutInflater mInflater;
    private String view_type;

    ImagePagerAdapter(Context context, List<ImageList> data, String view_type) {
        this.mInflater = LayoutInflater.from(context);
        this.listData = data;
        this.view_type = view_type;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if (view_type.equals("new_service") || view_type.equals("edit_service")) {
            view = mInflater.inflate(R.layout.new_post_image, parent, false);
        }
        if (view_type.equals("gallery")) {
            view = mInflater.inflate(R.layout.gallery_image_view, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ImageList ld = listData.get(position);

        Picasso.get()
                .load(ld.getImage())
                .error(R.drawable.error_loading_image)
                .into(holder.image);

        if (view_type.equals("edit_service")) {
            holder.delete.setVisibility(View.VISIBLE);
        }
        if (view_type.equals("new_service")) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i_i = new Intent(holder.itemView.getContext(), FullScreenGallery.class);
                    Bundle i_b = new Bundle();
                    i_b.putString("parent", ld.getParent_id());
                    i_i.putExtras(i_b);
                    holder.itemView.getContext().startActivity(i_i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, delete;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            delete = itemView.findViewById(R.id.delete);
        }
    }

}
