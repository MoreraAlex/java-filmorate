package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
@Primary
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    @Autowired
    private GenreDbStorage genreDbStorage;

    private static final String FILM_FIND_GENRES = """
            SELECT g.* FROM genres g
            LEFT JOIN film_genres f
            ON f.genre_id = g.id
            WHERE f.film_id=?
            """;

    private static final String INSERT_QUERY = """
            INSERT INTO films (name, description, releaseDate, duration, rating)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String FIND_ALL_QUERY = "SELECT * FROM films";

    private static final String UPDATE_QUERY = """
            UPDATE films SET name=?, description=?, releaseDate=?, duration=?, rating=?
            WHERE id=?
            """;

    private static final String FIND_BY_ID_QUERY = """
            SELECT f.*, r.rating AS ratingName FROM films f
            LEFT JOIN ratings r ON r.id = f.rating
            WHERE f.id = ?
            """;

    private static final String FIND_POPULAR_QUERY = """
            SELECT f.*, COUNT(fl.user_id) AS likes_count
            FROM films f
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id
            ORDER BY likes_count DESC
            LIMIT ?
            """;

    private static final String INSERT_LIKE_QUERY =
            "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";

    private static final String DELETE_LIKE_QUERY =
            "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private static final String INSERT_FILM_GENRE_QUERY =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

    @Autowired
    public FilmDbStorage(
            JdbcTemplate jdbc,
            RowMapper<Film> mapper
    ) {
        super(jdbc, mapper, Film.class);
    }

    @Override
    public Film addFilm(Film film) {
        log.info("Creating film: {}", film);

        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );

        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            var ids = film.getGenres().stream()
                    .map(Genre::getId).collect(Collectors.toSet());

            List<Object[]> batchArgs = ids.stream()
                    .map(g -> new Object[]{id, g})
                    .collect(Collectors.toList());

            jdbc.batchUpdate(INSERT_FILM_GENRE_QUERY, batchArgs);
        }

        log.info("Film added: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Updating film: {}", film);

        Film updatedFilm = findFilmById(film.getId());
        UpdateFilmRequest updateFilmRequest = FilmMapper.mapToUpdateFilmRequest(film);
        FilmMapper.updateFilmFields(updatedFilm, updateFilmRequest);

        update(
                UPDATE_QUERY,
                updatedFilm.getName(),
                updatedFilm.getDescription(),
                updatedFilm.getReleaseDate(),
                updatedFilm.getDuration(),
                updatedFilm.getMpa().getId(),
                updatedFilm.getId()
        );

        log.info("Film updated: {}", updatedFilm);
        return updatedFilm;
    }

    @Override
    public Collection<Film> getAllFilms() {
        log.info("Get all films");
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film findFilmById(Long id) {
        log.info("Find film by id: {}", id);

        Film film = findOne(FIND_BY_ID_QUERY, id).orElseThrow(() -> {
            String message = "Фильм с id = " + id + " не найден";
            log.warn("findFilmById: NotFoundException: {}", message);
            return new NotFoundException(message);
        });
        List<Genre> genres = genreDbStorage.findGenresByFilmId(id);
        film.setGenres(genres);
        return film;
    }

    @Override
    public void addLike(Long id, Long userId) {
        log.info("Add like film: {} by user {}", id, userId);
        insert(INSERT_LIKE_QUERY, id, userId);
    }

    @Override
    public void removeLike(Long id, Long userId) {
        log.info("Remove like film {} by user: {}", id, userId);
        delete(DELETE_LIKE_QUERY, id, userId);
    }

    @Override
    public Collection<Film> getPopular(int count) {
        log.info("Get popular films: {}", count);
        return findMany(FIND_POPULAR_QUERY, count);
    }
}
