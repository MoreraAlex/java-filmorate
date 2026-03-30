package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    public Collection<Film> getAll() {
        return filmStorage.getAllFilms();
    }

    public Film create(Film film) throws ValidationException {
        return filmStorage.addFilm(film);
    }

    public Film update(Film newFilm) throws ValidationException, NotFoundException {
        return filmStorage.updateFilm(newFilm);
    }

    public Film getFilmById(Long id) throws NotFoundException {
        return filmStorage.getFilmById(id);
    }

    public void addLike(Long id, Long userId) {
        filmStorage.addLike(id, userId);
    }

    public void removeLike(Long id, Long userId) {
        filmStorage.removeLike(id, userId);
    }

    public Collection<Film> getPopular(int count) {
        log.info("Get popular films by count: {}", count);
        Comparator<Film> comparingByLikes = Comparator.comparingInt((Film f) -> f.getLikes().size());
        comparingByLikes = comparingByLikes.reversed();

        return filmStorage.getAllFilms().stream()
                .sorted(comparingByLikes)
                .limit(count)
                .toList();
    }
}
