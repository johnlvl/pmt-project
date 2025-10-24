package com.pmt.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoopEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(NoopEmailService.class);

    @Override
    public void send(String toEmail, String subject, String body) {
        // Mock email sending by logging the payload. Replace with real SMTP integration when needed.
        if (log.isInfoEnabled()) {
            log.info("[MockEmail] To: {} | Subject: {}\n{}", toEmail, subject, body);
        }
    }
}
