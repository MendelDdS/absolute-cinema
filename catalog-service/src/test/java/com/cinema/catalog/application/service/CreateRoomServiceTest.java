package com.cinema.catalog.application.service;

import com.cinema.catalog.application.fake.InMemoryRoomRepository;
import com.cinema.catalog.application.port.in.CreateRoomCommand;
import com.cinema.catalog.domain.exception.DomainException;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.room.RoomId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateRoomServiceTest {

    private final InMemoryRoomRepository roomRepository = new InMemoryRoomRepository();
    private final CreateRoomService service = new CreateRoomService(roomRepository);

    @Test
    @DisplayName("deve salvar a sala e retornar o ID")
    void createsRoom() {
        RoomId id = service.create(new CreateRoomCommand("Sala 1", 1, 1));

        assertThat(roomRepository.findById(id)).isPresent()
                .get().extracting(Room::getName).isEqualTo("Sala 1");
    }

    @Test
    @DisplayName("não deve salvar sala inválida")
    void doesNotSaveInvalidRoom() {
        assertThatThrownBy(() -> service.create(new CreateRoomCommand("", 1, 1)))
                .isInstanceOf(DomainException.class);

        assertThat(roomRepository.count()).isZero();
    }
}
