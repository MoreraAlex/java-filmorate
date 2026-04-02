package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    Collection<User> getAllUsers();

    User getUserById(Long id);

    void addFriend(Long id, Long friendId);

    void removeFriend(Long id, Long friendId);
}
