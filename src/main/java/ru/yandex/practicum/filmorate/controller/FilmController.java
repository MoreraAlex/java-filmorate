package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.Collection;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> getAll() {
        return ResponseEntity.ok(filmService.getAll());
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<Film> getFilmById(@PathVariable Long id) {
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getPopular(
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(filmService.getPopular(count));
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(filmService.create(film));
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film newFilm) {
        return ResponseEntity.ok(filmService.update(newFilm));
    }


    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id,
                           @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

}
