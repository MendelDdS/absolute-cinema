package com.cinema.catalog.application.service.query;

import com.cinema.catalog.application.exception.RoomNotFoundException;
import com.cinema.catalog.application.fake.InMemoryRoomRepository;
import com.cinema.catalog.application.query.RoomView;
import com.cinema.catalog.domain.model.room.Room;
import com.cinema.catalog.domain.model.room.RoomId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomQueryServiceTest {

    private final InMemoryRoomRepository rooms = new InMemoryRoomRepository();
    private final RoomQueryService service = new RoomQueryService(rooms);

    @Test
    @DisplayName("deve devolver a sala convertida para view")
    void getsRoomAsView() {
        Room room = Room.withGrid("Sala 1", 2, 3);
        rooms.save(room);

        RoomView view = service.get(room.getId());

        assertThat(view.id()).isEqualTo(room.getId().value());
        assertThat(view.name()).isEqualTo("Sala 1");
        assertThat(view.capacity()).isEqualTo(6);
    }

    @Test
    @DisplayName("deve listar todas as salas salvas")
    void listsAllRooms() {
        Room room = Room.withGrid("Sala 1", 1, 1);
        Room room2 = Room.withGrid("Sala 2", 1, 1);

        rooms.save(room);
        rooms.save(room2);

        List<RoomView> allRooms = service.list();

        assertThat(allRooms).hasSize(2);
    }

    @Test
    @DisplayName("deve falhar quando não encontrar a sala")
    void failsWhenRoomNotFound() {
        assertThatThrownBy(() -> service.get(RoomId.generate())).isInstanceOf(RoomNotFoundException.class);
    }
}
