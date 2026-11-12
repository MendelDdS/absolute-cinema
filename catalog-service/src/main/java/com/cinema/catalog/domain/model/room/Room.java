package com.cinema.catalog.domain.model.room;

import com.cinema.catalog.domain.exception.DomainException;

import java.util.*;

public class Room {
    private final RoomId id;
    private final String name;
    private final Set<Seat> seats;

    private Room(RoomId id, String name, List<Seat> seats) {
        if (name == null || name.isBlank()) {
            throw new DomainException("O nome da sala é obrigatório");
        }
        if (seats == null || seats.isEmpty()) {
            throw new DomainException("A sala precisa ter pelo menos um assento");
        }
        Set<Seat> unique = new LinkedHashSet<>(seats);
        if (unique.size() != seats.size()) {
            throw new DomainException("A sala não pode ter assentos repetidos");
        }

        this.id = Objects.requireNonNull(id);
        this.name = name.strip();
        this.seats = Collections.unmodifiableSet(unique);
    }

    public RoomId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public static Room create(String name, List<Seat> seats) {
        return new Room(RoomId.generate(), name, seats);
    }

    public static Room restore(RoomId id, String name, List<Seat> seats) {
        return new Room(id, name, seats);
    }

    public static Room withGrid(String name, int rows, int seatsPerRow) {
        if (rows < 1 || rows > 26) {
            throw new DomainException("A sala deve ter entre 1 e 26 fileiras");
        }
        if (seatsPerRow < 1) {
            throw new DomainException("Cada fileira precisa de pelo menos um assento");
        }

        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            String row = String.valueOf((char) ('A' + r));
            for (int n = 1; n <= seatsPerRow; n++) {
                seats.add(new Seat(row, n));
            }
        }

        return create(name, seats);
    }

    public int capacity() {
        return seats.size();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Room other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
