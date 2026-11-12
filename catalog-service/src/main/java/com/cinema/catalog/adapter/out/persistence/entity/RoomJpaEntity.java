package com.cinema.catalog.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rooms")
public class RoomJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @ElementCollection
    @CollectionTable(name = "room_seats", joinColumns = @JoinColumn(name = "room_id"))
    @OrderBy("seatRow ASC, seatNumber ASC")
    private List<SeatEmbeddable> seats = new ArrayList<>();

    protected RoomJpaEntity() { }

    public RoomJpaEntity(UUID id, String name, List<SeatEmbeddable> seats) {
        this.id = id;
        this.name = name;
        this.seats = seats;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<SeatEmbeddable> getSeats() {
        return seats;
    }
}
