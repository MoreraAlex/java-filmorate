package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

	private FilmController filmController;
	private UserController userController;
	private UserStorage userStorage;
	private final int maxLengthOfDescription = 200;
	private final LocalDate releaseDateOfFirstFilm = LocalDate.of(1895, 12, 28);

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		userStorage = new InMemoryUserStorage(validator);
		userController = new UserController(new UserService(userStorage));
		filmController = new FilmController(
				new FilmService(new InMemoryFilmStorage(validator, userStorage)));
	}

	@Test
	void filmController_create_mustBeValidationException() {
		Film film = new Film();
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
		Film film = new Film();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			stringBuilder.append("a");
		}
		film.setName(stringBuilder.toString());
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);

		Film newFilm = (filmController.create(film)).getBody();

		assertEquals(
				1,
				newFilm.getId(),
				"Неверный id фильма"
		);
	}

	@Test
	void filmController_update_mustBeNotFoundException() throws NotFoundException {
		Film film = new Film();
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
	void filmController_update_mustBeValidationException() {
		Film film = new Film();
		film.setName("Название фильма");
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);

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
		Film film = new Film();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			stringBuilder.append("a");
		}
		film.setName(stringBuilder.toString());
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);

		filmController.create(film);

		Film updateFilm = new Film();
		updateFilm.setId((long) 1);
		updateFilm.setDescription("Описание");
		film = (filmController.update(updateFilm)).getBody();

		Film standardFilm = new Film();
		standardFilm.setName(stringBuilder.toString());
		standardFilm.setReleaseDate(releaseDateOfFirstFilm);
		standardFilm.setDuration(120);
		standardFilm.setId((long) 1);
		standardFilm.setDescription("Описание");

		assertTrue(film.equals(standardFilm),
				"Фильмы не равны"
		);

	}

	@Test
	void filmController_update_returnFilmWithCorrectName() {
		Film film = new Film();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			stringBuilder.append("a");
		}
		film.setName(stringBuilder.toString());
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);

		filmController.create(film);

		Film updateFilm = new Film();
		updateFilm.setId((long) 1);
		updateFilm.setName("Наименование");
		film = (filmController.update(updateFilm)).getBody();

		Film standardFilm = new Film();
		standardFilm.setName("Наименование");
		standardFilm.setReleaseDate(releaseDateOfFirstFilm);
		standardFilm.setDuration(120);
		standardFilm.setId((long) 1);

		assertTrue(film.equals(standardFilm),
				"Фильмы не равны"
		);

	}

	@Test
	void filmController_update_returnFilmWithCorrectReleaseDate() {
		Film film = new Film();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			stringBuilder.append("a");
		}
		film.setName(stringBuilder.toString());
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);

		filmController.create(film);

		Film updateFilm = new Film();
		updateFilm.setId((long) 1);
		updateFilm.setReleaseDate(LocalDate.of(2025, 12, 29));
		film = (filmController.update(updateFilm)).getBody();

		Film standardFilm = new Film();
		standardFilm.setName(stringBuilder.toString());
		standardFilm.setReleaseDate(LocalDate.of(2025, 12, 29));
		standardFilm.setDuration(120);
		standardFilm.setId((long) 1);

		assertTrue(film.equals(standardFilm),
				"Фильмы не равны"
		);

	}

	@Test
	void filmController_update_returnFilmWithCorrectDurationInMinutes() {
		Film film = new Film();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			stringBuilder.append("a");
		}
		film.setName(stringBuilder.toString());
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);

		filmController.create(film);

		Film updateFilm = new Film();
		updateFilm.setId((long) 1);
		updateFilm.setDuration(60);
		film = (filmController.update(updateFilm)).getBody();

		Film standardFilm = new Film();
		standardFilm.setName(stringBuilder.toString());
		standardFilm.setReleaseDate(releaseDateOfFirstFilm);
		standardFilm.setDuration(60);
		standardFilm.setId((long) 1);

		assertTrue(film.equals(standardFilm),
				"Фильмы не равны"
		);

	}

	@Test
	void filmController_updateAll_returnFilmWithCorrectFields() {
		Film film = new Film();
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			stringBuilder.append("a");
		}
		film.setName(stringBuilder.toString());
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);

		filmController.create(film);

		Film updateFilm = new Film();
		updateFilm.setId((long) 1);
		updateFilm.setName("New name");
		updateFilm.setDuration(60);
		updateFilm.setReleaseDate(LocalDate.of(2024, 06, 01));
		updateFilm.setDescription("New description");
		film = (filmController.update(updateFilm)).getBody();

		Film standardFilm = new Film();
		standardFilm.setName("New name");
		standardFilm.setDuration(60);
		standardFilm.setReleaseDate(LocalDate.of(2024, 06, 01));
		standardFilm.setDescription("New description");
		standardFilm.setId((long) 1);

		assertTrue(film.equals(standardFilm),
				"Фильмы не равны"
		);

	}

	@Test
	void userController_createWithIncorrectEmail_mustBeValidationException() {
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
	void userController_createWithIncorrectLogin_mustBeValidationException() {
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
	void userController_createWithIncorrectBirthday_mustBeValidationException() {
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
		User user = new User();
		user.setLogin("login");
		user.setEmail("aa@bb.com");

		ResponseEntity<User> response = userController.create(user);
		User newUser = response.getBody();

		assertEquals(
				1L,
				newUser.getId(),
				"Неверный id пользователя"
		);
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
	void userController_update_mustBeValidationException() {
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
		User user = new User();
		user.setLogin("login");
		user.setEmail("aa@bb.com");
		user.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user);

		User updateUser = new User();
		updateUser.setId((long) 1);
		updateUser.setLogin("newLogin");
		user = (userController.update(updateUser)).getBody();

		User standardUser = new User();
		standardUser.setName("newLogin");
		standardUser.setLogin("newLogin");
		standardUser.setEmail("aa@bb.com");
		standardUser.setBirthday(LocalDate.now().plusYears(-20));
		standardUser.setId((long) 1);

		assertTrue(user.equals(standardUser),
				"Пользователи не равны"
		);

	}

	@Test
	void userController_update_returnUserWithCorrectLogin() {
		User user = new User();
		user.setLogin("login");
		user.setName("name");
		user.setEmail("aa@bb.com");
		user.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user);

		User updateUser = new User();
		updateUser.setId((long) 1);
		updateUser.setLogin("newLogin");
		user = (userController.update(updateUser)).getBody();

		User standardUser = new User();
		standardUser.setName("name");
		standardUser.setLogin("newLogin");
		standardUser.setEmail("aa@bb.com");
		standardUser.setBirthday(LocalDate.now().plusYears(-20));
		standardUser.setId((long) 1);

		assertTrue(user.equals(standardUser),
				"Пользователи не равны"
		);

	}

	@Test
	void userController_update_returnUserWithCorrectName() {
		User user = new User();
		user.setLogin("login");
		user.setName("name");
		user.setEmail("aa@bb.com");
		user.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user);

		User updateUser = new User();
		updateUser.setId((long) 1);
		updateUser.setName("newName");
		user = (userController.update(updateUser)).getBody();

		User standardUser = new User();
		standardUser.setName("newName");
		standardUser.setLogin("login");
		standardUser.setEmail("aa@bb.com");
		standardUser.setBirthday(LocalDate.now().plusYears(-20));
		standardUser.setId((long) 1);

		assertTrue(user.equals(standardUser),
				"Пользователи не равны"
		);

	}

	@Test
	void userController_update_returnUserWithCorrectEmail() {
		User user = new User();
		user.setLogin("login");
		user.setName("name");
		user.setEmail("aa@bb.com");
		user.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user);

		User updateUser = new User();
		updateUser.setId((long) 1);
		updateUser.setEmail("newEmail@aa.com");
		user = (userController.update(updateUser)).getBody();

		User standardUser = new User();
		standardUser.setName("name");
		standardUser.setLogin("login");
		standardUser.setEmail("newEmail@aa.com");
		standardUser.setBirthday(LocalDate.now().plusYears(-20));
		standardUser.setId((long) 1);

		assertTrue(user.equals(standardUser),
				"Пользователи не равны"
		);

	}

	@Test
	void userController_update_returnUserWithCorrectBirthday() {
		User user = new User();
		user.setLogin("login");
		user.setName("name");
		user.setEmail("aa@bb.com");
		user.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user);

		User updateUser = new User();
		updateUser.setId((long) 1);
		updateUser.setBirthday(LocalDate.now().plusYears(-10));
		user = (userController.update(updateUser)).getBody();

		User standardUser = new User();
		standardUser.setName("name");
		standardUser.setLogin("login");
		standardUser.setEmail("aa@bb.com");
		standardUser.setBirthday(LocalDate.now().plusYears(-10));
		standardUser.setId((long) 1);

		assertTrue(user.equals(standardUser),
				"Пользователи не равны"
		);

	}

	@Test
	void userController_updateAll_returnUserWithCorrectFields() {
		User user = new User();
		user.setLogin("login");
		user.setName("name");
		user.setEmail("aa@bb.com");
		user.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user);

		User updateUser = new User();
		updateUser.setId((long) 1);
		user.setLogin("newLogin");
		user.setName("newName");
		user.setEmail("newEmail@bb.com");
		updateUser.setBirthday(LocalDate.now().plusYears(-10));
		user = (userController.update(updateUser)).getBody();

		User standardUser = new User();
		standardUser.setLogin("newLogin");
		standardUser.setName("newName");
		standardUser.setEmail("newEmail@bb.com");
		standardUser.setBirthday(LocalDate.now().plusYears(-10));
		standardUser.setId((long) 1);

		assertTrue(user.equals(standardUser),
				"Пользователи не равны"
		);

	}

	@Test
	void userController_getUserById_getUserByCorrectId() {
		User user = new User();
		user.setLogin("login");
		user.setName("name");
		user.setEmail("aa@bb.com");
		user.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user);

		User findUser = (userController.getUserById((long) 1)).getBody();

		assertEquals(
				1,
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
				() -> userController.addFriend((long) 1, (long) 2),
				"Вернулось не NotFoundException"
		);
		assertEquals(
				"Пользователь с id = 1 не найден",
				exception.getMessage(),
				"Не верный текст сообщения"
		);

		User user = new User();
		user.setLogin("login");
		user.setName("name");
		user.setEmail("aa@bb.com");
		user.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user);

		exception = assertThrows(
				NotFoundException.class,
				() -> userController.addFriend((long) 1, (long) 2),
				"Вернулось не NotFoundException"
		);
		assertEquals(
				"Пользователь с id = 2 не найден",
				exception.getMessage(),
				"Не верный текст сообщения"
		);
	}

	@Test
	void userController_addFriend_addFriendWithEqualsIdUser_mustValidationException() {
		Exception exception = assertThrows(
				ValidationException.class,
				() -> userController.addFriend((long) 1, (long) 1),
				"Вернулось не ValidationException"
		);
	}

	@Test
	void userController_addFriend() {
		User user1 = new User();
		user1.setLogin("login1");
		user1.setName("name");
		user1.setEmail("aa1@bb.com");
		user1.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user1);


		User user2 = new User();
		user2.setLogin("login2");
		user2.setName("name");
		user2.setEmail("aa2@bb.com");
		user2.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user2);
		userController.addFriend((long) 1, (long) 2);

		assertEquals(
				1,
				(userController.getUserById((long) 1)).getBody().getFriends().size(),
				"Неверное количество друзей"
		);

		assertEquals(
				1,
				(userController.getUserById((long) 2)).getBody().getFriends().size(),
				"Неверное количество друзей"
		);
	}

	@Test
	void userController_removeFriend_addFriendByIncorrectId() {
		Exception exception = assertThrows(
				NotFoundException.class,
				() -> userController.removeFriend((long) 1, (long) 2),
				"Вернулось не NotFoundException"
		);
		assertEquals(
				"Пользователь с id = 1 не найден",
				exception.getMessage(),
				"Не верный текст сообщения"
		);

		User user = new User();
		user.setLogin("login");
		user.setName("name");
		user.setEmail("aa@bb.com");
		user.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user);

		exception = assertThrows(
				NotFoundException.class,
				() -> userController.removeFriend((long) 1, (long) 2),
				"Вернулось не NotFoundException"
		);
		assertEquals(
				"Пользователь с id = 2 не найден",
				exception.getMessage(),
				"Не верный текст сообщения"
		);
	}

	@Test
	void userController_removeFriend_addFriendWithEqualsIdUser_mustValidationException() {
		assertThrows(
				ValidationException.class,
				() -> userController.removeFriend((long) 1, (long) 1),
				"Вернулось не ValidationException"
		);
	}

	@Test
	void userController_removeFriend() {
		User user1 = new User();
		user1.setLogin("login1");
		user1.setName("name");
		user1.setEmail("aa1@bb.com");
		user1.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user1);


		User user2 = new User();
		user2.setLogin("login2");
		user2.setName("name");
		user2.setEmail("aa2@bb.com");
		user2.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user2);

		User user3 = new User();
		user3.setLogin("login2");
		user3.setName("name");
		user3.setEmail("aa2@bb.com");
		user3.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user3);

		userController.addFriend((long) 1, (long) 2);
		userController.addFriend((long) 1, (long) 3);

		assertEquals(2,
				(userController.getUserById((long) 1)).getBody().getFriends().size(),
				"Неверное количество друзей"
		);

		userController.removeFriend((long) 1, (long) 3);

		assertEquals(1,
				(userController.getUserById((long) 1)).getBody().getFriends().size(),
				"Неверное количество друзей"
		);
		assertEquals(0,
				(userController.getUserById((long) 3)).getBody().getFriends().size(),
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
		User user1 = new User();
		user1.setLogin("login1");
		user1.setName("name");
		user1.setEmail("aa1@bb.com");
		user1.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user1);


		User user2 = new User();
		user2.setLogin("login2");
		user2.setName("name");
		user2.setEmail("aa2@bb.com");
		user2.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user2);

		User user3 = new User();
		user3.setLogin("login2");
		user3.setName("name");
		user3.setEmail("aa2@bb.com");
		user3.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user3);

		userController.addFriend((long) 1, (long) 2);
		userController.addFriend((long) 1, (long) 3);

		Collection<User> users = (userController.getUserFriends((long) 1)).getBody();

		assertEquals(2,
				users.size(),
				"Неверное количество друзей"
		);
		assertTrue(users.contains(user2),
				"Друг не найден"
		);
	}

	@Test
	void userController_getCommonFriendsByIncorrectId() {
		Exception exception = assertThrows(
				NotFoundException.class,
				() -> userController.addFriend((long) 1, (long) 2),
				"Вернулось не NotFoundException"
		);
		assertEquals(
				"Пользователь с id = 1 не найден",
				exception.getMessage(),
				"Не верный текст сообщения"
		);

		User user = new User();
		user.setLogin("login");
		user.setName("name");
		user.setEmail("aa@bb.com");
		user.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user);

		exception = assertThrows(
				NotFoundException.class,
				() -> userController.getCommonFriends((long) 1, (long) 2),
				"Вернулось не NotFoundException"
		);
		assertEquals(
				"Пользователь с id = 2 не найден",
				exception.getMessage(),
				"Не верный текст сообщения"
		);
	}

	@Test
	void userController_getCommonFriendsWithEqualsIdUser_mustValidationException() {
		Exception exception = assertThrows(
				ValidationException.class,
				() -> userController.getCommonFriends((long) 1, (long) 1),
				"Вернулось не ValidationException"
		);
	}

	@Test
	void userController_getCommonFriends() {
		User user1 = new User();
		user1.setLogin("login1");
		user1.setName("name");
		user1.setEmail("aa1@bb.com");
		user1.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user1);


		User user2 = new User();
		user2.setLogin("login2");
		user2.setName("name");
		user2.setEmail("aa2@bb.com");
		user2.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user2);

		User user3 = new User();
		user3.setLogin("login2");
		user3.setName("name");
		user3.setEmail("aa2@bb.com");
		user3.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user3);

		userController.addFriend((long) 1, (long) 2);
		userController.addFriend((long) 1, (long) 3);
		userController.addFriend((long) 2, (long) 3);

		Collection<User> users = (userController.getCommonFriends((long) 1, (long) 3)).getBody();

		assertEquals(1,
				users.size(),
				"Неверное количество друзей"
		);
		assertTrue(users.contains(user2),
				"Друг не найден"
		);
	}

	@Test
	void filmController_getUFilmById_getByCorrectId() {
		Film film = new Film();
		film.setName("Название фильма");
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);

		filmController.create(film);

		Film findFilm = (filmController.getFilmById((long) 1)).getBody();

		assertEquals(
				1,
				findFilm.getId(),
				"Неверный id"
		);
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
	void filmController_addLike() {
		Film film = new Film();
		film.setName("Название фильма");
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);
		filmController.create(film);

		User user1 = new User();
		user1.setLogin("login1");
		user1.setName("name");
		user1.setEmail("aa1@bb.com");
		user1.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user1);

		filmController.addLike((long) 1, (long) 1);

		assertEquals(
				1,
				film.getLikes().size(),
				"Неверное количество лайков id"
		);

		assertTrue(
				film.getLikes().contains(user1.getId()),
				"Лайк пользователя не найден"
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

		Film film = new Film();
		film.setName("Название фильма");
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);

		filmController.create(film);

		exception = assertThrows(
				NotFoundException.class,
				() -> filmController.addLike((long) 1, (long) 1),
				"Вернулось не NotFoundException"
		);
		assertEquals(
				"Пользователь с id = 1 не найден",
				exception.getMessage(),
				"Не верный текст сообщения"
		);
	}

	@Test
	void filmController_removeLike() {
		Film film = new Film();
		film.setName("Название фильма");
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);
		filmController.create(film);

		User user1 = new User();
		user1.setLogin("login1");
		user1.setName("name");
		user1.setEmail("aa1@bb.com");
		user1.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user1);

		filmController.addLike((long) 1, (long) 1);
		filmController.removeLike((long) 1, (long) 1);

		assertEquals(
				0,
				film.getLikes().size(),
				"Неверное количество лайков id"
		);

		assertFalse(
				film.getLikes().contains(user1.getId()),
				"Лайк пользователя найден"
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
		Film film = new Film();
		film.setName("Название фильма");
		film.setReleaseDate(releaseDateOfFirstFilm);
		film.setDuration(120);
		filmController.create(film);
		Film film1 = new Film();
		film1.setName("Название фильма1");
		film1.setReleaseDate(releaseDateOfFirstFilm);
		film1.setDuration(120);
		filmController.create(film1);
		Film film2 = new Film();
		film2.setName("Название фильма2");
		film2.setReleaseDate(releaseDateOfFirstFilm);
		film2.setDuration(120);
		filmController.create(film2);

		User user1 = new User();
		user1.setLogin("login1");
		user1.setName("name");
		user1.setEmail("aa1@bb.com");
		user1.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user1);
		User user2 = new User();
		user2.setLogin("login1");
		user2.setName("name");
		user2.setEmail("aa1@bb.com");
		user2.setBirthday(LocalDate.now().plusYears(-20));
		userController.create(user2);

		filmController.addLike((long) 1, (long) 1);

		filmController.addLike((long) 3, (long) 1);
		filmController.addLike((long) 3, (long) 2);

		Collection<Film> films = (filmController.getPopular(2)).getBody();
		assertEquals(
				2,
				films.size(),
				"Неверный id фильма"
		);

		assertEquals(
				3,
				films.iterator().next().getId(),
				"Неверный id фильма"
		);
	}
}