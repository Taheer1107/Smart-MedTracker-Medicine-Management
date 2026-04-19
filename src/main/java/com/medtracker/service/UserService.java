package com.medtracker.service;

import com.medtracker.model.*;
import com.medtracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service Layer for User Management
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // ================= REGISTER USER =================
    public User registerUser(User user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash password before saving
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    // ================= DEFAULT ADMIN =================
    /**
     * Ensures default admin user exists. Creates admin/admin123 only if no admin user found.
     * Uses existsByUsername to avoid loading entity (prevents failures with legacy/incompatible DB data).
     */
    public void ensureDefaultAdminExists() {
        if (!userRepository.existsByUsername("admin")) {
            PrimaryCaretaker admin = new PrimaryCaretaker("admin", "admin123", "Administrator", "admin@medtracker.local");
            admin.setRole("ADMIN");
            registerUser(admin);
        }
    }

    // ================= LOGIN =================
    public Optional<User> login(String username, String password) {

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Check hashed password + active status
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(password, user.getPassword()) && user.isActive()) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }
    // ================= GET USER =================
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public List<User> getUsersByHousehold(Household household) {
        return userRepository.findByHousehold(household);
    }

    public List<User> getUsersByHouseholdId(Long householdId) {
        return userRepository.findByHouseholdId(householdId);
    }

    // ================= UPDATE USER =================
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getUserId())) {
            throw new IllegalArgumentException("User not found");
        }
        return userRepository.save(user);
    }

    // ================= DELETE USER =================
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // ================= ACTIVATE / DEACTIVATE =================
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(false);
        return userRepository.save(user);
    }

    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(true);
        return userRepository.save(user);
    }

    // ================= UPDATE LAST LOGIN =================
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.updateLastLogin();
        userRepository.save(user);
    }

    // ================= FAMILY / CARETAKER =================
    public List<FamilyMember> getAllFamilyMembers() {
        return userRepository.findAllFamilyMembers();
    }

    public List<PrimaryCaretaker> getAllPrimaryCaretakers() {
        return userRepository.findAllPrimaryCaretakers();
    }

    public Optional<PrimaryCaretaker> getPrimaryCaretakerForHousehold(Long householdId) {
        return userRepository.findPrimaryCaretakerByHouseholdId(householdId);
    }

    // ================= COUNT =================
    public Long countUsersByRole(String role) {
        return userRepository.countByRole(role);
    }

    // ================= CHANGE PASSWORD =================
    public User changePassword(Long userId, String oldPassword, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify old password using BCrypt
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // Hash new password
        String hashedNewPassword = encoder.encode(newPassword);
        user.setPassword(hashedNewPassword);

        return userRepository.save(user);
    }

    // ================= AVAILABILITY CHECKS =================
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}