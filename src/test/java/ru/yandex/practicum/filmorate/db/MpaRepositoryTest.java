package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaRepository.class, MpaRowMapper.class})
class MpaRepositoryTest {

    private final MpaRepository mpaRepository;

    @Test
    void shouldFindMpaById() {
        Optional<Mpa> mpa = mpaRepository.findById(1);

        assertThat(mpa)
                .isPresent()
                .hasValueSatisfying(m ->
                        assertThat(m).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    void shouldFindAllMpa() {
        List<Mpa> mpaList = mpaRepository.findAll();

        assertThat(mpaList)
                .isNotEmpty()
                .allMatch(m -> m.getId() > 0);
    }
}