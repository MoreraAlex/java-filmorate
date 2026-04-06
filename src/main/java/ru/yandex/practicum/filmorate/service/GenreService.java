package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<GenreDto> findAll() {
        return genreRepository.findAll().stream()
                .map(GenreMapper::mapToGenreDto)
                .collect(Collectors.toList());
    }

    public GenreDto findById(Long id) {
        return genreRepository.findById(id)
                .map(GenreMapper::mapToGenreDto)
                .orElseThrow(() ->  {
                    String message = "Genre not found with id: " + id;
                    log.warn("findById: NotFoundException: {}", message);
                    return new NotFoundException(message);
                });
    }

    public List<GenreDto> findGenresByFilmId(Long filmId) {
        return genreRepository.findFilmGenresByFilmId(filmId).stream()
                .map(GenreMapper::mapToGenreDto)
                .collect(Collectors.toList());
    }
}
