package com.cinema.catalog.domain.model.room;

import com.cinema.catalog.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RoomTest {

    @Test
    @DisplayName("deve gerar grade de assentos com fileiras A, B, C...")
    void createsGrid() {
        Room room = Room.withGrid("Sala 1", 3, 4);

        assertThat(room.capacity()).isEqualTo(12);
        assertThat(room.getSeats())
                .extracting(Seat::code)
                .containsExactly("A1", "A2", "A3", "A4",
                        "B1", "B2", "B3", "B4",
                        "C1", "C2", "C3", "C4");
    }

    @Test
    @DisplayName("deve rejeitar sala sem assentos")
    void rejectsEmptyRoom() {
        assertThatThrownBy(() -> Room.create("Sala 1", List.of()))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("deve rejeitar assentos repetidos")
    void rejectsDuplicateSeats() {
        List<Seat> seats = List.of(new Seat("A", 1), new Seat("A", 1));

        assertThatThrownBy(() -> Room.create("Sala 1", seats))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("repetidos");
    }

    @Test
    @DisplayName("deve rejeitar mais de 26 fileiras")
    void rejectsTooManyRows() {
        assertThatThrownBy(() -> Room.withGrid("Sala 1", 27, 10))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("deve rejeitar fileira inválida no assento")
    void rejectsInvalidSeatRow() {
        assertThatThrownBy(() -> new Seat("a", 1)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Seat("AA", 1)).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("a lista de assentos não pode ser alterada por fora")
    void seatsAreImmutable() {
        Room room = Room.withGrid("Sala 1", 1, 1);

        assertThatThrownBy(() -> room.getSeats().add(new Seat("Z", 99)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}