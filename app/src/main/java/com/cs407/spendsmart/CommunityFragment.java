package com.cs407.spendsmart;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommunityFragment extends Fragment {
    private List<Message> messageList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private boolean recyclerLoaded = false;

    public CommunityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button sendBtn = requireActivity().findViewById(R.id.buttonChat);
        sendBtn.setOnClickListener(view1 -> {
            EditText messageText = requireActivity().findViewById(R.id.chat_message);
            Date current = Calendar.getInstance().getTime();
            Map<String, Object> message = new HashMap<>();
            message.put("date",current);
            message.put("message",messageText.getText().toString());
            message.put("name", Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
            message.put("email",Objects.requireNonNull(mAuth.getCurrentUser().getEmail()));
            db.collection("chat").add(message);
            messageText.setText("");
            loadMessages();
        });

        if(!recyclerLoaded) {
            loadMessages();
        }
        else {
            displayMessages();
        }
    }

    public void displayMessages() {
        RecyclerView mMessageRecycler = (RecyclerView) requireView().findViewById(R.id.recycler_chat);
        MessageListAdapter mMessageAdapter = new MessageListAdapter(getContext(), messageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setSmoothScrollbarEnabled(false);
        mMessageRecycler.setLayoutManager(linearLayoutManager);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.smoothScrollToPosition(messageList.size()-1);
    }

    public void loadMessages() {
        messageList = new ArrayList<>();
        db.collection("chat").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> docList = queryDocumentSnapshots.getDocuments();
            for (int i = 0; i < docList.size(); i++) {
                Message message = new Message(Objects.requireNonNull(docList.get(i).get("message")).toString(),
                        ((Timestamp) Objects.requireNonNull(docList.get(i).get("date"))).toDate(),
                        Objects.requireNonNull(docList.get(i).get("name")).toString(),
                        Objects.requireNonNull(docList.get(i).get("email")).toString());
                messageList.add(message);
                messageList.sort((message1, t1) -> {
                    if (message1.getDate().after(t1.getDate())) {
                        return 1;
                    }
                    else if(message1.getDate().before(t1.getDate())){
                        return -1;
                    }
                    else{
                        return 0;
                    }
                });
                displayMessages();
                recyclerLoaded = true;
            }
        });
    }
}