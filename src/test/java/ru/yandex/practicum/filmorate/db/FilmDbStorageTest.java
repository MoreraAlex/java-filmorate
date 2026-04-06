package ru.yandex.practicum.filmorate.db;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.ValidateFilm;
import ru.yandex.practicum.filmorate.storage.ValidateUser;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({
        FilmDbStorage.class,
        FilmRowMapper.class,
        UserDbStorage.class,
        ValidateFilm.class,
        GenreService.class,
        GenreRowMapper.class,
        GenreRepository.class,
        MpaService.class,
        FilmDbStorageTest.TestConfig.class,
        UserRowMapper.class,
        ValidateUser.class,
        MpaRepository.class,
        MpaRowMapper.class
})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbc;
    private static final String FIND_FILM_LIKE_QUERY = """
            SELECT l.user_id
            FROM film_likes l
            WHERE l.film_id = ?
            """;

    @Autowired
    public FilmDbStorageTest(
            FilmDbStorage filmStorage,
            @Qualifier("userDbStorage") UserDbStorage userStorage,
            JdbcTemplate jdbc
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbc = jdbc;
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        return film;
    }

    private Long createUser() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.addUser(user).getId();
    }

    private List<Long> getFilmLikes(Object... params) {
        return jdbc.query(
                FIND_FILM_LIKE_QUERY,
                (rs, rowNum) -> rs.getLong("user_id"),
                params
        );
    }


    @Test
    void shouldAddFilm() {
        Film film = createFilm("Film 1");

        Film saved = filmStorage.addFilm(film);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Film 1");
    }


    @Test
    void shouldFindFilmById() {
        Film film = filmStorage.addFilm(createFilm("Film 1"));

        Film found = filmStorage.findFilmById(film.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(film.getId());
    }


    @Test
    void shouldUpdateFilm() {
        Film film = filmStorage.addFilm(createFilm("Film 1"));

        film.setName("Updated");

        Film updated = filmStorage.updateFilm(film);

        assertThat(updated.getName()).isEqualTo("Updated");
    }


    @Test
    void shouldGetAllFilms() {
        filmStorage.addFilm(createFilm("Film 1"));
        filmStorage.addFilm(createFilm("Film 2"));

        Collection<Film> films = filmStorage.getAllFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    void shouldAddLike() {
        Film film = filmStorage.addFilm(createFilm("Film 1"));
        Long userId = createUser();
        filmStorage.addLike(film.getId(), userId);

        List<Long> likes = getFilmLikes(film.getId());

        assertThat(likes).contains(userId);
    }


    @Test
    void shouldRemoveLike() {
        Film film = filmStorage.addFilm(createFilm("Film 1"));
        Long userId = createUser();

        filmStorage.addLike(film.getId(), userId);
        filmStorage.removeLike(film.getId(), userId);

        List<Long> likes = getFilmLikes(film.getId());

        assertThat(likes).doesNotContain(userId);
    }


    @Test
    void shouldReturnPopularFilms() {
        Film film1 = filmStorage.addFilm(createFilm("Film 1"));
        Film film2 = filmStorage.addFilm(createFilm("Film 2"));

        Long user1 = createUser();
        Long user2 = createUser();

        filmStorage.addLike(film1.getId(), user1);
        filmStorage.addLike(film1.getId(), user2);

        filmStorage.addLike(film2.getId(), user1);

        List<Film> popular = (List<Film>) filmStorage.getPopular(2);

        assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
        assertThat(popular.get(1).getId()).isEqualTo(film2.getId());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Qualifier("userDbStorage")  // ← это важно!
        public UserStorage userDbStorage(JdbcTemplate jdbc, UserRowMapper userRowMapper, ValidateUser validateUser) {
            return new UserDbStorage(jdbc, userRowMapper, validateUser);
        }

        @Bean
        public Validator validator() {
            return Validation.buildDefaultValidatorFactory().getValidator();
        }

    }
}
