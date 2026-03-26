package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<Long, User> users = new HashMap<>();

    private final Validator validator;

    public UserController(Validator validator) {
        this.validator = validator;
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("Get all users");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) throws ValidationException {
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

    @PutMapping
    public User update(@Valid @RequestBody User newUser) throws ValidationException, NotFoundException {
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
                    if (oldUser.getName().equals(oldUser.getLogin())) {
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