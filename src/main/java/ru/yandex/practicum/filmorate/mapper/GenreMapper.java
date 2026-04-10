package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenreMapper {

    public static GenreDto mapToGenreDto(Genre genre) {
        log.debug("mapToGenreDto in Genre: {}", genre);
        GenreDto genreDto = new GenreDto();
        genreDto.setId(genre.getId());
        genreDto.setName(genre.getName());
        log.debug("mapToGenreDto return GenreDto: {}", genreDto);
        return genreDto;
    }

    public static Genre mapToGenre(GenreDto genreDto) {
        log.debug("mapToGenre in GenreDto: {}", genreDto);
        Genre genre = new Genre();
        genre.setId(genreDto.getId());
        genre.setName(genreDto.getName());
        log.debug("mapToGenre return Genre: {}", genre);
        return genre;
    }

}
