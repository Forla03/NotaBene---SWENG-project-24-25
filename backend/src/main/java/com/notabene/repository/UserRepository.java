package com.notabene.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.notabene.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT u.id, u.username FROM User u WHERE u.id IN :userIds")
    List<Object[]> findUsernamesByIds(@Param("userIds") List<Long> userIds);
}
