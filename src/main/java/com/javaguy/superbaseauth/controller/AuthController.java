package com.javaguy.superbaseauth.controller;

import com.javaguy.superbaseauth.model.*;
import com.javaguy.superbaseauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(
            @Valid @RequestBody SignupRequest signupRequest,
            BindingResult bindingResult) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append("; ")
            );
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(errorMessage.toString()));
        }

        try {
            System.out.println("Signup request received for email: " + signupRequest.getEmail());
            AuthResponse authResponse = authService.signup(signupRequest);

            return ResponseEntity.ok()
                    .body(ApiResponse.success("User registered successfully", authResponse));

        } catch (Exception e) {
            System.err.println("Signup controller error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            BindingResult bindingResult) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append("; ")
            );
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(errorMessage.toString()));
        }

        try {
            AuthResponse authResponse = authService.login(loginRequest);
            return ResponseEntity.ok()
                    .body(ApiResponse.success("Login successful", authResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }
    /*

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getUserProfile(String authHeader){
        try{
            String token = authHeader.replace("Bearer ", "");
            User user = authService.getUserProfile(token);
            return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", user));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(ApiResponse.error("Error retrieving user profile"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            authService.logout(token);
            return ResponseEntity.ok(ApiResponse.success("Logout successful", "You have been logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error logging out: " + e.getMessage()));
        }
    }
    /
     */
}
