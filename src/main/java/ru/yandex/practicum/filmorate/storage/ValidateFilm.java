package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ValidateFilm {

    private final Validator validator;
    private final LocalDate releaseDateOfFirstFilm = LocalDate.of(1895, 12, 28);

    public void validate(Film film) throws ValidationException {

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
