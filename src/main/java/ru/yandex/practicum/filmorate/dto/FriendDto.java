package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.TypeFriendship;

@Data
@AllArgsConstructor
public class FriendDto {
    private Long userId;
    private TypeFriendship type;
}