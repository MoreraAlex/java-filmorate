package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;


@Data
public class Film {
    private final int maxLengthDescription = 200;
    private Long id;
    private Set<Long> likes = new HashSet<>();
    private List<Genre> genres = new ArrayList<>();
    private Mpa mpa;
    private final LocalDate releaseDateOfFirstFilm = LocalDate.of(1895, 12, 28);


    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = maxLengthDescription, message = "Описание не должно превышать " + maxLengthDescription + " символов")
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

    public static Film randomGeneratedFilm() {
        final LocalDate releaseDateOfFirstFilm = LocalDate.of(1895, 12, 28);

        Mpa mpa = Mpa.builder()
                .id(3)
                .name("PG-13")
                .build();

        Film film = new Film();
        film.setName("Название фильма " + RandomGenerator.getDefault().nextInt(0, 1000));
        film.setReleaseDate(releaseDateOfFirstFilm);
        film.setDuration(120);
        film.setMpa(mpa);

        List<Genre> genres = List.of(
                new Genre(1L, "Комедия"),
                new Genre(2L, "Драма"),
                new Genre(3L, "Мультфильм")
        );
        film.setGenres(genres.stream().distinct().collect(Collectors.toList()));

        return film;
    }

}
