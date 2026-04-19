package com.medtracker.controller;

import com.medtracker.model.User;
import com.medtracker.model.FamilyMember;
import com.medtracker.model.PrimaryCaretaker;
import com.medtracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for User Management
 * Handles all user-related HTTP requests
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // Register new user (web API - accepts simple JSON)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering user: " + e.getMessage());
        }
    }

    // Register from web form (username, password, name, email, role)
    @PostMapping("/register-form")
    public ResponseEntity<?> registerFromForm(@RequestBody RegisterRequest request) {
        try {
            User newUser;
            if ("PRIMARY_CARETAKER".equals(request.getRole())) {
                newUser = new PrimaryCaretaker(request.getUsername(), request.getPassword(),
                        request.getName(), request.getEmail() != null ? request.getEmail() : "");
            } else {
                newUser = new FamilyMember(request.getUsername(), request.getPassword(),
                        request.getName(), request.getEmail() != null ? request.getEmail() : "");
            }
            User registeredUser = userService.registerUser(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering user: " + e.getMessage());
        }
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = userService.login(request.getUsername(), request.getPassword());
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(userOpt.get());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed: " + e.getMessage());
        }
    }

    // Get user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with ID: " + userId);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching user: " + e.getMessage());
        }
    }

    // Get all users
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users: " + e.getMessage());
        }
    }

    // Get active users
    @GetMapping("/active")
    public ResponseEntity<?> getActiveUsers() {
        try {
            List<User> users = userService.getActiveUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching active users: " + e.getMessage());
        }
    }

    // Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            List<User> users = userService.getUsersByRole(role);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users by role: " + e.getMessage());
        }
    }

    // Get users by household ID
    @GetMapping("/household/{householdId}")
    public ResponseEntity<?> getUsersByHouseholdId(@PathVariable Long householdId) {
        try {
            List<User> users = userService.getUsersByHouseholdId(householdId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users by household: " + e.getMessage());
        }
    }

    // Get all family members
    @GetMapping("/family-members")
    public ResponseEntity<?> getAllFamilyMembers() {
        try {
            List<FamilyMember> familyMembers = userService.getAllFamilyMembers();
            return ResponseEntity.ok(familyMembers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching family members: " + e.getMessage());
        }
    }

    // Get all primary caretakers
    @GetMapping("/caretakers")
    public ResponseEntity<?> getAllPrimaryCaretakers() {
        try {
            List<PrimaryCaretaker> caretakers = userService.getAllPrimaryCaretakers();
            return ResponseEntity.ok(caretakers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching caretakers: " + e.getMessage());
        }
    }

    // Update user
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody User user) {
        try {
            user.setUserId(userId);
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user: " + e.getMessage());
        }
    }

    // Delete user
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user: " + e.getMessage());
        }
    }

    // Deactivate user
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            User user = userService.deactivateUser(userId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deactivating user: " + e.getMessage());
        }
    }

    // Activate user
    @PutMapping("/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            User user = userService.activateUser(userId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating user: " + e.getMessage());
        }
    }

    // Change password
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long userId, 
                                           @RequestBody PasswordChangeRequest request) {
        try {
            User user = userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing password: " + e.getMessage());
        }
    }

    // Check username availability
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        try {
            boolean available = userService.isUsernameAvailable(username);
            return ResponseEntity.ok(new AvailabilityResponse(available));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking username: " + e.getMessage());
        }
    }

    // Check email availability
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmailAvailability(@PathVariable String email) {
        try {
            boolean available = userService.isEmailAvailable(email);
            return ResponseEntity.ok(new AvailabilityResponse(available));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking email: " + e.getMessage());
        }
    }

    // Count users by role
    @GetMapping("/count/role/{role}")
    public ResponseEntity<?> countUsersByRole(@PathVariable String role) {
        try {
            Long count = userService.countUsersByRole(role);
            return ResponseEntity.ok(new CountResponse(count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error counting users: " + e.getMessage());
        }
    }

    // Inner classes for request/response
    public static class RegisterRequest {
        private String username;
        private String password;
        private String name;
        private String email;
        private String role = "FAMILY_MEMBER";
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class AvailabilityResponse {
        private boolean available;

        public AvailabilityResponse(boolean available) { this.available = available; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }

    public static class CountResponse {
        private Long count;

        public CountResponse(Long count) { this.count = count; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}