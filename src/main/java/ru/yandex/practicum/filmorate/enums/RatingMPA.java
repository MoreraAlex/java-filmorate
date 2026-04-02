package ru.yandex.practicum.filmorate.enums;

public enum RatingMPA {
    G("G"),
    PG("PG"),
    PG13("PG-13"),
    R("R"),
    NC17("NC-17");

    private final String representation;

    RatingMPA(String representation) {
        this.representation = representation;
    }

    public String getRepresentation() {
        return representation;
    }
}
