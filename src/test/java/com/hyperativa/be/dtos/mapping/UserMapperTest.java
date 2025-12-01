package com.hyperativa.be.dtos.mapping;

import com.hyperativa.be.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void test() {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setName("any name");
        user.setEmail("any@email.com");
        user.setPassword("any-pwd");
        user.setSurname("any surname");
        user.setRole("any role");
        user.setUsername("any username");

        var dto = userMapper.toDTO(user);

        assertAll(
                () -> assertEquals(user.getId(), dto.getId()),
                () -> assertEquals(user.getEmail(), dto.getEmail()),
                () -> assertEquals(user.getName(), dto.getName()),
                () -> assertEquals(user.getSurname(), dto.getSurname()),
                () -> assertEquals(user.getUsername(), dto.getUsername()),
                () -> assertTrue(dto.isEnabled()),
                () -> assertEquals(user.getCreatedBy(), dto.getCreatedBy()),
                () -> assertEquals(user.getCreatedAt(), dto.getCreatedAt()),
                () -> assertEquals(user.getUpdatedBy(), dto.getUpdatedBy()),
                () -> assertEquals(user.getUpdatedAt(), dto.getUpdatedAt())
        );
    }

    @Test
    void test_when_user_is_null() {
        assertNull(userMapper.toDTO(null));
    }
}