package com.example.consoleapp.dao;

import com.example.consoleapp.config.HibernateUtil;
import com.example.consoleapp.entity.User;
import com.example.consoleapp.exception.DaoException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);

    @Override
    public User create(User user) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            log.info("Пользователь создан: id={}, email={}", user.getId(), user.getEmail());
            return user;
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Ошибка при создании пользователя email={}", user.getEmail(), e);
            throw new DaoException("Не удалось создать пользователя: " + rootMessage(e), e);
        } finally {
            close(session);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            User user = session.get(User.class, id);
            tx.commit();
            log.info("Поиск пользователя id={}: {}", id, user != null ? "найден" : "не найден");
            return Optional.ofNullable(user);
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Ошибка при поиске пользователя id={}", id, e);
            throw new DaoException("Не удалось найти пользователя: " + rootMessage(e), e);
        } finally {
            close(session);
        }
    }

    @Override
    public List<User> findAll() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<User> users = session
                    .createQuery("from User order by id", User.class)
                    .list();
            tx.commit();
            log.info("Получен список пользователей, количество: {}", users.size());
            return users;
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Ошибка при получении списка пользователей", e);
            throw new DaoException("Не удалось получить список пользователей: " + rootMessage(e), e);
        } finally {
            close(session);
        }
    }

    @Override
    public User update(User user) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            User merged = session.merge(user);
            tx.commit();
            log.info("Пользователь обновлён: id={}", merged.getId());
            return merged;
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Ошибка при обновлении пользователя id={}", user.getId(), e);
            throw new DaoException("Не удалось обновить пользователя: " + rootMessage(e), e);
        } finally {
            close(session);
        }
    }

    @Override
    public boolean delete(Long id) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user == null) {
                tx.commit();
                log.info("Удаление: пользователь id={} не найден", id);
                return false;
            }
            session.remove(user);
            tx.commit();
            log.info("Пользователь удалён: id={}", id);
            return true;
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Ошибка при удалении пользователя id={}", id, e);
            throw new DaoException("Не удалось удалить пользователя: " + rootMessage(e), e);
        } finally {
            close(session);
        }
    }

    // ---------- вспомогательные методы ----------

    private void rollback(Transaction tx) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("Транзакция откачена (rollback)");
            } catch (HibernateException rollbackEx) {
                log.error("Не удалось выполнить rollback", rollbackEx);
            }
        }
    }

    private void close(Session session) {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    /** Достаёт самое глубокое сообщение (например, от PostgreSQL: duplicate key ...). */
    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage();
    }
}