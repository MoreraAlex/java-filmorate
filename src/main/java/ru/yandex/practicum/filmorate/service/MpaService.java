package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {
    private final MpaRepository mpaRepository;

    public List<MpaDto> findAll() {
        return mpaRepository.findAll().stream()
                .map(MpaMapper::mapToMpaDto)
                .collect(Collectors.toList());
    }

    public MpaDto findById(Integer id) {
        return mpaRepository.findById(id)
                .map(MpaMapper::mapToMpaDto)
                .orElseThrow(() -> {
                    String message = "Mpa not found with id: " + id;
                    log.warn("findById: NotFoundException: {}", message);
                    return new NotFoundException(message);
                });
    }
}
