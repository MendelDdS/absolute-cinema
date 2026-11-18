package com.cinema.catalog.adapter.out.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query(value = """
            SELECT *
            FROM outbox
            WHERE published_at IS NULL
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
           """, nativeQuery = true
    )
    List<OutboxEventJpaEntity> findBatchToPublish(@Param("limit") int limit);
}
