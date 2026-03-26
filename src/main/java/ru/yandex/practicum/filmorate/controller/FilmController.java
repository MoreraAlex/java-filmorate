package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate releaseDateOfFirstFilm = LocalDate.of(1895, 12, 28);

    private final Validator validator;

    public FilmController(Validator validator) {
        this.validator = validator;
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Get all films");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
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

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
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
                if (newFilm.getDuration() == 0) {
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

    private void validate(Film film) {

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
