package com.example.consoleapp.service;

import com.example.consoleapp.dto.UserDto;
import com.example.consoleapp.dto.UserRequestDto;
import com.example.consoleapp.entity.User;
import com.example.consoleapp.event.OperationType;
import com.example.consoleapp.event.UserEvent;
import com.example.consoleapp.mapper.UserMapper;
import com.example.consoleapp.repository.UserRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    //
    private final UserRepository userRepository;
    private final UserMapper userMapper;
//
//    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
//        this.userRepository = userRepository;
//        this.userMapper = userMapper;
//    }

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "user-events";

    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository , KafkaTemplate<String, Object> kafkaTemplate) {
        this.userMapper = userMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;

    }

    @Override
    public UserDto createUser(UserRequestDto requestDto) {
        User user = userMapper.toEntity(requestDto);
        User savedUser = userRepository.save(user);

        UserEvent event = new UserEvent(OperationType.CREATED, savedUser.getEmail());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(TOPIC, event);
            }
        });

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с id = " + id + " не найден"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserDto updateUser(Long id, UserRequestDto requestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с id = " + id + " не найден"));

        user.setName(requestDto.name().trim());
        user.setEmail(requestDto.email().trim().toLowerCase());
        user.setAge(requestDto.age());

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с id = " + id + " не найден"));

        String email = user.getEmail(); 
        userRepository.delete(user);

        UserEvent event = new UserEvent(OperationType.DELETED, email);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(TOPIC, event);
            }
        });
    }



//    @Override
//    public void deleteUser(Long id) {
//        if (!userRepository.existsById(id)) {
//            throw new IllegalArgumentException("Пользователь с id = " + id + " не найден");
//        }
//        userRepository.deleteById(id);
//
//        UserEvent event = new UserEvent(OperationType.DELETED, );
//    }
}