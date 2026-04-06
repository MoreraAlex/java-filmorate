package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MpaMapper {

    public static MpaDto mapToMpaDto(Mpa mpa) {
        log.debug("mapToMpaDto in Mpa: {}", mpa);
        MpaDto mpaDto = new MpaDto();
        mpaDto.setId(mpa.getId());
        mpaDto.setName(mpa.getName());
        log.debug("mapToMpaDto return MpaDto: {}", mpaDto);
        return mpaDto;
    }
}
