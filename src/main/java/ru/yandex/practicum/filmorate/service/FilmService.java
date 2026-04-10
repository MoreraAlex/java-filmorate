package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.ValidateFilm;

import java.util.Collection;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final ValidateFilm validateFilm;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, GenreService genreService, MpaService mpaService, ValidateFilm validateFilm) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
        this.validateFilm = validateFilm;
    }

    public Collection<Film> getAll() {
        return filmStorage.getAllFilms();
    }

    public Film create(Film film) throws ValidationException {
        log.info("Create film: {}", film);

        validateFilm.validate(film);

        mpaService.findById(film.getMpa().getId());

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreService.findById(genre.getId());
            }
        }

        return filmStorage.addFilm(film);
    }

    public Film update(Film film) throws ValidationException, NotFoundException {
        log.info("Update film: {}", film);

        filmStorage.findFilmById(film.getId());

        validateFilm.validate(film);

        mpaService.findById(film.getMpa().getId());

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreService.findById(genre.getId());
            }
        }

        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(Long id) throws NotFoundException {
        return filmStorage.findFilmById(id);
    }

    public void addLike(Long id, Long userId) {
        log.info("Add like: film {}, user {}", id, userId);

        filmStorage.findFilmById(id);
        userStorage.findUserById(userId);

        filmStorage.addLike(id, userId);
    }

    public void removeLike(Long id, Long userId) {
        log.info("Remove like: film {}, user {}", id, userId);

        filmStorage.findFilmById(id);
        userStorage.findUserById(userId);

        filmStorage.removeLike(id, userId);
    }

    public Collection<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }
}
