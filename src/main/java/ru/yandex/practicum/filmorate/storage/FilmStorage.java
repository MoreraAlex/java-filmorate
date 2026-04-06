package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getAllFilms();

    Film findFilmById(Long id);

    void addLike(Long id, Long userId);

    void removeLike(Long id, Long userId);

    Collection<Film> getPopular(int count);
}
