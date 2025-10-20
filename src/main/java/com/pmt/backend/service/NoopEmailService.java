package com.pmt.backend.service;

import org.springframework.stereotype.Service;

@Service
public class NoopEmailService implements EmailService {
    @Override
    public void send(String toEmail, String subject, String body) {
        // No-op implementation for development/tests. Replace with real SMTP integration when needed.
    }
}
