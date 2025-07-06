package com.javaguy.superbaseauthdemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaguy.superbaseauthdemo.config.SupabaseConfig;
import com.javaguy.superbaseauthdemo.model.AuthResponse;
import com.javaguy.superbaseauthdemo.model.LoginRequest;
import com.javaguy.superbaseauthdemo.model.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RestTemplate restTemplate;
    private final SupabaseConfig supabaseConfig;
    private final HttpHeaders supabaseHeaders;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthResponse signup(SignupRequest signupRequest) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/signup";

            // Create request body with proper structure
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", signupRequest.getEmail());
            requestBody.put("password", signupRequest.getPassword());

            // Create headers with proper authorization
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseConfig.getSupabaseKey());
            headers.set("Authorization", "Bearer " + supabaseConfig.getSupabaseKey());
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("Making signup request to: " + url);
            System.out.println("Request body: " + requestBody);

            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    AuthResponse.class
            );
            System.out.println("Raw Supabase response: " + objectMapper.writeValueAsString(response.getBody()));            System.out.println("Signup response status: " + response.getStatusCode());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            System.err.println("Signup HTTP error: " + e.getStatusCode());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            throw new RuntimeException("Signup failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Signup general error: " + e.getMessage());
            throw new RuntimeException("Signup failed: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest loginRequest) {
        try{
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/token?grant_type=password";
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", loginRequest.getEmail());
            requestBody.put("password", loginRequest.getPassword());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, supabaseHeaders);
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, entity, AuthResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            // Handle specific HTTP errors
            String errorMessage = "Error logging in user: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while logging in user", e);
        }
    }

}
