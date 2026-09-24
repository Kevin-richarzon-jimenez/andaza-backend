package com.andanza.backend.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByStatus(CommentStatus status, Pageable pageable);

    List<Comment> findByProductIdAndStatusOrderByCreatedAtDesc(UUID productId, CommentStatus status);

    List<Comment> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
