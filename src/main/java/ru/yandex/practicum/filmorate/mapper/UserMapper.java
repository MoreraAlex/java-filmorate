package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static UpdateUserRequest mapToUpdateUserRequest(User user) {
        log.debug("mapToUpdateUserRequest in User: {}", user);
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName(user.getName());
        request.setEmail(user.getEmail());
        request.setLogin(user.getLogin());
        request.setBirthday(user.getBirthday());
        log.debug("mapToUpdateUserRequest return UpdateUserRequest: {}", request);
        return request;
    }

    public static User updateUserFields(User user, UpdateUserRequest request) {
        log.debug("updateUserFields in User: {}", user);
        log.debug("updateUserFields in UpdateUserRequest: {}", request);
        if (request.hasEmail()) {
            user.setEmail(request.getEmail());
        }
        if (request.hasLogin()) {
            user.setLogin(request.getLogin());
        }
        if (request.hasBirthday()) {
            user.setBirthday(request.getBirthday());
        }
        if (request.hasName()) {
            user.setName(request.getName());
        }
        log.debug("updateUserFields return User: {}", user);
        return user;
    }
}
