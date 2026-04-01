package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.TypeFriendship;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
@RequiredArgsConstructor
public class InMemoryUserStorage implements UserStorage {

    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<Long, User> users = new HashMap<>();

    private final Validator validator;

    @Override
    public User addUser(User user) throws ValidationException {
        log.info("Creating user: {}", user);
        try {
            validate(user);
            user.setId(idGenerator.incrementAndGet());
            users.put(user.getId(), user);
            log.info("User created: {}", user);
            return user;
        } catch (ValidationException exception) {
            log.error("Creating user: Validation exception: {}", exception.getMessage());
            throw exception;
        }
    }

    @Override
    public User updateUser(User newUser) throws ValidationException, NotFoundException {
        log.info("Updating user: {}", newUser);
        if (newUser.getId() == null) {
            String warning = "Id должен быть указан";
            log.error("Updating user: Validation exception: {}", warning);
            throw new ValidationException(warning);
        }
        if (users.containsKey(newUser.getId())) {
            try {
                User oldUser = users.get(newUser.getId());
                log.info("User before update: {}", oldUser);
                if (newUser.getLogin() == null) {
                    newUser.setLogin(oldUser.getLogin());
                }
                if (newUser.getName() == null) {
                    if (oldUser.getName() == null || oldUser.getName().equals(oldUser.getLogin())) {
                        newUser.setName(newUser.getLogin());
                    } else {
                        newUser.setName(oldUser.getName());
                    }
                }
                if (newUser.getEmail() == null) {
                    newUser.setEmail(oldUser.getEmail());
                }
                if (newUser.getBirthday() == null) {
                    newUser.setBirthday(oldUser.getBirthday());
                }
                validate(newUser);

                users.put(newUser.getId(), newUser);
                log.info("User updated: {}", newUser);
                return newUser;
            } catch (ValidationException exception) {
                log.error("Updating user: Validation exception: {}", exception.getMessage());
                throw exception;
            }
        }
        String warning = "Пользователь с id = " + newUser.getId() + " не найден";
        log.warn("Updating user: NotFoundException exception: {}", warning);
        throw new NotFoundException(warning);
    }

    @Override
    public Collection<User> getAllUsers() {
        log.info("Get all users");
        return users.values();
    }

    @Override
    public User getUserById(Long id) throws NotFoundException {
        log.info("Get user by id: {}", id);
        return getUserByIdOrThrow(id);
    }

    @Override
    public void addFriend(Long id, Long friendId) throws NotFoundException, ValidationException {
        if (id.equals(friendId)) {
            String warning = "id друга не может совпадать с id пользователя";
            log.error("Add friend: Validation exception: {}", warning);
            throw new ValidationException(warning);
        }

        User user = getUserById(id);
        User friend = getUserById(friendId);

        user.getFriends().put(friendId, TypeFriendship.NOT_CONFIRMED);
        log.info("Added user: {} friend: {}", user, friend);
        friend.getFriends().put(id, TypeFriendship.NOT_CONFIRMED);
        log.info("Added user: {} friend: {}", friend, user);
    }

    @Override
    public void removeFriend(Long id, Long friendId) throws NotFoundException, ValidationException {
        if (id.equals(friendId)) {
            String warning = "id друга не может совпадать с id пользователя";
            log.error("Remove friend: Validation exception: {}", warning);
            throw new ValidationException(warning);
        }

        User user = getUserById(id);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        log.info("Removed user: {} friend: {}", user, friend);
        friend.getFriends().remove(id);
        log.info("Removed user: {} friend: {}", friend, user);
    }

    private User getUserByIdOrThrow(Long id) {
        return Optional.ofNullable(users.get(id))
                .orElseThrow(() -> {
                    String message = "Пользователь с id = " + id + " не найден";
                    log.warn("getUserByIdOrThrow: NotFoundException: {}", message);
                    return new NotFoundException(message);
                });
    }

    public void validate(User user) throws ValidationException {

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new ValidationException(violations.iterator().next().getMessage());
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
