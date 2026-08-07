package com.kartyavya.email.mail;

public interface EmailService {

    /**
     * Sends an HTML email.
     *
     * @param recipientEmail Recipient email address
     * @param subject Email subject
     * @param body HTML email body
     */
    void sendEmail(String recipientEmail, String subject, String body);

}