package com.shopapp.UserService.service.impl;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsSenderServiceImpl {

    @Value("${twilio.account_sid}")
    private String ACCOUNT_SID;

    @Value("${twilio.auth_token}")
    private String AUTH_TOKEN;

    @Value("${twilio.phone_number}")
    private String TWILIO_PHONE_NUMBER;

    @Value("${twilio.mss}")
    private String MESSAGING_SERVICE_SID;

    @PostConstruct
    public void init() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        System.out.println("Twilio initialized successfully.");
        System.out.println("Twilio phone number: " + TWILIO_PHONE_NUMBER);
        System.out.println("MessagingServiceSid: " + MESSAGING_SERVICE_SID);
    }

    public void sendSms(String phoneNumber, String messageText) {
        try {
            if (MESSAGING_SERVICE_SID != null && !MESSAGING_SERVICE_SID.isEmpty()) {
                // Send using MessagingServiceSid
                Message message = Message.creator(
                        new com.twilio.type.PhoneNumber(phoneNumber),
                        MESSAGING_SERVICE_SID,
                        messageText
                ).create();
                System.out.println("Sent SMS via MessagingServiceSid. SID: " + message.getSid());
            } else if (TWILIO_PHONE_NUMBER != null && !TWILIO_PHONE_NUMBER.isEmpty()) {
                // Send using Twilio phone number
                Message message = Message.creator(
                        new com.twilio.type.PhoneNumber(phoneNumber),
                        new com.twilio.type.PhoneNumber(TWILIO_PHONE_NUMBER),
                        messageText
                ).create();
                System.out.println("Sent SMS via Twilio phone number. SID: " + message.getSid());
            } else {
                throw new IllegalArgumentException("Neither MessagingServiceSid nor Twilio phone number is set.");
            }
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
