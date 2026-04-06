MERGE INTO types_friendship KEY(id) VALUES (1, 'CONFIRMED');
MERGE INTO types_friendship KEY(id) VALUES (2, 'NOT_CONFIRMED');

MERGE INTO ratings KEY(id) VALUES (1, 'G');
MERGE INTO ratings KEY(id) VALUES (2, 'PG');
MERGE INTO ratings KEY(id) VALUES (3, 'PG-13');
MERGE INTO ratings KEY(id) VALUES (4, 'R');
MERGE INTO ratings KEY(id) VALUES (5, 'NC-17');

MERGE INTO genres KEY(id) VALUES (1, 'Комедия');
MERGE INTO genres KEY(id) VALUES (2, 'Драма');
MERGE INTO genres KEY(id) VALUES (3, 'Мультфильм');
MERGE INTO genres KEY(id) VALUES (4, 'Триллер');
MERGE INTO genres KEY(id) VALUES (5, 'Документальный');
MERGE INTO genres KEY(id) VALUES (6, 'Боевик');