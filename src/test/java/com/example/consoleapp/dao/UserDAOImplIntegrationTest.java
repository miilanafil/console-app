package com.example.consoleapp.dao;

import com.example.consoleapp.BaseIntegrationTest;
import com.example.consoleapp.config.HibernateUtil;
import com.example.consoleapp.entity.User;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOImplIntegrationTest extends BaseIntegrationTest {

    private UserDAOImpl userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAOImpl();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createMutationQuery("delete from User").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    @DisplayName("Создание: сохранение в реальную БД")
    void createUser_shouldSaveToDatabase() {
        User user = new User("Милана", "mila@test.ru", 25);
        User saved = userDAO.create(user);

        assertNotNull(saved.getId());
        assertEquals("mila@test.ru", saved.getEmail());
    }

    @Test
    @DisplayName("Поиск по ID: успешный поиск")
    void findById_shouldReturnUser() {
        User user = new User("Карина", "karina@test.ru", 30);
        userDAO.create(user);

        Optional<User> found = userDAO.findById(user.getId());

        assertTrue(found.isPresent());
        assertEquals("Карина", found.get().getName());
    }

    @Test
    @DisplayName("Поиск по ID")
    void findById_whenNotFound_shouldReturnEmpty() {
        Optional<User> found = userDAO.findById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Получение всех: возврат списка из БД")
    void findAll_shouldReturnList() {
        userDAO.create(new User("Алексей", "alex@test.ru", 20));
        userDAO.create(new User("Борис", "boris@test.ru", 21));

        List<User> users = userDAO.findAll();
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Обновление: изменение данных в БД")
    void update_shouldChangeData() {
        User user = new User("Старое Имя", "old@test.ru", 20);
        userDAO.create(user);

        user.setName("Новое Имя");
        user.setAge(25);
        userDAO.update(user);

        User updated = userDAO.findById(user.getId()).orElseThrow();
        assertEquals("Новое Имя", updated.getName());
        assertEquals(25, updated.getAge());
    }

    @Test
    @DisplayName("Удаление: успешное удаление из БД")
    void delete_shouldRemoveUser() {
        User user = new User("Удаляемый", "delete@test.ru", 20);
        userDAO.create(user);

        boolean deleted = userDAO.delete(user.getId());

        assertTrue(deleted);
        assertTrue(userDAO.findById(user.getId()).isEmpty());
    }

    @Test
    @DisplayName("Удаление")
    void delete_whenNotFound_shouldReturnFalse() {
        boolean deleted = userDAO.delete(999L);
        assertFalse(deleted);
    }
}