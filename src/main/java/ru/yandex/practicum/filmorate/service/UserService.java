package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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
        return userStorage.findUserById(id);
    }

    public void addFriend(Long id, Long friendId) throws NotFoundException, ValidationException {
        userStorage.addFriend(id, friendId);
    }

    public void removeFriend(Long id, Long friendId) throws NotFoundException, ValidationException {
        userStorage.removeFriend(id, friendId);
    }

    public Collection<User> getUserFriends(Long id) throws NotFoundException {
        return userStorage.getUserFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) throws NotFoundException {
        return userStorage.getCommonFriends(id, otherId);
    }
}
