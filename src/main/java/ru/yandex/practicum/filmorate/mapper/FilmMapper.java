package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static UpdateFilmRequest mapToUpdateFilmRequest(Film film) {
        log.debug("mapToUpdateFilmRequest in Film: {}", film);
        UpdateFilmRequest request = new UpdateFilmRequest();
        request.setName(film.getName());
        request.setDescription(film.getDescription());
        request.setDuration(film.getDuration());
        request.setReleaseDate(film.getReleaseDate());
        log.debug("mapToUpdateFilmRequest return UpdateFilmRequest: {}", request);
        return request;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
        log.debug("updateFilmFields in Film: {}", film);
        log.debug("updateFilmFields in UpdateFilmRequest: {}", request);
        if (request.hasDescription()) {
            film.setDescription(request.getDescription());
        }
        if (request.hasName()) {
            film.setName(request.getName());
        }
        if (request.hasDuration()) {
            film.setDuration(request.getDuration());
        }
        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }
        log.debug("updateFilmFields return Film: {}", film);
        return film;
    }
}
