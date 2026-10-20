package com.example.consoleapp.ui;

import com.example.consoleapp.dao.UserDAO;
import com.example.consoleapp.entity.User;
import com.example.consoleapp.exception.DaoException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;


class InMemoryUserDAO implements UserDAO {

    static final Instant CREATED_AT = Instant.parse("2026-10-20T09:30:00Z");

    private final Map<Long, User> storage = new TreeMap<>();
    private long nextId = 1;
    private int updateCalls;
    private int deleteCalls;
    private DaoException nextFailure;


    long given(String name, String email, int age) {
        User user = new User(name, email, age);
        user.setId(nextId++);
        user.setCreatedCode(CREATED_AT);
        storage.put(user.getId(), copy(user));
        return user.getId();
    }


    void failNextCallWith(DaoException failure) {
        this.nextFailure = failure;
    }

    Optional<User> stored(long id) {
        return Optional.ofNullable(storage.get(id)).map(InMemoryUserDAO::copy);
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


    @Override
    public User create(User user) {
        throwIfFailureConfigured();
        requireUniqueEmail(user.getEmail(), null);
        user.setId(nextId++);
        user.setCreatedCode(CREATED_AT);
        storage.put(user.getId(), copy(user));
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        throwIfFailureConfigured();
        return stored(id);
    }

    @Override
    public List<User> findAll() {
        throwIfFailureConfigured();
        List<User> result = new ArrayList<>();
        storage.values().forEach(u -> result.add(copy(u)));
        return result;
    }

    @Override
    public User update(User user) {
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
    public boolean delete(Long id) {
        throwIfFailureConfigured();
        deleteCalls++;
        return storage.remove(id) != null;
    }

    private void throwIfFailureConfigured() {
        if (nextFailure != null) {
            DaoException failure = nextFailure;
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
