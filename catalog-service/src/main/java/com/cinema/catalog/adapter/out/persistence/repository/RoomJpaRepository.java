package com.cinema.catalog.adapter.out.persistence.repository;

import com.cinema.catalog.adapter.out.persistence.entity.RoomJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = "seats")
    List<RoomJpaEntity> findAll();
}
