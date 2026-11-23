package com.cinema.booking.adapter.out.persistence.repository;

import com.cinema.booking.adapter.out.persistence.entity.SessionJpaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionJpaRepository extends JpaRepository<SessionJpaEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = "seats")
    Optional<SessionJpaEntity> findById(@NonNull UUID id);
}
