package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreRepository.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreRepositoryTest {

    private final GenreRepository genreRepository;

    @Test
    void shouldFindGenreById() {
        Optional<Genre> genre = genreRepository.findById((long) 1);

        assertThat(genre)
                .isPresent()
                .hasValueSatisfying(g ->
                        assertThat(g).hasFieldOrPropertyWithValue("id", (long) 1)
                );
    }

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreRepository.findAll();

        assertThat(genres)
                .isNotEmpty()
                .allMatch(g -> g.getId() > 0);
    }
}
