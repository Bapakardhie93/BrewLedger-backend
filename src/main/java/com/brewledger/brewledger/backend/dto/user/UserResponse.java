package com.brewledger.brewledger.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String username;
    private Boolean active;
    private Boolean mustChangePassword;
    private LocalDateTime lastLogin;
    private String phoneNumber;
    private LocalDateTime lastActivity;
    private Boolean isOnline;
    private RoleResponse role;
}
