package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.practicum.filmorate.model.Film.randomGeneratedFilm;
import static ru.yandex.practicum.filmorate.model.User.randomUser;

@Transactional
@SpringBootTest
class FilmorateApplicationTests {

    @Autowired
    private FilmController filmController;

    @Autowired
    private UserController userController;

    private UserStorage userStorage;
    private final int maxLengthOfDescription = 200;
    private final LocalDate releaseDateOfFirstFilm = LocalDate.of(1895, 12, 28);

    @Test
    void filmController_create_mustBeValidationException() throws ValidationException {
        Film film = randomGeneratedFilm();
        film.setName("Название фильма");
        film.setReleaseDate(releaseDateOfFirstFilm);
        film.setDuration(0);
        Exception exception = assertThrows(
                ValidationException.class,
                () -> filmController.create(film),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Продолжительность фильма должна быть положительным числом",
                exception.getMessage()
        );

        film.setName(null);
        film.setDuration(120);
        exception = assertThrows(
                ValidationException.class,
                () -> filmController.create(film),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Название не может быть пустым",
                exception.getMessage()
        );

        film.setName("Name");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setName("Название фильма");
        exception = assertThrows(
                ValidationException.class,
                () -> filmController.create(film),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Год релиза должен быть после " +
                        releaseDateOfFirstFilm.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                exception.getMessage()
        );

        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            stringBuilder.append("a");
        }
        film.setDescription(stringBuilder.toString());
        exception = assertThrows(
                ValidationException.class,
                () -> filmController.create(film),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Описание не должно превышать 200 символов",
                exception.getMessage()
        );
    }

    @Test
    void filmController_create_returnFilmWithId() {
        Film film = randomGeneratedFilm();
        Genre genre1 = new Genre(1L, "Комедия");
        Genre genre2 = new Genre(1L, "Комедия");
        Genre genre3 = new Genre(3L, "Ntcn");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            stringBuilder.append("a");
        }
        film.setName(stringBuilder.toString());
        film.setReleaseDate(releaseDateOfFirstFilm);
        film.setDuration(120);
        film.setMpa(new Mpa().setId(1));
        film.setGenres(List.of(genre1, genre2, genre3));

        Film newFilm = (filmController.create(film)).getBody();

        assertEquals(
                1,
                newFilm.getId(),
                "Неверный id фильма"
        );
    }

    @Test
    void filmController_update_mustBeNotFoundException() throws NotFoundException {
        Film film = randomGeneratedFilm();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            stringBuilder.append("a");
        }
        film.setName(stringBuilder.toString());
        film.setReleaseDate(releaseDateOfFirstFilm);
        film.setDuration(120);

        filmController.create(film);
        film.setId((long) 2);

        Exception exception = assertThrows(
                NotFoundException.class,
                () -> filmController.update(film),
                "Вернулось не NotFoundException"
        );
        assertEquals(
                "Фильм с id = " + film.getId() + " не найден",
                exception.getMessage()
        );
    }

    @Test
    void filmController_update_mustBeValidationException() throws ValidationException {
        Film film = randomGeneratedFilm();

        filmController.create(film);
        film.setDuration(0);

        Exception exception = assertThrows(
                ValidationException.class,
                () -> filmController.update(film),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Продолжительность фильма должна быть положительным числом",
                exception.getMessage()
        );

        film.setName(null);
        film.setDuration(120);
        exception = assertThrows(
                ValidationException.class,
                () -> filmController.update(film),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Название не может быть пустым",
                exception.getMessage()
        );

        film.setName("Name");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setName("Название фильма");
        exception = assertThrows(
                ValidationException.class,
                () -> filmController.update(film),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Год релиза должен быть после " +
                        releaseDateOfFirstFilm.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                exception.getMessage()
        );

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            stringBuilder.append("a");
        }
        film.setDescription(stringBuilder.toString());
        exception = assertThrows(
                ValidationException.class,
                () -> filmController.update(film),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Описание не должно превышать " + maxLengthOfDescription + " символов",
                exception.getMessage()
        );
    }

    @Test
    void filmController_update_returnFilmWithCorrectDescription() {
        Film film = randomGeneratedFilm();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 200; i++) stringBuilder.append("a");
        film.setName(stringBuilder.toString());
        film.setReleaseDate(releaseDateOfFirstFilm);
        film.setDuration(120);
        Genre genre1 = new Genre(1L, "Комедия");
        Genre genre2 = new Genre(1L, "Комедия");
        Genre genre3 = new Genre(3L, "Ntcn");

        Film createdFilm = filmController.create(film).getBody();

        Film updateFilm = randomGeneratedFilm();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setDescription("Описание");
        updateFilm.setGenres(List.of(genre1, genre2, genre3));
        Film updatedFilm = filmController.update(updateFilm).getBody();

        assertEquals("Описание", updatedFilm.getDescription(), "Фильмы не равны");
    }

    @Test
    void filmController_update_returnFilmWithCorrectName() {
        Film film = randomGeneratedFilm();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            stringBuilder.append("a");
        }
        film.setName(stringBuilder.toString());
        film.setReleaseDate(releaseDateOfFirstFilm);
        film.setDuration(120);

        filmController.create(film);

        Film updateFilm = new Film();
        updateFilm.setMpa(film.getMpa());
        updateFilm.setId(film.getId());
        updateFilm.setName("Наименование");
        updateFilm.setReleaseDate(releaseDateOfFirstFilm);
        updateFilm.setDuration(120);
        film = (filmController.update(updateFilm)).getBody();

        Film standardFilm = updateFilm;
        standardFilm.setMpa(film.getMpa());
        standardFilm.setName("Наименование");
        standardFilm.setReleaseDate(releaseDateOfFirstFilm);
        standardFilm.setDuration(120);
        standardFilm.setId(film.getId());

        assertEquals(film, standardFilm, "Фильмы не равны");

    }

    @Test
    void filmController_update_returnFilmWithCorrectReleaseDate() {
        Film film = randomGeneratedFilm();
        film.setName("Название фильма");
        film.setReleaseDate(releaseDateOfFirstFilm);
        film.setDuration(120);
        Film createdFilm = filmController.create(film).getBody();

        Film updateFilm = randomGeneratedFilm();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setReleaseDate(LocalDate.of(2025, 12, 29));
        updateFilm.setDuration(120);
        Film updatedFilm = filmController.update(updateFilm).getBody();

        assertEquals(LocalDate.of(2025, 12, 29), updatedFilm.getReleaseDate(), "Фильмы не равны");
    }

    @Test
    void userController_addAndRemoveFriends_worksCorrectly() {
        User user1 = User.randomUser();
        user1.setLogin("u1");
        user1.setEmail("u1@email.com");
        Long id1 = userController.create(user1).getBody().getId();

        User user2 = User.randomUser();
        user2.setLogin("u2");
        user2.setEmail("u2@email.com");
        Long id2 = userController.create(user2).getBody().getId();

        userController.addFriend(id1, id2);

        Collection<User> friends1 = userController.getUserFriends(id1).getBody();
        Collection<User> friends2 = userController.getUserFriends(id2).getBody();

        assertEquals(1, friends1.size());
        assertEquals(1, friends2.size());

        userController.removeFriend(id1, id2);

        assertEquals(0, userController.getUserFriends(id1).getBody().size());
        assertEquals(0, userController.getUserFriends(id2).getBody().size());
    }

    @Test
    void userController_getCommonFriends_returnsCorrectly() {
        User user1 = User.randomUser();
        user1.setLogin("u1");
        user1.setEmail("u1@email.com");
        Long id1 = userController.create(user1).getBody().getId();

        User user2 = User.randomUser();
        user2.setLogin("u2");
        user2.setEmail("u2@email.com");
        Long id2 = userController.create(user2).getBody().getId();

        User user3 = User.randomUser();
        user3.setLogin("u3");
        user3.setEmail("u3@email.com");
        Long id3 = userController.create(user3).getBody().getId();

        userController.addFriend(id1, id3);
        userController.addFriend(id2, id3);

        Collection<User> common = userController.getCommonFriends(id1, id2).getBody();
        assertEquals(1, common.size());
        assertEquals(id3, common.iterator().next().getId());
    }


    @Test
    void userController_createAndUpdate_returnsCorrectUser() {
        User user = User.randomUser();
        user.setLogin("login");
        user.setEmail("aa@bb.com");
        Long id = userController.create(user).getBody().getId();

        User update = User.randomUser();
        update.setId(id);
        update.setLogin("newLogin");
        update.setEmail("newEmail@bb.com");
        update.setName("newName");

        User updated = userController.update(update).getBody();

        assertEquals("newLogin", updated.getLogin());
        assertEquals("newEmail@bb.com", updated.getEmail());
        assertEquals("newName", updated.getName());
    }

    @Test
    void filmController_getFilmById_returnsCorrectFilm() {
        Film film = Film.randomGeneratedFilm();
        Long filmId = filmController.create(film).getBody().getId();

        Film fetched = filmController.getFilmById(filmId).getBody();
        assertEquals(filmId, fetched.getId());
    }


    @Test
    void userController_createWithIncorrectEmail_mustBeValidationException() throws ValidationException {
        User user = new User();
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(-1));
        Exception exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Электронная почта не может быть пустой и должна содержать символ '@'",
                exception.getMessage()
        );

        user.setEmail("email");
        exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Электронная почта не может быть пустой и должна содержать символ '@'",
                exception.getMessage()
        );

    }

    @Test
    void userController_createWithIncorrectLogin_mustBeValidationException() throws ValidationException {
        User user = new User();
        user.setEmail("ss@ss.com");
        user.setBirthday(LocalDate.now().plusDays(-1));

        Exception exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Логин не может быть пустым и содержать пробелы",
                exception.getMessage()
        );
        user.setLogin("log in");
        exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Логин не может быть пустым и содержать пробелы",
                exception.getMessage()
        );

    }

    @Test
    void userController_createWithIncorrectBirthday_mustBeValidationException() throws ValidationException {
        User user = new User();
        user.setEmail("ss@ss.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));
        Exception exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Дата рождения не может быть в будущем",
                exception.getMessage()
        );
    }

    @Test
    void userController_create_returnUserWithId() {
        User user = User.randomUser();
        User newUser = userController.create(user).getBody();

        assertNotNull(newUser.getId());
    }

    @Test
    void userController_update_mustBeNotFoundException() throws NotFoundException {
        User user = new User();
        user.setLogin("login");
        user.setEmail("aa@bb.com");

        userController.create(user);
        user.setId((long) 2);

        Exception exception = assertThrows(
                NotFoundException.class,
                () -> (userController.update(user)).getBody(),
                "Вернулось не NotFoundException"
        );
        assertEquals(
                "Пользователь с id = " + user.getId() + " не найден",
                exception.getMessage()
        );
    }

    @Test
    void userController_update_mustBeValidationException() throws ValidationException {
        User user = new User();
        user.setLogin("login");
        user.setEmail("aa@bb.com");
        userController.create(user);

        user.setEmail("ccdd.com");
        Exception exception = assertThrows(
                ValidationException.class,
                () -> userController.update(user),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Электронная почта не может быть пустой и должна содержать символ '@'",
                exception.getMessage()
        );

        user.setEmail("cc@dd.com");
        user.setLogin("log in");
        exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Логин не может быть пустым и содержать пробелы",
                exception.getMessage()
        );

        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));
        exception = assertThrows(
                ValidationException.class,
                () -> userController.create(user),
                "Вернулось не ValidationException"
        );
        assertEquals(
                "Дата рождения не может быть в будущем",
                exception.getMessage()
        );
    }

    @Test
    void userController_update_returnUserWithCorrectLoginAndName() {
        User user = User.randomUser();
        user.setLogin("login");
        user.setEmail("aa@bb.com");
        user.setBirthday(LocalDate.now().minusYears(20));
        User created = userController.create(user).getBody();

        User updateUser = User.randomUser();
        updateUser.setId(created.getId());
        updateUser.setName("newLogin");
        updateUser.setLogin("newLogin");
        updateUser.setEmail(created.getEmail());
        updateUser.setBirthday(created.getBirthday());

        User updated = userController.update(updateUser).getBody();

        assertEquals("newLogin", updated.getName());
        assertEquals("newLogin", updated.getLogin());
    }

    @Test
    void userController_update_returnUserWithCorrectLogin() {
        User user = User.randomUser();
        user.setLogin("login");
        user.setName("name");
        user.setEmail("aa@bb.com");
        user.setBirthday(LocalDate.now().minusYears(20));
        User created = userController.create(user).getBody();

        User updateUser = User.randomUser();
        updateUser.setId(created.getId());
        updateUser.setName(created.getName());
        updateUser.setLogin("newLogin");
        updateUser.setEmail(created.getEmail());
        updateUser.setBirthday(created.getBirthday());

        User updated = userController.update(updateUser).getBody();

        assertEquals("newLogin", updated.getLogin());
    }

    @Test
    void userController_update_returnUserWithCorrectName() {
        User user = randomUser();
        user.setLogin("login");
        user.setEmail("aa@bb.com");
        user.setName("name");
        user.setBirthday(LocalDate.now().minusYears(20));
        userController.create(user);

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLogin("newLogin");
        updateUser.setName("newName");
        updateUser.setEmail("newEmail@bb.com");
        updateUser.setBirthday(LocalDate.now().minusYears(10));

        User updatedUser = userController.update(updateUser).getBody();

        assertEquals("newName", updatedUser.getName());
    }

    @Test
    void userController_update_returnUserWithCorrectEmail() {
        User user = User.randomUser();
        user.setLogin("login");
        user.setName("name");
        user.setEmail("aa@bb.com");
        user.setBirthday(LocalDate.now().minusYears(20));
        User created = userController.create(user).getBody();

        User updateUser = User.randomUser();
        updateUser.setId(created.getId());
        updateUser.setName(created.getName());
        updateUser.setLogin(created.getLogin());
        updateUser.setEmail("newEmail@aa.com");
        updateUser.setBirthday(created.getBirthday());

        User updated = userController.update(updateUser).getBody();

        assertEquals("newEmail@aa.com", updated.getEmail());
        assertEquals(created.getLogin(), updated.getLogin());
        assertEquals(created.getName(), updated.getName());
    }

    @Test
    void userController_update_returnUserWithCorrectBirthday() {
        User user = userController.create(User.randomUser()).getBody();

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setName(user.getName());
        updateUser.setLogin(user.getLogin());
        updateUser.setEmail(user.getEmail());
        updateUser.setBirthday(LocalDate.now().minusYears(10));

        User updated = userController.update(updateUser).getBody();

        assertEquals(LocalDate.now().minusYears(10), updated.getBirthday());
    }

    @Test
    void userController_updateAll_returnUserWithCorrectFields() {
        User user = randomUser();
        user.setLogin("login");
        user.setEmail("aa@bb.com");
        user.setName("name");
        user.setBirthday(LocalDate.now().minusYears(20));
        userController.create(user);

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLogin("newLogin");
        updateUser.setName("newName");
        updateUser.setEmail("newEmail@bb.com");
        updateUser.setBirthday(LocalDate.now().minusYears(10));

        User updatedUser = userController.update(updateUser).getBody();

        assertEquals("newLogin", updatedUser.getLogin());
        assertEquals("newName", updatedUser.getName());
        assertEquals("newEmail@bb.com", updatedUser.getEmail());
        assertEquals(LocalDate.now().minusYears(10), updatedUser.getBirthday());
    }


    @Test
    void userController_getUserById_getUserByCorrectId() {
        User user = randomUser();
        user.setLogin("login");
        user.setName("name");
        user.setEmail("aa@bb.com");
        user.setBirthday(LocalDate.now().plusYears(-20));
        userController.create(user);

        User findUser = (userController.getUserById(user.getId()).getBody());

        assertEquals(
                user.getId(),
                findUser.getId(),
                "Неверный id пользователя"
        );
    }

    @Test
    void userController_getUserById_getUserByIncorrectId_mustBeNotFoundException() {
        Exception exception = assertThrows(
                NotFoundException.class,
                () -> userController.getUserById((long) 1),
                "Вернулось не NotFoundException"
        );
        assertEquals(
                "Пользователь с id = 1 не найден",
                exception.getMessage(),
                "Не верный текст сообщения"
        );
    }

    @Test
    void userController_addFriend_addFriendByIncorrectId() {
        Exception exception = assertThrows(
                NotFoundException.class,
                () -> userController.addFriend(1L, 2L)
        );
        assertEquals("Пользователь с id = 1 не найден", exception.getMessage());

        User user = User.randomUser();
        User created = userController.create(user).getBody();

        exception = assertThrows(
                NotFoundException.class,
                () -> userController.addFriend(created.getId(), 2L)
        );
        assertEquals("Пользователь с id = 2 не найден", exception.getMessage());
    }

    @Test
    void userController_addFriend() {
        User user1 = randomUser();

        userController.create(user1);

        User user2 = randomUser();

        userController.create(user2);
        userController.addFriend(user1.getId(), user2.getId());

        assertEquals(
                1,
                (userController.getUserById((user1.getId()))).getBody().getFriends().size(),
                "Неверное количество друзей"
        );

        assertEquals(
                1,
                (userController.getUserById(user2.getId())).getBody().getFriends().size(),
                "Неверное количество друзей"
        );
    }

    @Test
    void userController_removeFriend_addFriendByIncorrectId() {
        Exception exception = assertThrows(
                NotFoundException.class,
                () -> userController.removeFriend(1L, 2L)
        );
        assertEquals("Пользователь с id = 1 не найден", exception.getMessage());

        User user = User.randomUser();
        User createdUser = userController.create(user).getBody();

        exception = assertThrows(
                NotFoundException.class,
                () -> userController.removeFriend(createdUser.getId(), 2L)
        );
        assertEquals("Пользователь с id = 2 не найден", exception.getMessage());
    }


    @Test
    void userController_removeFriend() {
        User user1 = randomUser();
        user1.setLogin("login1");
        user1.setName("name");
        user1.setEmail("aa1@bb.com");
        user1.setBirthday(LocalDate.now().plusYears(-20));
        userController.create(user1);


        User user2 = randomUser();
        user2.setLogin("login2");
        user2.setName("name");
        user2.setEmail("aa2@bb.com");
        user2.setBirthday(LocalDate.now().plusYears(-20));
        userController.create(user2);

        User user3 = randomUser();
        user3.setLogin("login2");
        user3.setName("name");
        user3.setEmail("aa2@bb.com");
        user3.setBirthday(LocalDate.now().plusYears(-20));
        userController.create(user3);

        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3.getId());

        assertEquals(2,
                (userController.getUserById(user1.getId())).getBody().getFriends().size(),
                "Неверное количество друзей"
        );

        userController.removeFriend(user1.getId(), user3.getId());

        assertEquals(1,
                (userController.getUserById(user1.getId())).getBody().getFriends().size(),
                "Неверное количество друзей"
        );
        assertEquals(0,
                (userController.getUserById(user3.getId())).getBody().getFriends().size(),
                "Неверное количество друзей"
        );
    }

    @Test
    void userController_getUserFriends_incorrectionId_mustNotFoundException() {
        assertThrows(
                NotFoundException.class,
                () -> userController.getUserFriends((long) 1),
                "Вернулось не NotFoundException"
        );
    }

    @Test
    void userController_getUserFriends() {
        User user1 = userController.create(User.randomUser()).getBody();
        User user2 = userController.create(User.randomUser()).getBody();
        User user3 = userController.create(User.randomUser()).getBody();

        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3.getId());

        Collection<User> friends = userController.getUserFriends(user1.getId()).getBody();

        assertEquals(2, friends.size());
        assertTrue(friends.contains(user2));
        assertTrue(friends.contains(user3));
    }

    @Test
    void userController_getCommonFriendsByIncorrectId() {
        Exception exception = assertThrows(
                NotFoundException.class,
                () -> userController.addFriend(1L, 2L)
        );
        assertEquals("Пользователь с id = 1 не найден", exception.getMessage());

        User user = User.randomUser();
        userController.create(user);

        exception = assertThrows(
                NotFoundException.class,
                () -> userController.getCommonFriends(user.getId(), 2L)
        );
        assertEquals("Пользователь с id = 2 не найден", exception.getMessage());
    }


    @Test
    void userController_getCommonFriends() {
        User user1 = userController.create(User.randomUser()).getBody();
        User user2 = userController.create(User.randomUser()).getBody();
        User user3 = userController.create(User.randomUser()).getBody();

        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3.getId());
        userController.addFriend(user2.getId(), user3.getId());

        Collection<User> common = userController.getCommonFriends(user1.getId(), user3.getId()).getBody();

        assertEquals(1, common.size());
        assertTrue(common.contains(user2));
    }

    @Test
    void filmController_getFilmById_getByCorrectId() {
        Film film = randomGeneratedFilm();
        film.setName("Название фильма");
        film.setReleaseDate(releaseDateOfFirstFilm);
        film.setDuration(120);
        Film created = filmController.create(film).getBody();

        Film found = filmController.getFilmById(created.getId()).getBody();

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void filmController_getUFilmById_getByIncorrectId_mustBeNotFoundException() {
        Exception exception = assertThrows(
                NotFoundException.class,
                () -> filmController.getFilmById((long) 1),
                "Вернулось не NotFoundException"
        );
        assertEquals(
                "Фильм с id = 1 не найден",
                exception.getMessage(),
                "Не верный текст сообщения"
        );
    }


    @Test
    void filmController_addLike_getByIncorrectId_mustBeNotFoundException() {
        Exception exception = assertThrows(
                NotFoundException.class,
                () -> filmController.addLike((long) 1, (long) 1),
                "Вернулось не NotFoundException"
        );
        assertEquals(
                "Фильм с id = 1 не найден",
                exception.getMessage(),
                "Не верный текст сообщения"
        );

        Film film = randomGeneratedFilm();
        film.setName("Название фильма");
        film.setReleaseDate(releaseDateOfFirstFilm);
        film.setDuration(120);

        filmController.create(film);

        exception = assertThrows(
                NotFoundException.class,
                () -> filmController.addLike(film.getId(), (long) 1),
                "Вернулось не NotFoundException"
        );
        assertEquals(
                "Пользователь с id = 1 не найден",
                exception.getMessage(),
                "Не верный текст сообщения"
        );
    }

    @Test
    void filmController_removeLike_getByIncorrectId_mustBeNotFoundException() {
        Exception exception = assertThrows(
                NotFoundException.class,
                () -> filmController.removeLike((long) 1, (long) 1),
                "Вернулось не NotFoundException"
        );
        assertEquals(
                "Фильм с id = 1 не найден",
                exception.getMessage(),
                "Не верный текст сообщения"
        );
    }

    @Test
    void filmController_getPopular() {
        Film film1 = Film.randomGeneratedFilm();
        Film film2 = Film.randomGeneratedFilm();
        Film film3 = Film.randomGeneratedFilm();

        Long id1 = filmController.create(film1).getBody().getId();
        Long id2 = filmController.create(film2).getBody().getId();
        Long id3 = filmController.create(film3).getBody().getId();

        User user1 = User.randomUser();
        user1.setLogin("login1");
        user1.setEmail("u1@email.com");
        Long u1 = userController.create(user1).getBody().getId();

        User user2 = User.randomUser();
        user2.setLogin("login2");
        user2.setEmail("u2@email.com");
        Long u2 = userController.create(user2).getBody().getId();

        filmController.addLike(id1, u1);
        filmController.addLike(id3, u1);
        filmController.addLike(id3, u2);

        Collection<Film> popular = filmController.getPopular(2).getBody();

        assertEquals(2, popular.size());
        assertEquals(id3, popular.iterator().next().getId());
    }

}