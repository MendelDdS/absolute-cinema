package com.cinema.catalog.application.service;

import com.cinema.catalog.application.fake.InMemoryMovieRepository;
import com.cinema.catalog.application.port.in.CreateMovieCommand;
import com.cinema.catalog.domain.exception.DomainException;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.movie.MovieId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class CreateMovieServiceTest {

    private final InMemoryMovieRepository movieRepository = new InMemoryMovieRepository();
    private final CreateMovieService service = new CreateMovieService(movieRepository);

    @Test
    @DisplayName("deve salvar o filme e retornar o ID")
    void createsMovie() {
        MovieId id = service.create(new CreateMovieCommand("Duna", 155));

        assertThat(movieRepository.findById(id)).isPresent()
                .get().extracting(Movie::getTitle).isEqualTo("Duna");
    }

    @Test
    @DisplayName("não deve salvar filme inválido")
    void doesNotSaveInvalidMovie() {
        assertThatThrownBy(() -> service.create(new CreateMovieCommand("", 155)))
                .isInstanceOf(DomainException.class);

        assertThat(movieRepository.count()).isZero();
    }
}
