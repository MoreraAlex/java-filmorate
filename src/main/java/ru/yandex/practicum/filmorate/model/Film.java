package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;


@Data
public class Film {
    private final int maxLengthDescription = 200;
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = maxLengthDescription, message = "Описание не должно превышать " + maxLengthDescription + " символов")
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;
}
