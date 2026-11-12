package com.cinema.catalog.adapter.out.persistence.repository;

import com.cinema.catalog.adapter.out.persistence.entity.MovieJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MovieJpaRepository extends JpaRepository<MovieJpaEntity, UUID> { }