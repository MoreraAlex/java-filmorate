package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class MpaRepository extends BaseRepository<Mpa> {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM ratings WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM ratings";

    @Autowired
    public MpaRepository(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper, Mpa.class);
    }

    public List<Mpa> findAll() {
        log.info("Find all ratings");
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Mpa> findById(Integer id) {
        log.info("Find rating by id {}", id);
        return findOne(FIND_BY_ID_QUERY, id);
    }
}
