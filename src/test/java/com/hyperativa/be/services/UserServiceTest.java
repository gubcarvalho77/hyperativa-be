package com.hyperativa.be.services;

import com.hyperativa.be.dtos.mapping.UserMapper;
import com.hyperativa.be.model.User;
import com.hyperativa.be.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserService userService;

    @Test
    void test_get_all_users() {

        var user = new User();
        user.setId(UUID.randomUUID());
        user.setName("any name");
        user.setEmail("any@email.com");
        user.setPassword("any-pwd");
        user.setSurname("any surname");
        user.setRole("any role");
        user.setUsername("any username");

        var users = List.of(user);

        when(userRepository.findAll()).thenReturn(users);

        var result = userService.getAllUsers();

        assertEquals(users.size(), result.size());
    }
}