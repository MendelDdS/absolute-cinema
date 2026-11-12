package com.cinema.catalog.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class SessionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "movie_id", nullable = false)
    private UUID movieId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected SessionJpaEntity() {}

    public SessionJpaEntity(UUID id, UUID movieId, UUID roomId,
                            Instant startsAt, Instant endsAt, BigDecimal price) {
        this.id = id;
        this.movieId = movieId;
        this.roomId = roomId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.price = price;
    }

    public UUID getId() { return id; }
    public UUID getMovieId() { return movieId; }
    public UUID getRoomId() { return roomId; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public BigDecimal getPrice() { return price; }
}