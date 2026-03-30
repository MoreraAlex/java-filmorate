package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public Collection<User> getAll() {
        return userStorage.getAllUsers();
    }

    public User create(User user) throws ValidationException {
        return userStorage.addUser(user);
    }

    public User update(User newUser) throws ValidationException, NotFoundException {
        return userStorage.updateUser(newUser);
    }

    public User getUserById(Long id) throws NotFoundException {
        return userStorage.getUserById(id);
    }

    public void addFriend(Long id, Long friendId) throws NotFoundException, ValidationException {
        userStorage.addFriend(id, friendId);
    }

    public void removeFriend(Long id, Long friendId) throws NotFoundException, ValidationException {
        userStorage.removeFriend(id, friendId);
    }

    public Collection<User> getUserFriends(Long id) throws NotFoundException {
        User user = getUserById(id);
        return user.getFriends().keySet().stream()
                .map(userStorage::getUserById)
                .toList();
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) throws NotFoundException {
        log.info("getCommonFriends: id={}, otherId={}", id, otherId);
        if (id.equals(otherId)) {
            String warning = "id друга не может совпадать с id пользователя";
            log.error("Remove friend: Validation exception: {}", warning);
            throw new ValidationException(warning);
        }

        User user = getUserById(id);
        User other = getUserById(otherId);

        Set<Long> userFriends = user.getFriends().keySet();
        Set<Long> otherFriends = other.getFriends().keySet();

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::getUserById)
                .toList();
    }
}
