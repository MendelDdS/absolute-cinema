package com.cinema.catalog.adapter.in.web;

import com.cinema.catalog.application.port.in.CreateRoomUseCase;
import com.cinema.catalog.application.port.in.query.RoomQueryUseCase;
import com.cinema.catalog.application.query.RoomView;
import com.cinema.catalog.domain.model.room.RoomId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
class RoomController {

    private final CreateRoomUseCase createRoom;
    private final RoomQueryUseCase roomQuery;

    RoomController(CreateRoomUseCase createRoom, RoomQueryUseCase roomQuery) {
        this.createRoom = createRoom;
        this.roomQuery = roomQuery;
    }

    @PostMapping
    ResponseEntity<Void> create(@RequestBody CreateRoomRequest request) {
        RoomId id = createRoom.create(request.toCommand());
        return ResponseEntity.created(URI.create("/rooms/" + id.value())).build();
    }

    @GetMapping
    List<RoomView> list() {
        return roomQuery.list();
    }

    @GetMapping("/{id}")
    RoomView findRoom(@PathVariable UUID id) {
        return roomQuery.get(new RoomId(id));
    }
}
