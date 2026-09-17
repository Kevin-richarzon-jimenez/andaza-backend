package com.andanza.backend.repository;

import com.andanza.backend.entity.ConnectionCheck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionCheckRepository extends JpaRepository<ConnectionCheck, Long> {
}
