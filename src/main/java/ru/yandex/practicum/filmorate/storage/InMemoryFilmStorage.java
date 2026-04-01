package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate releaseDateOfFirstFilm = LocalDate.of(1895, 12, 28);
    private final Validator validator;
    private final UserStorage userStorage;

    @Override
    public Film addFilm(Film film) throws ValidationException {
        log.info("Creating film: {}", film);
        try {
            validate(film);
            film.setId(idGenerator.incrementAndGet());
            films.put(film.getId(), film);
            log.info("Film created: {}", film);
            return film;
        } catch (ValidationException exception) {
            log.error("Creating film: Validation exception: {}", exception.getMessage());
            throw exception;
        }
    }

    @Override
    public Film updateFilm(Film newFilm) throws ValidationException, NotFoundException {
        log.info("Updating film: {}", newFilm);
        if (newFilm.getId() == null) {
            String warning = "Id должен быть указан";
            log.error("Updating film: Validation exception: {}", warning);
            throw new ValidationException(warning);
        }
        if (films.containsKey(newFilm.getId())) {
            try {
                Film oldFilm = films.get(newFilm.getId());
                log.info("Film before update: {}", oldFilm);
                if (newFilm.getName() == null) {
                    newFilm.setName(oldFilm.getName());
                }
                if (newFilm.getReleaseDate() == null) {
                    newFilm.setReleaseDate(oldFilm.getReleaseDate());
                }
                if (newFilm.getDescription() == null) {
                    newFilm.setDescription(oldFilm.getDescription());
                }
                if (newFilm.getDuration() <= 0) {
                    newFilm.setDuration(oldFilm.getDuration());
                }
                validate(newFilm);

                films.put(newFilm.getId(), newFilm);
                log.info("Film updated: {}", newFilm);
                return newFilm;
            } catch (ValidationException exception) {
                log.error("Updating film: Validation exception: {}", exception.getMessage());
                throw exception;
            }
        }
        String warning = "Фильм с id = " + newFilm.getId() + " не найден";
        log.warn("Updating film: NotFoundException exception: {}", warning);
        throw new NotFoundException(warning);
    }

    @Override
    public Collection<Film> getAllFilms() {
        log.info("Get all films");
        return films.values();
    }

    @Override
    public Film getFilmById(Long id) throws NotFoundException {
        log.info("Get film by id: {}", id);
        return getFilmByIdOrThrow(id);
    }

    private Film getFilmByIdOrThrow(Long id) {
        return Optional.ofNullable(films.get(id))
                .orElseThrow(() -> {
                    String message = "Фильм с id = " + id + " не найден";
                    log.warn("getFilmByIdOrThrow: NotFoundException: {}", message);
                    return new NotFoundException(message);
                });
    }

    @Override
    public void addLike(Long id, Long userId) {
        log.info("Add like film {} by user: {}", id, userId);
        Film film = getFilmById(id);
        userStorage.getUserById(userId);

        film.getLikes().add(userId);
        log.info("Film like added: {}", film);
    }

    @Override
    public void removeLike(Long id, Long userId) {
        log.info("Remove like film {} by user: {}", id, userId);
        Film film = getFilmById(id);
        userStorage.getUserById(userId);

        film.getLikes().remove(userId);
        log.info("Removed film like: {}", film);
    }

    private void validate(Film film) throws ValidationException {

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        if (!violations.isEmpty()) {
            throw new ValidationException(violations.iterator().next().getMessage());
        }

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(releaseDateOfFirstFilm)) {
            throw new ValidationException(
                    "Год релиза должен быть после " +
                            releaseDateOfFirstFilm.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            );
        }
    }
}
