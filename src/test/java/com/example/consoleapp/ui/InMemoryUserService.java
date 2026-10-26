package com.example.consoleapp.ui;

import com.example.consoleapp.entity.User;
import com.example.consoleapp.exception.DaoException;
import com.example.consoleapp.service.UserService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Fake-реализация {@link UserService} для unit-тестов интерфейса: хранит пользователей в памяти.
 * <p>
 * UI зависит только от {@code UserService}, поэтому тесты UI подставляют вместо него этот класс
 * и не зависят ни от логики сервиса, ни от DAO, ни от БД. Сервис и DAO тестируются отдельно
 * их авторами.
 * <p>
 * Fake повторяет те свойства настоящего стека, от которых зависит код действий:
 * <ul>
 *     <li>{@code createUser} присваивает id и дату создания переданному объекту (как {@code session.persist});</li>
 *     <li>{@code getUserById}/{@code getAllUsers} возвращают <b>копии</b>: после закрытия сессии Hibernate
 *         объекты detached, и их изменение без {@code updateUser} в БД не попадает;</li>
 *     <li>повторный email даёт {@link DaoException}, как нарушение {@code UNIQUE} в PostgreSQL.</li>
 * </ul>
 */
class InMemoryUserService implements UserService {

    static final Instant CREATED_AT = Instant.parse("2026-10-20T09:30:00Z");

    private final Map<Long, User> storage = new TreeMap<>();
    private long nextId = 1;
    private int updateCalls;
    private int deleteCalls;
    private RuntimeException nextFailure;

    // ---------- настройка из тестов ----------

    /** Добавляет пользователя в обход createUser, возвращает присвоенный id. */
    long given(String name, String email, int age) {
        User user = new User(name, email, age);
        user.setId(nextId++);
        user.setCreatedCode(CREATED_AT);
        storage.put(user.getId(), copy(user));
        return user.getId();
    }

    /**
     * Следующий вызов любого метода сервиса бросит это исключение.
     * {@link DaoException} — ошибка БД, {@link IllegalArgumentException} — сервис отклонил данные.
     */
    void failNextCallWith(RuntimeException failure) {
        this.nextFailure = failure;
    }

    /** Что сейчас лежит «в БД» (копия). */
    Optional<User> stored(long id) {
        return Optional.ofNullable(storage.get(id)).map(InMemoryUserService::copy);
    }

    int size() {
        return storage.size();
    }

    int updateCalls() {
        return updateCalls;
    }

    int deleteCalls() {
        return deleteCalls;
    }

    // ---------- UserService ----------

    @Override
    public User createUser(User user) {
        throwIfFailureConfigured();
        requireUniqueEmail(user.getEmail(), null);
        user.setId(nextId++);
        user.setCreatedCode(CREATED_AT);
        storage.put(user.getId(), copy(user));
        return user;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        throwIfFailureConfigured();
        return stored(id);
    }

    @Override
    public List<User> getAllUsers() {
        throwIfFailureConfigured();
        List<User> result = new ArrayList<>();
        storage.values().forEach(u -> result.add(copy(u)));
        return result;
    }

    @Override
    public User updateUser(User user) {
        throwIfFailureConfigured();
        updateCalls++;
        if (user.getId() == null || !storage.containsKey(user.getId())) {
            throw new DaoException("Пользователь с id = " + user.getId() + " не существует");
        }
        requireUniqueEmail(user.getEmail(), user.getId());
        storage.put(user.getId(), copy(user));
        return copy(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        throwIfFailureConfigured();
        deleteCalls++;
        return storage.remove(id) != null;
    }

    // ---------- вспомогательное ----------

    private void throwIfFailureConfigured() {
        if (nextFailure != null) {
            RuntimeException failure = nextFailure;
            nextFailure = null;
            throw failure;
        }
    }

    private void requireUniqueEmail(String email, Long ownId) {
        boolean taken = storage.values().stream()
                .anyMatch(u -> u.getEmail().equals(email) && !u.getId().equals(ownId));
        if (taken) {
            throw new DaoException("Не удалось сохранить пользователя: duplicate key value violates "
                    + "unique constraint \"users_email_key\"");
        }
    }

    private static User copy(User source) {
        User copy = new User(source.getName(), source.getEmail(), source.getAge());
        copy.setId(source.getId());
        copy.setCreatedCode(source.getCreatedCode());
        return copy;
    }
}
