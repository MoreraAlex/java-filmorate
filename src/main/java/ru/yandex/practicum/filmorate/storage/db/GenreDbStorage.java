package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@Slf4j
public class GenreDbStorage extends BaseRepository<Genre> {

    private static final String FILM_FIND_GENRES = """
            SELECT g.* FROM genres g
            LEFT JOIN film_genres f
            ON f.genre_id = g.id
            WHERE f.film_id=?
            """;

    @Autowired
    public GenreDbStorage(
            JdbcTemplate jdbc,
            RowMapper<Genre> mapper
    ) {
        super(jdbc, mapper, Genre.class);
    }

    public List<Genre> findGenresByFilmId(Long id) {
        log.info("Find film by id: {}", id);
        return findMany(FILM_FIND_GENRES, id);
    }

}
