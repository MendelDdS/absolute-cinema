package com.cinema.catalog.adapter.out.persistence.repository;

import com.cinema.catalog.adapter.out.persistence.entity.SessionJpaEntity;
import com.cinema.catalog.application.query.SessionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionJpaRepository extends JpaRepository<SessionJpaEntity, UUID> {

    @Query("""
            SELECT s 
            FROM SessionJpaEntity s 
            WHERE s.roomId = :roomId
              and s.startsAt < :to
              and :from < s.endsAt
            """)
    List<SessionJpaEntity> findOverlapping(
            @Param("roomId") UUID roomId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    // --- Leitura (usado pelo SessionQueryRepository) ---

    @Query("""
            select new com.cinema.catalog.application.query.SessionView(
                s.id, m.id, m.title, r.id, r.name, s.startsAt, s.endsAt, s.price)
            from SessionJpaEntity s
            join MovieJpaEntity m on m.id = s.movieId
            join RoomJpaEntity r on r.id = s.roomId
            where s.startsAt >= :from and s.startsAt < :to
            order by s.startsAt
            """)
    List<SessionView> findViewsStartingBetween(@Param("from") Instant from,
                                               @Param("to") Instant to);

    @Query("""
            select new com.cinema.catalog.application.query.SessionView(
                s.id, m.id, m.title, r.id, r.name, s.startsAt, s.endsAt, s.price)
            from SessionJpaEntity s
            join MovieJpaEntity m on m.id = s.movieId
            join RoomJpaEntity r on r.id = s.roomId
            where s.movieId = :movieId
              and s.startsAt >= :from and s.startsAt < :to
            order by s.startsAt
            """)
    List<SessionView> findViewsByMovieStartingBetween(@Param("movieId") UUID movieId,
                                                      @Param("from") Instant from,
                                                      @Param("to") Instant to);

    @Query("""
            select new com.cinema.catalog.application.query.SessionView(
                s.id, m.id, m.title, r.id, r.name, s.startsAt, s.endsAt, s.price)
            from SessionJpaEntity s
            join MovieJpaEntity m on m.id = s.movieId
            join RoomJpaEntity r on r.id = s.roomId
            where s.id = :id
            """)
    Optional<SessionView> findViewById(@Param("id") UUID id);
}
