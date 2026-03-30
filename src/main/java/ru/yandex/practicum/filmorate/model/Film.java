package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.RatingMPA;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
public class Film {
    private final int maxLengthDescription = 200;
    private Long id;
    private Set<Long> likes = new HashSet<>();
    private Set<Long> genres = new HashSet<>();
    private RatingMPA ratingMPA;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = maxLengthDescription, message = "Описание не должно превышать " + maxLengthDescription + " символов")
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;
}
