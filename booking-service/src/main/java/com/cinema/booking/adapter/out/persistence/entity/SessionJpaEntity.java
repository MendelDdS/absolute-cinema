package com.cinema.booking.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class SessionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @ElementCollection
    @CollectionTable(name = "session_seats", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "seat_code", nullable = false)
    @OrderColumn(name = "seat_order")
    private List<String> seats = new ArrayList<>();

    protected SessionJpaEntity() {}


    public SessionJpaEntity(UUID id, Instant startsAt, BigDecimal price, List<String> seats) {
        this.id = id;
        this.startsAt = startsAt;
        this.price = price;
        this.seats = seats;
    }

    public UUID getId() {
        return id;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public List<String> getSeats() {
        return seats;
    }
}
