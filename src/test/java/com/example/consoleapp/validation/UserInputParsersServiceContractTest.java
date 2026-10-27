package com.example.consoleapp.validation;

import com.example.consoleapp.dao.UserDAO;
import com.example.consoleapp.entity.User;
import com.example.consoleapp.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Контракт между валидацией UI и валидацией {@link UserServiceImpl}.
 * <p>
 * Данные проверяются дважды: в консоли ({@link UserInputParsers}) и в сервисе. Если правила UI
 * окажутся мягче, пользователь введёт значение, консоль его примет, а сервис отклонит уже после
 * ввода всех полей. Поэтому правило одно: <b>всё, что принимает UI, должен принимать сервис</b>.
 * <p>
 * Это не тест сервиса (его тестирует автор сервиса), а проверка того, что UI с ним согласован.
 * Если правила сервиса изменятся, этот тест покажет, где UI нужно поправить.
 * DAO подменяется простой реализацией в памяти: БД здесь не нужна.
 */
class UserInputParsersServiceContractTest {

    private final RecordingUserDAO dao = new RecordingUserDAO();
    private final UserServiceImpl service = new UserServiceImpl(dao);

    /** Пользователь из значений, которые прошли UI-валидацию. */
    private static User fromUi(String name, String email, String age) {
        return new User(
                UserInputParsers.parseName(name),
                UserInputParsers.parseEmail(email),
                UserInputParsers.parseAge(age));
    }

    @ParameterizedTest(name = "возраст {0}")
    @ValueSource(strings = {"1", "25", "100"})
    void ageAcceptedByUi_isAcceptedByService(String age) {
        assertDoesNotThrow(() -> service.createUser(fromUi("Ivan", "ivan@example.com", age)));
    }

    @ParameterizedTest(name = "возраст {0}")
    @ValueSource(strings = {"0", "101"})
    void ageRejectedByService_isRejectedByUi(String age) {
        assertThrows(IllegalArgumentException.class,
                () -> service.createUser(new User("Ivan", "ivan@example.com", Integer.parseInt(age))),
                "предусловие: сервис отклоняет возраст " + age);
        assertThrows(ValidationException.class, () -> UserInputParsers.parseAge(age),
                "UI должен отклонить возраст " + age + " ещё при вводе");
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {
            "ivan@example.com",
            "Ivan.Petrov@Example.COM",
            "ivan+tag@mail.example.org",
            "i_v-a.n@my-domain.ru"
    })
    void emailAcceptedByUi_isAcceptedByService(String email) {
        assertDoesNotThrow(() -> service.createUser(fromUi("Ivan", email, "25")));
    }

    @Test
    void emailWithPercent_isRejectedByBoth() {
        String email = "ivan%tag@example.com";

        assertThrows(IllegalArgumentException.class,
                () -> service.createUser(new User("Ivan", email, 25)),
                "предусловие: сервис не принимает % в email");
        assertThrows(ValidationException.class, () -> UserInputParsers.parseEmail(email));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"Анна-Мария", "O'Neil", "Jean Luc"})
    void nameAcceptedByUi_isAcceptedByService(String name) {
        assertDoesNotThrow(() -> service.createUser(fromUi(name, "ivan@example.com", "25")));
    }

    @Test
    void nameOfMaxUiLength_isAcceptedByService() {
        String name = "a".repeat(UserInputParsers.MAX_NAME_LENGTH);

        assertDoesNotThrow(() -> service.createUser(fromUi(name, "ivan@example.com", "25")));
    }

    @Test
    void valuesNormalizedByUi_areNotChangedByService() {
        User user = fromUi("  Иван   Петров ", " Ivan@Example.COM ", " 30 ");

        service.createUser(user);

        User saved = dao.created.get(0);
        assertEquals("Иван Петров", saved.getName());
        assertEquals("ivan@example.com", saved.getEmail());
        assertEquals(30, saved.getAge());
    }

    @Test
    void updateWithUiValidValues_isAcceptedByService() {
        User user = fromUi("Ivan", "ivan@example.com", "100");
        user.setId(1L);

        assertDoesNotThrow(() -> service.updateUser(user));
    }

    @Test
    void idAcceptedByUi_isAcceptedByService() {
        Long id = UserInputParsers.parseId("1");

        assertDoesNotThrow(() -> service.getUserById(id));
        assertDoesNotThrow(() -> service.deleteUser(id));
    }

    /** Простая заглушка DAO: запоминает, что ей передали. */
    private static final class RecordingUserDAO implements UserDAO {

        final List<User> created = new ArrayList<>();

        @Override
        public User create(User user) {
            created.add(user);
            return user;
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public List<User> findAll() {
            return List.of();
        }

        @Override
        public User update(User user) {
            return user;
        }

        @Override
        public boolean delete(Long id) {
            return false;
        }
    }
}
