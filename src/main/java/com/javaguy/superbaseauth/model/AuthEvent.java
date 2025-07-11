package com.javaguy.superbaseauth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthEvent {
    private AuthEventType eventType;
    private User user;
    private LocalDateTime timestamp = LocalDateTime.now();

    public AuthEvent(AuthEventType eventType, User user) {
        this.eventType = eventType;
        this.user = user;
    }
}