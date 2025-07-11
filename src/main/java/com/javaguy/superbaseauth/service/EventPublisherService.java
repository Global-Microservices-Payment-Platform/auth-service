package com.javaguy.superbaseauth.service;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaguy.superbaseauth.model.AuthEvent;
import com.javaguy.superbaseauth.model.AuthEventType;
import com.javaguy.superbaseauth.model.AuthResponse;
import com.javaguy.superbaseauth.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final ServiceBusSenderClient serviceBusSenderClient;
    private final ObjectMapper objectMapper;

    public void publishAuthEvent(AuthEventType eventType, AuthResponse authResponse) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventType", eventType);
            eventData.put("user", authResponse.getUser());
            eventData.put("accessToken", authResponse.getAccessToken());
            eventData.put("refreshToken", authResponse.getRefreshToken());
            eventData.put("tokenType", authResponse.getTokenType());
            eventData.put("expiresIn", authResponse.getExpiresIn());
            eventData.put("expiresAt", authResponse.getExpiresIn());
            eventData.put("timestamp", Instant.now().toString());

            String messagePayload = objectMapper.writeValueAsString(eventData);

            // Create message
            ServiceBusMessage message = new ServiceBusMessage(messagePayload);

            // Essential message properties
            message.setMessageId(UUID.randomUUID().toString());
            message.setContentType("application/json");
            message.setSubject(eventType.toString());
            message.setTimeToLive(java.time.Duration.ofHours(24)); // Message expires in 24 hours

            // Custom properties for filtering and routing
            message.getApplicationProperties().put("eventType", eventType.toString());
            message.getApplicationProperties().put("userId", authResponse.getUser().getId());
            message.getApplicationProperties().put("userEmail", authResponse.getUser().getEmail());
            message.getApplicationProperties().put("hasAccessToken", authResponse.getAccessToken() != null);
            message.getApplicationProperties().put("hasRefreshToken", authResponse.getRefreshToken() != null);
            message.getApplicationProperties().put("timestamp", Instant.now().toString());
            message.getApplicationProperties().put("source", "superbase-auth-service");
            message.getApplicationProperties().put("version", "2.0");

            // Send the message
            serviceBusSenderClient.sendMessage(message);
            log.info("📤 Sent {} event for user: {} | MessageId: {}",
                    eventType, authResponse.getUser().getEmail(), message.getMessageId());
            log.info("📋 User Details: ID={}, Email={}, Role={}",
                    authResponse.getUser().getId(), authResponse.getUser().getEmail());
            log.info("🔑 Token Details: AccessToken={}, RefreshToken={}, ExpiresIn={}",
                    authResponse.getAccessToken() != null ? "Present" : "Missing",
                    authResponse.getRefreshToken() != null ? "Present" : "Missing",
                    authResponse.getExpiresIn());
            log.info("📦 Complete Message Payload: {}", messagePayload);

        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize auth message for user {}: {}", authResponse.getUser().getEmail(), e.getMessage());
            throw new RuntimeException("Auth message serialization failed", e);
        } catch (Exception e) {
            log.error("❌ Failed to send auth message for user {}: {}", authResponse.getUser().getEmail(), e.getMessage());
            throw new RuntimeException("Auth message sending failed", e);
        }
    }
}