package com.andanza.backend.contact;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, UUID> {

    boolean existsByEmailIgnoreCase(String email);
}
