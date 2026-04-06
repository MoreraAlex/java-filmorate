package ru.yandex.practicum.filmorate.db;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.ValidateUser;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class,
        UserRowMapper.class,
        ValidateUser.class,
        UserDbStorageTest.TestConfig.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.addUser(user);
    }

    @Test
    void shouldAddAndFindUser() {
        User created = createUser("test@mail.com", "test");

        User found = userStorage.findUserById(created.getId());

        assertThat(found)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("email", "test@mail.com");
    }


    @Test
    void shouldReturnAllUsers() {
        createUser("1@mail.com", "user1");
        createUser("2@mail.com", "user2");

        Collection<User> users = userStorage.getAllUsers();

        assertThat(users)
                .hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateUser() {
        User user = createUser("old@mail.com", "old");

        user.setEmail("new@mail.com");
        user.setName("newName");

        User updated = userStorage.updateUser(user);

        assertThat(updated.getEmail()).isEqualTo("new@mail.com");
        assertThat(updated.getName()).isEqualTo("newName");
    }

    @Test
    void shouldAddFriend() {
        User user1 = createUser("1@mail.com", "user1");
        User user2 = createUser("2@mail.com", "user2");

        userStorage.addFriend(user1.getId(), user2.getId());

        Collection<User> friends = userStorage.getUserFriends(user1.getId());

        assertThat(friends)
                .extracting(User::getId)
                .contains(user2.getId());
    }


    @Test
    void shouldRemoveFriend() {
        User user1 = createUser("1@mail.com", "user1");
        User user2 = createUser("2@mail.com", "user2");

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());

        Collection<User> friends = userStorage.getUserFriends(user1.getId());

        assertThat(friends).isEmpty();
    }


    @Test
    void shouldReturnCommonFriends() {
        User user1 = createUser("1@mail.com", "user1");
        User user2 = createUser("2@mail.com", "user2");
        User common = createUser("3@mail.com", "common");

        userStorage.addFriend(user1.getId(), common.getId());
        userStorage.addFriend(user2.getId(), common.getId());

        Collection<User> commonFriends =
                userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends)
                .extracting(User::getId)
                .contains(common.getId());
    }


    @Test
    void shouldLoadFriendsInsideUser() {
        User user1 = createUser("1@mail.com", "user1");
        User user2 = createUser("2@mail.com", "user2");

        userStorage.addFriend(user1.getId(), user2.getId());

        User loaded = userStorage.findUserById(user1.getId());

        assertThat(loaded.getFriends().keySet())
                .contains(user2.getId());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public Validator validator() {
            return Validation.buildDefaultValidatorFactory().getValidator();
        }

    }

}

