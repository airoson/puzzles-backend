# Back end часть приложения Puzzle game

Запуск:
1.  `gradle clean build`
2. `docker compose up -d`

Использованный стек:
* Java 17
* Spring
* PostgreSQL для хранения данных о пользователях
* Redis для хранения игровых сессий
* WebSocket для взаимодействия

API приложения доступно по адресу http://localhost:8080

[Front end приложения](https://github.com/airoson/puzzles-frontend) 