package com.cs407.spendsmart;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.util.Objects;

public class Message {
    private final String message;
    private final Date date;
    private final String name;
    private final String email;
    boolean sender;
    FirebaseAuth mAuth;

    public Message(String message,Date date, String name, String email) {
        this.message = message;
        this.date = date;
        this.name = name;
        this.email = email;
        mAuth = FirebaseAuth.getInstance();
        if(name.equals(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName())){
            this.sender = true;
        }
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isSender() {
        return sender;
    }
}
