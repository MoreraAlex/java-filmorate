package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.ValidateFilm;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private final ValidateFilm validateFilm;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaService mpaService;

    private static final String INSERT_QUERY = """
            INSERT INTO films (name, description, releaseDate, duration, rating)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String UPDATE_QUERY = """
            UPDATE films SET name=?, description=?, releaseDate=?, duration=?, rating=?
            WHERE id=?
            """;
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String FIND_POPULAR_QUERY = """
            SELECT f.*, COUNT(fl.user_id) AS likes_count
            FROM films f
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id
            ORDER BY likes_count DESC
            LIMIT ?
            """;
    private static final String INSERT_LIKE_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

    @Autowired
    public FilmDbStorage(
            JdbcTemplate jdbc,
            RowMapper<Film> mapper,
            @Qualifier("userDbStorage") UserStorage userStorage,
            ValidateFilm validateFilm,
            GenreService genreService,
            MpaService mpaService) {
        super(jdbc, mapper, Film.class);
        this.userStorage = userStorage;
        this.validateFilm = validateFilm;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    @Override
    public Film addFilm(Film film) {
        log.info("Creating film: {}", film);
        validateFilm.validate(film);
        mpaService.findById(film.getMpa().getId());
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        film.getGenres().forEach(genre -> {
            genreService.findById(genre.getId());
            insert(INSERT_FILM_GENRE_QUERY, id, genre.getId());
        });
        setGenresToFilm(film);
        setNameMpa(film);
        log.info("Film added: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Updating film: {}", film);
        Film updatedFilm = findFilmById(film.getId());
        UpdateFilmRequest updateFilmRequest = FilmMapper.mapToUpdateFilmRequest(film);
        FilmMapper.updateFilmFields(updatedFilm, updateFilmRequest);
        validateFilm.validate(updatedFilm);
        update(
                UPDATE_QUERY,
                updatedFilm.getName(),
                updatedFilm.getDescription(),
                updatedFilm.getReleaseDate(),
                updatedFilm.getDuration(),
                updatedFilm.getMpa().getId(),
                updatedFilm.getId()
        );
        setGenresToFilm(updatedFilm);
        setNameMpa(updatedFilm);
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
        Film film = findOne(FIND_BY_ID_QUERY, id).orElseThrow(
                () -> {
                    String message = "Фильм с id = " + id + " не найден";
                    log.warn("findFilmById: NotFoundException: {}", message);
                    return new NotFoundException(message);
                });
        setGenresToFilm(film);
        setNameMpa(film);
        log.info("Film found: {}", film);
        return film;
    }

    private void setGenresToFilm(Film film) {
        log.info("Set genres to film: {}", film);
        List<GenreDto> genreDtoList = genreService.findGenresByFilmId(film.getId());
        List<Genre> genreList = genreDtoList.stream()
                .map(GenreMapper::mapToGenre)
                .collect(Collectors.toList());
        film.setGenres(genreList);
        log.info("Genres founded: {}", genreList);
    }

    private void setNameMpa(Film film) {
        log.info("Set name pa: {}", film);
        Mpa mpa = film.getMpa();
        String nameMpa = mpaService.findById(mpa.getId()).getName();
        mpa.setName(nameMpa);
        log.info("Mpa name: {}", nameMpa);
    }

    @Override
    public void addLike(Long id, Long userId) {
        log.info("Add like film: {} by user {}", id, userId);
        Film film = findFilmById(id);
        userStorage.findUserById(userId);
        insert(INSERT_LIKE_QUERY, id, userId);
        log.info("Film like added: {}", film);
    }

    @Override
    public void removeLike(Long id, Long userId) {
        log.info("Remove like film {} by user: {}", id, userId);
        Film film = findFilmById(id);
        userStorage.findUserById(userId);
        delete(DELETE_LIKE_QUERY, id, userId);
        log.info("Removed film like: {}", film);
    }

    @Override
    public Collection<Film> getPopular(int count) {
        log.info("Get popular films: {}", count);
        return findMany(FIND_POPULAR_QUERY, count);
    }
}
