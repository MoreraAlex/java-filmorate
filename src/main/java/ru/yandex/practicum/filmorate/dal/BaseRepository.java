package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;
    private final Class<T> entityType;

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Object... params) {
        log.debug("Find many query {} with paras {}", query, params);
        return jdbc.query(query, mapper, params);
    }

    public boolean delete(String query, Object... params) {
        log.debug("Delete query {} with paras {}", query, params);
        int rowsDeleted = jdbc.update(query, params);
        return rowsDeleted > 0;
    }

    protected long insert(String query, Object... params) {
        log.debug("Insert query {} with paras {}", query, params);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                for (int idx = 0; idx < params.length; idx++) {
                    ps.setObject(idx + 1, params[idx]);
                }
                return ps;
            }, keyHolder);

            if (keyHolder.getKeys().size() > 1) {
                return (long) 1;
            }
            Number id = keyHolder.getKey();
            if (id != null) {
                return id.longValue();
            } else {
                throw new InternalServerException("Не удалось сохранить данные");
            }
        } catch (DuplicateKeyException e) {
            log.info("Duplicate key found for query: {}", query);
            return -1;
        }
    }

    protected void update(String query, Object... params) {
        log.debug("Update query {} with paras {}", query, params);
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }
}
