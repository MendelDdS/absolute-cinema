package com.cinema.catalog.adapter.out.persistence.mapper;

import com.cinema.catalog.adapter.out.persistence.entity.RoomJpaEntity;
import com.cinema.catalog.adapter.out.persistence.entity.SeatEmbeddable;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.room.RoomId;
import com.cinema.catalog.domain.model.room.Seat;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoomPersistenceMapper {

    public RoomJpaEntity toEntity(Room room) {
        List<SeatEmbeddable> seats = room.getSeats().stream()
                .map(seat -> new SeatEmbeddable(seat.row(), seat.number()))   // Seat → SeatEmbeddable
                .toList();
        return new RoomJpaEntity(room.getId().value(), room.getName(), seats);
    }

    public Room toDomain(RoomJpaEntity entity) {
        List<Seat> seats = entity.getSeats().stream()
                .map(seat -> new Seat(seat.getSeatRow(), seat.getSeatNumber()))   // SeatEmbeddable → Seat
                .toList();
        return Room.restore(new RoomId(entity.getId()), entity.getName(), seats);
    }
}