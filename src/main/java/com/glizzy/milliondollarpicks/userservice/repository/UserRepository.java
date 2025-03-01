package com.glizzy.milliondollarpicks.userservice.repository;

import com.glizzy.milliondollarpicks.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find a user by their username
     * @param username the username to search for
     * @return an Optional with the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user with the given username exists
     * @param username the username to check
     * @return true if the user exists, false otherwise
     */
    boolean existsByUsername(String username);
}