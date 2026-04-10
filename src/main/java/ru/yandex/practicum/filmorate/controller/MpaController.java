package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public ResponseEntity<List<MpaDto>> getAll() {
        return ResponseEntity.ok(mpaService.findAll());
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<MpaDto> getMpaById(@PathVariable Integer id) {
        return ResponseEntity.ok(mpaService.findById(id));
    }
}
