package com.example.project.email;

import jakarta.mail.MessagingException;

public interface EmailSender {

    void send(String to, String email, String subject) throws MessagingException;
}
