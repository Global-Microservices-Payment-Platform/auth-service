package com.javaguy.superbaseauth.service;
import com.javaguy.superbaseauth.config.SupabaseConfig;
import com.javaguy.superbaseauth.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;


import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final RestTemplate restTemplate;
    private final SupabaseConfig supabaseConfig;
    private final HttpHeaders supabaseHeaders;
    private final EventPublisherService eventPublisherService;

    public AuthResponse signup(SignupRequest signupRequest) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/signup";

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", signupRequest.getEmail());
            requestBody.put("password", signupRequest.getPassword());

            // Create headers with proper authorization
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseConfig.getSupabaseKey());
            headers.set("Authorization", "Bearer " + supabaseConfig.getSupabaseKey());
            headers.setContentType(APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Making signup request to: {}", url);
            log.info("Request headers: {}", headers);
            log.info("Request body: {}", requestBody);

            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    AuthResponse.class
            );
            log.info("Signup response status: {}", response.getStatusCode());

            // Publish the event with full auth response
            try {
                if (response.getBody() != null && response.getBody().getUser() != null) {
                    eventPublisherService.publishAuthEvent(AuthEventType.SIGNUP, response.getBody());
                }
            } catch (Exception e) {
                log.error("Failed to publish signup event, but continuing with signup: {}", e.getMessage());
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Signup HTTP error: {}", e.getStatusCode());
            log.error("Response body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Signup failed: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Connection error to Supabase: {}", e.getMessage(), e);
            throw new RuntimeException("Connection error to Supabase: " + e.getMessage() +
                    ". Please verify network connectivity and Supabase service availability.");
        } catch (Exception e) {
            log.error("Signup error: {}", e.getMessage(), e);
            throw new RuntimeException("Signup failed: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest loginRequest) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/token?grant_type=password";
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", loginRequest.getEmail());
            requestBody.put("password", loginRequest.getPassword());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, supabaseHeaders);
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, entity, AuthResponse.class);
            log.info("Login response status: {}", response.getStatusCode());

            // Publish the login event with full auth response
            if (response.getBody() != null && response.getBody().getUser() != null) {
                eventPublisherService.publishAuthEvent(AuthEventType.LOGIN, response.getBody());
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            // Handle specific HTTP errors
            log.error("Login HTTP error: {}", e.getStatusCode());
            String errorMessage = "Error logging in user: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while logging in user", e);
        }
    }
}