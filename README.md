# console-app

## Задание №2

Разработать консольное приложение (user-service) на Java, использующее Hibernate для взаимодействия с PostgreSQL, без использования Spring. Приложение должно поддерживать базовые операции CRUD (Create, Read, Update, Delete) над сущностью User.

### Требования

- Использовать Hibernate в качестве ORM.
- База данных — PostgreSQL.
- Настроить Hibernate без Spring, используя `hibernate.cfg.xml` или properties-файл.
- Реализовать CRUD-операции для сущности User (создание, чтение, обновление, удаление), которая состоит из полей: `id`, `name`, `email`, `age`, `created_at`.
- Использовать консольный интерфейс для взаимодействия с пользователем.
- Использовать Maven для управления зависимостями.
- Настроить логирование.
- Настроить транзакционность для операций с базой данных.
- Использовать DAO-паттерн для отделения логики работы с БД.
- Обработать возможные исключения, связанные с Hibernate и PostgreSQL.

## Команда

| Участник | Зона ответственности |
|---|---|
| Филиппович Милана | Maven, логирование, docker-compose для PostgreSQL, README |
| Чеботарев Андрей | Сущность `User`, `hibernate.cfg.xml`, `HibernateUtil`, `schema.sql` |
| Кучугова Яна | DAO, транзакции, обработка ошибок |
| Волкова Анастасия | Консольное меню, валидация, JUnit-тесты |

## Стек

- Java 17
- Maven
- Hibernate ORM 6
- PostgreSQL 16 (Docker)
- Logback + SLF4J
- JUnit 5

## Требования

- JDK 17+
- Maven 3.8+
- Docker Desktop

## Как запустить

### 1. Поднять PostgreSQL в Docker

```bash
docker compose up -d
```

Проверить, что контейнер живой:

```bash
docker compose ps
```

Должен быть статус `Up ... (healthy)`.

Остановить (данные сохранятся):

```bash
docker compose down
```

Остановить и удалить данные:

```bash
docker compose down -v
```

### 2. Собрать проект

```bash
mvn clean package
```

### 3. Запустить приложение

```bash
java -jar target/console-app.jar
```

## Структура проекта

```
src/main/java/com/example/consoleapp/
├── config/      — HibernateUtil и конфигурация
├── entity/      — JPA-сущности (User)
├── dao/         — DAO-интерфейсы и реализации
├── ui/          — консольное меню (Main)
└── exception/   — кастомные исключения (DaoException и др.)
```

## Параметры подключения к БД

- URL: `jdbc:postgresql://localhost:5432/console_app`
- User: `postgres`
- Password: `postgres`


## Логи

Пишутся в консоль и в `logs/console-app.log`. Файл ротируется по дням и размеру (10 МБ), старые версии хранятся 30 дней.

## Работа с ветками

Две постоянные ветки:

- `main` — стабильная версия, только готовый и проверенный код
- `develop` — рабочая ветка, сюда собираем все задачи через Pull Request

Под каждую задачу — отдельная временная ветка от `develop`:

- `feature/<название>` — новая функциональность, например `feature/user-entity`, `feature/user-dao`
- `bugfix/<название>` — исправление бага

Порядок работы:

1. Обновить `develop` и создать ветку:
   ```bash
   git checkout develop
   git pull
   git checkout -b feature/<название>
   ```
2. Сделать коммиты с понятными сообщениями (`feat:`, `fix:`, `chore:`, `docs:`).
3. Запушить и открыть Pull Request в `develop`.
4. После ревью и мержа ветку удалить.

## Вопросы

