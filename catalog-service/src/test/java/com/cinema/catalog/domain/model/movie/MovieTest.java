package com.cinema.catalog.domain.model.movie;

import com.cinema.catalog.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

class MovieTest {

    @Test
    @DisplayName("deve criar filme válido com ID gerado")
    void createsValidMovie() {
        Movie movie = Movie.create("Duna", Duration.ofMinutes(155));

        assertThat(movie.getId()).isNotNull();
        assertThat(movie.getTitle()).isEqualTo("Duna");
        assertThat(movie.getDuration()).isEqualTo(Duration.ofMinutes(155));
    }

    @Test
    @DisplayName("deve remover espaços extras do título")
    void stripsTitle() {
        Movie movie = Movie.create("  Duna  ", Duration.ofMinutes(155));

        assertThat(movie.getTitle()).isEqualTo("Duna");
    }

    @Test
    @DisplayName("deve rejeitar título em branco")
    void rejectsBlankTitle() {
        assertThatThrownBy(() -> Movie.create("   ", Duration.ofMinutes(155)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("título");
    }

    @Test
    @DisplayName("deve rejeitar duração zero ou negativa")
    void rejectsInvalidDuration() {
        assertThatThrownBy(() -> Movie.create("Duna", Duration.ZERO))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> Movie.create("Duna", Duration.ofMinutes(-10)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("filmes com o mesmo ID são iguais, mesmo com dados diferentes")
    void equalityById() {
        MovieId id = MovieId.generate();
        Movie a = Movie.restore(id, "Duna", Duration.ofMinutes(155));
        Movie b = Movie.restore(id, "Duna: Parte Dois", Duration.ofMinutes(166));

        assertThat(a).isEqualTo(b);
    }

    @Test
    @DisplayName("deve aceitar título com exatamente 200 caracteres")
    void acceptTitleAtMaxLength() {
        String title = "x".repeat(200);

        Movie movie = Movie.create(title, Duration.ofMinutes(120));

        assertThat(movie.getTitle()).hasSize(200);
    }

    @Test
    @DisplayName("deve rejeitar título com mais de 200 caracteres")
    void rejectsTitleAboveMaxLength() {
        String title = "x".repeat(201);

        assertThatThrownBy(() -> Movie.create(title, Duration.ofMinutes(120)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("200");
    }

    @Test
    @DisplayName("deve ignorar espaços nas pontas ao validar o tamanho do título")
    void acceptsMaxLengthTitleWithSurroundingSpaces() {
        String title = "  " + "x".repeat(200) + "  ";

        Movie movie = Movie.create(title, Duration.ofMinutes(120));

        assertThat(movie.getTitle()).hasSize(200);
    }
}
