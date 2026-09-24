package com.andanza.backend.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select u from User u where lower(u.email) like :pattern or lower(u.firstName) like :pattern "
            + "or lower(u.lastName) like :pattern")
    Page<User> search(@Param("pattern") String pattern, Pageable pageable);
}
