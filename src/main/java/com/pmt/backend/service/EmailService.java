package com.pmt.backend.service;

public interface EmailService {
    void send(String toEmail, String subject, String body);
}
