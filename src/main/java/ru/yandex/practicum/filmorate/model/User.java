package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.TypeFriendship;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class User {
    private Long id;
    private Map<Long, TypeFriendship> friends = new HashMap<>();

    @NotNull(message = "Электронная почта не может быть пустой и должна содержать символ '@'")
    @Email(message = "Электронная почта не может быть пустой и должна содержать символ '@'")
    private String email;

    @NotBlank(message = "Логин не может быть пустым и содержать пробелы")
    @Pattern(regexp = "^\\S+$", message = "Логин не может быть пустым и содержать пробелы")
    private String login;
    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public static User randomUser() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("loginUser");
        user.setName("nameUser");

        return user;
    }
}
