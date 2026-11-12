package com.cinema.catalog.application.service;

import com.cinema.catalog.application.port.in.CreateRoomCommand;
import com.cinema.catalog.application.port.in.CreateRoomUseCase;
import com.cinema.catalog.application.port.out.RoomRepository;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.room.RoomId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRoomService implements CreateRoomUseCase {

    private final RoomRepository roomRepository;

    public CreateRoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public RoomId create(CreateRoomCommand command) {
        Room room = Room.withGrid(command.name(), command.rows(), command.seatsPerRow());
        roomRepository.save(room);
        return room.getId();
    }
}