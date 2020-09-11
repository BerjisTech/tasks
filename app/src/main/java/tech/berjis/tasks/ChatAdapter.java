package tech.berjis.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vanniktech.emoji.EmojiTextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context mContext;
    private List<Chat> listData;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private String UID;

    ChatAdapter(Context mContext, List<Chat> listData) {
        this.listData = listData;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        UID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        View view = LayoutInflater.from(mContext).inflate(R.layout.dm, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Chat ld = listData.get(position);

        hideViews(ld, holder, position);
        if (ld.getSender().equals(UID)) {
            loadSenderView(ld, holder);
        }
        if (ld.getReceiver().equals(UID)) {
            loadReceiverView(ld, holder);
        }
        if (ld.getType().equals("photo")) {
            if (!ld.isDelete()) {
                holder.senderImageCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewGallery(ld, holder);
                    }
                });
                holder.receiverImageCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewGallery(ld, holder);
                    }
                });
                holder.senderImageCard.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener(holder, position, ld);
                        return false;
                    }
                });
                holder.receiverImageCard.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener(holder, position, ld);
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        EmojiTextView receiverChatText, senderChatText, receiverImageCard, senderImageCard;
        TextView receiverChatTime, senderChatTime;
        View mView;
        Context vContext;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverImageCard = itemView.findViewById(R.id.receiverImageCard);
            receiverChatText = itemView.findViewById(R.id.receiverChatText);
            receiverChatTime = itemView.findViewById(R.id.receiverChatTime);
            senderImageCard = itemView.findViewById(R.id.senderImageCard);
            senderChatText = itemView.findViewById(R.id.senderChatText);
            senderChatTime = itemView.findViewById(R.id.senderChatTime);
            mView = itemView;
            vContext = itemView.getContext();
        }
    }

    /*private void itemSelection(ViewHolder holder, String action, int position, Chat ld) {
        // Retrieving the item

        if (position != RecyclerView.NO_POSITION) {
            if (action.equals("delete")) {
                DMActivity.manageSelection(holder, ld, position);
            } else {
                DMActivity.manageSelection(holder, ld, position);
                DMActivity.replyTo(holder, ld, position);
            }
        }
    }*/

    /* The method for managing the long click on an image.
    public boolean onLongClick(View view) {

        int position = getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            Intent intent = new Intent(mContext, DMActivity.class);
            intent.putExtra("KEY4URL", position);
            mContext.startActivity(intent);
        }

        // return true to indicate that the click was handled (if you return false onClick will be triggered too)
        return true;
    }*/

    private void longClickListener(final ViewHolder holder, final int position, final Chat ld) {
        PopupMenu popup = new PopupMenu(mContext, holder.mView);
        //inflating menu from xml resource
        popup.inflate(R.menu.chat_options_menu);
        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_for_me:
                        deleteChatForMe(holder, ld, position);
                        return true;
                    case R.id.delete_for_all:
                        deleteChatForAll(holder, ld, position);
                        return true;
                    case R.id.reply_chat:
                        DMActivity.replyTo(holder, ld, position);
                        return true;
                    default:
                        return false;
                }
            }
        });
        //displaying the popup
        popup.show();
    }

    private void deleteChatForMe(ViewHolder holder, Chat ld, int position) {
        dbRef.child("Chats").child(ld.getSender()).child(ld.getReceiver()).child(ld.getSender_chat_id()).child("delete").setValue(true);
        holder.senderChatText.setText("Deleted message");
        holder.senderChatText.setTypeface(null, Typeface.ITALIC);
        holder.senderImageCard.setText("Deleted message");
        holder.senderImageCard.setTypeface(null, Typeface.ITALIC);
        holder.receiverChatText.setText("Deleted message");
        holder.receiverChatText.setTypeface(null, Typeface.ITALIC);
        holder.receiverImageCard.setText("Deleted message");
        holder.receiverImageCard.setTypeface(null, Typeface.ITALIC);
    }

    private void deleteChatForAll(ViewHolder holder, Chat ld, int position) {
        dbRef.child("Chats").child(ld.getSender()).child(ld.getReceiver()).child(ld.getSender_chat_id()).child("delete").setValue(true);
        dbRef.child("Chats").child(ld.getReceiver()).child(ld.getSender()).child(ld.getReceiver_chat_id()).child("delete").setValue(true);

        holder.senderChatText.setText("Deleted message");
        holder.senderChatText.setTypeface(null, Typeface.ITALIC);
        holder.senderImageCard.setText("Deleted message");
        holder.senderImageCard.setTypeface(null, Typeface.ITALIC);
        holder.receiverChatText.setText("Deleted message");
        holder.receiverChatText.setTypeface(null, Typeface.ITALIC);
        holder.receiverImageCard.setText("Deleted message");
        holder.receiverImageCard.setTypeface(null, Typeface.ITALIC);
    }

    private void hideViews(final Chat ld, final ViewHolder holder, final int position) {

        holder.receiverImageCard.setVisibility(View.GONE);
        holder.receiverChatText.setVisibility(View.GONE);
        holder.receiverChatTime.setVisibility(View.GONE);
        holder.senderImageCard.setVisibility(View.GONE);
        holder.senderChatText.setVisibility(View.GONE);
        holder.senderChatTime.setVisibility(View.GONE);
        if (!ld.isDelete()) {
            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longClickListener(holder, position, ld);
                    return false;
                }
            });
        }
    }

    private void loadSenderView(Chat ld, ViewHolder holder) {
        holder.senderChatTime.setVisibility(View.VISIBLE);

        long time = ld.getTime() * 1000;
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String ago = prettyTime.format(new Date(time));

        holder.senderChatTime.setText(ago);

        if (ld.getType().equals("text")) {
            loadSenderText(ld, holder);
        }
        if (ld.getType().equals("photo")) {
            loadSenderImage(ld, holder);
        }

        if (ld.isDelete()) {
            holder.mView.setAlpha(0.5f);
        }
    }

    private void loadReceiverView(Chat ld, ViewHolder holder) {
        holder.receiverChatTime.setVisibility(View.VISIBLE);

        long time = ld.getTime() * 1000;
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String ago = prettyTime.format(new Date(time));

        holder.receiverChatTime.setText(ago);

        if (ld.getType().equals("text")) {
            loadReceiverText(ld, holder);
        }
        if (ld.getType().equals("photo")) {
            loadReceiverImage(ld, holder);
        }
    }

    private void loadSenderText(Chat ld, ViewHolder holder) {
        holder.senderChatText.setVisibility(View.VISIBLE);

        if (ld.isDelete()) {
            holder.senderChatText.setText("Deleted message");
            holder.senderChatText.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.senderChatText.setText(ld.getText());
        }
    }

    private void loadSenderImage(Chat ld, ViewHolder holder) {
        holder.senderImageCard.setVisibility(View.VISIBLE);
        if (ld.isDelete()) {
            holder.senderImageCard.setText("Deleted message");
            holder.senderImageCard.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.senderImageCard.setText("Image \uD83D\uDCF7\uD83C\uDF51\uD83E\uDDD6");
        }
    }

    private void loadReceiverText(Chat ld, ViewHolder holder) {
        holder.receiverChatText.setVisibility(View.VISIBLE);
        if (ld.isDelete()) {
            holder.receiverChatText.setText("Deleted message");
            holder.receiverChatText.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.receiverChatText.setText(ld.getText());
        }
    }

    private void loadReceiverImage(Chat ld, ViewHolder holder) {
        holder.receiverImageCard.setVisibility(View.VISIBLE);
        if (ld.isDelete()) {
            holder.receiverImageCard.setText("Deleted message");
            holder.receiverImageCard.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.receiverImageCard.setText("Image \uD83D\uDCF7\uD83C\uDF51\uD83E\uDDD6");
        }
    }

    private void viewGallery(Chat ld, ViewHolder holder) {
        Intent imageIntent = new Intent(mContext, ChatGallery.class);
        Bundle imageBundle = new Bundle();
        imageBundle.putString("sender_chat_id", ld.getSender_chat_id());
        imageBundle.putString("receiver_chat_id", ld.getReceiver_chat_id());
        imageBundle.putString("sender", ld.getSender());
        imageBundle.putString("receiver", ld.getReceiver());
        imageIntent.putExtras(imageBundle);
        mContext.startActivity(imageIntent);
    }

}
