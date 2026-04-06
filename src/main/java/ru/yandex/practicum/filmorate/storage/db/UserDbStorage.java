package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.dto.FriendDto;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.enums.TypeFriendship;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.ValidateUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private final ValidateUser validateUser;

    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_FRIENDS_QUERY = """
            SELECT u.*
            FROM users u
            JOIN friends f ON u.id = f.user2_id
            WHERE f.user1_id = ?
            """;
    private static final String FIND_FRIENDS_TYPE_QUERY = """
            SELECT f.user2_id,
            tf.type
            FROM friends f
            JOIN types_friendship tf ON f.type_friendship_id = tf.id
            WHERE f.user1_id = ?
            """;
    private static final String INSERT_FRIENDS_QUERY = """
            INSERT INTO friends (user1_id, user2_id, type_friendship_id)
            VALUES (?, ?, ?)
            """;
    private static final String UPDATE_TYPE_FRIENDSHIP_QUERY = """
            UPDATE friends SET type_friendship_id = ?
            WHERE user1_id = ? AND user2_id = ?
            """;
    private static final String UPDATE_QUERY = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";
    private static final String DELETE_FRIENDS_QUERY = "DELETE FROM friends WHERE user1_id = ? AND user2_id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY = """
            SELECT u.*
            FROM users u
            JOIN friends f1 ON u.id = f1.user2_id
            JOIN friends f2 ON u.id = f2.user2_id
            WHERE f1.user1_id = ? AND f2.user1_id = ?
            """;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper, ValidateUser validateUser) {
        super(jdbc, mapper, User.class);
        this.validateUser = validateUser;
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        User user1 = findUserById(id);
        User user2 = findUserById(friendId);
        int typeFriendshipId = 2;
        Collection<User> user2Friends = getUserFriends(friendId);
        if (user2Friends.contains(user1)) {
            typeFriendshipId = 1;
            update(UPDATE_TYPE_FRIENDSHIP_QUERY, typeFriendshipId, friendId, id);
        }
        insert(INSERT_FRIENDS_QUERY, id, friendId, typeFriendshipId);
    }

    @Override
    public User addUser(User user) {
        log.info("Creating user: {}", user);
        try {
            validateUser.validate(user);
        } catch (ValidationException exception) {
            log.error("Creating user: Validation exception: {}", exception.getMessage());
            throw exception;
        }
        long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        user.setId(id);
        log.info("User created: {}", user);
        return user;
    }

    @Override
    public User findUserById(Long id) {
        log.info("Finding user by id {}", id);
        User user = findOne(FIND_BY_ID_QUERY, id).orElseThrow(
                () -> {
                    String message = "Пользователь с id = " + id + " не найден";
                    log.warn("findUserById: NotFoundException: {}", message);
                    return new NotFoundException(message);
                });
        setFriendsToUser(user);
        log.info("User found: {}", user);
        return user;
    }

    private void setFriendsToUser(User user) {
        log.info("Set friends to user: {}", user);
        List<FriendDto> friendsDto = jdbc.query(
                FIND_FRIENDS_TYPE_QUERY,
                (rs, rowNum) -> new FriendDto(
                        rs.getLong("user2_id"),
                        TypeFriendship.valueOf(rs.getString("type"))
                ),
                user.getId()
        );
        Map<Long, TypeFriendship> friendsMap = friendsDto.stream()
                .collect(Collectors.toMap(
                        FriendDto::getUserId,
                        FriendDto::getType
                ));
        user.setFriends(friendsMap);
        log.info("User friends set: {}", friendsMap);
    }

    @Override
    public Collection<User> getAllUsers() {
        log.info("Get all users");
        List<User> users = findMany(FIND_ALL_QUERY);
        return users;
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Get common friends for user {} and other {}", userId, otherId);
        findUserById(userId);
        findUserById(otherId);
        return findMany(FIND_COMMON_FRIENDS_QUERY, userId, otherId).stream()
                .map(user -> {
                    setFriendsToUser(user);
                    return user;
                })
                .toList();
    }

    @Override
    public void removeFriend(Long id, Long friendId) {
        log.info("Remove user {} from friends {}", id, friendId);
        User user1 = findUserById(id);
        User user2 = findUserById(friendId);
        delete(DELETE_FRIENDS_QUERY, id, friendId);
        Collection<User> user2Friends = getUserFriends(friendId);
        if (user2Friends.contains(user1)) {
            update(UPDATE_TYPE_FRIENDSHIP_QUERY, 2, friendId, id);
        }
    }

    @Override
    public Collection<User> getUserFriends(Long userId) {
        log.info("Get user friends for user {}", userId);
        findUserById(userId);
        return findMany(FIND_FRIENDS_QUERY, userId).stream()
                .map(user -> {
                    setFriendsToUser(user);
                    return user;
                })
                .toList();
    }

    @Override
    public User updateUser(User user) {
        log.info("Updating user: {}", user);
        User updatedUser = findUserById(user.getId());
        log.info("User before update: {}", updatedUser);
        UpdateUserRequest updateUserRequest = UserMapper.mapToUpdateUserRequest(user);
        UserMapper.updateUserFields(updatedUser, updateUserRequest);
        try {
            validateUser.validate(updatedUser);
        } catch (ValidationException exception) {
            log.error("Updating user: Validation exception: {}", exception.getMessage());
            throw exception;
        }
        update(
                UPDATE_QUERY,
                updatedUser.getEmail(),
                updatedUser.getLogin(),
                updatedUser.getName(),
                updatedUser.getBirthday(),
                updatedUser.getId()
        );
        log.info("User updated: {}", updatedUser);
        return updatedUser;
    }
}
