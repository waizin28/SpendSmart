package com.cs407.spendsmart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.List;

import io.getstream.avatarview.AvatarView;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
	private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private final Context mContext;
    private final List<Message> mMessageList;
    private String prevDate = null;
    private String prevName = null;

    public MessageListAdapter(Context context, List<Message> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = (Message) mMessageList.get(position);
        if (message.isSender()) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_message, parent, false);
            return new SentMessageHolder(view);
        }
        else if(viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_message, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder)holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder)holder).bind(message);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText, dateText;
        AvatarView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.chat_message_me);
            timeText = (TextView) itemView.findViewById(R.id.chat_timestamp_other);
            nameText = (TextView) itemView.findViewById(R.id.chat_user_other);
            dateText = (TextView) itemView.findViewById(R.id.chat_date_other);
            profileImage = (AvatarView) itemView.findViewById(R.id.chat_profile_other);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            timeText.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(message.getDate()));
            if(DateFormat.getDateInstance().format(message.getDate()).equals(prevDate)){
                dateText.setVisibility(View.GONE);
            }
            else {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateFormat.getDateInstance().format(message.getDate()));
                prevDate = DateFormat.getDateInstance().format(message.getDate());
            }
            nameText.setVisibility(View.VISIBLE);
            nameText.setText(message.getName());
            prevName = message.getName();
            profileImage.setVisibility(View.VISIBLE);

            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(message.getEmail());
            docRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // User has uploaded a profile pic
                        if(document.get("profile_pic") != null){
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference pathReference = storageRef.child("profile_pics/"+message.getEmail());
                            final long LIMIT = 2048 * 2048;
                            pathReference.getBytes(LIMIT).addOnSuccessListener(bytes -> {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                profileImage.setAvatarInitials(null);
                                profileImage.setImageBitmap(bitmap);
                                profileImage.setAvatarInitialsBackgroundColor(ContextCompat.getColor(mContext,R.color.white));
                            });
                        }
                        // User doesn't have profile_pic: Assign default initial pic.
                        else {
                            profileImage.setAvatarInitials(message.getName().substring(0,1).toUpperCase());
                            profileImage.setAvatarInitialsBackgroundColor(ContextCompat.getColor(mContext, R.color.dark_green));
                        }
                    }
                }
            });

        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, dateText, nameText;
        AvatarView profileImage;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.chat_message_me);
            timeText = (TextView) itemView.findViewById(R.id.chat_timestamp_me);
            nameText = (TextView) itemView.findViewById(R.id.chat_user_me);
            dateText = (TextView) itemView.findViewById(R.id.chat_date_me);
            profileImage = (AvatarView) itemView.findViewById(R.id.chat_profile_me);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            timeText.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(message.getDate()));
            if(DateFormat.getDateInstance().format(message.getDate()).equals(prevDate)){
                dateText.setVisibility(View.GONE);
            }
            else {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateFormat.getDateInstance().format(message.getDate()));
                prevDate = DateFormat.getDateInstance().format(message.getDate());
            }
            nameText.setVisibility(View.VISIBLE);
            nameText.setText(message.getName());
            prevName = message.getName();
            profileImage.setVisibility(View.VISIBLE);

            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(message.getEmail());
            docRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // User has uploaded a profile pic
                        if(document.get("profile_pic") != null){
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference pathReference = storageRef.child("profile_pics/"+message.getEmail());
                            final long LIMIT = 2048 * 2048;
                            pathReference.getBytes(LIMIT).addOnSuccessListener(bytes -> {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                profileImage.setAvatarInitials(null);
                                profileImage.setImageBitmap(bitmap);
                                profileImage.setAvatarInitialsBackgroundColor(ContextCompat.getColor(mContext,R.color.white));
                            });
                        }
                        // User doesn't have profile_pic: Assign default initial pic.
                        else {
                            profileImage.setAvatarInitials(message.getName().substring(0,1).toUpperCase());
                            profileImage.setAvatarInitialsBackgroundColor(ContextCompat.getColor(mContext, R.color.dark_green));
                        }
                    }
                }
            });

        }
    }

}

