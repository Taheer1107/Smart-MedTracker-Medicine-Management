package com.medtracker.repository;

import com.medtracker.model.User;
import com.medtracker.model.FamilyMember;
import com.medtracker.model.PrimaryCaretaker;
import com.medtracker.model.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by username
    Optional<User> findByUsername(String username);

    // Find by email
    Optional<User> findByEmail(String email);

    // Find by role
    List<User> findByRole(String role);

    // Find all active users
    List<User> findByActiveTrue();

    // Find by household
    List<User> findByHousehold(Household household);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find all family members
    @Query("SELECT u FROM FamilyMember u")
    List<FamilyMember> findAllFamilyMembers();

    // Find all primary caretakers
    @Query("SELECT u FROM PrimaryCaretaker u")
    List<PrimaryCaretaker> findAllPrimaryCaretakers();

    // Find users by household ID
    @Query("SELECT u FROM User u WHERE u.household.householdId = :householdId")
    List<User> findByHouseholdId(@Param("householdId") Long householdId);

    // Count users by role
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") String role);

    // Find primary caretaker for household
    @Query("SELECT u FROM PrimaryCaretaker u WHERE u.household.householdId = :householdId")
    Optional<PrimaryCaretaker> findPrimaryCaretakerByHouseholdId(@Param("householdId") Long householdId);
}