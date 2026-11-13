package com.cinema.catalog.application.service.query;

import com.cinema.catalog.application.exception.MovieNotFoundException;
import com.cinema.catalog.application.fake.InMemoryMovieRepository;
import com.cinema.catalog.application.query.MovieView;
import com.cinema.catalog.domain.model.movie.Movie;
import com.cinema.catalog.domain.model.movie.MovieId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MovieQueryServiceTest {

    private final InMemoryMovieRepository movies = new InMemoryMovieRepository();
    private final MovieQueryService service = new MovieQueryService(movies);

    @Test
    @DisplayName("deve devolver o filme convertido para view")
    void getsMovieAsView() {
        Movie movie = Movie.create("Duna", Duration.ofMinutes(155));
        movies.save(movie);

        MovieView view = service.get(movie.getId());

        assertThat(view.id()).isEqualTo(movie.getId().value());
        assertThat(view.title()).isEqualTo("Duna");
        assertThat(view.durationMinutes()).isEqualTo(155);
    }

    @Test
    @DisplayName("deve listar todos os filmes salvos")
    void listsAllMovies() {
        Movie movie = Movie.create("Duna", Duration.ofMinutes(155));
        Movie movie2 = Movie.create("Avengers", Duration.ofMinutes(135));

        movies.save(movie);
        movies.save(movie2);

        List<MovieView> allMovies = service.list();

        assertThat(allMovies).hasSize(2);
    }

    @Test
    @DisplayName("deve falhar quando não encontrar o filme")
    void failsWhenMovieNotFound() {
        assertThatThrownBy(() -> service.get(MovieId.generate())).isInstanceOf(MovieNotFoundException.class);
    }
}
