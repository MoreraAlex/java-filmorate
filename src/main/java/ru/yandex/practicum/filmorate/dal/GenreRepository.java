package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class GenreRepository extends BaseRepository<Genre> {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_FILM_GENRE_QUERY = """
            SELECT g.*
            FROM genres g
            JOIN film_genres fg ON g.id = fg.genre_id
            WHERE fg.film_id = ?
            ORDER BY g.id
            """;

    @Autowired
    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper, Genre.class);
    }

    public List<Genre> findAll() {
        log.info("Find all genres");
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> findById(Long id) {
        log.info("Find genre by id {}", id);
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public List<Genre> findFilmGenresByFilmId(Long filmId) {
        log.info("Find film genres by film id {}", filmId);
        return findMany(FIND_FILM_GENRE_QUERY, filmId);
    }
}
